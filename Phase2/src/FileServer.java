
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.PublicKey;
import java.security.Signature;
import java.util.ArrayList;

/**
 * File server
 *
 * FileServer is a simple server providing file storage service.
 * It is based on ServerFramework and can handle 3 messages: download, upload,
 * and list. It can return upload message when receives download request, success
 * message when receives upload request, view message when receives list
 * request, and error message when something goes wrong.
 * 
 * The class consists of 3 callbacks for each messages, 1 error message generator,
 * and 1 run() method to register callbacks and start the server.
 * 
 * The default file saving location is ./FileServer/<GROUP NAME>/. This is
 * defined by FS_STORAGE.
 * 
 * The default port is 8766. This is defined by FS_PORT.
 */
public class FileServer {

    //  Define the index of each object in the Content
    public final static int FS_GENERAL_USER_TOKEN = 0;  //UserToken Token
    public final static int FS_GENERAL_GROUP_NAME = 1;  //String    Group

    public final static String FS_DOWNLOAD = "download";//download
    public final static int FS_DOWNLOAD_USER_TOKEN = 0; //UserToken Token
    public final static int FS_DOWNLOAD_GROUP_NAME = 1; //String    Group
    public final static int FS_DOWNLOAD_FILE_NAME = 2;  //String    FileName

    public final static String FS_UPLOAD = "upload";    //upload
    public final static int FS_UPLOAD_USER_TOKEN = 0;   //UserToken Token
    public final static int FS_UPLOAD_GROUP_NAME = 1;   //String    Group
    public final static int FS_UPLOAD_FILE_NAME = 2;    //String    FileName
    public final static int FS_UPLOAD_FILE_CONTENT = 3; //byte[]    Content

    public final static String FS_LIST = "list";        //list
    public final static int FS_LIST_USER_TOKEN = 0;     //UserToken Token
    public final static int FS_LIST_GROUP_NAME = 1;     //String    Group

    public final static String FS_VIEW = "view";        //view
    public final static int FS_VIEW_USER_TOKEN = 0;     //UserToken Token
    public final static int FS_VIEW_GROUP_NAME = 1;     //String    Group
    public final static int FS_VIEW_FILE_LIST = 2;      //String[]  List

    public final static String FS_SUCCESS = "success";  //success
    public final static int FS_SUCCESS_USER_TOKEN = 0;  //UserToken Token
    public final static int FS_SUCCESS_GROUP_NAME = 1;  //String    Group

    public final static String FS_ERROR = "error";      //error
    public final static int FS_ERROR_USER_TOKEN = 0;    //UserToken Token
    public final static int FS_ERROR_GROUP_NAME = 1;    //String    Group
    public final static int FS_ERROR_EXCEPTION = 2;     //Exception e

    static class listCallback implements ServerFramework.ServerCallback {

        @Override
        public Message CallbackProc(SecureSocket Client, ArrayList<Object> Content) {
            System.out.println("Received a list message");

            Message Response = new Message(FS_VIEW);

            try {
                // Check permission
                UserToken Token = (UserToken) Content.get(FS_DOWNLOAD_USER_TOKEN);
                
                // Authenticate token
                if(!authenticate(Token)) {
                    throw new Exception("Failed to authenticate token.");
                }
                
                String Group = (String) Content.get(FS_DOWNLOAD_GROUP_NAME);
                if (!Token.getGroups().contains(Group)) {
                    throw new Exception("Access denied.");
                }

                //  Read file content
                File FolderHandle = new File((String) FS_STORAGE + File.separator
                        + Content.get(FS_LIST_GROUP_NAME));
                File[] FileList = FolderHandle.listFiles();
                ArrayList<String> FileNameList = new ArrayList<String>();

                for (int i = 0; i < FileList.length; i++) {
                    FileNameList.add(FileList[i].getName());
                }

                //  Create Message
                Response.addObject((UserToken) Token);
                Response.addObject((String) Group);
                Response.addObject((ArrayList<String>) FileNameList);
            } catch (Exception e) {
                //  Return error message
                System.out.println("Failed to list the file, continue");
                Response = GenerateErrorMessage(Content, e);
            }

            return Response;
        }
    }

    static class downloadCallback implements ServerFramework.ServerCallback {

        @Override
        public Message CallbackProc(SecureSocket Client, ArrayList<Object> Content) {
            System.out.println("Received a download message");

            Message Response = new Message(FS_UPLOAD);

            try {
                // Check permission
                UserToken Token = (UserToken) Content.get(FS_DOWNLOAD_USER_TOKEN);
                
                // Authenticate token
                if(!authenticate(Token)) {
                    throw new Exception("Failed to authenticate.");
                }
                
                String Group = (String) Content.get(FS_DOWNLOAD_GROUP_NAME);
                if (!Token.getGroups().contains(Group)) {
                    throw new Exception("Access denied.");
                }

                //  Read file content
                File FileHandle = new File((String) FS_STORAGE + File.separator
                        + Content.get(FS_DOWNLOAD_GROUP_NAME) + File.separator
                        + Content.get(FS_DOWNLOAD_FILE_NAME));
                FileHandle.getParentFile().mkdirs();
                byte[] FileContent = new byte[(int) FileHandle.length()];
                FileInputStream FileStream = new FileInputStream(FileHandle);
                BufferedInputStream FileBuff = new BufferedInputStream(FileStream);
                FileBuff.read(FileContent, 0, FileContent.length);

                //  Create Message
                Response.addObject((UserToken) Token);
                Response.addObject((String) Group);
                Response.addObject((String) Content.get(FS_DOWNLOAD_FILE_NAME));
                Response.addObject(FileContent);

                //  Free resources
                FileBuff.close();
                FileStream.close();
            } catch (Exception e) {
                //  Return error message
                System.out.println("Failed to send the file, continue");
                Response = GenerateErrorMessage(Content, e);
            }

            return Response;
        }

    }

    static class uploadCallback implements ServerFramework.ServerCallback {

        @Override
        public Message CallbackProc(SecureSocket Client, ArrayList<Object> Content) {
            System.out.println("Received an upload message");

            Message Response = new Message(FS_SUCCESS);

            try {
                // Check permission
                UserToken Token = (UserToken) Content.get(FS_DOWNLOAD_USER_TOKEN);
                
                // Authenticate token
                if(!authenticate(Token)) {
                    throw new Exception("Failed to authenticate token.");
                }
                
                String Group = (String) Content.get(FS_DOWNLOAD_GROUP_NAME);
                if (!Token.getGroups().contains(Group)) {
                    throw new Exception("Access denied.");
                }

                // Save file content
                byte[] FileContent = (byte[]) Content.get(FS_UPLOAD_FILE_CONTENT);
                File FileHandle = new File((String) FS_STORAGE + File.separator
                        + Content.get(FS_UPLOAD_GROUP_NAME) + File.separator
                        + Content.get(FS_UPLOAD_FILE_NAME));
                FileHandle.getParentFile().mkdirs();
                FileOutputStream FileStream = new FileOutputStream(FileHandle);
                BufferedOutputStream FileBuff = new BufferedOutputStream(FileStream);
                FileBuff.write(FileContent, 0, FileContent.length);
                FileBuff.flush();

                // Create Message
                Response.addObject((UserToken) Token);
                Response.addObject((String) Group);

                // Free resources
                FileBuff.close();
                FileStream.close();
            } catch (Exception e) {
                // Return error message
                System.out.println("Failed to receive the file, continue");
                Response = GenerateErrorMessage(Content, e);
            }

            return Response;
        }

    }

    static public Message GenerateErrorMessage(ArrayList<Object> Content, Exception e) {
        Message Response = new Message(FS_ERROR);
        Response.addObject((UserToken) Content.get(FS_GENERAL_USER_TOKEN));
        Response.addObject((String) Content.get(FS_GENERAL_GROUP_NAME));
        Response.addObject(e);
        return Response;
    }
    
    public static int FS_PORT = 8766;
    public static String FS_STORAGE = System.getProperty("user.dir") + File.separator + "FileServer";
    
    public static String GS_IDENTITY = "127.0.0.1";

    public static void run() {
        // Create instances
        System.out.println("Initalize file server");
        File FileHandle = new File(FS_STORAGE);
        FileHandle.mkdirs();
        ServerFramework Server = new ServerFramework(FS_PORT);
        
        downloadCallback download = new downloadCallback();
        uploadCallback upload = new uploadCallback();
        listCallback list = new listCallback();

        // Register callbacks
        System.out.println("Register messages");
        Server.RegisterMessage(FS_DOWNLOAD, download);
        Server.RegisterMessage(FS_UPLOAD, upload);
        Server.RegisterMessage(FS_LIST, list);

        // Start listener
        System.out.println("Start the listener");
        Server.run();
    }
    
    public static boolean authenticate(UserToken Token)
    {
        String issuer = Token.getIssuer();
                
        if(!issuer.equals(GS_IDENTITY))
        {
            return false;
        }
        
        Client2 client = new Client2();
        client.connectGS(issuer);
        client.getToken("file", "file");
        
        return client.authenticate(Token);
    }
}
