/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 *
 * 
 */
public abstract class Client {
    
    //Member Varibales
    Socket sock;   
    ObjectOutputStream output;
    ObjectInputStream input;
    
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
                Message msg = new Message("EXIT");
                output.writeObject(msg);
            }
            catch(Exception e){
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace(System.err);
            }
        }
        
        
        
    }
    
}
