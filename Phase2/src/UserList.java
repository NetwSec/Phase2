    
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * 
 */

public class UserList implements java.io.Serializable {
    
    private static final long SERIAL_VERSION_UID = 1L;
    private Hashtable<String,User> list;
    private String LocalStorage;
    
    UserList(String File)
    {
        LocalStorage = File;
        Load();
    }
    
    public boolean Save()
    {
        ObjectOutputStream outStream;
        try
        {
                outStream = new ObjectOutputStream(new FileOutputStream(LocalStorage));
                outStream.writeObject(list);
        }
        catch(Exception ex)
        {
                System.err.println("Error: " + ex.getMessage());
                ex.printStackTrace(System.err);
                return false;
        }
        return true;
    }
    
    public boolean Load()
    {
        try
        {
            // Try reading the file containing the user list
            FileInputStream fis = new FileInputStream(LocalStorage);
            ObjectInputStream userStream = new ObjectInputStream(fis);
            list = (Hashtable<String,User>)userStream.readObject();
        }
        catch(FileNotFoundException e)
        {
            // No file available
            System.out.println("UserList file does not exist. A default user will be created.");
            System.out.println("User name: admin");
            System.out.println("Group name: admin");

            //Create new user list
            list = new Hashtable<String,User>();
            // Add current user to user list
            addUser("admin");
            // Add current user to Admin group
            addGroup("admin", "admin");
            // Give ownership of Admin to current user
            addOwnerships("admin", "admin");
        }
        catch(IOException | ClassNotFoundException e)
        {
            // Other error
            System.out.println("Error reading from UserList file");
            return false;
        }
        // Success, tell user to run client
        System.out.println("User list retrieved. Please run the Client.");
        return true;
    }
    
    public synchronized void addUser(String username){
        
        User newUser = new User();
        list.put(username, newUser);
        Save();
    }
    
    public synchronized void deleteUser(String username){
        
        list.remove(username);
        Save();
    }
    
    public synchronized boolean checkUser(String username){
        
        if(list.containsKey(username))
            return true;
        else  
            return false;
    }
    
    public synchronized Enumeration<String> getUsernames(){
        
        return list.keys();
    }
    
    public synchronized ArrayList<String> getUserGroups(String username){
        
        return list.get(username).getGroups();
    }
    
    public synchronized ArrayList<String> getUserOwnerships(String username){
        
        return list.get(username).getOwnership();
    }
    
    public synchronized void addGroup(String user, String groupname){
        list.get(user).addGroup(groupname);
        Save();
    }
    
    public synchronized void removeGroup(String user, String groupname){
        removeOwnerships(user, groupname);
        list.get(user).removeGroup(groupname);
        Save();
    }
    
    public synchronized void removeOwnerships(String user, String groupname){
        list.get(user).removeOwnership(groupname);
        Save();
    }
    
    public synchronized void addOwnerships(String user, String groupname){
        addGroup(user, groupname);
        list.get(user).addOwnerships(groupname);
        Save();
    }
    
}

class User implements java.io.Serializable{
    
    private static final long serialVersionUID = 1L;
    private ArrayList<String> groups;
    private ArrayList<String> ownerships;
    
    public User(){
        
        groups = new ArrayList<String>();
        ownerships = new ArrayList<String>();
    }
    
    public ArrayList<String> getGroups(){
        return groups;
    }
    
    public ArrayList<String> getOwnership(){
        return ownerships;
    }
    
    public void addGroup(String group){
        groups.add(group);
    }
    
    public void removeGroup(String group){
        if(!groups.isEmpty()){
            if(groups.contains(group)){
                groups.remove(group.indexOf(group));
            }
        }
    }
    
    public void addOwnerships(String group){
        ownerships.add(group);
    }
    
    public void removeOwnership(String group){
        if(!ownerships.isEmpty())
            if(ownerships.contains(group))
                ownerships.remove(ownerships.indexOf(group));
    }
}
