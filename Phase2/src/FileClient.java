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

/**
 * File client
 * 
 * @author saber
 */
public class FileClient
{
    static String FS_ADDRESS = "localhost";
    static int FS_PORT = 8766;
    
    static Socket Server;
    static ObjectInputStream Input;
    static ObjectOutputStream Output;
    
    FileClient(String server, int port) throws IOException
    {
        FS_ADDRESS = server;
        FS_PORT = port;
        connect(FS_ADDRESS,FS_PORT);
    }
    
    static boolean connect(String server, int port) throws IOException
    {
        Server = new Socket(server,port);

        Output = new ObjectOutputStream(Server.getOutputStream());
        Input = new ObjectInputStream(Server.getInputStream());
        return true;
    }
    
    static void disconnect()
    {
        try {
            Input.close();
        } catch (IOException ex) {
        }
        
        try {
            Output.close();
        } catch (IOException ex) {
        }
        
        try {
            Server.close();
        } catch (IOException ex) {
        }
    }
    
    static List<String> listFiles(UserToken token)
    {
        return null;
    }
    
    static boolean upload(UserToken token, String group, String remoteFile, String localFile)
    {
        Message Upload = new Message("upload");
        
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
        } catch (IOException ex) {
            return false;
        }
        
        //  Receive response
        Message Response;
        try {
            Response = (Message) Input.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            return false;
        }
        
        return (Response.getMessage() == "success");
    }

    static boolean download(UserToken token, String group, String remoteFile, String localFile)
    {
        Message Download = new Message("download");
        
         // Create Message header
        Download.addObject((UserToken) token);
        Download.addObject((String) group);
        Download.addObject((String) remoteFile);
        
        // Send the message
        try {            
            Output.writeObject(Download);
            Output.flush();
        } catch (IOException ex) {
            return false;
        }
        
        //  Receive response
        Message Response;
        try {
            Response = (Message) Input.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            return false;
        }
        
        // Check response
        if (Response.getMessage() != "upload")
        {
            return false;
        }
        
        // Save file
        try
        {
            ArrayList<Object> Content = Response.getObjCont();
            byte[] FileContent = (byte[]) Content.get(3);
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
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        //  Check arguments
        if (args.length == 2)
        {
            FS_ADDRESS = args[0];
            FS_PORT = Integer.parseInt(args[1]);
        }
        else if (args.length != 0)
        {
            System.out.println("Usage:");
            System.out.println("client <address> <port>");
            System.out.println("default: localhost 8766");
            return;
        }
        
        //  Connect to server
        try {
            System.out.println("Connect to " + FS_ADDRESS + ":" + FS_PORT);
            connect(FS_ADDRESS,FS_PORT);
        } catch (IOException ex) {
            System.out.println("Connection failed");
            return;
        }
        
        //  Get UserToken
        System.out.println("Get UserToken");
        UserToken Token = new UserTokenImp("localhost", "admin", null);
        
        //  Create an upload message
        System.out.println("Create an upload message");
        upload(Token,"group","test_remote.txt","test_local");
        
        //  Create an download message
        System.out.println("Create a download message");
        download(Token,"group","test_remote.txt","test_local");
        
        //  Disconnect
        System.out.println("Disconnect");
        disconnect();
    }
    
}
