/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 */
public class GroupServerStart {
    
    public static void main(String args[]){
        
        //User passing server name and port
        if(args.length == 2){
            try{
            GroupServer server = new GroupServer(args[0], Integer.parseInt(args[1]));
            server.start();
            
            }
            
            catch(Exception e){
                System.err.println("Please enter <server_name>_<port_number>.\n");
                System.err.println("Usage:  java GroupClient <Server name or IP>\n");
                System.exit(-1);
            }
        }
        
        //User passing 0 arguments( By Default server: localhost, and port: 8765
        else if(args.length == 0){
            
            GroupServer server = new GroupServer();
            server.start();
        }
        
        
        
    }
    
}
