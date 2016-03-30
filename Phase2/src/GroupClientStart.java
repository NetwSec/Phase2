import java.util.List;
import java.util.Scanner;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * GroupClient.java now implements this functionality.
 * @author reticent
 */
public class GroupClientStart {
        
        private static GroupClient gc;
    
    public static void main (String args[]){
        
//        gc = new GroupClient();
        Scanner console = new Scanner(System.in); // Scanner object for input

        //Declared Variables
        boolean exitKey = false;
        
        if (args.length == 2){
           
            gc.connect(args[0], Integer.parseInt(args[1]));
            
            if (gc.isConnect()){
                System.out.println("Client is connected...\n"); //not really neccessary 
                while(!exitKey){
                    run();
                }
            }
        }
        
        else if (args.length == 0){
            
             gc.connect("localhost", 8765);
             
             if (gc.isConnect()){
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
    
    //Designed as an interface where the user can navigate between gc and server
    private static void run(){
        Scanner console = new Scanner(System.in); // Scanner object for input

        String inputString;
        int menuChoice;
        boolean exitKey = false;
        boolean hasToken = false;
        String userName = new String();
        UserToken userToken = null;
        
        while (!exitKey)
        {
            System.out.print("Welcome to the Group Client! Please enter:\n" +
                    "1: Login\n" + 
                    "2: Exit\n> ");
            
            // Get the choice
            inputString = console.nextLine();

            // Parse it
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
                    System.out.print("Enter your username to login...\n> ");
                    // Get username
                    userName = console.nextLine();
                    // TODO: should already be connected to server
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
                            exitKey = true;
                            // TODO: should not 
                            gc.disconnect();
                        }
                    }
                    else
                    {
                        System.out.println("Error - Group Server not running. Contact Admin.");
                    }   break;
                case 2:
                    System.out.println("Exiting...");
                    exitKey = true;
                    break;
                default:
                    System.out.println("Unknown command. Please try again.");
                    break;
            }
        }
        
        while (hasToken)
        {
            System.out.print("Welcome to the Group Client main menu! Please enter:\n" +
                             "1: Connect to the Group Server\n" +
                             "2: Logout\n" +
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

            // MAIN MENU
            switch (menuChoice)
            {
                case 1:
                    System.out.println("Connecting to Group Server...");
                    hasToken = false;

                    String aUserName, aGroupName;
                    boolean eKey = false;
                    List<String> aList;
                    final int MAXUSERLENGTH = 32;
                    final int MAXGROUPLENGTH = 32;

                    // TODO: should not have to connect again
                    gc.connect("localhost", 8765);

                    while (!eKey)
                    {
                        System.out.print("Welcome to the Group Client! Please enter:\n" + 
                                         "1: Create a new user,\n" +
                                         "2: Delete a user from a group,\n" +
                                         "3: Create a new group,\n" +
                                         "4: List the members of a group,\n" +
                                         "5: Add a user to a group,\n" +
                                         "0: Disconnect from Group Server:\n" +
                                         userName + "> ");
                        
                        // Get the response
                        inputString = console.nextLine();

                        // Parse it
                        try
                        {
                            menuChoice = Integer.parseInt(inputString);
                        }
                        catch(Exception e)
                        {
                            menuChoice = -1;
                        }
                        
                        // METHODS MENU
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
                case 2:
                    // TODO: Log out
                    System.out.println("Logging out from Group Client.");
                    gc.disconnect();
                    break;
                default:
                    System.out.println("Unknown command. Please try again.");
                    break;
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
    
    
    

