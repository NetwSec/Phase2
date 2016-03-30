/**
 * Superclass for GroupServer and FileServer.
 */
public abstract class Server {
    
    private String serverName;  //Stores server name
    private int serverPort; //Stores port number
    
    //Abstract Start function
    abstract void start();
    
    public Server(String serverName, int serverPort){
        this.serverName = serverName;
        this.serverPort = serverPort;
    }
    
    //return server name
    public String getServerName(){
        return serverName;
    }
    
    //return server port number
    public int getServerPort(){
        return serverPort;
    }
    
}
