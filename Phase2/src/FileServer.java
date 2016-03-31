import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class FileServer
{
    public final static short FS_PORT = 8766;
    
    //  Define the index of each object in the Content
    public final static short FS_DOWNLOAD_USER_TOKEN = 0;   //UserToken Token
    public final static short FS_DOWNLOAD_GROUP_NAME = 1;   //String    Group
    public final static short FS_DOWNLOAD_FILE_NAME = 2;    //String    FileName
    
    public final static short FS_UPLOAD_USER_TOKEN = 0;     //UserToken Token
    public final static short FS_UPLOAD_GROUP_NAME = 1;     //String    Group
    public final static short FS_UPLOAD_FILE_NAME = 2;      //String    FileName
    public final static short FS_UPLOAD_FILE_CONTENT = 3;   //byte[]    Content
    
    public final static short FS_SUCCESS_USER_TOKEN = 0;    //UserToken Token
    
    public final static short FS_ERROR_USER_TOKEN = 0;      //UserToken Token
    public final static short FS_ERROR_EXCEPTION = 1;       //Exception e
    
    static class downloadCallback implements ServerFramework.ServerCallback
    {
        @Override
        public Message CallbackProc(Socket Client, ArrayList<Object> Content)
        {
            System.out.println("Received a download message");
            
            Message Response = new Message("upload");
            
            try
            {
                //  Read file content
                File FileHandle = new File((String) Content.get(FS_DOWNLOAD_FILE_NAME));
                byte[] FileContent = new byte[(int) FileHandle.length()];
                FileInputStream FileStream = new FileInputStream(FileHandle);
                BufferedInputStream FileBuff = new BufferedInputStream(FileStream);
                FileBuff.read(FileContent, 0, FileContent.length);
                
                //  Create Message
                Response.addObject((UserToken) Content.get(FS_DOWNLOAD_USER_TOKEN));
                Response.addObject((String) "");
                Response.addObject((String) Content.get(FS_DOWNLOAD_FILE_NAME));
                Response.addObject(FileContent);
                
                //  Free resources
                FileBuff.close();
                FileStream.close();
            }
            catch (Exception e)
            {
                //  Return error message
                System.out.println("Failed to send the file, continue");
                Response = new Message("error");
                Response.addObject(e);
            }
            
            return Response;
        }
        
    }
    
    static class uploadCallback implements ServerFramework.ServerCallback
    {
        @Override
        public Message CallbackProc(Socket Client, ArrayList<Object> Content)
        {
            System.out.println("Received an upload message");
            
            Message Response = new Message("success");
            
            try
            {
                //  Save file content
                byte[] FileContent = (byte[]) Content.get(FS_UPLOAD_FILE_CONTENT);
                FileOutputStream FileStream = new FileOutputStream((String) Content.get(FS_UPLOAD_FILE_NAME));
                BufferedOutputStream FileBuff = new BufferedOutputStream(FileStream);
                FileBuff.write(FileContent, 0, FileContent.length);
                FileBuff.flush();
                
                //  Create Message
                Response.addObject((UserToken) Content.get(FS_DOWNLOAD_USER_TOKEN));
                
                //  Free resources
                FileBuff.close();
                FileStream.close();
            }
            catch (Exception e)
            {
                //  Return error message
                System.out.println("Failed to save the file, continue");
                Response = new Message("error");
                Response.addObject(e);
            }
            
            return Response;
        }
        
    }
    
    public static void main(String args[])
    {
        System.out.println("Initalize file server");
        ServerFramework Server = new ServerFramework(FS_PORT);
        downloadCallback download = new downloadCallback();
        uploadCallback upload = new uploadCallback();
        
        System.out.println("Register messages");
        Server.RegisterMessage("download", download);
        Server.RegisterMessage("upload", upload);
        
        System.out.println("Start the listener");
        Server.run();
    }
}