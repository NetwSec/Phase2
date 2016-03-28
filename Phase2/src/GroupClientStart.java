

import java.util.List;
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
        GroupClient gc = new GroupClient();
        Scanner console = new Scanner(System.in); // Scanner object for input

        String inputString;
        int menuChoice;
        boolean exitKey = false;
        boolean hasToken = false;
        String userName = new String();
        UserToken userToken = null;
        
        while (!exitKey)
        {
            System.out.print("Enter 1 to login,\nenter 2 to exit...\n> ");
            inputString = console.nextLine();

            try
            {
                menuChoice = Integer.parseInt(inputString);
            }
            catch(Exception e)
            {
                menuChoice = -1;
            }

            if (menuChoice == 1)
            {
                System.out.print("Enter your username to login...\n> ");
                userName = console.nextLine();

                // connect to group server and get token
                // may want to prompt user here for server name, port?
                gc.connect("localhost", 8765);
                if (gc.isConnect()) // check that server is running
                {
                    userToken = gc.getToken(userName);
                    if (userToken == null) // no login for that name
                    {
                        System.out.println("Username not recognized. Contact Admin.");
                        gc.disconnect();
                    }
                    else // has a valid token, can disconnect from gc
                    {
                        hasToken = true;
                        gc.disconnect();
                    }
                }
                else
                {
                    System.out.println("Error - Group Server not running. Contact Admin.");
                }
            }
            else if (menuChoice == 2)
            {
                System.out.println("Exiting...");
                exitKey = true;
            }
            else
            {
                System.out.println("Unknown command. Please try again.");
            }
        }
        
        while (hasToken)
        {
            System.out.print("Main menu:\n" +
                             "enter 1 to connect to the Group Server,\n" +
                             "enter 2 to logout...\n" +
                             userName + "> ");
            inputString = console.nextLine();

            try
            {
                menuChoice = Integer.parseInt(inputString);
            }
            catch(Exception e)
            {
                menuChoice = -1;
            }
            
           
            switch (menuChoice)
            {
                case 1:
                    System.out.println("Connecting to Group Server...");
                    // may want to prompt user here for server name, port
                    hasToken = false;

                    String uName = userToken.getSubject();
                    String aUserName, aGroupName;
                    String iString;
                    int clientChoice;
                    boolean eKey = false;
                    List<String> aList;
                    final int MAXUSERLENGTH = 32;
                    final int MAXGROUPLENGTH = 32;

                    gc.connect("localhost", 8765);

                    while (!eKey)
                    {
                        System.out.print("Enter 1 to create a user,\n" +
                                         "enter 2 to delete a user from a group,\n" +
                                         "enter 3 to create a group,\n" +
                                         "enter 4 to list the members of a group,\n" +
                                         "enter 5 to add a user to a group,\n" +
                                         "enter 0 to disconnect from Group Server:\n" +
                                         userName + "> ");
                        
                        inputString = console.nextLine();

                        try
                        {
                            menuChoice = Integer.parseInt(inputString);
                        }
                        catch(Exception e)
                        {
                            menuChoice = -1;
                        }
                        
                        
                        switch (menuChoice)
                        {
                            //Create a user
                            case 1:
                                if (userToken.getGroups().contains("ADMIN"))
                                {
                                    aUserName = getNonEmptyString("Enter the username to be added: ", MAXUSERLENGTH);
                                    if (gc.createUser(aUserName, userToken))
                                    {
                                        System.out.println("Added " + aUserName + " to the User List.");
                                    }
                                    else
                                    {
                                        System.out.println("Error adding user - name already exists.");
                                    }
                                }
                                else
                                {
                                        System.out.println("Forbidden operation. You must be an ADMIN to create a user.");
                                }
                                break;

                            //delete user from group
                            case 2:
                                if (userToken.getGroups().contains("ADMIN"))
                                {
                                    aUserName = getNonEmptyString("Enter the username to be deleted: ", MAXUSERLENGTH);
                                    aGroupName = getNonEmptyString("Enter the group to be deleted: ", MAXUSERLENGTH);

                                    if (gc.deleterUserFromGroup(aUserName, aGroupName ,userToken))
                                    {
                                        System.out.println("Deleted " + aUserName + " from the User List.");
                                    }
                                    else
                                    {
                                        System.out.println("Error deleting user - unknown username.");
                                    }
                                }
                                else
                                {
                                    System.out.println("Forbidden operation. You must be an ADMIN to delete a user.");
                                }
                                break;

                            //Create a group
                            case 3:
                                aGroupName = getNonEmptyString("Enter the group name to be created: ", MAXGROUPLENGTH);
                                if (gc.createGroup(aGroupName, userToken))
                                {
                                    System.out.println("Added the group " + aGroupName + " to your Group List.");
                                }
                                else
                                {
                                    System.out.println("Error creating group - group name already exists.");
                                }
                                break;

                            //List member of the group
                            case 4:
                                aGroupName = getNonEmptyString("Enter the group name: ", MAXGROUPLENGTH);
                                aList = gc.listMembers(aGroupName, userToken);
                                if (aList != null)
                                {
                                    for (String s: aList)
                                    {
                                            System.out.println(s);
                                    }
                                }
                                else
                                {
                                    System.out.println("Error. You are not a member of group " +
                                                                           aGroupName + ".");
                                }
                                break;
                            
                            //Add user to group
                            case 5:
                                aUserName = getNonEmptyString("Enter the username: ", MAXUSERLENGTH);
                                aGroupName = getNonEmptyString("Enter the group name: ", MAXGROUPLENGTH);
                                if (gc.addUserToGroup(aUserName, aGroupName, userToken))
                                {
                                    System.out.println("Added " + aUserName + " to group " + aGroupName + ".");
                                }
                                else
                                {
                                    System.out.println("Error adding user to group - check username and group name.");
                                }
                                break;

                            //Disconnect
                            case 0:
                                System.out.println("Disconnecting from Group Server...");
                                gc.disconnect();
                                eKey = true;
                                break;
                            default:
                                System.out.println("Unknown command. Please try again.");
                                break;
                        }
                    }
                    
                    //Logout
                    case 2:
                        gc.disconnect();
                        hasToken = false;
                        
		}
            }
        }
    
    public static String getNonEmptyString(String prompt, int maxLength)
	{
            String str = "";
            Scanner scan = new Scanner(System.in);

            System.out.print(prompt);        

            while (str.length() == 0)
            {
                str = scan.nextLine();

                if (str.length() == 0)
                {
                    System.out.print(prompt);
                }
                else if (str.length() > maxLength)
                {
                    System.out.println("Maximum length allowed is " + maxLength + " characters. Please re-enter.");
                    System.out.print(prompt);
                    str = "";
                }
            }

            return str;
	}
    }
    
    
    

