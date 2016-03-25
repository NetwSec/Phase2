/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * 
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
    
    public String getServerName(){
        return serverName;
    }
    
    public int getServerPort(){
        return serverPort;
    }
    
}
