/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.ArrayList;
import java.util.List;

/**
 *
 * 
 */
public class GroupClient extends Client {
    
    //Communicates back and forth with server to get ACK for token
    public UserToken getToken(String username){
        
        try {
           
             //Local Varibles
            UserToken token= null;   //Holds token object
            Message sendM = null;    //Holds message sent to server
            Message receiveM = null; //Receives the response from the server
            
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
                    
                    token = (UserToken)temp.get(0);
                    return token;
                }
                
            }
            return null;
        }
        catch (Exception e){
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return null;
        }
    }
    
    //Communicates back and forth with server to get ACK for creating a user
    public boolean createUser(String username, UserToken token){
        
        //Local Variables
        Message sendM = null;    //Holds message sent to server
        Message receiveM = null; //Receives the response from the server
        
        try {
           
            //Reads, sends, and receives the message
            sendM =  new Message("createUser");
            sendM.addObject(username); //adds username
            sendM.addObject(token);  //adds token
            output.writeObject(sendM);   //writes to the server
            
            //System.out.println("Testing #1 prior to response from server(before object read)"); //Testing Purpose
            receiveM = (Message)input.readObject();  //receives from the server
            //System.out.println("Testing #1 prior to response from server(after read)");   //Testing Purpose
            
            //Successful Response from server
            if(receiveM.getMessage().equals("OK"))
            {
                return true;
                
            }
                return false;
            
        }
        catch (Exception e){
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return false;
        }
    }
    
    //Communicates back and forth with server to get ACK to create a group
    public boolean createGroup(String groupname, UserToken token){
     
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
            
                return false;
            
        }
        catch (Exception e){
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return false;
        }
    }
    
    //Communicates back and forth with server to get ACK to add user to group
    public boolean addUserToGroup(String user, String group, UserToken token){
        
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
            
                return false;
            
        }
        catch (Exception e){
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return false;
        }
    }
    
    //Communicates back and forth with server to get ACK to delete user from group
    public boolean deleterUserFromGroup(String user, String group, UserToken token){
        
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
            
                return false;
            
        }
        catch (Exception e){
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return false;
        }
    }
    
    //Communicates back and forth with server to get ACK to receive list of members
    public List<String> listMembers(String group, UserToken token){
        
        //Local Variables
        Message sendM = null;    //Holds message sent to server
        Message receiveM = null; //Receives the response from the server
        
        try {
            //Reads, sends, and receives the message
            sendM =  new Message("listMembers");
            sendM.addObject(group);  //adds user
            sendM.addObject(token);  //adds token

            output.writeObject(sendM);  //writes to the server
            
            receiveM = (Message)input.readObject(); //receives from the server
            
            //Successful Response from server
            if(receiveM.getMessage().equals("OK"))
            {
                ArrayList<Object> temp = null;
                temp = receiveM.getObjCont();
                
                return (ArrayList<String>)temp.get(0);
            }
            
                return null;
            
        }
        catch (Exception e){
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return null;
        }
    }
}
    

