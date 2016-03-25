
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

public class UserList extends User implements java.io.Serializable {
    
    private static final long SERIAL_VERSION_UID = 1L;
    private Hashtable<String,User> list = new Hashtable<String, User>();
    
}
