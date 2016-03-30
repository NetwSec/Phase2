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
public class GroupServer extends Server{
    
    // Port to listen to client connections
    private static final int S_PORT = 8765;
    // List of users
    public UserList userL;
    
    // Default Constructor
    public GroupServer()
    {
        super("localhost", S_PORT);
    }

    // Constructor with 2 parameters
    public GroupServer(String serverName, int serverPort) 
    {
        super(serverName, serverPort);
    }
        
    @Override
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
        Scanner console = new Scanner(System.in);
        ObjectInputStream userStream;

        //This runs a thread that saves the lists on program exit
        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(new ShutDownListener(this));

        try
        {
            // Try reading the file containing the user list
            FileInputStream fis = new FileInputStream(userFile);
            userStream = new ObjectInputStream(fis);
            userL = (UserList)userStream.readObject();
            
            // Success, tell user to run client
            System.out.println("User list retrieved. Please run the Client.");
        }
        catch(FileNotFoundException e)
        {
            // No file available
            System.out.println("UserList file does not exist. Creating UserList...");
            System.out.println("No users currently exist. Your account will be the administrator.");
            System.out.print("Enter your username: ");
            // Get the username
            String username = console.next();

            //Create new user list
            userL = new UserList();
            // Add current user to user list
            userL.addUser(username);
            // Add current user to ADMIN group
            userL.addGroup(username, "ADMIN");
            // Give ownership of ADMIN to current user
            userL.addOwnerships(username, "ADMIN");
            
            System.out.println("You have been added to the user list. Please run the Client.");
        }
        catch(IOException | ClassNotFoundException e)
        {
            // Other error
            System.out.println("Error reading from UserList file");
            System.exit(-1);
        }

        //Autosave Daemon. Saves lists every 5 minutes
        AutoSave aSave = new AutoSave(this);
        aSave.setDaemon(true);
        aSave.start();
    }
    
    //This thread saves the user list
class ShutDownListener extends Thread
{
	public GroupServer gs;
	
	public ShutDownListener (GroupServer gs) {
		this.gs = gs;
	}
	
	public void run()
	{
		System.out.println("Shutting down server");
		ObjectOutputStream outStream;
		try
		{
			outStream = new ObjectOutputStream(new FileOutputStream("UserList.bin"));
			outStream.writeObject(gs.userL);
		}
		catch(Exception e)
		{
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace(System.err);
		}
	}
}

class AutoSave extends Thread
{
	public GroupServer gs;
	
	public AutoSave (GroupServer gs) {
		this.gs = gs;
	}
	
	public void run()
	{
		do
		{
			try
			{
				Thread.sleep(300000); //Save group and user lists every 5 minutes
				System.out.println("Autosaved group and user lists.");
				ObjectOutputStream outStream;
				try
				{
					outStream = new ObjectOutputStream(new FileOutputStream("UserList.bin"));
					outStream.writeObject(gs.userL);
				}
				catch(Exception e)
				{
					System.err.println("Error: " + e.getMessage());
					e.printStackTrace(System.err);
				}
			}
			catch(Exception e)
			{
				System.out.println("Autosave Interrupted");
			}
		}while(true);
	}
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