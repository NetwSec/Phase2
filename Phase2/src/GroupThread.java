/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 *
 */
public class GroupThread extends Thread {

    private final Socket socket; //The socket where the communication will be held
    private final GroupServer gs;    //Groupserver Object 

    public GroupThread(Socket socket, GroupServer name) {
        this.socket = socket;
        this.gs = name;
    }

    @Override
    public void run() {
        try {
            // Print incoming message
            System.out.println("** New connection from " + socket.getInetAddress() + ":" + socket.getPort() + " **");

            // set up I/O streams with the client
            final ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            final ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

            boolean loop = true;    //Used for the disconnect

            do {
                //read and print message
                Message sent = (Message) input.readObject();
                Message response;
                System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "] " + sent.getMessage());

                //Get Token
                if (sent.getMessage().equals("getToken")) {

                    String username = (String) sent.getObjCont().get(0);

                    //if there is no username
                    if (username == null) {
                        response = new Message("FAIL");
                        response.addObject(null);
                        output.writeObject(response);
                    } else {
                        UserToken userToken = createToken(username); //Create Token
                        response = new Message("OK");
                        response.addObject(userToken);
                        output.writeObject(response);
                    }
                } //Create User
                else if (sent.getMessage().equals("createUser")) {

                    if (sent.getObjCont().size() < 2) {
                        response = new Message("FAIL");
                    } else {
                        response = new Message("FAIL");

                        if (sent.getObjCont().get(0) != null) {
                            if (sent.getObjCont().get(1) != null) {

                                String username = (String) sent.getObjCont().get(0);
                                UserToken token = (UserToken) sent.getObjCont().get(1);

                                if (createUser(username, token)) {
                                    response = new Message("OK");
                                }

                            }
                        }
                    }
                    output.writeObject(response);
                } //Create Group
                else if (sent.getMessage().equals("createGroup")) {

                    if (sent.getObjCont().size() < 2) {
                        response = new Message("FAIL");
                    } else {
                        response = new Message("FAIL");

                        if ((sent.getObjCont().get(0) != null)
                                && (sent.getObjCont().get(1) != null)) {

                            String groupname = (String) sent.getObjCont().get(0);
                            UserToken token = (UserToken) sent.getObjCont().get(1);

                            if (createGroup(groupname, token)) {
                                System.out.println("Creating Group");
                                response = new Message("OK");
                            }
                        }
                    }

                    output.writeObject(response);
                } //Add user to the group
                else if (sent.getMessage().equals("addUserToGroup")) {

                    if (sent.getObjCont().size() < 3) {
                        response = new Message("FAIL");
                    } else {
                        response = new Message("FAIL");

                        if ((sent.getObjCont().get(0) != null) && (sent.getObjCont().get(1) != null) && (sent.getObjCont().get(2) != null)) {
                            String username = (String) sent.getObjCont().get(0);
                            String groupname = (String) sent.getObjCont().get(1);
                            UserToken token = (UserToken) sent.getObjCont().get(2);

                            if (addUserToGroup(username, groupname, token)) {
                                response = new Message("OK");
                            }
                        }
                    }
                    output.writeObject(response);
                } //Delete a user from the group
                else if (sent.getMessage().equals("deleteUserFromGroup")) {

                    if (sent.getObjCont().size() < 2) {
                        response = new Message("FAIL");
                    } else {
                        response = new Message("FAIL");

                        if ((sent.getObjCont().get(0) != null) && (sent.getObjCont().get(1) != null) && (sent.getObjCont().get(2) != null)) {

                            String username = (String) sent.getObjCont().get(0);
                            String groupname = (String) sent.getObjCont().get(1);
                            UserToken token = (UserToken) sent.getObjCont().get(2);

                            if (deleteUserFromGroup(username, groupname, token)) {
                                response = new Message("OK");
                            }

                        }
                    }
                    output.writeObject(response);
                } //List Members of the Group
                else if (sent.getMessage().equals("listMembers")) {

                    String groupName = (String) sent.getObjCont().get(0);
                    UserToken token = (UserToken) sent.getObjCont().get(1);

                    if (groupName == null) {
                        response = new Message("FAIL");
                        response.addObject(null);
                        output.writeObject(response);
                    } else {
                        List<String> members = listMembers(groupName, token);
                        response = new Message("OK");
                        response.addObject(members);
                        output.writeObject(response);
                    }
                } //Disconnect
                else if (sent.getMessage().equals("disconnect")) {
                    // Close and cleanup
                    System.out.println("** Closing connection with " + socket.getInetAddress() + ":" + socket.getPort() + " **");
                    loop = false;
                    socket.close();
                } else {
                    response = new Message("FAIL"); //Server does not understand client request
                    output.writeObject(response);
                }

            } while (loop);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    //create token method
    private UserToken createToken(String username) {

        if (gs.userL.checkUser(username)) {

            UserToken token = new UserTokenImp(gs.getServerName(), username, gs.userL.getUserGroups(username));
            return token;
        } else {
            return null;
        }
    }

    //create user method
    private boolean createUser(String username, UserToken token) {

        String request = token.getSubject();

        //Check if request exist
        if (gs.userL.checkUser(request)) {

            //Get the user's group
            ArrayList<String> tempUG = gs.userL.getUserGroups(request);

            if (tempUG.contains("ADMIN")) {

                //Check if user exist
                if (gs.userL.checkUser(username)) {
                    return false;   //if user exist
                } else {
                    gs.userL.addUser(username);
                    return true;
                }
            } else {
                return false;   //Returns false if request is not "ADMIN"
            }
        } else {
            return false;   //Returns false if no request exist
        }
    }

    //create group method
    private boolean createGroup(String groupname, UserToken token) {

        //
        String request = token.getSubject();
        String temp;

        if (gs.userL.checkUser(request)) {

            for (Enumeration<String> usernameL = gs.userL.getUsernames(); usernameL.hasMoreElements();) {

                temp = usernameL.nextElement();

                //If groupname is taken
                if (gs.userL.getUserOwnerships(temp).contains(groupname)) {
                    return false;
                }
            }

            //Adds ownership and group
            gs.userL.addOwnerships(request, groupname);
            gs.userL.addGroup(request, groupname);

            return true;
        } else {
            return false;
        }
    }

    //adds user to group 
    private boolean addUserToGroup(String user, String group, UserToken token) {
        String request = token.getSubject();

        //Checks if user and the request of current user are valid
        if (gs.userL.checkUser(request) && gs.userL.checkUser(user)) {

            //Checks if user currently request has ownership
            if (gs.userL.getUserOwnerships(request).contains(group)) {

                if (!gs.userL.getUserGroups(user).contains(group)) {

                    gs.userL.addGroup(user, group);
                    return true;
                } else {
                    return false;   //False if user is found within the group
                }
            } else {
                return false;   //False if user lacks ownership
            }
        } else {
            return false;   //False if they fail to exist
        }
    }

    //delete user from group method
    private boolean deleteUserFromGroup(String user, String group, UserToken token) {
        String request = token.getSubject();

        //Checks if user and the request of current user are valid
        if (gs.userL.checkUser(request) && gs.userL.checkUser(user)) {

            //Checks if user currently request has ownership
            if (gs.userL.getUserOwnerships(request).contains(group)) {

                //Checks if user is a member of the group
                if (gs.userL.getUserGroups(user).contains(group)) {

                    gs.userL.removeGroup(user, group);
                    return true;    //True if user is found within group
                } else {
                    return false;   //False if member is not apart of the group
                }
            } else {
                return false;   //False if user lacks ownership
            }
        } else {
            return false;   //False if they fail to exist
        }
    }

    //
    private List<String> listMembers(String groupName, UserToken token) {

        String request = token.getSubject();
        String tUser = new String();
        List<String> list = new ArrayList<String>();

        //Check if request exist
        if (gs.userL.checkUser(request)) {

            ArrayList<String> temp = gs.userL.getUserGroups(request);   //Holds users within requested group

            //Checks if user requesting is in the same group
            if (temp.contains(groupName)) {

                for (Enumeration<String> unList = gs.userL.getUsernames(); unList.hasMoreElements();) {

                    tUser = unList.nextElement();

                    if (gs.userL.getUserGroups(tUser).contains(groupName)) {

                        list.add(tUser);
                    }
                }
                return list;    //returns list
            } else {
                return null;    //Null if user is not in the same group
            }
        } else {
            return null;    //Null if request does not exist
        }
    }

}
