import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * A general message driven server class
 * 
 * ServerFramework class provide the basic functionality of a message
 * driver server. Developer should create an instance of this class, create
 * instances of ServerCallback for each message, RegisterMessage(), and run().
 * 
 * 
 * @author Yuntian Zhang
 */
public class ServerFramework implements Runnable
{
    // Since in Java we don't have function pointer, using interface instead
    public abstract interface ServerCallback
    {
        public Message CallbackProc(Socket Client, ArrayList<Object> Content);
    }
    
    // Using hashtable to hold callbacks
    private Hashtable<String,ServerCallback> MessageDispatcher;
    int Port;
    
    public ServerFramework(int ServerPort)
    {
        MessageDispatcher = new Hashtable<String, ServerCallback>();
        Port = ServerPort;
    }
    
    //  Add callback into hashtable
    public boolean RegisterMessage(String Msg,ServerCallback Callback)
    {
        if (MessageDispatcher.containsKey(Msg))
        {
            return false;
        }
        else
        {
            MessageDispatcher.put(Msg, Callback);
            return true;
        }
    }
    
    // Listening thread main proc
    @Override
    public void run()
    {
        ServerSocket Listen;
        try
        {
            Listen = new ServerSocket(this.Port);
        }
        catch (Exception e)
        {
            System.out.println("Failed to create the listen thread, halt");
            System.out.println("Reason: " + e.toString());
            return;
        }
        
        // Main loop, create socket for each income connection
        while (true)
        {
            try
            {
                ServerDispatcher Dispatcher = new ServerDispatcher(Listen.accept(), this);
                new Thread(Dispatcher).start();
            }
            catch (Exception e)
            {
                System.out.println("Failed to create a service thread, continue");
            }
        }
    }
    
    // Service thread framework
    private class ServerDispatcher implements Runnable
    {
        Socket Client;
        ServerFramework Server;
        ObjectInputStream Input;
        ObjectOutputStream Output;
        
        // Do not catch exception and keep running
        // Instead, let main thread knows we failed
        ServerDispatcher(Socket Connection, ServerFramework Base) throws Exception
        {
            Client = Connection;
            Server = Base;
            Output = new ObjectOutputStream(Client.getOutputStream());
            Input = new ObjectInputStream(Client.getInputStream());
        }
        
        // The service thread main proc
        @Override
        public void run()
        {
            while(true)
            {
                try
                {
                    // Process the Message
                    Message Request = (Message)Input.readObject();
                    if (Request == null)
                    {
                        System.out.println("Invalid message, close connection");
                        throw new UnsupportedOperationException("Invalid message.");
                    }
                    
                    String Command = Request.getMessage();
                    ArrayList<Object> Content = Request.getObjCont();
                    
                    // Check permission
                    UserToken Token = (UserToken) Content.get(0);
                    String ClientName = Token.getSubject();
                    String RequestedGroup = (String) Content.get(1);
                    List<String> AvailableGroups = Token.getGroups();
                    
                      for (int x = 0; x < AvailableGroups.size();x++){
                        System.out.println(AvailableGroups.get(x));
                        }
            
                    System.out.println(AvailableGroups.contains("test1"));
                    
                    if (!Command.equals("login") && ((AvailableGroups == null) || (!AvailableGroups.contains(RequestedGroup))))
                    {
                        System.out.println(ClientName + " is requesting [" + Command + "] in group " + RequestedGroup + ", denied");
                        Message Response = new Message("error");
                        Response.addObject((UserToken) Token);
                        Response.addObject((String) RequestedGroup);
                        Response.addObject(new Exception("Access denied"));
                        Output.writeObject(Response);
                        continue;
                    }
                    
                    // Check callback
                    ServerCallback Callback = Server.MessageDispatcher.get(Command);
                    // Ignore unknown message
                    if (Callback == null)
                    {
                        System.out.println("Unknown message [" + Command + "], continue");
                        Message Response = new Message("error");
                        Response.addObject((UserToken) Token);
                        Response.addObject((String) RequestedGroup);
                        Response.addObject(new Exception("Unknown message"));
                        Output.writeObject(Response);
                        continue;
                    }
                    
                    // Invoke callback
                    Message Response = Callback.CallbackProc(Client, Content);
                    
                    // Send response
                    Output.writeObject(Response);
                }
                catch (IOException | ClassNotFoundException e)
                {
                    try {
                        // Connection ended, clean up resource
                        System.out.println("Client terminated the connection, continue");
                        Input.close();
                        Output.close();
                        Client.close();
                        return;
                    } catch (IOException ex) {
                        // If we cannot free resources then just let them leak
                        // Better keep server alive
                        System.out.println("Unable to free all the resource used in last connection, continue");
                        return;
                    }
                }
            }
        }
    }
}