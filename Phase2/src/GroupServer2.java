import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * File server
 * 
 * @author Yuntian Zhang
 */
public class GroupServer2
{
    //  Define the index of each object in the Content
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
    
    static class loginCallback implements ServerFramework.ServerCallback
    {
        @Override
        public Message CallbackProc(Socket Client, ArrayList<Object> Content)
        {
            System.out.println("Received a login message");
            
            Message Response = new Message(GS_SUCCESS);
            
            // Get the user info
            String UserName = (String) Content.get(GS_LOGIN_USER_NAME);
            User UserInfo = Account.getUser(UserName);
            
            // Permission: only register user can login
            if (UserInfo != null)
            {
                UserToken Token = new UserTokenImp(GS_IDENTITY, UserInfo);
                
                //  Create Message
                Response.addObject((UserToken) Token);
                Response.addObject((String) UserName);
            }
            else
            {
                //  Return error message
                System.out.println("Failed to find the user token, continue");
                Response = GenerateErrorMessage(Content);
            }
            
            return Response;
        }
    }
    
    static class adduserCallback implements ServerFramework.ServerCallback
    {
        @Override
        public Message CallbackProc(Socket Client, ArrayList<Object> Content)
        {
            System.out.println("Received an adduser message");
            
            Message Response = new Message(GS_SUCCESS);
            
            UserToken Token = (UserToken) Content.get(GS_ADDUSER_USER_TOKEN);
            String UserName = (String) Content.get(GS_ADDUSER_USER_NAME);
            User UserInfo = Account.getUser(Token.getSubject());
            
            // Permission: admin
            if (
                    (UserInfo != null) && 
                    (UserInfo.getGroups().contains(GS_ADMIN_GROUP)) &&
                    (Account.addUser(UserName))
                    )
            {
                //  Create Message
                Response.addObject((UserToken) Token);
                Response.addObject((String) UserName);
            }
            else
            {
                //  Return error message
                System.out.println("Failed to add user, continue");
                Response = GenerateErrorMessage(Content);
            }
            
            return Response;
        }
    }
    
    static class addgroupCallback implements ServerFramework.ServerCallback
    {
        @Override
        public Message CallbackProc(Socket Client, ArrayList<Object> Content)
        {
            System.out.println("Received an addgroup message");
            
            Message Response = new Message(GS_SUCCESS);
            
            UserToken Token = (UserToken) Content.get(GS_ADDGROUP_USER_TOKEN);
            String GroupName = (String) Content.get(GS_ADDGROUP_GROUP_NAME);
            User UserInfo = Account.getUser(Token.getSubject());
            
            // Permission: anyone
            if (UserInfo == null)
            {
                //  Return error message
                System.out.println("Failed to add group, continue");
                Response = GenerateErrorMessage(Content);
                return Response;
            }

            // Check if group was created before
            for (Enumeration<String> UserList = Account.getUsernames(); UserList.hasMoreElements();) {
                //If groupname is taken
                if (Account.getUserOwnerships(UserList.nextElement()).contains(GroupName))
                {
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
    
    static class mgntCallback implements ServerFramework.ServerCallback
    {
        @Override
        public Message CallbackProc(Socket Client, ArrayList<Object> Content)
        {
            System.out.println("Received a mgnt message");
            
            Message Response = new Message(GS_SUCCESS);
            
            UserToken Token = (UserToken) Content.get(GS_MGNT_USER_TOKEN);
            String GroupName = (String) Content.get(GS_MGNT_GROUP_NAME);
            String UserName = (String) Content.get(GS_MGNT_USER_NAME);
            boolean Option = (boolean) Content.get(GS_MGNT_OPTION);
            User UserInfo = Account.getUser(Token.getSubject());
            
            // Permission: owner
            if (
                    (UserInfo != null) && 
                    (UserInfo.getOwnerships().contains(GroupName)) &&
                    (Option == GS_MGNT_OPTION_ADD ? Account.addGroup(UserName, GroupName) : Account.removeGroup(UserName, GroupName))
                    )
            {
                //  Create Message
                if (UserName.equals(Token.getSubject()))
                {
                    Token = new UserTokenImp(GS_IDENTITY, Account.getUser(Token.getSubject()));
                }
                Response.addObject((UserToken) Token);
                Response.addObject((String) GroupName);
            }
            else
            {
                //  Return error message
                System.out.println("Failed to manage group member, continue");
                Response = GenerateErrorMessage(Content);
            }
            
            return Response;
        }
    }
    
    static class listgroupCallback implements ServerFramework.ServerCallback
    {
        @Override
        public Message CallbackProc(Socket Client, ArrayList<Object> Content)
        {
            System.out.println("Received a listgroup message");
            
            Message Response = new Message(GS_VIEW);
            
            UserToken Token = (UserToken) Content.get(GS_LISTGROUP_USER_TOKEN);
            String GroupName = (String) Content.get(GS_LISTGROUP_GROUP_NAME);
            User UserInfo = Account.getUser(Token.getSubject());

            // Permission: owner
            if (
                    (UserInfo != null) && 
                    (UserInfo.getOwnerships().contains(GroupName))
                    )
            {
                ArrayList<String> UserList = new ArrayList<String>();
                for (Enumeration<String> unList = Account.getUsernames(); unList.hasMoreElements();)
                {
                    String tUser = unList.nextElement();
                    if (Account.getUserGroups(tUser).contains(GroupName))
                    {
                        UserList.add(tUser);
                    }
                }
                
                //  Create Message
                Response.addObject((UserToken) Token);
                Response.addObject((String) GroupName);
                Response.addObject((ArrayList<String>) UserList);
            }
            else
            {
                //  Return error message
                System.out.println("Failed to list group, continue");
                Response = GenerateErrorMessage(Content);
            }
            
            return Response;
        }
    }
    
    static public Message GenerateErrorMessage(ArrayList<Object> Content)
    {
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
    
    GroupServer2(int Port)
    {
        GS_PORT = Port;
    }
    
    public static void run()
    {
        // Create instances
        System.out.println("Initalize group server");
        ServerFramework Server = new ServerFramework(GS_PORT);
        loginCallback login = new loginCallback();
        adduserCallback adduser = new adduserCallback();
        addgroupCallback addgroup = new addgroupCallback();
        mgntCallback mgnt = new mgntCallback();
        listgroupCallback listgroup = new listgroupCallback();
        
        // Register callbacks
        System.out.println("Register messages");
        Server.RegisterMessage(GS_LOGIN, login);
        Server.RegisterMessage(GS_ADDUSER, adduser);
        Server.RegisterMessage(GS_ADDGROUP, addgroup);
        Server.RegisterMessage(GS_MGNT, mgnt);
        Server.RegisterMessage(GS_LISTGROUP, listgroup);
        
        // Initalize account information
        Account = new UserList("UserList.bin", GS_ADMIN_GROUP);
        
        // Start listener
        System.out.println("Start the listener");
        Server.run();
    }
}