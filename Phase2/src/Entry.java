
import java.security.Security;
import java.util.ArrayList;
import java.util.Scanner;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * The entrypoint of the package
 * Accept command line argument
 * See main()
 */
public class Entry {

    static ClientFramework GroupServer = new ClientFramework("Start group server") {
        @Override
        public void run() {
            
            Security.addProvider(new BouncyCastleProvider());
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
        ArrayList<ClientFramework> Subtask = new ArrayList<ClientFramework>();
        Subtask.add(GroupServer);
        Subtask.add(FileServer);
        Subtask.add(Client);

        if (args.length == 0) {
            Launcher.RegisterItem(Subtask);
            Launcher.run();
        } else {
            try {
                int TaskID = Integer.parseInt(args[0]);
                Subtask.get(TaskID).run();
            } catch (Exception e) {
                System.out.println("Invalid argument");
                System.out.println();
                System.out.println("Usage");
                try {
                    System.out.println("java -jar " + Entry.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath() + " <Subtask ID>");
                } catch (Exception ex) {
                    System.out.println("java -jar <Path of this file> <Subtask ID>");
                }
                System.out.println("Available subtask ID:");
                for (int i = 0; i < Subtask.size(); i++) {
                    System.out.println(i + " = " + Subtask.get(i).GetName());
                }
            }
        }
    }

}
