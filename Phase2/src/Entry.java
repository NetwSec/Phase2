
import java.util.Scanner;

/**
 * The entrypoint of the package
 *
 * @author Yuntian Zhang
 */
public class Entry {

    static ClientFramework GroupServer = new ClientFramework("Start group server") {
        @Override
        public void run() {
            Scanner Input = new Scanner(System.in);

            System.out.println("Please enter the group server port");
            System.out.print("Default[8765]:");
            int Port = 8765;
            try {
                Port = Integer.parseInt(Input.nextLine());
            } catch (Exception e) {
                Port = 8765;
            }
            System.out.println("Starting server at port " + Port);

            GroupServer2 Server = new GroupServer2(Port);
            Server.run();
        }
    };
    
    static ClientFramework FileServer = new ClientFramework("Start file server") {
        @Override
        public void run() {
            Scanner Input = new Scanner(System.in);

            System.out.println("Please enter the group server port");
            System.out.print("Default[8766]:");
            int Port = 8766;
            try {
                Port = Integer.parseInt(Input.nextLine());
            } catch (Exception e) {
                Port = 8766;
            }
            System.out.println("Starting server at port " + Port);

            FileServer Server = new FileServer(Port);
            Server.run();
        }
    };
    
    static ClientFramework Client = new ClientFramework("Start client") {
        @Override
        public void run() {
            Client2 Client = new Client2();
            Client.run();
        }
    };

    static ClientFramework Launcher = new ClientFramework("Launcher");
    
    public static void main(String[] args) {
        Launcher.RegisterItem(GroupServer);
        Launcher.RegisterItem(FileServer);
        Launcher.RegisterItem(Client);
        Launcher.run();
    }

}
