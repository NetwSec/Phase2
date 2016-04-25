
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.security.*;

/**
 * General client for both servers
 *
 * The Client2 is a universal client that connects to one group server and one
 * file server upon starting. It is based on ClientFramework to display the main
 * menu, 7 leaf nodes to take extra information from user, and subroutines
 * defined in the specification:
 *
 * connect(): Implemented
 *
 * disconnect():Implemented
 *
 * getToken(): Implemented
 *
 * createUser(): Implemented
 *
 * createGroup(): Implemented
 *
 * addUserToGroup()/deleteUserFromGroup(): Implemented as manageUser()
 *
 * listMembers(): Implemented
 *
 * listFiles(): Implemented
 *
 * upload(): Implemented
 *
 * download(): Implemented
 *
 * The Client2 consists 10 standard subroutines, 1 run() method to start the main
 * ClientFramework class, and other 10 ClientFramework in following topology:
 * 
 * [Welcome] - [Log In] - [Main Menu] - [Add User]
 *           L Exit                   L [Add Group]
 *                                    L [User Management]
 *                                    L [List Members]
 *                                    L [List Files]
 *                                    L [Upload]
 *                                    L [Download]
 *                                    L Exit - [Disconnect]
 * 
 * The reason for Main Menu node is Log In is a leaf node, thus has no loop for
 * user input.
 * 
 * The default file saving location is ./Client/. This is defined by LocalFile.
 */
public class Client2 {

    // Can't get import working, just copy the definition
    public final static int GS_GENERAL_USER_TOKEN = 0;  //UserToken Token
    public final static int GS_GENERAL_GROUP_NAME = 1;  //String    Group
    public final static int GS_GENERAL_USER_NAME = 1;   //String    User

    public final static String GS_LOGIN = "login";      //login
    public final static int GS_LOGIN_USER_TOKEN = 0;    //UserToken Token
    public final static int GS_LOGIN_USER_NAME = 1;     //String    User

    public final static String GS_CHANGEPASS = "changepass"; //changepass
    public final static int GS_CHANGEPASS_USER_TOKEN = 0;    //UserToken Token
//    public final static int GS_CHANGEPASS_USER_NAME = 1;     //String User
    public final static int GS_CHANGEPASS_OLD_PW = 1;        //String old pass
    public final static int GS_CHANGEPASS_NEW_PW = 2;        //String new pass

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

    // Common services
    static boolean connect() {
        try {
            GServer = new Socket(GS_ADDRESS, GS_PORT);
            GOutput = new ObjectOutputStream(GServer.getOutputStream());
            GInput = new ObjectInputStream(GServer.getInputStream());

            FServer = new Socket(FS_ADDRESS, FS_PORT);
            FOutput = new ObjectOutputStream(FServer.getOutputStream());
            FInput = new ObjectInputStream(FServer.getInputStream());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    static boolean getToken(String UserName, String PassWord) {
        Message Login = new Message(GS_LOGIN);
        // Create Message header
        Login.addObject((UserToken) null);
        Login.addObject((String) UserName);
        Login.addObject((String) PassWord);
//        Login.addObject((byte[]) getHash(PassWord));

        //  Send message
        try {
            GOutput.writeObject(Login);
            GOutput.flush();
        } catch (Exception ex) {
            return false;
        }

        //  Receive response
        Message Response;
        try {
            Response = (Message) GInput.readObject();
        } catch (Exception ex) {
            return false;
        }

        // Update token
        if (Response.getMessage().equals("success")) {
            UpdateToken((UserToken) Response.getObjCont().get(GS_SUCCESS_USER_TOKEN));
            return true;
        }
        return false;
    }
    
    static ClientFramework Login = new ClientFramework("Log in") {
        @Override
        public void run() {
            Scanner Input = new Scanner(System.in);

            // 1. Collect server information
            System.out.println("Please enter the group server address");
            System.out.print("Default[localhost]: ");
            GS_ADDRESS = Input.nextLine();
            if (GS_ADDRESS.equals("")) {
                GS_ADDRESS = "localhost";
            }

            System.out.println("Please enter the group server port");
            System.out.print("Default[8765]: ");
            String GPort = Input.nextLine();
            try {
                GS_PORT = Integer.parseInt(GPort);
            } catch (Exception e) {
                GS_PORT = 8765;
            }

            System.out.println("Please enter the file server address");
            System.out.print("Default[localhost]: ");
            FS_ADDRESS = Input.nextLine();
            if (FS_ADDRESS.equals("")) {
                FS_ADDRESS = "localhost";
            }

            System.out.println("Please enter the file server port");
            System.out.print("Default[8766]: ");
            String FPort = Input.nextLine();
            try {
                FS_PORT = Integer.parseInt(FPort);
            } catch (Exception e) {
                FS_PORT = 8766;
            }

            // 2. Connect to remote servers
            if (!connect()) {
                System.out.println("Connection failed");
                return;
            } else {
                System.out.println("Connected");
            }

            // 3. Login
            // Get user token before further action
            while (true) {
                System.out.print("Please enter your user name: ");
                String UserName = Input.nextLine();
                // Get password
                System.out.print("Please enter your password: ");
                String PassWord = Input.nextLine();
                
                // See if we can get the token
                if (getToken(UserName, PassWord)) { 
                    break;
                }

                System.out.println("Invalid user name or password. Please retry");
            }
            System.out.println();

            // 4. Start the main menu
            ClientFramework MainMenu = new ClientFramework("Main Menu");

            // Group server specific
            MainMenu.RegisterItem(AddUser);
            MainMenu.RegisterItem(AddGroup);
            MainMenu.RegisterItem(Management);
            MainMenu.RegisterItem(ListMembers);
            MainMenu.RegisterItem(ChangePassword);

            // File server specific
            MainMenu.RegisterItem(ListFiles);
            MainMenu.RegisterItem(Upload);
            MainMenu.RegisterItem(Download);

            // Set exit handler
            MainMenu.SetExitHandler(Disconnect);
            MainMenu.run();
        }
    };

    static void disconnect() {
        try {
            GInput.close();
            GOutput.close();
            GServer.close();
            FInput.close();
            FOutput.close();
            FServer.close();
        } catch (Exception ex) {
        }
    }
    static ClientFramework Disconnect = new ClientFramework("Disconnect") {
        @Override
        public void Exit() {
            disconnect();
            System.out.println("Disconnected.");
        }
    };

    // Group server services
    static boolean changePassword(UserToken token, String oldPassword, String newPassword)
    {
         Message Upload = new Message(GS_CHANGEPASS);

        // Create Message header
        Upload.addObject((UserToken) token);
        Upload.addObject((String) oldPassword);
        Upload.addObject((String) newPassword);

        //  Send message
        try {
            GOutput.writeObject(Upload);
            GOutput.flush();
        } catch (Exception ex) {
            return false;
        }

        //  Receive response
        Message Response;
        try {
            Response = (Message) GInput.readObject();
        } catch (Exception ex) {
            return false;
        }

        if (!Response.getMessage().equals(GS_SUCCESS)) {
            return false;
        } else {
            return true;
        }
        
    }
    static ClientFramework ChangePassword = new ClientFramework("Change password") {
        @Override
        public void run() {
            
            Scanner Input = new Scanner(System.in);

            System.out.print("Please enter the current password: ");
            String currentPass = Input.nextLine();
            System.out.print("Please enter the new password: ");
            String newPass = Input.nextLine();

            if (!changePassword(Token, currentPass, newPass)) {
                System.out.println("Operation failed");
            } else {
                System.out.println("Operation succeed");
            }
        }
    };
    
    
    
    static boolean createUser(UserToken token, String Username) {
        Message Upload = new Message(GS_ADDUSER);

        // Create Message header
        Upload.addObject((UserToken) token);
        Upload.addObject((String) Username);

        //  Send message
        try {
            GOutput.writeObject(Upload);
            GOutput.flush();
        } catch (Exception ex) {
            return false;
        }

        //  Receive response
        Message Response;
        try {
            Response = (Message) GInput.readObject();
        } catch (Exception ex) {
            return false;
        }

        if (!Response.getMessage().equals(GS_SUCCESS)) {
            return false;
        } else {
            return true;
        }
    }
    static ClientFramework AddUser = new ClientFramework("Create new user") {
        @Override
        public void run() {
            
            Scanner Input = new Scanner(System.in);

            System.out.print("Please enter the user name: ");
            String Username = Input.nextLine();
            System.out.println("A default password will be set.");

            if (!createUser(Token, Username)) {
                System.out.println("Operation failed");
            } else {
                System.out.println("Operation succeed");
            }
        }
    };

    static boolean createGroup(UserToken token, String Group) {
        Message Upload = new Message(GS_ADDGROUP);

        // Create Message header
        Upload.addObject((UserToken) token);
        Upload.addObject((String) Group);

        //  Send message
        try {
            GOutput.writeObject(Upload);
            GOutput.flush();
        } catch (Exception ex) {
            return false;
        }

        //  Receive response
        Message Response;
        try {
            Response = (Message) GInput.readObject();
        } catch (Exception ex) {
            return false;
        }

        if (!Response.getMessage().equals(GS_SUCCESS)) {
            return false;
        } else {
            // Update token
            UpdateToken((UserToken)Response.getObjCont().get(GS_SUCCESS_USER_TOKEN));
            return true;
        }
    }
    static ClientFramework AddGroup = new ClientFramework("Create new group") {
        @Override
        public void run() {
            Scanner Input = new Scanner(System.in);

            System.out.print("Please enter the group name: ");
            String Group = Input.nextLine();

            if (!createGroup(Token, Group)) {
                System.out.println("Operation failed");
            } else {
                System.out.println("Operation succeed");
            }
        }
    };

    static boolean manageUser(UserToken token, String Group, String UserName, String Operation) {
        Message Upload = new Message(GS_MGNT);

        // Create Message header
        Upload.addObject((UserToken) token);
        Upload.addObject((String) Group);
        Upload.addObject((String) UserName);
        boolean Option;
        if (Operation.equals("add")) {
            Option = GS_MGNT_OPTION_ADD;
        } else if (Operation.equals("remove")) {
            Option = GS_MGNT_OPTION_REMOVE;
        } else {
            return false;
        }
        Upload.addObject((boolean) Option);

        //  Send message
        try {
            GOutput.writeObject(Upload);
            GOutput.flush();
        } catch (Exception ex) {
            return false;
        }

        //  Receive response
        Message Response;
        try {
            Response = (Message) GInput.readObject();
        } catch (Exception ex) {
            return false;
        }

        if (!Response.getMessage().equals(GS_SUCCESS)) {
            return false;
        } else {
            // Update token
            if (token.getSubject().equals(UserName)) {
                
                UpdateToken((UserToken) Response.getObjCont().get(FS_SUCCESS_USER_TOKEN));
            }
            return true;
        }
    }
    static ClientFramework Management = new ClientFramework("User management") {
        @Override
        public void run() {
            Scanner Input = new Scanner(System.in);

            System.out.print("Please enter the group name: ");
            String Group = Input.nextLine();

            // Code in listmembers
            List<String> UserList = listMembers(Token, Group);
            if (UserList == null) {
                System.out.println("Operation failed");
                return;
            } else {
                System.out.println("The following members are in the group " + Group);
                for (int i = 0; i < UserList.toArray().length; i++) {
                    System.out.println(UserList.toArray()[i]);
                }
                System.out.println();
            }
            // End of copypasta

            System.out.print("Please enter the user name: ");
            String UserName = Input.nextLine();

            System.out.print("Please enter the operation [add|remove]: ");
            String Operation = Input.nextLine();

            if (!manageUser(Token, Group, UserName, Operation)) {
                System.out.println("Operation failed");
            } else {
                System.out.println("Operation succeed");
            }

            // Code in listmembers
            UserList = listMembers(Token, Group);
            if (UserList == null) {
                System.out.println("Operation failed");
                return;
            } else {
                System.out.println("The following members are in the group " + Group);
                for (int i = 0; i < UserList.toArray().length; i++) {
                    System.out.println(UserList.toArray()[i]);
                }
                System.out.println();
            }
            // End of copypasta
        }
    };

    static List<String> listMembers(UserToken token, String group) {
        Message Upload = new Message(GS_LISTGROUP);
        
        // Create Message header
        Upload.addObject((UserToken) token);
        Upload.addObject((String) group);

        //  Send message
        try {
            GOutput.writeObject(Upload);
            GOutput.flush();
        } catch (Exception ex) {
            return null;
        }

        //  Receive response
        Message Response;
        try {
            Response = (Message) GInput.readObject();
        } catch (Exception ex) {
            return null;
        }

        if (!Response.getMessage().equals(GS_VIEW)) {
            return null;
        } else {
            ArrayList<Object> Content = Response.getObjCont();
            return (List<String>) Content.get(GS_VIEW_USER_LIST);
        }
    }
    static ClientFramework ListMembers = new ClientFramework("List members in your group") {
        @Override
        public void run() {
            Scanner Input = new Scanner(System.in);

            System.out.print("Please enter the group name: ");
            String Group = Input.nextLine();

            List<String> UserList = listMembers(Token, Group);
            if (UserList == null) {
                System.out.println("Operation failed");
            } else {
                System.out.println("The following members are in the group " + Group);
                for (int i = 0; i < UserList.toArray().length; i++) {
                    System.out.println(UserList.toArray()[i]);
                }
            }
        }
    };

    // File server services
    static List<String> listFiles(UserToken token, String group) {
        Message Upload = new Message(FS_LIST);

        // Create Message header
        Upload.addObject((UserToken) token);
        Upload.addObject((String) group);

        //  Send message
        try {
            FOutput.writeObject(Upload);
            FOutput.flush();
        } catch (Exception ex) {
            return null;
        }

        //  Receive response
        Message Response;
        try {
            Response = (Message) FInput.readObject();
        } catch (Exception ex) {
            return null;
        }

        if (!Response.getMessage().equals(FS_VIEW)) {
            return null;
        } else {
            ArrayList<Object> Content = Response.getObjCont();
            return (List<String>) Content.get(FS_VIEW_FILE_LIST);
        }
    }
    static ClientFramework ListFiles = new ClientFramework("List Files") {
        @Override
        public void run() {
            Scanner Input = new Scanner(System.in);

            System.out.print("Please enter the group name: ");
            String Group = Input.nextLine();

            List<String> FileList = listFiles(Token, Group);
            if (FileList == null) {
                System.out.println("Operation failed");
            } else {
                System.out.println("In group " + Group + "the following files are available to you: ");
                for (int i = 0; i < FileList.toArray().length; i++) {
                    System.out.println(FileList.toArray()[i]);
                }
            }
        }
    };

    static boolean upload(UserToken token, String group, String remoteFile, String localFile) {
        Message Upload = new Message(FS_UPLOAD);

        try {
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
        } catch (Exception e) {
            return false;
        }

        //  Send message
        try {
            FOutput.writeObject(Upload);
            FOutput.flush();
        } catch (Exception ex) {
            return false;
        }

        //  Receive response
        Message Response;
        try {
            Response = (Message) FInput.readObject();
        } catch (Exception ex) {
            return false;
        }

        return (Response.getMessage().equals(FS_SUCCESS));
    }
    static ClientFramework Upload = new ClientFramework("Upload") {
        @Override
        public void run() {
            // Create an upload message
            Scanner Input = new Scanner(System.in);

            System.out.print("Please enter the local file name: ");
            String Local = Input.nextLine();
            System.out.print("Please enter the group name: ");
            String Group = Input.nextLine();
            System.out.print("Please enter the remote file name: ");
            String Remote = Input.nextLine();

            if (!upload(Token, Group, Remote, LocalFile + Local)) {
                System.out.println("Operation failed");
            } else {
                System.out.println("Operation Succeed");
            }
        }
    };

    static boolean download(UserToken token, String group, String remoteFile, String localFile) {
        Message Download = new Message(FS_DOWNLOAD);

        // Create Message header
        Download.addObject((UserToken) token);
        Download.addObject((String) group);
        Download.addObject((String) remoteFile);

        // Send the message
        try {
            FOutput.writeObject(Download);
            FOutput.flush();
        } catch (Exception ex) {
            return false;
        }

        //  Receive response
        Message Response;
        try {
            Response = (Message) FInput.readObject();
        } catch (Exception ex) {
            return false;
        }

        // Check response
        if (!Response.getMessage().equals(FS_UPLOAD)) {
            return false;
        }

        // Save file
        try {
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
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    static ClientFramework Download = new ClientFramework("Download") {
        @Override
        public void run() {
            // Create an download message
            Scanner Input = new Scanner(System.in);

            System.out.print("Please enter the group name: ");
            String Group = Input.nextLine();
            System.out.print("Please enter the remote file name: ");
            String Remote = Input.nextLine();
            System.out.print("Please enter the local file name: ");
            String Local = Input.nextLine();

            if (!download(Token, Group, Remote, LocalFile + Local)) {
                System.out.println("Operation failed");
            } else {
                System.out.println("Operation Succeed");
            }
        }
    };

    static String GS_ADDRESS = "localhost";
    static int GS_PORT = 8765;

    static String FS_ADDRESS = "localhost";
    static int FS_PORT = 8766;

    static Socket GServer;
    static ObjectInputStream GInput;
    static ObjectOutputStream GOutput;

    static Socket FServer;
    static ObjectInputStream FInput;
    static ObjectOutputStream FOutput;

    static UserToken Token;

    static String LocalFile = System.getProperty("user.dir") + File.separator + "Client" + File.separator;

    public static void UpdateToken(UserToken newToken) {
        Token = newToken;
    }

    public static void run() {

        // Set up the local file folder
        File FileHandle = new File(LocalFile);
        FileHandle.mkdirs();

        // Start the log in menu
        ClientFramework ClientEntry = new ClientFramework("Welcome");
        ClientEntry.RegisterItem(Login);
        ClientEntry.run();
    }
}
