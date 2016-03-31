import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * A general message driven server class
 * @author Yuntian Zhang
 */
public class ServerFramework implements Runnable
{
    public abstract interface ServerCallback
    {
        public Message CallbackProc(Socket Client, ArrayList<Object> Content);
    }
    
    private Hashtable<String,ServerCallback> MessageDispatcher;
    short Port;
    
    public ServerFramework(short ServerPort)
    {
        MessageDispatcher = new Hashtable<String, ServerCallback>();
        Port = ServerPort;
    }
    
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
    
    // Listening thread
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
            // Cannot start Listen thread
            System.out.println("Failed to create the listen thread, halt");
            return;
        }
        while (true)
        {
            try
            {
                ServerDispatcher Dispatcher = new ServerDispatcher(Listen.accept(), this);
                new Thread(Dispatcher).start();
            }
            catch (Exception e)
            {
                // Failed to establish connection, ignore it
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
        ServerDispatcher(Socket Connection, ServerFramework Base) throws Exception
        {
            Client = Connection;
            Server = Base;
            Output = new ObjectOutputStream(Client.getOutputStream());
            Input = new ObjectInputStream(Client.getInputStream());
        }
        
        // The service thread
        @Override
        public void run()
        {
            while(true)
            {
                try
                {
                    Message Request = (Message)Input.readObject();
                    ServerCallback Callback = Server.MessageDispatcher.get(Request.getMessage());
                    ArrayList<Object> Content = Request.getObjCont();
                    Message Response = Callback.CallbackProc(Client, Content);
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
