
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

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
    public UserList userL;
    
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
        
        
		String userFile = "UserList.bin";
		Scanner console = new Scanner(System.in);
		ObjectInputStream userStream;
		
		//This runs a thread that saves the lists on program exit
		Runtime runtime = Runtime.getRuntime();
		runtime.addShutdownHook(new ShutDownListener(this));
		
		//Open user file to get user list
		try
		{
			FileInputStream fis = new FileInputStream(userFile);
			userStream = new ObjectInputStream(fis);
			userL = (UserList)userStream.readObject();
		}
		catch(FileNotFoundException e)
		{
			System.out.println("UserList File Does Not Exist. Creating UserList...");
			System.out.println("No users currently exist. Your account will be the administrator.");
			System.out.print("Enter your username: ");
			String username = console.next();
			
			//Create a new list, add current user to the ADMIN group. They now own the ADMIN group.
			userL = new UserList();
			userL.addUser(username);
			userL.addGroup(username, "ADMIN");
			userL.addOwnerships(username, "ADMIN");
		}
		catch(IOException e)
		{
			System.out.println("Error reading from UserList file");
			System.exit(-1);
		}
		catch(ClassNotFoundException e)
		{
			System.out.println("Error reading from UserList file");
			System.exit(-1);
		}
		
		//Autosave Daemon. Saves lists every 5 minutes
		AutoSave aSave = new AutoSave(this);
		aSave.setDaemon(true);
		aSave.start();
        
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
				System.out.println("Autosave group and user lists...");
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
    
}
