
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


    // Common services
    static boolean connectGS(String Address) {
        try {
            GServer = new SecureSocket(Address, GroupServer2.GS_PORT);
            GServer.connect();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    static boolean connectFS(String Address) {
        try {
            FServer = new SecureSocket(Address, FileServer.FS_PORT);
            FServer.connect();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    static boolean getToken(String UserName, String Password) {
        Message Login = new Message(GroupServer2.GS_LOGIN);
        // Create Message header
        Login.addObject((UserToken) null);
        Login.addObject((String) UserName);
        Crypto crypto = new Crypto();
        Login.addObject((byte[]) crypto.getHash(Password));

        //  Send message
        try {
            GServer.send(Login);
        } catch (Exception ex) {
            return false;
        }

        //  Receive response
        Message Response;
        try {
            Response = (Message) GServer.receive();
        } catch (Exception ex) {
            return false;
        }

        // Update token
        if (Response.getMessage().equals("success")) {
            UpdateToken((UserToken) Response.getObjCont().get(GroupServer2.GS_SUCCESS_USER_TOKEN));
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

            System.out.println("Please enter the file server address");
            System.out.print("Default[localhost]: ");
            FS_ADDRESS = Input.nextLine();
            if (FS_ADDRESS.equals("")) {
                FS_ADDRESS = "localhost";
            }

            // 2. Connect to remote servers
            if (!connectGS(GS_ADDRESS) || !connectFS(FS_ADDRESS)) {
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

                System.out.println("Invalid user name/password or authentication failure. Please retry");
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
            GServer.close();
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
    static boolean changePassword(String oldPassword, String newPassword) {
        Message Upload = new Message(GroupServer2.GS_CHANGEPASS);

        // Create Message header
        Upload.addObject((UserToken) Token);
        Upload.addObject((String) Token.getSubject());
        Crypto crypto = new Crypto();
        Upload.addObject((byte[]) Crypto.getHash(oldPassword));
        Upload.addObject((byte[]) Crypto.getHash(newPassword));

        //  Send message
        try {
            GServer.send(Upload);
        } catch (Exception ex) {
            return false;
        }

        //  Receive response
        Message Response;
        try {
            Response = (Message) GServer.receive();
        } catch (Exception ex) {
            return false;
        }

        if (!Response.getMessage().equals(GroupServer2.GS_SUCCESS)) {
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

            if (!changePassword(currentPass, newPass)) {
                System.out.println("Operation failed");
            } else {
                System.out.println("Operation succeed");
            }
        }
    };

    static boolean createUser(String Username, String Password) {
        Message Upload = new Message(GroupServer2.GS_ADDUSER);

        // Create Message header
        Upload.addObject((UserToken) Token);
        Upload.addObject((String) Username);
        Crypto crypto = new Crypto();
        Upload.addObject((byte[]) Crypto.getHash(Password));

        //  Send message
        try {
            GServer.send(Upload);
        } catch (Exception ex) {
            return false;
        }

        //  Receive response
        Message Response;
        try {
            Response = (Message) GServer.receive();
        } catch (Exception ex) {
            return false;
        }

        if (!Response.getMessage().equals(GroupServer2.GS_SUCCESS)) {
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
            System.out.print("Please enter the password: ");
            String Password = Input.nextLine();

            if (!createUser(Username, Password)) {
                System.out.println("Operation failed");
            } else {
                System.out.println("Operation succeed");
            }
        }
    };

    static boolean createGroup(String Group) {
        Message Upload = new Message(GroupServer2.GS_ADDGROUP);

        // Create Message header
        Upload.addObject((UserToken) Token);
        Upload.addObject((String) Group);

        //  Send message
        try {
            GServer.send(Upload);
        } catch (Exception ex) {
            return false;
        }

        //  Receive response
        Message Response;
        try {
            Response = (Message) GServer.receive();
        } catch (Exception ex) {
            return false;
        }

        if (!Response.getMessage().equals(GroupServer2.GS_SUCCESS)) {
            return false;
        } else {
            // Update token
            UpdateToken((UserToken)Response.getObjCont().get(GroupServer2.GS_SUCCESS_USER_TOKEN));
            return true;
        }
    }
    static ClientFramework AddGroup = new ClientFramework("Create new group") {
        @Override
        public void run() {
            Scanner Input = new Scanner(System.in);

            System.out.print("Please enter the group name: ");
            String Group = Input.nextLine();

            if (!createGroup(Group)) {
                System.out.println("Operation failed");
            } else {
                System.out.println("Operation succeed");
            }
        }
    };

    static boolean manageUser(String Group, String UserName, String Operation) {
        Message Upload = new Message(GroupServer2.GS_MGNT);

        // Create Message header
        Upload.addObject((UserToken) Token);
        Upload.addObject((String) Group);
        Upload.addObject((String) UserName);
        boolean Option;
        if (Operation.equals("add")) {
            Option = GroupServer2.GS_MGNT_OPTION_ADD;
        } else if (Operation.equals("remove")) {
            Option = GroupServer2.GS_MGNT_OPTION_REMOVE;
        } else {
            return false;
        }
        Upload.addObject((boolean) Option);

        //  Send message
        try {
            GServer.send(Upload);
        } catch (Exception ex) {
            return false;
        }

        //  Receive response
        Message Response;
        try {
            Response = (Message) GServer.receive();
        } catch (Exception ex) {
            return false;
        }

        if (!Response.getMessage().equals(GroupServer2.GS_SUCCESS)) {
            return false;
        } else {
            // Update token
            if (Token.getSubject().equals(UserName)) {
                UpdateToken((UserToken) Response.getObjCont().get(GroupServer2.GS_SUCCESS_USER_TOKEN));
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
            List<String> UserList = listMembers(Group);
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

            if (!manageUser(Group, UserName, Operation)) {
                System.out.println("Operation failed");
            } else {
                System.out.println("Operation succeed");
            }

            // Code in listmembers
            UserList = listMembers(Group);
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

    static List<String> listMembers(String group) {
        Message Upload = new Message(GroupServer2.GS_LISTGROUP);
        
        // Create Message header
        Upload.addObject((UserToken) Token);
        Upload.addObject((String) group);

        //  Send message
        try {
            GServer.send(Upload);
        } catch (Exception ex) {
            return null;
        }

        //  Receive response
        Message Response;
        try {
            Response = (Message) GServer.receive();
        } catch (Exception ex) {
            return null;
        }

        if (!Response.getMessage().equals(GroupServer2.GS_VIEW)) {
            return null;
        } else {
            ArrayList<Object> Content = Response.getObjCont();
            return (List<String>) Content.get(GroupServer2.GS_VIEW_USER_LIST);
        }
    }
    static ClientFramework ListMembers = new ClientFramework("List members in your group") {
        @Override
        public void run() {
            Scanner Input = new Scanner(System.in);

            System.out.print("Please enter the group name: ");
            String Group = Input.nextLine();

            List<String> UserList = listMembers(Group);
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
    static List<String> listFiles(String group) {
        Message Upload = new Message(FileServer.FS_LIST);

        // Create Message header
        Upload.addObject((UserToken) Token);
        Upload.addObject((String) group);

        //  Send message
        try {
            FServer.send(Upload);
        } catch (Exception ex) {
            return null;
        }

        //  Receive response
        Message Response;
        try {
            Response = (Message) FServer.receive();
        } catch (Exception ex) {
            return null;
        }

        if (!Response.getMessage().equals(FileServer.FS_VIEW)) {
            return null;
        } else {
            ArrayList<Object> Content = Response.getObjCont();
            return (List<String>) Content.get(FileServer.FS_VIEW_FILE_LIST);
        }
    }
    static ClientFramework ListFiles = new ClientFramework("List Files") {
        @Override
        public void run() {
            Scanner Input = new Scanner(System.in);

            System.out.print("Please enter the group name: ");
            String Group = Input.nextLine();

            List<String> FileList = listFiles(Group);
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

    static boolean upload(String group, String remoteFile, String localFile) {
        Message Upload = new Message(FileServer.FS_UPLOAD);

        try {
            // Create Message header
            Upload.addObject((UserToken) Token);
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
            FServer.send(Upload);
        } catch (Exception ex) {
            return false;
        }

        //  Receive response
        Message Response;
        try {
            Response = (Message) FServer.receive();
        } catch (Exception ex) {
            return false;
        }

        return (Response.getMessage().equals(FileServer.FS_SUCCESS));
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

            if (!upload(Group, Remote, LocalFile + Local)) {
                System.out.println("Operation failed");
            } else {
                System.out.println("Operation Succeed");
            }
        }
    };

    static boolean download(String group, String remoteFile, String localFile) {
        Message Download = new Message(FileServer.FS_DOWNLOAD);

        // Create Message header
        Download.addObject((UserToken) Token);
        Download.addObject((String) group);
        Download.addObject((String) remoteFile);

        // Send the message
        try {
            FServer.send(Download);
        } catch (Exception ex) {
            return false;
        }

        //  Receive response
        Message Response;
        try {
            Response = (Message) FServer.receive();
        } catch (Exception ex) {
            return false;
        }

        // Check response
        if (!Response.getMessage().equals(FileServer.FS_UPLOAD)) {
            return false;
        }

        // Save file
        try {
            ArrayList<Object> Content = Response.getObjCont();
            byte[] FileContent = (byte[]) Content.get(FileServer.FS_UPLOAD_FILE_CONTENT);
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

            if (!download(Group, Remote, LocalFile + Local)) {
                System.out.println("Operation failed");
            } else {
                System.out.println("Operation Succeed");
            }
        }
    };

    static boolean authenticate(UserToken token)
    {
        Message Authenticate = new Message(GroupServer2.GS_AUTH);
        
        Authenticate.addObject((UserToken) Token);
        Authenticate.addObject((String) Token.getSubject());
        Authenticate.addObject((UserToken) token);

        //  Send message
        try {
            GServer.send(Authenticate);
        } catch (Exception ex) {
            return false;
        }

        //  Receive response
        Message Response;
        try {
            Response = (Message) GServer.receive();
        } catch (Exception ex) {
            return false;
        }
        
        return Response.getMessage().equals(GroupServer2.GS_SUCCESS);
    }
    
    static String GS_ADDRESS = "localhost";

    static String FS_ADDRESS = "localhost";

    static SecureSocket GServer;

    static SecureSocket FServer;

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
