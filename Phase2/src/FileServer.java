import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * File server
 * 
 * @author Yuntian Zhang
 */
public class FileServer
{
    //  Define the index of each object in the Content
    public final static int FS_GENERAL_USER_TOKEN = 0;  //UserToken Token
    public final static int FS_GENERAL_GROUP_NAME = 1;  //String    Group
    
    public final static int FS_DOWNLOAD_USER_TOKEN = 0; //UserToken Token
    public final static int FS_DOWNLOAD_GROUP_NAME = 1; //String    Group
    public final static int FS_DOWNLOAD_FILE_NAME = 2;  //String    FileName
    
    public final static int FS_UPLOAD_USER_TOKEN = 0;   //UserToken Token
    public final static int FS_UPLOAD_GROUP_NAME = 1;   //String    Group
    public final static int FS_UPLOAD_FILE_NAME = 2;    //String    FileName
    public final static int FS_UPLOAD_FILE_CONTENT = 3; //byte[]    Content
    
    public final static int FS_LIST_USER_TOKEN = 0;     //UserToken Token
    public final static int FS_LIST_GROUP_NAME = 1;     //String    Group
    
    public final static int FS_VIEW_USER_TOKEN = 0;     //UserToken Token
    public final static int FS_VIEW_GROUP_NAME = 1;     //String    Group
    public final static int FS_VIEW_FILE_LIST = 2;      //String[]  List
    
    public final static int FS_SUCCESS_USER_TOKEN = 0;  //UserToken Token
    public final static int FS_SUCCESS_GROUP_NAME = 1;  //String    Group
    
    public final static int FS_ERROR_USER_TOKEN = 0;    //UserToken Token
    public final static int FS_ERROR_GROUP_NAME = 1;    //String    Group
    public final static int FS_ERROR_EXCEPTION = 2;     //Exception e
    
    static class listCallback implements ServerFramework.ServerCallback
    {
        @Override
        public Message CallbackProc(Socket Client, ArrayList<Object> Content)
        {
            System.out.println("Received a list message");
            
            Message Response = new Message("view");
            
            try
            {
                //  Read file content
                File FolderHandle = new File((String)
                        FS_STORAGE + File.separator +
                        Content.get(FS_LIST_GROUP_NAME));
                File[] FileList = FolderHandle.listFiles();
                String[] FileNameList = new String[FileList.length];
                
                for (int i=0; i<FileList.length; i++)
                {
                    FileNameList[i] = FileList[i].getName();
                }
                
                //  Create Message
                Response.addObject((UserToken) Content.get(FS_DOWNLOAD_USER_TOKEN));
                Response.addObject((String) Content.get(FS_DOWNLOAD_GROUP_NAME));
                Response.addObject((String[]) FileNameList);
            }
            catch (Exception e)
            {
                //  Return error message
                System.out.println("Failed to send the file, continue");
                Response = GenerateErrorMessage(Content,e);
            }
            
            return Response;
        }
        
    }
    
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
                File FileHandle = new File((String)
                        FS_STORAGE + File.separator +
                        Content.get(FS_DOWNLOAD_GROUP_NAME) + File.separator + 
                        Content.get(FS_DOWNLOAD_FILE_NAME));
                FileHandle.getParentFile().mkdirs();
                byte[] FileContent = new byte[(int) FileHandle.length()];
                FileInputStream FileStream = new FileInputStream(FileHandle);
                BufferedInputStream FileBuff = new BufferedInputStream(FileStream);
                FileBuff.read(FileContent, 0, FileContent.length);
                
                //  Create Message
                Response.addObject((UserToken) Content.get(FS_DOWNLOAD_USER_TOKEN));
                Response.addObject((String) Content.get(FS_DOWNLOAD_GROUP_NAME));
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
                Response = GenerateErrorMessage(Content,e);
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
                // Save file content
                byte[] FileContent = (byte[]) Content.get(FS_UPLOAD_FILE_CONTENT);
                File FileHandle = new File((String)
                        FS_STORAGE + File.separator + 
                        Content.get(FS_UPLOAD_GROUP_NAME) + File.separator + 
                        Content.get(FS_UPLOAD_FILE_NAME));
                FileHandle.getParentFile().mkdirs();
                FileOutputStream FileStream = new FileOutputStream(FileHandle);
                BufferedOutputStream FileBuff = new BufferedOutputStream(FileStream);
                FileBuff.write(FileContent, 0, FileContent.length);
                FileBuff.flush();
                
                // Create Message
                Response.addObject((UserToken) Content.get(FS_UPLOAD_USER_TOKEN));
                Response.addObject((String) Content.get(FS_UPLOAD_GROUP_NAME));
                
                // Free resources
                FileBuff.close();
                FileStream.close();
            }
            catch (Exception e)
            {
                // Return error message
                System.out.println("Failed to save the file, continue");
                Response = GenerateErrorMessage(Content,e);
            }
            
            return Response;
        }
        
    }
    
    static public Message GenerateErrorMessage(ArrayList<Object> Content, Exception e)
    {
        Message Response = new Message("error");
        Response.addObject((UserToken) Content.get(FS_GENERAL_USER_TOKEN));
        Response.addObject((String) Content.get(FS_GENERAL_GROUP_NAME));
        Response.addObject(e);
        return Response;
    }

    public static int FS_PORT = 8766;
    public static String FS_STORAGE = System.getProperty("user.dir") + File.separator + "FileServer";
    
    FileServer(int Port, String Storage)
    {
        FS_PORT = Port;
        FS_STORAGE = Storage;
    }
    
    public static void main(String args[])
    {
        run();
    }
    
    public static void run()
    {
        // Create instances
        System.out.println("Initalize file server");
        ServerFramework Server = new ServerFramework(FS_PORT);
        downloadCallback download = new downloadCallback();
        uploadCallback upload = new uploadCallback();
        listCallback list = new listCallback();
        
        // Register callbacks
        System.out.println("Register messages");
        Server.RegisterMessage("download", download);
        Server.RegisterMessage("upload", upload);
        Server.RegisterMessage("list", list);
        
        // Start listener
        System.out.println("Start the listener");
        Server.run();
    }
}