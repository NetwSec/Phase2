
import java.util.Scanner;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author saber
 */
public class Entry {

    static ClientFramework GroupServer = new ClientFramework("Start group server")
    {
        @Override
        public void run()
        {
            Scanner Input = new Scanner(System.in);
        
            System.out.println("Please enter the group server port");
            System.out.print("Default[8765]:");
            int Port = 8765;
            try
            {
                Port = Integer.parseInt(Input.nextLine());
            }
            catch (Exception e)
            {
                Port = 8765;
            }
            System.out.println("Starting server at port " + Port);
            
            GroupServer2 Server = new GroupServer2(Port);
            Server.run();
        }
    };
    static ClientFramework FileServer = new ClientFramework("Start file server")
    {
        @Override
        public void run()
        {
            Scanner Input = new Scanner(System.in);
        
            System.out.println("Please enter the group server port");
            System.out.print("Default[8765]:");
            int Port = 8765;
            try
            {
                Port = Integer.parseInt(Input.nextLine());
            }
            catch (Exception e)
            {
                Port = 8765;
            }
            System.out.println("Starting server at port " + Port);
            
            FileServer Server = new FileServer(Port);
            Server.run();
        }
    };
    static ClientFramework Client = new ClientFramework("Start client")
    {
        @Override
        public void run()
        {
            Client2 Client = new Client2();
            Client.run();
        }
    };
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ClientFramework Launcher = new ClientFramework("Launcher");
        Launcher.RegisterItem(GroupServer);
        Launcher.RegisterItem(FileServer);
        Launcher.RegisterItem(Client);
        Launcher.run();
    }
    
}
