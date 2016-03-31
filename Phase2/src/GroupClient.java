
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 *
 */
public class GroupClient {

    static Socket sock;
    static ObjectOutputStream output;
    static ObjectInputStream input;

    public static void main(String[] args) {
        run();
    }

    //Atempt to connect client with server
    static public boolean connect(final String server, final int port) {
        System.out.println("Connecting to server...");

        try {
            //Connected to the specified server
            sock = new Socket(server, port);
            System.out.println("Connected to " + server + " on port " + port);

            // Set up I/O streams with server
            output = new ObjectOutputStream(sock.getOutputStream());
            input = new ObjectInputStream(sock.getInputStream());

            return true;
        } catch (Exception e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace(System.err);
            return false;
        }
    }

    //Returns true is client is connect to server, else returns false is not connected
    static public boolean isConnect() {
        if (sock == null || !sock.isConnected()) {
            return false;
        } else {
            return true;
        }

    }

    //Disconnects client from server
    static public void disconnect() {
        if (isConnect()) {
            try {
                // TODO: may need to change message to "disconnect"
                Message msg = new Message("disconnect");
                output.writeObject(msg);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace(System.err);
            }
        }
    }

    static public void run() {
        Scanner console = new Scanner(System.in); // Scanner object for input

        String inputString;
        int menuChoice;
        String userName = new String();
        UserToken userToken = null;

        System.out.print("Please enter your username:\n> ");
        // Get username
        userName = console.nextLine();
        // Connect to group server and get token
        connect("localhost", 8765);
        if (isConnect()) // check that server is running
        {
            userToken = getToken(userName);
            if (userToken == null) // no login for that name
            {
                System.out.println("Username not recognized. Contact Admin.");
                // Disconnect from GS
                disconnect();
            }
        } else {
            System.out.println("Error - Group Server not running. Contact Admin.");
            return;
        }

        String aUserName, aGroupName;
        boolean eKey = false;
        List<String> aList;
        final int MAXUSERLENGTH = 32;
        final int MAXGROUPLENGTH = 32;

        // TODO: should not have to connect again
//                    gc.connect("localhost", 8765);
        while (!eKey) {
            System.out.print("Please choose from the below options:\n"
                    + "1: Create a new user,\n"
                    + "2: Create a new group,\n"
                    + "3: Add a user to a group,\n"
                    + "4: Delete a user from a group,\n"
                    + "5: List the members of a group,\n"
                    + "6: Log into File Server,\n"
                    + "0: Disconnect from Group Server:\n"
                    + userName + "> ");

            // Get the response
            inputString = console.nextLine();

            // Parse it
            try {
                menuChoice = Integer.parseInt(inputString);
            } catch (Exception e) {
                menuChoice = -1;
            }

            // METHODS MENU
            switch (menuChoice) {
                //Create a user
                case 1:
                    if (userToken.getGroups().contains("ADMIN")) {
                        aUserName = getNonEmptyString("Enter the username to be added: ", MAXUSERLENGTH);
                        if (createUser(aUserName, userToken)) {
                            System.out.println("Added " + aUserName + " to the User List.");
                        } else {
                            System.out.println("Error adding user - name already exists.");
                        }
                    } else {
                        System.out.println("Forbidden operation. You must be an ADMIN to create a user.");
                    }
                    break;

                // 2: Create a group 
                case 2:
                    aGroupName = getNonEmptyString("Enter the group name to be created: ", MAXGROUPLENGTH);
                    if (createGroup(aGroupName, userToken)) {
                        System.out.println("Added the group " + aGroupName + " to your Group List.");
                    } else {
                        System.out.println("Error creating group - group name already exists.");
                    }
                    break;

                // 3: Add user to group
                case 3:
                    aUserName = getNonEmptyString("Enter the username: ", MAXUSERLENGTH);
                    aGroupName = getNonEmptyString("Enter the group name: ", MAXGROUPLENGTH);
                    if (addUserToGroup(aUserName, aGroupName, userToken)) {
                        System.out.println("Added " + aUserName + " to group " + aGroupName + ".");
                    } else {
                        System.out.println("Error adding user to group - check username and group name.");
                    }
                    break;

                // 4: Delete user from group
                case 4:
                    if (userToken.getGroups().contains("ADMIN")) {
                        aUserName = getNonEmptyString("Enter the username: ", MAXUSERLENGTH);
                        aGroupName = getNonEmptyString("Enter the group name: ", MAXUSERLENGTH);

                        if (deleterUserFromGroup(aUserName, aGroupName, userToken)) {
                            System.out.println("Deleted " + aUserName + " from the group " + aGroupName + ".");
                        } else {
                            System.out.println("Error deleting user - unknown username.");
                        }
                    } else {
                        System.out.println("Forbidden operation. You must be an ADMIN to delete a user.");
                    }
                    break;

                // 5: List members of a group
                case 5:
                    aGroupName = getNonEmptyString("Enter the group name: ", MAXGROUPLENGTH);
                    aList = listMembers(aGroupName, userToken);
                    if (aList != null) {
                        for (String s : aList) {
                            System.out.println(s);
                        }
                    } else {
                        System.out.println("Error. You are not a member of group "
                                + aGroupName + ".");
                    }
                    break;

                // 6: file server
                case 6: {
                    FileClient FC = new FileClient(userToken);
                    FC.run();
                    break;
                }
                // 0: Disconnect from server
                case 0:
                    System.out.println("Disconnecting from Group Server...");
                    disconnect();
                    eKey = true;
                    break;

                // Unknown command
                default:
                    System.out.println("Unknown command. Please try again.");
                    break;
            }
        }
    }

    public static String getNonEmptyString(String prompt, int maxLength) {
        String str = "";
        Scanner scan = new Scanner(System.in);

        System.out.print(prompt);

        while (str.length() == 0) {
            str = scan.nextLine();

            if (str.length() == 0) {
                System.out.print(prompt);
            } else if (str.length() > maxLength) {
                System.out.println("Maximum length allowed is " + maxLength + " characters. Please re-enter.");
                System.out.print(prompt);
                str = "";
            }
        }

        return str;
    }

    //Communicates back and forth with server to get ACK for token
    public static UserToken getToken(String username) {

        try {

            //Local Varibles
            UserToken token = null;   //Holds token object
            Message sendM = null;    //Holds message sent to server
            Message receiveM = null; //Receives the response from the server

            //Reads, sends, and receives the message
            sendM = new Message("getToken");
            sendM.addObject(username);   //adds username
            output.writeObject(sendM);   //writes to the server
            receiveM = (Message) input.readObject();  //receives from the server

            //Successful Response from server
            if (receiveM.getMessage().equals("OK")) {
                //Return token if available in the message
                ArrayList<Object> temp = null;
                temp = receiveM.getObjCont();

                if (temp.size() == 1) {

                    token = (UserToken) temp.get(0);
                    return token;
                }

            }
            return null;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return null;
        }
    }

    //Communicates back and forth with server to get ACK for creating a user
    public static boolean createUser(String username, UserToken token) {

        //Local Variables
        Message sendM = null;    //Holds message sent to server
        Message receiveM = null; //Receives the response from the server

        try {

            //Reads, sends, and receives the message
            sendM = new Message("createUser");
            sendM.addObject(username); //adds username
            sendM.addObject(token);  //adds token
            output.writeObject(sendM);   //writes to the server

            //System.out.println("Testing #1 prior to response from server(before object read)"); //Testing Purpose
            receiveM = (Message) input.readObject();  //receives from the server
            //System.out.println("Testing #1 prior to response from server(after read)");   //Testing Purpose

            //Successful Response from server
            if (receiveM.getMessage().equals("OK")) {
                return true;

            }
            return false;

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return false;
        }
    }

    //Communicates back and forth with server to get ACK to create a group
    public static boolean createGroup(String groupname, UserToken token) {

        //Local Variables
        Message sendM = null;    //Holds message sent to server
        Message receiveM = null; //Receives the response from the server

        try {

            //Reads, sends, and receives the message
            sendM = new Message("createGroup");
            sendM.addObject(groupname); //adds groupname
            sendM.addObject(token);  //adds token
            output.writeObject(sendM);  //writes to the server

            receiveM = (Message) input.readObject(); //receives from the server

            //Successful Response from server
            if (receiveM.getMessage().equals("OK")) {
                return true;
            }

            return false;

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return false;
        }
    }

    //Communicates back and forth with server to get ACK to add user to group
    public static boolean addUserToGroup(String user, String group, UserToken token) {

        //Local Variables
        Message sendM = null;    //Holds message sent to server
        Message receiveM = null; //Receives the response from the server

        try {

            //Reads, sends, and receives the message
            sendM = new Message("addUserToGroup");
            sendM.addObject(user);  //ads user
            sendM.addObject(group); //adds groupname
            sendM.addObject(token);  //adds token

            output.writeObject(sendM);  //writes to the server

            receiveM = (Message) input.readObject(); //receives from the server

            //Successful Response from server
            if (receiveM.getMessage().equals("OK")) {
                return true;
            }

            return false;

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return false;
        }
    }

    //Communicates back and forth with server to get ACK to delete user from group
    public static boolean deleterUserFromGroup(String user, String group, UserToken token) {

        //Local Variables
        Message sendM = null;    //Holds message sent to server
        Message receiveM = null; //Receives the response from the server

        try {

            //Reads, sends, and receives the message
            sendM = new Message("deleteUserFromGroup");
            sendM.addObject(user);  //ads user
            sendM.addObject(group); //adds groupname
            sendM.addObject(token);  //adds token

            output.writeObject(sendM);  //writes to the server

            receiveM = (Message) input.readObject(); //receives from the server

            //Successful Response from server
            if (receiveM.getMessage().equals("OK")) {
                return true;
            }

            return false;

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return false;
        }
    }

    //Communicates back and forth with server to get ACK to receive list of members
    public static List<String> listMembers(String group, UserToken token) {

        //Local Variables
        Message sendM = null;    //Holds message sent to server
        Message receiveM = null; //Receives the response from the server

        try {
            //Reads, sends, and receives the message
            sendM = new Message("listMembers");
            sendM.addObject(group);  //adds user
            sendM.addObject(token);  //adds token

            output.writeObject(sendM);  //writes to the server

            receiveM = (Message) input.readObject(); //receives from the server

            //Successful Response from server
            if (receiveM.getMessage().equals("OK")) {
                ArrayList<Object> temp = null;
                temp = receiveM.getObjCont();

                return (ArrayList<String>) temp.get(0);
            }

            return null;

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return null;
        }
    }
}
