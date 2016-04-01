/**
 * Auxiliary class to start the GroupServer
 */
public class GroupServerStart {
    
    public static void main(String args[]){
        
        //User passing server name and port
        switch (args.length) {
            case 2: // 2 arguments: server name, port number
                try{
                    System.out.println("Server connecting...");
                    GroupServer server = new GroupServer(args[0], Integer.parseInt(args[1]));
                    server.start(); 
                }
                catch(Exception e){
                    System.err.println("Please enter <server_name>_<port_number>.\n");
                    System.err.println("Usage:  java GroupClient <Server name or IP>\n");
                    System.exit(-1);
                }   
                break;
            case 0: // No arguments - default localhost, 8765
                System.out.println("Starting Group Server...");
                GroupServer server = new GroupServer();
                server.start();
                break;
            default:
                System.out.println("Error");
                break;
        }
        
        
        
    }
    
}
