/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * 
 */
public class GroupClient extends Client{
    
    //Declared Variables
    protected Socket s;
    protected ObjectOutputStream output;
    protected ObjectInputStream input;
    
    //Communicates back and forth with server to get ACK for token
    public UserTokenImp getToken(String username){
        
        //Local Varibles
        UserTokenImp token= null;   //Holds token object
        Message sendM = null;    //Holds message sent to server
        Message receiveM = null; //Receives the response from the server
        
        try {
           
            //Reads, sends, and receives the message
            sendM =  new Message("getToken");
            sendM.addObject(username);   //adds username
            output.writeObject(sendM);   //writes to the server
            receiveM = (Message)input.readObject();  //receives from the server
            
            //Successful Response from server
            if (receiveM.getMessage().equals("OK"))
            {
                //Return token if available in the message
                ArrayList<Object> temp = null;
                temp = receiveM.getObjCont();
                
                if (temp.size() ==1){
                    
                    token = (UserTokenImp)temp.get(0);
                    return token;
                }
                
            }
            
        }
        catch (Exception e){
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return null;
        }
        return null;
    }
    
    //Communicates back and forth with server to get ACK for creating a user
    public boolean createUser(String username, UserTokenImp token){
        
        //Local Variables
        Message sendM = null;    //Holds message sent to server
        Message receiveM = null; //Receives the response from the server
        
        try {
           
            //Reads, sends, and receives the message
            sendM =  new Message("createUser");
            sendM.addObject(username); //adds username
            sendM.addObject(token);  //adds token
            output.writeObject(sendM);   //writes to the server
            
            
            receiveM = (Message)input.readObject();  //receives from the server
            
            //Successful Response from server
            if(receiveM.getMessage().equals("OK"))
            {
                return true;
            }
            else
                return false;
            
        }
        catch (Exception e){
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return false;
        }
    }
    
    //Communicates back and forth with server to get ACK to create a group
    public boolean createGroup(String groupname, UserTokenImp token){
     
        //Local Variables
        Message sendM = null;    //Holds message sent to server
        Message receiveM = null; //Receives the response from the server
        
        try {
           
            //Reads, sends, and receives the message
            sendM =  new Message("createGroup");
            sendM.addObject(groupname); //adds groupname
            sendM.addObject(token);  //adds token
            output.writeObject(sendM);  //writes to the server
            
            receiveM = (Message)input.readObject(); //receives from the server
            
            //Successful Response from server
            if(receiveM.getMessage().equals("OK"))
            {
                return true;
            }
            else
                return false;
            
        }
        catch (Exception e){
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return false;
        }
    }
    
    //Communicates back and forth with server to get ACK to add user to group
    public boolean addUserToGroup(String user, String group, UserTokenImp token){
        
        //Local Variables
        Message sendM = null;    //Holds message sent to server
        Message receiveM = null; //Receives the response from the server
        
        try {
           
            //Reads, sends, and receives the message
            sendM =  new Message("addUserToGroup");
            sendM.addObject(user);  //ads user
            sendM.addObject(group); //adds groupname
            sendM.addObject(token);  //adds token

            output.writeObject(sendM);  //writes to the server
            
            receiveM = (Message)input.readObject(); //receives from the server
            
            //Successful Response from server
            if(receiveM.getMessage().equals("OK"))
            {
                return true;
            }
            else
                return false;
            
        }
        catch (Exception e){
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return false;
        }
    }
    
    //Communicates back and forth with server to get ACK to delete user from group
    public boolean deleterUserFromGroup(String user, String group, UserTokenImp token){
        
        //Local Variables
        Message sendM = null;    //Holds message sent to server
        Message receiveM = null; //Receives the response from the server
        
        try {
           
            //Reads, sends, and receives the message
            sendM =  new Message("deleteUserFromGroup");
            sendM.addObject(user);  //ads user
            sendM.addObject(group); //adds groupname
            sendM.addObject(token);  //adds token

            output.writeObject(sendM);  //writes to the server
            
            receiveM = (Message)input.readObject(); //receives from the server
            
            //Successful Response from server
            if(receiveM.getMessage().equals("OK"))
            {
                return true;
            }
            else
                return false;
            
        }
        catch (Exception e){
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return false;
        }
    }
    
    //Communicates back and forth with server to get ACK to receive list of members
    public List<String> listMembers(String user, UserTokenImp token){
        
        //Local Variables
        Message sendM = null;    //Holds message sent to server
        Message receiveM = null; //Receives the response from the server
        
        try {
            //Reads, sends, and receives the message
            sendM =  new Message("listMembers");
            sendM.addObject(user);  //ads user
            sendM.addObject(token);  //adds token

            output.writeObject(sendM);  //writes to the server
            
            receiveM = (Message)input.readObject(); //receives from the server
            
            //Successful Response from server
            if(receiveM.getMessage().equals("OK"))
            {
                List<Object> temp = null;
                temp = receiveM.getObjCont();
                
                return (List<String>)temp.get(0);
            }
            else
                return null;
            
        }
        catch (Exception e){
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return null;
        }
    }
}
    

