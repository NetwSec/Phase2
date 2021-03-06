
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;

//TESTING
/**
 * A general message driven server class
 *
 * ServerFramework class provide the basic functionality of a message driver
 * server. Developer should create an instance of this class, create instances
 * of ServerCallback for each message, RegisterMessage(), and run().
 * 
 * 
 * ServerFramework itself is only a listener thread class. It will fire a new 
 * thread to handle incoming connection. The incoming connection is handled by
 * ServerDispatcher.
 * 
 * ServerDispatcher is a working thread designed specific for Message class.
 * The communication assumes that both sides will send one Message object each
 * time and Message class only. After receiving the object, ServerDispatcher will
 * check the registered message handler in ServerFramework, and invoke the proc
 * with the content of Message.
 * 
 * Since Java doesn't support function pointer, ServerCallback is provided as a
 * workaround. By implementing a class, create an instance, and register in
 * ServerFramework, the ServerDispatcher will be able to find the correct function
 * to handle the specific Message. Since the ServerDispatcher handles the 
 * commonication between server and the client, a ServerCallback is free from
 * socket programming and can focus on the specific task. However if non-Message
 * packet or very large stream is required, ServerCallback can use the client socket
 * that is passing in the parameter to make the direct communication.
 */
public class ServerFramework implements Runnable {

    // Since in Java we don't have function pointer, using interface instead
    public abstract interface ServerCallback {

        public Message CallbackProc(SecureSocket Client, ArrayList<Object> Content);
    }

    // Using hashtable to hold callbacks
    private Hashtable<String, ServerCallback> MessageDispatcher;
    int Port;

    public ServerFramework(int ServerPort) {
        MessageDispatcher = new Hashtable<String, ServerCallback>();
        Port = ServerPort;
    }

    //  Add callback into hashtable
    public boolean RegisterMessage(String Msg, ServerCallback Callback) {
        if (MessageDispatcher.containsKey(Msg)) {
            return false;
        } else {
            MessageDispatcher.put(Msg, Callback);
            return true;
        }
    }

    // Listening thread main proc
    @Override
    public void run() {
        // Open the port
        ServerSocket Listen;
        try {
            Listen = new ServerSocket(this.Port);
        } catch (Exception e) {
            System.out.println("Failed to create the listen thread, halt");
            System.out.println("Reason: " + e.toString());
            return;
        }

        // Main loop, create socket for each income connection
        while (true) {
            try {
                ServerDispatcher Dispatcher = new ServerDispatcher(Listen.accept(), this);
                new Thread(Dispatcher).start();
            } catch (Exception e) {
                System.out.println("Failed to create a service thread, continue");
            }
        }
    }

    public Message Decode(Object o)
    {
        return (Message) o;
    }

    public Object Encode(Message o)
    {
        return (Object) o;
    }
    
    // Service thread framework
    private class ServerDispatcher implements Runnable {

        SecureSocket Client;
        ServerFramework Server;

        // Do not catch exception and keep running
        // Instead, let main thread knows we failed
        ServerDispatcher(Socket Connection, ServerFramework Base) throws Exception {
            Client = new SecureSocket(Connection);
            Client.listen();
            Server = Base;
        }
        
        // The service thread main proc
        @Override
        public void run() {
            while (true) {
                try {
                    // Process the Message
                    Message Request = Decode(Client.receive());
                    if (Request == null) {
                        System.out.println("Invalid message, close connection");
                        throw new UnsupportedOperationException("Invalid message.");
                    }
                    String Command = Request.getMessage();
                    ArrayList<Object> Content = Request.getObjCont();

                    // Check callback
                    ServerCallback Callback = Server.MessageDispatcher.get(Command);
                    // Ignore unknown message
                    if (Callback == null) {
                        System.out.println("Unknown message [" + Command + "], continue");
                        
                        // Send an error packet
                        Message Response = new Message(GroupServer2.GS_ERROR);
                        UserToken Token = (UserToken) Content.get(GroupServer2.GS_ERROR_USER_TOKEN);
                        String RequestedGroup = (String) Content.get(GroupServer2.GS_ERROR_GROUP_NAME);
                        Response.addObject((UserToken) Token);
                        Response.addObject((String) RequestedGroup);
                        Response.addObject(new Exception("Unknown message"));
                        Client.send(Response);
                        continue;
                    }

                    // Invoke callback
                    Message Response = Callback.CallbackProc(Client, Content);

                    // Send response
                    Client.send(Encode(Response));
                } catch (Exception e) {
                    // Connection ended, clean up resource
                    System.out.println("Client terminated the connection, exit thread");
                    Client.close();
                    return;
                }
            }
        }
    }
}
