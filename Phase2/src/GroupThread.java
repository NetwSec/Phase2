/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

/**
 *
 */
public class GroupThread extends Thread {
    
    private final Socket socket; //The socket where the communication will be held
    private final GroupServer name;    //Groupserver Object 
    
    public GroupThread(Socket socket, GroupServer name){
        this.socket = socket;
        this.name = name;
    }
    
    @Override
    public void run(){
        try{
            // Print incoming message
            System.out.println("** New connection from " + socket.getInetAddress() + ":" + socket.getPort() + " **");

            // set up I/O streams with the client
            final ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            final ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());


            // Loop to read messages 
            Message sent = null;    //Message sent
            Message response = null;    //Message received
            boolean loop = true;    //Used for the disconnect
            

            do{
                //read and print message
                sent = (Message)input.readObject();
                System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "] " + sent.getMessage());
                
                //Get Token
                if (sent.getMessage().equals("getToken")){
                    
                }
                
                //Create User
                else if(sent.getMessage().equals("createUser")){
                    
                }
                
                //Create Group
                else if (sent.getMessage().equals("createGroup")){
                    
                }
                
                //Add user to the group
                else if (sent.getMessage().equals("addUserToGroup")){
                    // If group = /ADMIN, set admin in user token to true
                }
                
                //Delete a user from the group
                else if (sent.getMessage().equals("deleteUserFromGroup")){
                    // If group = /ADMIN, set admin in user token to false
                }
                
                //List Members of the Group
                else if (sent.getMessage().equals("listMembers")){
                    
                }
                
                //Disconnect
                else if (sent.getMessage().equals("disconnect")){
                    // Close and cleanup
                    System.out.println("** Closing connection with " + socket.getInetAddress() + ":" + socket.getPort() + " **");
                    loop = false; 
                    socket.close();
                }
                
            }while(loop);
        }
        catch(Exception e){
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
        }   
    }
    
    private boolean createToken(){
        
        return false;
    }
    
    private boolean createUser(){
    
        return false;
    }
    
    private boolean createGroup(){
        return false;
    }
    
    private boolean addUserToGroup(){
        return false;
    }
    
    private boolean deleteUserFromGroup(){
        return false;
    }
    
    private List<String> listMembers(){
        
        return null;
    }
    
    
}
