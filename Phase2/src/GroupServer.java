
import java.net.ServerSocket;
import java.net.Socket;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * 
 */
public class GroupServer extends Server{
    
    private static final int S_PORT = 8765;
    public UserList user;
    
    //Default Constructor
    public GroupServer(){
        super("localhost",S_PORT);
    }

    //Constructor with 2 parameters
    public GroupServer(String serverName, int serverPort) {
        super(serverName, serverPort);
    }

    
    //Starts the GroupServer
    @Override
    void start() {
        
        try{
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
    
    //Returns server port
    public int getPort(){
        return S_PORT;
    }
    
}
