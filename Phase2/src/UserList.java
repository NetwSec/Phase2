
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * A user info database with auto save function
 *
 * UserList is a simple hashtable to store username as key and group info as value
 * 
 * The group info is stored in User class, which is supposed to be used by UserList only
 * However since getUser method can provide checkUser and getUserGroups at same time,
 * which is a common scenario in permission checking, User class has been made public
 * 
 * Yet it's only a local copy, thus you should use UserList to make changes to user account
 */
public class UserList implements java.io.Serializable {

    private static final long SERIAL_VERSION_UID = 1L;
    private Hashtable<String, User> list;
    private String LocalStorage;

    UserList(String File) {
        LocalStorage = File;
    }

    // Save list to LocalStorage
    public boolean Save() {
        ObjectOutputStream outStream;
        try {
            outStream = new ObjectOutputStream(new FileOutputStream(LocalStorage));
            outStream.writeObject(list);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    // Load list from LocalStorage
    // If the file doesn't exist, create it with an admin account
    public boolean Load(String DefaultAdmin) {
        try {
            // Try reading the file containing the user list
            FileInputStream fis = new FileInputStream(LocalStorage);
            ObjectInputStream userStream = new ObjectInputStream(fis);
            list = (Hashtable<String, User>) userStream.readObject();
        } catch (FileNotFoundException e) {
            // No file available
            System.out.println("UserList file does not exist. A default user will be created.");
            System.out.println("User name: " + DefaultAdmin);
            System.out.println("Password: " + DefaultAdmin);
            System.out.println("Group name: " + DefaultAdmin);
            
            //Create new user list
            list = new Hashtable<String, User>();
            // Add current user to user list (username and password both admin)
            addUser(DefaultAdmin, DefaultAdmin);
            // Add current user to Admin group
            addGroup(DefaultAdmin, DefaultAdmin);
            // Give ownership of Admin to current user
            addOwnerships(DefaultAdmin, DefaultAdmin);
        } catch (IOException | ClassNotFoundException e) {
            // Other error
            return false;
        }
        return true;
    }

    public synchronized boolean addUser(String username, String password) {
        if (checkUser(username)) {
            return false;
        }

        User newUser = new User(username, password);
        list.put(username, newUser);
        Save();
        return true;
    }
    
    public synchronized boolean changePassword(String username, String oldPassword, String newPassword)
    {
        // Compare old to stored; if equal, set new
        if(comparePasswords(oldPassword, list.get(username)))
        {
            list.get(username).setPassword(newPassword);
            Save();
            return true;
        }
        else
            return false;
    }
    
    static byte[] getHash(String input)
    {
        byte[] toHash = input.getBytes();
        // TODO: other algorithm?
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA1", "BC");
            digest.update(toHash);
            return digest.digest();
        }
        catch(Exception e) { e.printStackTrace(System.err); return null; }
    }
    static boolean comparePasswords(String password, User UserInfo)
    {
            return Arrays.equals(getHash(password), UserInfo.getPasswordHash());
    }

    public synchronized boolean deleteUser(String username) {
        User result = list.remove(username);
        Save();
        return (result != null);
    }

    public synchronized boolean checkUser(String username) {
        return list.containsKey(username);
    }

    public synchronized User getUser(String username) {
        return list.get(username);
    }

    public synchronized Enumeration<String> getUsernames() {
        return list.keys();
    }

    public synchronized ArrayList<String> getUserGroups(String username) {
        return list.get(username).getGroups();
    }

    public synchronized ArrayList<String> getUserOwnerships(String username) {
        return list.get(username).getOwnerships();
    }

    public synchronized boolean addGroup(String user, String groupname) {
        if(list.get(user)==null)
            return false;
        
        boolean result = list.get(user).addGroup(groupname);
        Save();
        return result;
    }

    public synchronized boolean removeGroup(String user, String groupname) {
        if(list.get(user)==null)
            return false;
        
        boolean result = list.get(user).removeGroup(groupname);
        Save();
        return result;
    }

    public synchronized boolean addOwnerships(String user, String groupname) {
        if(list.get(user)==null)
            return false;
        
        boolean result = list.get(user).addOwnership(groupname);
        Save();
        return result;
    }

    public synchronized boolean removeOwnerships(String user, String groupname) {
       if(list.get(user)==null)
            return false;
        
        boolean result = list.get(user).removeOwnership(groupname);
        Save();
        return result;
    }

}

// Used internally in UserList to store user information
class User implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private byte[] passHash;
    private byte[] signature;
    private ArrayList<String> groups;
    private ArrayList<String> ownerships;

    public User(String username, String password) {
        name = username;
        passHash = getHash(password);
        groups = new ArrayList<String>();
        ownerships = new ArrayList<String>();
        signature = makeSignature();
    }

    public String getName() {
        return name;
    }
    
    public void setPassword(String password)
    {
        passHash = getHash(password);
    }
    public byte[] getPasswordHash()
    {
        return passHash;
    }
    
    public void setSignature(byte[] signature)
    {
        this.signature = signature;
    }

    public byte[] getSignature()
    {
        return signature;
    }
    
    public ArrayList<String> getGroups() {
        return groups;
    }

    public ArrayList<String> getOwnerships() {
        return ownerships;
    }

    public boolean addGroup(String group) {
        if (groups.contains(group)) {
            return false;
        }
        groups.add(group);
        // Update the signature
        signature = makeSignature();
        return true;
    }

    // If you are not in the group, you lose your ownership
    public boolean removeGroup(String group) {
        if (!groups.isEmpty()) {
            if (groups.contains(group)) {
                groups.remove(group.indexOf(group));
                return true;
            }
        }
        this.removeOwnership(group);
        return false;
    }

    // If you own a group, you are a member of it
    public boolean addOwnership(String group) {
        if (ownerships.contains(group)) {
            return false;
        }
        ownerships.add(group);
        this.addGroup(group);
        return true;
    }

    public boolean removeOwnership(String group) {
        if (!ownerships.isEmpty()) {
            if (ownerships.contains(group)) {
                ownerships.remove(ownerships.indexOf(group));
                return true;
            }
        }
        return false;
    }
    
     static byte[] getHash(String input)
    {
        byte[] toHash = input.getBytes();
        // TODO: other algorithm?
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA1", "BC");
            digest.update(toHash);
            return digest.digest();
        }
        catch(Exception e) { e.printStackTrace(System.err); return null; }
    }
     
    private String getContents()
    {
        StringBuilder contents = new StringBuilder(GroupServer2.GS_IDENTITY);
        contents.append(name);
        for (int i = 0; i < groups.size(); i++) {
                contents.append(groups.get(i));
        }
        return contents.toString();
    }
     private byte[] makeSignature()
     {
         try {
            Signature sig = Signature.getInstance("SHA1WithRSA", "BC");
            sig.initSign(GroupServer2.KEY.getPrivate());
            sig.update(this.getContents().getBytes());
//             System.out.println("Signed on contents: ");
//             System.out.println(this.getContents());
//             System.out.println("Bytes: " + this.getContents().getBytes());
            return sig.sign();
        }
        catch (Exception e) {
            System.err.println("Signing Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return null;
        }
     }
}
