import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * File client
 * 
 * @author saber
 */
public class FileClient
{
    static String FS_ADDRESS = "localhost";
    static int FS_PORT = 8766;
    
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
        Socket Server;
        ObjectInputStream Input;
        ObjectOutputStream Output;
        try {
            System.out.println("Connect to " + FS_ADDRESS + ":" + FS_PORT);
            Server = new Socket(FS_ADDRESS,FS_PORT);
            System.out.println("Connected");
            
            System.out.println("Create Output stream");
            Output = new ObjectOutputStream(Server.getOutputStream());
            System.out.println("Create Input stream");
            Input = new ObjectInputStream(Server.getInputStream());
        } catch (IOException ex) {
            System.out.println("Connection failed");
            return;
        }
        
        //  Get UserToken
        System.out.println("Get UserToken");
        UserToken Token = new UserTokenImp("localhost", "admin", null);
        
        //  Create an upload message
        System.out.println("Create an upload message");
        Message Upload = new Message("upload");
        Upload.addObject((UserToken) Token);
        Upload.addObject((String) "");
        Upload.addObject((String) "test.txt");
        String Content = "a test file";
        Upload.addObject((byte[]) Content.getBytes());
        
        //  Send message
        System.out.println("Send message");
        try {
            Output.writeObject(Upload);
            Output.flush();
        } catch (IOException ex) {
            System.out.println("Cannot send upload message");
        }
        
        //  Receive response
        System.out.println("Receive response");
        try {
            Message Response = (Message) Input.readObject();
            System.out.println("Upload response: " + Response.getMessage());
        } catch (IOException | ClassNotFoundException ex) {
            System.out.println("Cannot receive upload response");
        }
        
        //  Create an download message
        System.out.println("Create an download message");
        Message Download = new Message("download");
        Download.addObject((UserToken) Token);
        Download.addObject((String) "");
        Download.addObject((String) "test.txt");
        
        //  Send message
        System.out.println("Send message");
        try {
            Output.writeObject(Download);
            Output.flush();
        } catch (IOException ex) {
            System.out.println("Cannot send download message");
        }
        
        //  Receive response
        System.out.println("Receive response");
        try {
            Message Response = (Message) Input.readObject();
            System.out.println("Download response: " + Response.getMessage());
        } catch (IOException | ClassNotFoundException ex) {
            System.out.println("Cannot receive download response");
        }
        
        //  Disconnect
        System.out.println("Disconnect");
        try {
            Input.close();
            Output.close();
            Server.close();
        } catch (IOException ex) {
        }
    }
    
}
