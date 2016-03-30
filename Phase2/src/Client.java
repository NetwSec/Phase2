import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * Superclass for GroupClient and FileClient.
 * Run after running GroupServerStart and FileServerStart.
 * Sets up connections to the Group Server and File Server.
 */
public abstract class Client {
    
    Socket sock;   
    ObjectOutputStream output;
    ObjectInputStream input;
    
    public static void main(String[] args) {
        // Scanner for input
        Scanner console = new Scanner(System.in);
        
        // Output the menu
        System.out.println("Welcome! Please enter: ");
        System.out.println("g: Connect to Group Server");
        System.out.println("f: Connect to File Server");
        System.out.println("e: Exit");
        
        // Get the first character of the input
        char choice = console.nextLine().charAt(0);
        
        switch(choice)
        {
            case 'g':
//                System.out.println("got into the switch!");
                // Create a GroupServer
//                GroupServer groupServer = new GroupServer();
//                // Get it running so it listens for connections
//                groupServer.start();
                // Create a GroupClient
                GroupClient groupClient = new GroupClient();
                // Run it to interface with user
                groupClient.run();
                break;
            case 'f':
                // Run the file server/client
                break;
            case 'e':
                break;
            default:
                System.out.println("Unknown command. Please try again.");
                break;
                
        }
    }
    
    //Atempt to connect client with server
    public boolean connect(final String server, final int port) {
        System.out.println("Connecting to server...");
        
        try{
            //Connected to the specified server
            sock = new Socket(server,port);
            System.out.println("Connected to " + server +" on port " + port);
            
            // Set up I/O streams with server
            output = new ObjectOutputStream(sock.getOutputStream());
            input = new ObjectInputStream(sock.getInputStream());
            
            return true;
        }
        catch(Exception e){
            System.err.println("Error " + e.getMessage());
            e.printStackTrace(System.err);
            return false;
        }
    }
    
    //Returns true is client is connect to server, else returns false is not connected
    public boolean isConnect(){
        if (sock == null || !sock.isConnected())
            return false;
        else
            return true;
        
    }
    
    //Disconnects client from server
    public void disconnect(){
        if (isConnect()){
            try{
                // TODO: may need to change message to "disconnect"
                Message msg = new Message("disconnect");
                output.writeObject(msg);
            }
            catch(Exception e){
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace(System.err);
            }
        }
        
        
        
    }
    
}
