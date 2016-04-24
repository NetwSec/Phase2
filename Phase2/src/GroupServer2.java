
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import static jdk.nashorn.internal.objects.NativeObject.keys;

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
    public final static int GS_LOGIN_USER_PW = 2;     //String    User

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

    static class loginCallback implements ServerFramework.ServerCallback {

        @Override
        public Message CallbackProc(Socket Client, ArrayList<Object> Content) {
            System.out.println("Received a login message");

            Message Response = new Message(GS_SUCCESS);

            // Get the user info
            String UserName = (String) Content.get(GS_LOGIN_USER_NAME);
            String PassWord = (String) Content.get(GS_LOGIN_USER_PW);
            User UserInfo = Account.getUser(UserName);
                        
            // Permission: only register user can login
            if (UserInfo != null) {
                
                // Compare the password hashes
                if(Account.comparePasswords(PassWord, UserInfo))
                {
                    // Passwords match, return the token
                    UserToken Token = new UserTokenImp(GS_IDENTITY, UserInfo);   

                    //  Create Message
                    Response.addObject((UserToken)getSignedToken((UserTokenImp)Token));
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
//            String Username = (String) Content.get(GS_CHANGEPASS_USER_NAME);
            String OldPassword = (String) Content.get(GS_CHANGEPASS_OLD_PW);
            String NewPassword = (String) Content.get(GS_CHANGEPASS_NEW_PW);
            User UserInfo = Account.getUser(Token.getSubject());

            //Checks the legitimacy of the token
            if (authToken((UserTokenImp)Token)){
                // Permission: registered user and OldPassword hash matches stored hash
                if ((UserInfo != null) && Account.changePassword(Token.getSubject(), OldPassword, NewPassword)){
                    //  Create Message
                    Response.addObject((UserTokenImp) Token);                
                } else {
                    //  Return error message
                    System.out.println("Failed to change password, continue");
                    Response = GenerateErrorMessage(Content);
                }
           }
           else{
                //Return error message
                System.out.println("Failed to authenticate token, continue");
                Response = GenerateErrorMessage(Content);
           }
            return Response;
        }
    }
    
    private static String DEFAULT_USER_PASSWORD = "cs3326";
    
    static class adduserCallback implements ServerFramework.ServerCallback {

        @Override
        public Message CallbackProc(Socket Client, ArrayList<Object> Content) {
            System.out.println("Received an adduser message");

            Message Response = new Message(GS_SUCCESS);

            UserToken Token = (UserToken) Content.get(GS_ADDUSER_USER_TOKEN);
            String UserName = (String) Content.get(GS_ADDUSER_USER_NAME);
            User UserInfo = Account.getUser(Token.getSubject());

            // Permission: admin
            if ((UserInfo != null)
                    && (UserInfo.getGroups().contains(GS_ADMIN_GROUP))
                    && (Account.addUser(UserName, DEFAULT_USER_PASSWORD))) {
                
                 //Checks the legitimacy of the token
                if (authToken((UserTokenImp)Token)){
                    //  Create Message
                    Response.addObject((UserTokenImp) Token);
                    Response.addObject((String) UserName);
                }
                else{
                    //Return error message
                    System.out.println("Failed to authenticate token, continue");
                    Response = GenerateErrorMessage(Content);
                }
                
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

            //Checks the legitimacy of the token
            if (authToken((UserTokenImp)Token)){
               
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
                // Get new UserInfo with updated ownership
                UserInfo = Account.getUser(Token.getSubject());
                // Get new token with updated ownership
                Token = new UserTokenImp(GS_IDENTITY, UserInfo);
                // Send back signed token so future actions on this group will be authorized
                UserTokenImp signedToken = (UserTokenImp)getSignedToken((UserTokenImp)Token);
                System.out.println("GS addgroupCallback is sending token to client with contents: ");
                System.out.println(signedToken.getContents());
                Response.addObject((UserToken) signedToken); 
                Response.addObject((String) GroupName);
            }
            else{
                //Return error message
                System.out.println("Failed to authenticate token, continue");
                Response = GenerateErrorMessage(Content);
            }
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

            //Checks the legitimacy of the token
            if (authToken((UserTokenImp)Token)){
                // Permission: owner
                if ((UserInfo != null)
                    && (UserInfo.getOwnerships().contains(GroupName))
                    && (Option == GS_MGNT_OPTION_ADD ? Account.addGroup(UserName, GroupName) : Account.removeGroup(UserName, GroupName))) {

                    //  Create Message
                    // If managing user added/removed themselves, get their new token
                    if (UserName.equals(Token.getSubject())) {
                        Token = new UserTokenImp(GS_IDENTITY, Account.getUser(UserName));
                    }

                    // Send back new signed token so future actions are properly authorized
                    Response.addObject((UserToken)getSignedToken((UserTokenImp)Token)); 
                    Response.addObject((String) GroupName);
                } 
                else {
                //  Return error message
                System.out.println("Failed to manage group member, continue");
                Response = GenerateErrorMessage(Content);
                }
            }
            else{
                //Return error message
                System.out.println("Failed to authenticate token, continue");
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
                 //Checks the legitimacy of the token
                if (authToken((UserTokenImp)Token)){
                    
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
                }
                else{
                    //  Return error message
                    System.out.println("Failed to authenticate, continue");
                    Response = GenerateErrorMessage(Content);
                }
                
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
    public static String GS_KEYS = System.getProperty("user.dir") + File.separator + "GroupServer" + File.separator + "GSKeyList.bin";
    public static String GS_IDENTITY = "test_server";
    public static String GS_ADMIN_GROUP = "admin";
    public static UserList Account;
    public static KeyPair KEY = null;

    GroupServer2(int Port) {
        GS_PORT = Port;
    }

    public static void run() {
        // Create instances
        System.out.println("Initalize group server");
        ServerFramework Server = new ServerFramework(GS_PORT);
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

        
        //**************************************************************************************
        //**************************************************************************************
        //GSKey List
        ObjectInputStream userStream;
        ObjectInputStream groupStream;
        final int RSAKEYSIZE = 2048;
        
        try {
            FileInputStream fis = new FileInputStream(GS_KEYS);
            userStream = new ObjectInputStream(fis);
            KEY = (KeyPair)userStream.readObject();
            userStream.close();
            fis.close();
            System.out.println("Loaded keys.");
        }
        catch (FileNotFoundException e) {
            System.out.println("GSKeyList File Does Not Exist. Creating GSKeyList...");
            // create the keys
            try {
                    KeyPairGenerator keyGenRSA = KeyPairGenerator.getInstance("RSA", "BC");
                    SecureRandom keyGenRandom = new SecureRandom();
                    byte bytes[] = new byte[20];
                    keyGenRandom.nextBytes(bytes);
                    keyGenRSA.initialize(RSAKEYSIZE, keyGenRandom);
                    KEY = keyGenRSA.generateKeyPair();
                    System.out.println("Created keys.");
            }
            catch (Exception ee) {
                    System.err.println("Error generating RSA keys.");
                    ee.printStackTrace(System.err);
                    System.exit(-1);
            }
            // save the keys
            System.out.println("Saving GSKeyList...");
            ObjectOutputStream keyOut;
            try {
                    keyOut = new ObjectOutputStream(new FileOutputStream(GS_KEYS));
                    keyOut.writeObject(KEY);
                    keyOut.close();
            }
            catch(Exception ee) {
                    System.err.println("Error writing to GSKeyList.");
                    ee.printStackTrace(System.err);
                    System.exit(-1);
            }
        }
        catch (IOException e) {
                System.out.println("Error reading from GSKeyList file");
                System.exit(-1);
        }
        catch (ClassNotFoundException e) {
                System.out.println("Error reading from GSKeyList file");
                System.exit(-1);
        }
        //*******************************************************************************
        //*******************************************************************************
        
        
        // Start listener
        System.out.println("Start the listener");
        Server.run();
    }
    
    static UserToken getSignedToken(UserTokenImp token) {
        try {
            System.out.println("Signing the token, contents are: ");
            System.out.println(token.getContents());
            // Create the token's signature
            Signature tokenSign = Signature.getInstance("SHA1WithRSA", "BC");
            tokenSign.initSign(KEY.getPrivate());
            tokenSign.update(token.getContents().getBytes());
            token.setSignature(tokenSign.sign());
            
            //System.out.println("Token Signed");
            
            return token;
        }
        catch (Exception e) {
            System.err.println("Signing Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return null;
        }
    }
    
    static boolean authToken(UserTokenImp aToken) {
        try {
            // Signature verification
            Signature signed = Signature.getInstance("SHA1WithRSA", "BC");
            signed.initVerify(KEY.getPublic());
            signed.update(aToken.getContents().getBytes());
            
            
            if (signed.verify(aToken.getSignature())) {
                // RSA Signature verified
                return true;
            }
            else {
                // RSA Signature bad
                return false;
            }
        }
        catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
        }
        return false;
    }
    
    
}
