import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * General client
 * 
 * @author Yuntian Zhang
 */
public class Client2
{
    // Can't get import working, just copy the definition
    public final static int GS_GENERAL_USER_TOKEN = 0;  //UserToken Token
    public final static int GS_GENERAL_GROUP_NAME = 1;  //String    Group
    public final static int GS_GENERAL_USER_NAME = 1;   //String    User
    
    public final static String GS_LOGIN = "login";      //login
    public final static int GS_LOGIN_USER_TOKEN = 0;    //UserToken Token
    public final static int GS_LOGIN_USER_NAME = 1;     //String    User
    
    public final static String GS_ADDUSER = "adduser";  //adduser
    public final static int GS_ADDUSER_USER_TOKEN = 0;  //UserToken Token
    public final static int GS_ADDUSER_USER_NAME = 1;   //String    User
    
    public final static String GS_ADDGROUP = "addgroup";//addgroup
    public final static int GS_ADDGROUP_USER_TOKEN = 0; //UserToken Token
    public final static int GS_ADDGROUP_GROUP_NAME = 1; //String    Group
    
    public final static String GS_MGNT = "mgnt";        //mgnt
    public final static int GS_MGNT_USER_TOKEN = 0;     //UserToken Token
    public final static int GS_MGNT_GROUP_NAME = 1;     //String    Group
    public final static int GS_MGNT_USER_NAME = 2;      //String    User
    public final static int GS_MGNT_OPTION = 3;         //boolean   Option
    public final static boolean GS_MGNT_OPTION_ADD = true;      //boolean   Option
    public final static boolean GS_MGNT_OPTION_REMOVE = false;  //boolean   Option
    
    public final static String GS_LISTGROUP = "listgroup";//listgroup
    public final static int GS_LISTGROUP_USER_TOKEN = 0; //UserToken Token
    public final static int GS_LISTGROUP_GROUP_NAME = 1; //String    Group
    
    public final static String GS_VIEW = "view";        //view
    public final static int GS_VIEW_USER_TOKEN = 0;     //UserToken Token
    public final static int GS_VIEW_GROUP_NAME = 1;     //String    Group
    public final static int GS_VIEW_USER_LIST = 2;      //String[]  List
    
    public final static String GS_SUCCESS = "success";  //success
    public final static int GS_SUCCESS_USER_TOKEN = 0;  //UserToken Token
    public final static int GS_SUCCESS_GROUP_NAME = 1;  //String    Group
    public final static int GS_SUCCESS_USER_NAME = 1;   //String    User
    
    public final static String GS_ERROR = "error";      //error
    public final static int GS_ERROR_USER_TOKEN = 0;    //UserToken Token
    public final static int GS_ERROR_GROUP_NAME = 1;    //String    Group
    
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
    
    static String GS_ADDRESS = "localhost";
    static int GS_PORT = 8765;
    
    static String FS_ADDRESS = "localhost";
    static int FS_PORT = 8766;
    
    static Socket Server;
    static ObjectInputStream Input;
    static ObjectOutputStream Output;
    static UserToken Token;
    
    static boolean connect(String server, int port)
    {
        try
        {
        Server = new Socket(server,port);
        Output = new ObjectOutputStream(Server.getOutputStream());
        Input = new ObjectInputStream(Server.getInputStream());
        return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
    static ClientFramework Connect = new ClientFramework("Connect")
    {
        @Override
        public void run()
        {
            // Connect to server
            Scanner Input = new Scanner(System.in);

            System.out.println("Please enter server address");
            System.out.print("Default[localhost]:");
            String FS_ADDRESS = Input.nextLine();
            if (FS_ADDRESS.equals(""))
            {
                FS_ADDRESS = "localhost";
            }
            System.out.println("Please enter server port");
            System.out.print("Default[8766]:");
            String Port = Input.nextLine();
            try
            {
                FS_PORT = Integer.parseInt(Port);
            }
            catch (Exception e)
            {
                FS_PORT = 8766;
            }

            System.out.println("Connect to " + FS_ADDRESS + ":" + FS_PORT);
            if (!connect(FS_ADDRESS,FS_PORT))
            {
                System.out.println("Connection failed");
                return;
            }

            // Get UserToken
            /*
            List<String> Group = new ArrayList<>();
            Group.add("group");
            Token = new UserTokenImp("localhost", "admin", Group);
            */

            System.out.println("Connected.");
        }
    };
    
    static void disconnect()
    {
        try {
            Input.close();
        } catch (Exception ex) {
        }
        Input = null;
        
        try {
            Output.close();
        } catch (Exception ex) {
        }
        Output = null;
        
        try {
            Server.close();
        } catch (Exception ex) {
        }
        Server = null;
    }
    static ClientFramework Disconnect = new ClientFramework("Disconnect")
    {
        @Override
        public void run()
        {
            // Disconnect
            disconnect();
            System.out.println("Disconnected.");
        }
    };
    
    static List<String> listFile(UserToken token, String group)
    {
         Message Upload = new Message(FS_LIST);
        
        // Create Message header
        Upload.addObject((UserToken) token);
        Upload.addObject((String) group);
        
        //  Send message
        try {
            Output.writeObject(Upload);
            Output.flush();
        } catch (Exception ex) {
            return null;
        }
        
        //  Receive response
        Message Response;
        try {
            Response = (Message) Input.readObject();
        } catch (Exception ex) {
            return null;
        }
        
        if(!Response.getMessage().equals(FS_VIEW))
        {
            return null;
        }
        else
        {
            ArrayList<Object> Content = Response.getObjCont();
            return (List<String>)Content.get(FS_VIEW_FILE_LIST);
        }
    }
    static ClientFramework ListFile = new ClientFramework("List File")
    {
        @Override
        public void run()
        {
            Scanner Input = new Scanner(System.in);

            System.out.print("Please enter group name:");
            String Group = Input.nextLine();

            List<String> FileList = listFile(Token,Group);
            if (FileList == null)
            {
                System.out.println("List failed");
            }
            else
            {
                System.out.println("List:");
                for (int i=0; i<FileList.toArray().length; i++)
                {
                    System.out.println(FileList.toArray()[i]);
                }
                System.out.println("-------");
            }
        }
    };
    
    static boolean upload(UserToken token, String group, String remoteFile, String localFile)
    {
        Message Upload = new Message(FS_UPLOAD);
        
        try
        {
            // Create Message header
            Upload.addObject((UserToken) token);
            Upload.addObject((String) group);
            Upload.addObject((String) remoteFile);

            // Attach FileContent
            File FileHandle = new File((String) localFile);
            byte[] FileContent = new byte[(int) FileHandle.length()];
            FileInputStream FileStream = new FileInputStream(FileHandle);
            BufferedInputStream FileBuff = new BufferedInputStream(FileStream);
            FileBuff.read(FileContent, 0, FileContent.length);

            Upload.addObject((byte[]) FileContent);

            // Free resources
            FileBuff.close();
            FileStream.close();
        }
        catch (Exception e)
        {
            return false;
        }
        
        //  Send message
        try {
            Output.writeObject(Upload);
            Output.flush();
        } catch (Exception ex) {
            return false;
        }
        
        //  Receive response
        Message Response;
        try {
            Response = (Message) Input.readObject();
        } catch (Exception ex) {
            return false;
        }
        
        return (Response.getMessage().equals(FS_SUCCESS));
    }
    static ClientFramework Upload = new ClientFramework("Upload")
    {
        @Override
        public void run()
        {
            // Create an upload message
            Scanner Input = new Scanner(System.in);

            System.out.print("Please enter group name:");
            String Group = Input.nextLine();
            System.out.print("Please enter local file name:");
            String Local = Input.nextLine();
            System.out.print("Please enter remote file name:");
            String Remote = Input.nextLine();

            String localFile = System.getProperty("user.dir") + File.separator + "FileClient" + File.separator;

            if (!upload(Token,Group,Remote,localFile + Local))
            {
                System.out.println("Upload failed");
            }
            else
            {
                System.out.println("Upload Succeed");
            }
        }
    };

    static boolean download(UserToken token, String group, String remoteFile, String localFile)
    {
        Message Download = new Message(FS_DOWNLOAD);
        
         // Create Message header
        Download.addObject((UserToken) token);
        Download.addObject((String) group);
        Download.addObject((String) remoteFile);
        
        // Send the message
        try {            
            Output.writeObject(Download);
            Output.flush();
        } catch (Exception ex) {
            return false;
        }
        
        //  Receive response
        Message Response;
        try {
            Response = (Message) Input.readObject();
        } catch (Exception ex) {
            return false;
        }
        
        // Check response
        if (!Response.getMessage().equals(FS_UPLOAD))
        {
            return false;
        }
        
        // Save file
        try
        {
            ArrayList<Object> Content = Response.getObjCont();
            byte[] FileContent = (byte[]) Content.get(FS_UPLOAD_FILE_CONTENT);
            File FileHandle = new File(localFile);
            FileHandle.getParentFile().mkdirs();
            FileOutputStream FileStream = new FileOutputStream(FileHandle);
            BufferedOutputStream FileBuff = new BufferedOutputStream(FileStream);
            FileBuff.write(FileContent, 0, FileContent.length);
            FileBuff.flush();

            // Free resources
            FileBuff.close();
            FileStream.close();
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }
    static ClientFramework Download = new ClientFramework("Download")
    {
        @Override
        public void run()
        {
            // Create an download message
            Scanner Input = new Scanner(System.in);

            System.out.print("Please enter group name:");
            String Group = Input.nextLine();
            System.out.print("Please enter local file name:");
            String Local = Input.nextLine();
            System.out.print("Please enter remote file name:");
            String Remote = Input.nextLine();

            String localFile = System.getProperty("user.dir") + File.separator + "FileClient" + File.separator;

            if(!download(Token,Group,Remote,localFile + Local))
            {
                System.out.println("Download failed");
            }
            else
            {
                System.out.println("Download Succeed");
            }
        }
    };
    
    Client2(UserToken token)
    {
        Token = token;
    }
    
    public static void main(String[] args)
    {
        run();
    }
    
    public static void run()
    {
        ClientFramework MainMenu = new ClientFramework("Main Menu");

        MainMenu.RegisterItem(Connect);
        MainMenu.RegisterItem(Disconnect);
        MainMenu.RegisterItem(ListFile);
        MainMenu.RegisterItem(Upload);
        MainMenu.RegisterItem(Download);
        MainMenu.run();
    }
}
