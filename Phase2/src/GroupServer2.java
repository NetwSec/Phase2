
import java.io.File;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;

/**
 * Group server
 * 
 * GroupServer2 is a simple server providing account management.
 * It is based on ServerFramework and can handle 5 messages: login, adduser, 
 * addgroup, mgnt, and listgroup. It can return success message when receives
 * login/adduser/addgroup/mgnt request, view message when receives listgroup
 * request, and error message when something goes wrong.
 * 
 * The class consists of 5 callbacks for each messages, 1 error message generator,
 * and 1 run() method to register callbacks, populate UserList, and start the server.
 * 
 * The default admin account is admin and the default admin group is also admin.
 * They are defined by GS_ADMIN_GROUP.
 * 
 * The default issuer value in UserToken is test_server. This is defined by
 * GS_IDENTITY.
 * 
 * The default UserList file is ./GroupServer/UserList.bin. This is defined by
 * GS_STORAGE.
 * 
 * The default port is 8765. This is defined by GS_PORT.
 */
public class GroupServer2 {

    //  Define the index of each object in the Content
    public final static int GS_GENERAL_USER_TOKEN = 0;  //UserToken Token
    public final static int GS_GENERAL_GROUP_NAME = 1;  //String    Group
    public final static int GS_GENERAL_USER_NAME = 1;   //String    User

    public final static String GS_LOGIN = "login";      //login
    public final static int GS_LOGIN_USER_TOKEN = 0;    //UserToken Token
    public final static int GS_LOGIN_USER_NAME = 1;     //String    User
    public final static int GS_LOGIN_USER_PW = 2;       //byte[]    User

    public final static String GS_CHANGEPASS = "changepass"; //changepass
    public final static int GS_CHANGEPASS_USER_TOKEN = 0;    //UserToken Token
    public final static int GS_CHANGEPASS_USER_NAME = 1;     //String User
    public final static int GS_CHANGEPASS_OLD_PW = 2;        //byte[] old pass
    public final static int GS_CHANGEPASS_NEW_PW = 3;        //byte[] new pass
    
    public final static String GS_ADDUSER = "adduser";  //adduser
    public final static int GS_ADDUSER_USER_TOKEN = 0;  //UserToken Token
    public final static int GS_ADDUSER_USER_NAME = 1;   //String    User
    public final static int GS_ADDUSER_USER_PASSWD = 2; //byte[]    Password


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

    static class loginCallback implements ServerFramework.ServerCallback {

        @Override
        public Message CallbackProc(Socket Client, ArrayList<Object> Content) {
            System.out.println("Received a login message");

            Message Response = new Message(GS_SUCCESS);

            // Get the user info
            String UserName = (String) Content.get(GS_LOGIN_USER_NAME);
            byte[] Password = (byte[]) Content.get(GS_LOGIN_USER_PW);
//            byte[] PasswordHash = (byte[]) Content.get(GS_LOGIN_USER_PW);
            User UserInfo = Account.getUser(UserName);
                        
            // Permission: only register user can login
            if (UserInfo != null) {
                
                // Compare the password hashes
                if(Account.comparePasswords(Password, UserInfo))
                {
                    // Passwords match, return the token
                    UserToken Token = new UserTokenImp(GS_IDENTITY, UserInfo);

                    //  Create Message
                    Response.addObject((UserToken) Token);
                    Response.addObject((String) UserName);
                }
                else
                {
                    System.out.println("Incorrect password, please try again");
                    Response = GenerateErrorMessage(Content);
                }
            } else {
                //  Return error message
                System.out.println("Failed to find the user token, continue");
                Response = GenerateErrorMessage(Content);
            }

            return Response;
        }
    }
    
    static class changepassCallback implements ServerFramework.ServerCallback {

        @Override
        public Message CallbackProc(Socket Client, ArrayList<Object> Content) {
            System.out.println("Received a change password message");

            Message Response = new Message(GS_SUCCESS);

            UserToken Token = (UserToken) Content.get(GS_CHANGEPASS_USER_TOKEN);
            String Username = (String) Content.get(GS_CHANGEPASS_USER_NAME);
            byte[] OldPassword = (byte[]) Content.get(GS_CHANGEPASS_OLD_PW);
            byte[] NewPassword = (byte[]) Content.get(GS_CHANGEPASS_NEW_PW);
            User UserInfo = Account.getUser(Token.getSubject());

            // Permission: registered user and OldPassword hash matches stored hash
            if ((UserInfo != null) && Account.changePassword(Token.getSubject(), OldPassword, NewPassword)){
                //  Create Message
                Response.addObject((UserToken) Token);                
            } else {
                //  Return error message
                System.out.println("Failed to change password, continue");
                Response = GenerateErrorMessage(Content);
            }

            return Response;
        }
    }
    
    static class adduserCallback implements ServerFramework.ServerCallback {

        @Override
        public Message CallbackProc(Socket Client, ArrayList<Object> Content) {
            System.out.println("Received an adduser message");

            Message Response = new Message(GS_SUCCESS);

            UserToken Token = (UserToken) Content.get(GS_ADDUSER_USER_TOKEN);
            String UserName = (String) Content.get(GS_ADDUSER_USER_NAME);
            byte[] Password = (byte[]) Content.get(GS_ADDUSER_USER_PASSWD);
            User UserInfo = Account.getUser(Token.getSubject());

            // Permission: admin
            if ((UserInfo != null)
                    && (UserInfo.getGroups().contains(GS_ADMIN_GROUP))
                    && (Account.addUser(UserName, Password))) {
                //  Create Message
                Response.addObject((UserToken) Token);
                Response.addObject((String) UserName);
            } else {
                //  Return error message
                System.out.println("Failed to add user, continue");
                Response = GenerateErrorMessage(Content);
            }

            return Response;
        }
    }

    static class addgroupCallback implements ServerFramework.ServerCallback {

        @Override
        public Message CallbackProc(Socket Client, ArrayList<Object> Content) {
            System.out.println("Received an addgroup message");

            Message Response = new Message(GS_SUCCESS);

            UserToken Token = (UserToken) Content.get(GS_ADDGROUP_USER_TOKEN);
            String GroupName = (String) Content.get(GS_ADDGROUP_GROUP_NAME);
            User UserInfo = Account.getUser(Token.getSubject());

            // Permission: anyone
            if (UserInfo == null) {
                //  Return error message
                System.out.println("Failed to add group, continue");
                Response = GenerateErrorMessage(Content);
                return Response;
            }

            // Check if group was created before
            for (Enumeration<String> UserList = Account.getUsernames(); UserList.hasMoreElements();) {
                //If groupname is taken
                if (Account.getUserOwnerships(UserList.nextElement()).contains(GroupName)) {
                    //  Return error message
                    System.out.println("Failed to add group, continue");
                    Response = GenerateErrorMessage(Content);
                    return Response;
                }
            }

            Account.addOwnerships(Token.getSubject(), GroupName);

            //  Create Message
            Token = new UserTokenImp(GS_IDENTITY, UserInfo);
            Response.addObject((UserToken) Token);
            Response.addObject((String) GroupName);

            return Response;
        }

    }

    static class mgntCallback implements ServerFramework.ServerCallback {

        @Override
        public Message CallbackProc(Socket Client, ArrayList<Object> Content) {
            System.out.println("Received a mgnt message");

            Message Response = new Message(GS_SUCCESS);

            UserToken Token = (UserToken) Content.get(GS_MGNT_USER_TOKEN);
            String GroupName = (String) Content.get(GS_MGNT_GROUP_NAME);
            String UserName = (String) Content.get(GS_MGNT_USER_NAME);
            boolean Option = (boolean) Content.get(GS_MGNT_OPTION);
            User UserInfo = Account.getUser(Token.getSubject());

            // Permission: owner
            if ((UserInfo != null)
                    && (UserInfo.getOwnerships().contains(GroupName))
                    && (Option == GS_MGNT_OPTION_ADD ? Account.addGroup(UserName, GroupName) : Account.removeGroup(UserName, GroupName))) {
                //  Create Message
                if (UserName.equals(Token.getSubject())) {
                    Token = new UserTokenImp(GS_IDENTITY, Account.getUser(UserName));
                }
                Response.addObject((UserToken) Token);
                Response.addObject((String) GroupName);
            } else {
                //  Return error message
                System.out.println("Failed to manage group member, continue");
                Response = GenerateErrorMessage(Content);
            }

            return Response;
        }
    }

    static class listgroupCallback implements ServerFramework.ServerCallback {

        @Override
        public Message CallbackProc(Socket Client, ArrayList<Object> Content) {
            System.out.println("Received a listgroup message");

            Message Response = new Message(GS_VIEW);

            UserToken Token = (UserToken) Content.get(GS_LISTGROUP_USER_TOKEN);
            String GroupName = (String) Content.get(GS_LISTGROUP_GROUP_NAME);
            User UserInfo = Account.getUser(Token.getSubject());

            // Permission: owner
            if ((UserInfo != null)
                    && (UserInfo.getOwnerships().contains(GroupName))) {
                ArrayList<String> UserList = new ArrayList<String>();
                for (Enumeration<String> unList = Account.getUsernames(); unList.hasMoreElements();) {
                    String tUser = unList.nextElement();
                    if (Account.getUserGroups(tUser).contains(GroupName)) {
                        UserList.add(tUser);
                    }
                }

                //  Create Message
                Response.addObject((UserToken) Token);
                Response.addObject((String) GroupName);
                Response.addObject((ArrayList<String>) UserList);
            } else {
                //  Return error message
                System.out.println("Failed to list group, continue");
                Response = GenerateErrorMessage(Content);
            }

            return Response;
        }
    }

    static public Message GenerateErrorMessage(ArrayList<Object> Content) {
        Message Response = new Message(GS_ERROR);
        Response.addObject((UserToken) Content.get(GS_GENERAL_USER_TOKEN));
        Response.addObject((String) Content.get(GS_GENERAL_GROUP_NAME));
        return Response;
    }
    
    public static int GS_PORT = 8765;
    public static String GS_STORAGE = System.getProperty("user.dir") + File.separator + "GroupServer" + File.separator + "UserList.bin";
    public static String GS_IDENTITY = "test_server";
    public static String GS_ADMIN_GROUP = "admin";
    public static UserList Account;

    GroupServer2(int Port) {
        GS_PORT = Port;
    }

    public static void run() {
        // Create instances
        System.out.println("Initalize group server");
        ServerFramework Server = new ServerFramework(GS_PORT)
        {
            public Message Decode(Object o)
            {
                return (Message) o;
            }
            
            public Object Encode(Message o)
            {
                return (Object) o;
            }
        };
        loginCallback login = new loginCallback();
        changepassCallback changepass = new changepassCallback();
        adduserCallback adduser = new adduserCallback();
        addgroupCallback addgroup = new addgroupCallback();
        mgntCallback mgnt = new mgntCallback();
        listgroupCallback listgroup = new listgroupCallback();

        // Register callbacks
        System.out.println("Register messages");
        Server.RegisterMessage(GS_LOGIN, login);
        Server.RegisterMessage(GS_CHANGEPASS, changepass);
        Server.RegisterMessage(GS_ADDUSER, adduser);
        Server.RegisterMessage(GS_ADDGROUP, addgroup);
        Server.RegisterMessage(GS_MGNT, mgnt);
        Server.RegisterMessage(GS_LISTGROUP, listgroup);

        // Initalize account information
        File FileHandle = new File(GS_STORAGE);
        FileHandle.getParentFile().mkdirs();
        Account = new UserList(GS_STORAGE);
        if (!Account.Load(GS_ADMIN_GROUP))
        {
            System.out.println("Unable to initalize user account information, halt");
            return;
        }

        // Start listener
        System.out.println("Start the listener");
        Server.run();
    }
}
