import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 */
public class GroupServer{
    
    private String serverName;  //Stores server name
    private int serverPort; //Stores port number
    
    //return server name
    public String getServerName(){
        return serverName;
    }
    
    //return server port number
    public int getServerPort(){
        return serverPort;
    }
    
    // Port to listen to client connections
    private static final int S_PORT = 8765;
    // List of users
    public UserList userL;
    
    // Default Constructor
    public GroupServer()
    {
        this.serverName = "localhost";
        this.serverPort = S_PORT;
    }

    // Constructor with 2 parameters
    public GroupServer(String serverName, int serverPort){
        this.serverName = serverName;
        this.serverPort = serverPort;
    }
        
    // Start-up method
    void start()
    {   
        // Get user list before anything else
        this.getUserList();
        
        try
        {
            final ServerSocket serverSock = new ServerSocket(this.getServerPort());
			
            Socket socket = null;
            GroupThread thread = null;
		
            // A simple infinite loop to accept connections
            while(true)
            {
                socket = serverSock.accept();
		thread = new GroupThread(socket, this);
		thread.start();
            }
        }
        catch(Exception e){
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
	}
    }
    
    private void getUserList()
    {
        String userFile = "UserList.bin";
        userL = new UserList(userFile);
    }
    
     // Getter for server port
    public int getPort()
    {
        return S_PORT;
    } 
    
    // Setter for user list
    public void setUserList(UserList list)
    {
        userL = list;
    }  
}