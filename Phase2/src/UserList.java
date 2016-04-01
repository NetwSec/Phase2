    
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
    
    public synchronized boolean addUser(String username){
        
        if (checkUser(username)) return false;
        
        User newUser = new User(username);
        list.put(username, newUser);
        Save();
        return true;
    }
    
    public synchronized boolean deleteUser(String username){
        
        User result = list.remove(username);
        Save();
        return (result != null);
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
    
    public synchronized boolean addGroup(String user, String groupname){
        boolean result = list.get(user).addGroup(groupname);
        Save();
        return result;
    }
    
    public synchronized boolean removeGroup(String user, String groupname){
        removeOwnerships(user, groupname);
        boolean result = list.get(user).removeGroup(groupname);
        Save();
        return result;
    }
    
    public synchronized boolean removeOwnerships(String user, String groupname){
        boolean result = list.get(user).removeOwnership(groupname);
        Save();
        return result;
    }
    
    public synchronized boolean addOwnerships(String user, String groupname){
        addGroup(user, groupname);
        boolean result = list.get(user).addOwnerships(groupname);
        Save();
        return result;
    }
    
}

class User implements java.io.Serializable{
    
    private static final long serialVersionUID = 1L;
    private String name;
    private ArrayList<String> groups;
    private ArrayList<String> ownerships;
    
    public User(String username)
    {
        name = username;
        groups = new ArrayList<String>();
        ownerships = new ArrayList<String>();
    }
    
    public String getName(){
        return name;
    }
    
    public ArrayList<String> getGroups(){
        return groups;
    }
    
    public ArrayList<String> getOwnership(){
        return ownerships;
    }
    
    public boolean addGroup(String group){
        if (groups.contains(group)) return false;
        groups.add(group);
        return true;
    }
    
    public boolean removeGroup(String group){
        if(!groups.isEmpty()){
            if(groups.contains(group)){
                groups.remove(group.indexOf(group));
                return true;
            }
        }
        return false;
    }
    
    public boolean addOwnerships(String group){
        if(ownerships.contains(group)) return false;
        ownerships.add(group);
        return true;
    }
    
    public boolean removeOwnership(String group){
        if(!ownerships.isEmpty())
        {
            if(ownerships.contains(group))
            {
                ownerships.remove(ownerships.indexOf(group));
                return true;
            }
        }
        return false;
    }
}
