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
public class GroupServer2
{
    //  Define the index of each object in the Content
    public final static int GS_GENERAL_USER_TOKEN = 0;  //UserToken Token
    public final static int GS_GENERAL_GROUP_NAME = 1;  //String    Group
    public final static int GS_GENERAL_USER_NAME = 1;   //String    User
    
    public final static int GS_LOGIN_USER_TOKEN = 0;    //UserToken Token
    public final static int GS_LOGIN_USER_NAME = 1;     //String    User
    
    public final static int GS_ADDUSER_USER_TOKEN = 0;  //UserToken Token
    public final static int GS_ADDUSER_USER_NAME = 1;   //String    User
    
    public final static int GS_ADDGROUP_USER_TOKEN = 0; //UserToken Token
    public final static int GS_ADDGROUP_GROUP_NAME = 1; //String    Group
    
    public final static int GS_MGNT_USER_TOKEN = 0;     //UserToken Token
    public final static int GS_MGNT_GROUP_NAME = 1;     //String    Group
    public final static int GS_MGNT_USER_NAME = 2;      //String    User
    public final static int GS_MGNT_OPTION = 3;         //boolean   Option
    
    public final static boolean GS_MGNT_OPTION_ADD = true;      //boolean   Option
    public final static boolean GS_MGNT_OPTION_REMOVE = false;  //boolean   Option
    
    public final static int GS_LISTGROUP_USER_TOKEN = 0; //UserToken Token
    public final static int GS_LISTGROUP_GROUP_NAME = 1; //String    Group
    
    public final static int GS_VIEW_USER_TOKEN = 0;     //UserToken Token
    public final static int GS_VIEW_GROUP_NAME = 1;     //String    Group
    public final static int GS_VIEW_USER_LIST = 2;      //String[]  List
    
    public final static int GS_SUCCESS_USER_TOKEN = 0;  //UserToken Token
    public final static int GS_SUCCESS_GROUP_NAME = 1;  //String    Group
    public final static int GS_SUCCESS_USER_NAME = 1;   //String    User
    
    public final static int GS_ERROR_USER_TOKEN = 0;    //UserToken Token
    public final static int GS_ERROR_GROUP_NAME = 1;    //String    Group
    
    static class loginCallback implements ServerFramework.ServerCallback
    {
        @Override
        public Message CallbackProc(Socket Client, ArrayList<Object> Content)
        {
            System.out.println("Received a login message");
            
            Message Response = new Message("success");
            
            // Generate the user token
            UserToken Token = GetToken((String) Content.get(GS_LOGIN_USER_NAME));
            String UserName = (String) Content.get(GS_LOGIN_USER_NAME);
            
            if (Token != null)
            {
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
            
            Message Response = new Message("success");
            
            UserToken Token = (UserToken) Content.get(GS_ADDUSER_USER_TOKEN);
            String UserName = (String) Content.get(GS_ADDUSER_USER_NAME);
            
            if (AddUser(Token, UserName))
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
            
            Message Response = new Message("success");
            
            UserToken Token = (UserToken) Content.get(GS_ADDGROUP_USER_TOKEN);
            String GroupName = (String) Content.get(GS_ADDGROUP_GROUP_NAME);
            
            if (AddGroup(Token, GroupName))
            {
                //  Create Message
                Response.addObject((UserToken) Token);
                Response.addObject((String) GroupName);
            }
            else
            {
                //  Return error message
                System.out.println("Failed to add group, continue");
                Response = GenerateErrorMessage(Content);
            }
            
            return Response;
        }
        
    }
    
    static class mgntCallback implements ServerFramework.ServerCallback
    {
        @Override
        public Message CallbackProc(Socket Client, ArrayList<Object> Content)
        {
            System.out.println("Received a mgnt message");
            
            Message Response = new Message("success");
            
            UserToken Token = (UserToken) Content.get(GS_MGNT_USER_TOKEN);
            String GroupName = (String) Content.get(GS_MGNT_GROUP_NAME);
            String UserName = (String) Content.get(GS_MGNT_USER_NAME);
            boolean Option = (boolean) Content.get(GS_MGNT_OPTION);
            
            if (ManageGroupMember(Token, GroupName, UserName, Option))
            {
                //  Create Message
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
            
            Message Response = new Message("view");
            
            UserToken Token = (UserToken) Content.get(GS_LISTGROUP_USER_TOKEN);
            String GroupName = (String) Content.get(GS_LISTGROUP_GROUP_NAME);
            String [] UserList = ListGroup(Token, GroupName);
            if (UserList != null)
            {
                //  Create Message
                Response.addObject((UserToken) Token);
                Response.addObject((String) GroupName);
                Response.addObject((String []) UserList);
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
        Message Response = new Message("error");
        Response.addObject((UserToken) Content.get(GS_GENERAL_USER_TOKEN));
        Response.addObject((String) Content.get(GS_GENERAL_GROUP_NAME));
        return Response;
    }

    public static int GS_PORT = 8765;
    public static String GS_STORAGE = System.getProperty("user.dir") + File.separator + "GroupServer" + File.separator + "UserList.bin";
    public static UserList Account;
    
    GroupServer2(int Port, String Storage)
    {
        GS_PORT = Port;
        GS_STORAGE = Storage;
    }
    
    public static void main(String args[])
    {
        run();
    }
    
    public static void run()
    {
        // Create instances
        System.out.println("Initalize file server");
        ServerFramework Server = new ServerFramework(GS_PORT);
        loginCallback login = new loginCallback();
        adduserCallback adduser = new adduserCallback();
        addgroupCallback addgroup = new addgroupCallback();
        mgntCallback mgnt = new mgntCallback();
        listgroupCallback listgroup = new listgroupCallback();
        
        // Register callbacks
        System.out.println("Register messages");
        Server.RegisterMessage("login", login);
        Server.RegisterMessage("adduser", adduser);
        Server.RegisterMessage("addgroup", addgroup);
        Server.RegisterMessage("mgnt", mgnt);
        Server.RegisterMessage("listgroup", listgroup);

        
        // Start listener
        System.out.println("Start the listener");
        Server.run();
    }
    
    public class UserList
    {
        
    }
}