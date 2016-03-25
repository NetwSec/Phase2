
import java.util.Scanner;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author reticent
 */
public class GroupClientStart {
    
    public static void main (String args[]){
        
        GroupClient client = new GroupClient();
        Scanner console = new Scanner(System.in); // Scanner object for input

        //Declared Variables
        String inputString;
        int menuChoice;
        boolean exitKey = false;
        boolean hasToken = false;
        String userName = new String();
        UserToken userToken = null;
        
        if (args.length == 2){
           
            client.connect(args[0], Integer.parseInt(args[1]));
            
            if (client.isConnect()){
                System.out.println("Client is connected...\n"); //not really neccessary 
                while(!exitKey){
                    run();
                }
                
                
            }
        }
        
        else if (args.length == 0){
            
             client.connect("localhost", 8765);
             
             if (client.isConnect()){
                System.out.println("Client is connected...\n");
                
                while(!exitKey){
                    run();
                }
            }
        }
        
        //Needs to be changed
        else{
            System.err.println("Not enough arguments.\n");
            System.err.println("Usage:  java GroupClient <Server name or IP>\n");
            System.exit(-1);
        }
    }
    
    //Designed as an interface where the user can navigate between client and server
    private static void run(){
        
    }
}
