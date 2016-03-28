    
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
    private Hashtable<String,User> list = new Hashtable<String, User>();
    
    public synchronized void addUser(String username){
        
        User newUser = new User();
        list.put(username, newUser);
    }
    
    public synchronized void deleteUser(String username){
        
        list.remove(username);
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
    }
    
    public synchronized void removeGroup(String user, String groupname){
        list.get(user).removeGroup(groupname);
    }
    
    public synchronized void removeOwnerships(String user, String groupname){
        list.get(user).removeOwnership(groupname);
    }
    
    public synchronized void addOwnerships(String user, String groupname){
        list.get(user).addOwnerships(groupname);
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
