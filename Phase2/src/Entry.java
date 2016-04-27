
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
            GroupServer2 Server = new GroupServer2();
            Server.run();
        }
    };

    static ClientFramework FileServer = new ClientFramework("Start file server") {
        @Override
        public void run() {
            FileServer Server = new FileServer();
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

        Security.addProvider(new BouncyCastleProvider());
        
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
