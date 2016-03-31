import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * File client
 * 
 * @author Yuntian Zhang
 */
public class FileClient
{
    static String FS_ADDRESS = "localhost";
    static int FS_PORT = 8766;
    
    static Socket Server;
    static ObjectInputStream Input;
    static ObjectOutputStream Output;
    static UserToken Token;
    
    FileClient(UserToken token)
    {
        Token = token;
    }
    
    static boolean connect(String server, int port)
    {
        try
        {
        Server = new Socket(server,port);
        Output = new ObjectOutputStream(Server.getOutputStream());
        Input = new ObjectInputStream(Server.getInputStream());
        return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
    static ClientFramework Connect = new ClientFramework("Connect")
    {
        @Override
        public void run()
        {
            // Connect to server
            Scanner Input = new Scanner(System.in);

            System.out.println("Please enter server address");
            System.out.print("Default[localhost]:");
            String FS_ADDRESS = Input.nextLine();
            if (FS_ADDRESS.equals(""))
            {
                FS_ADDRESS = "localhost";
            }
            System.out.println("Please enter server port");
            System.out.print("Default[8766]:");
            String Port = Input.nextLine();
            try
            {
                FS_PORT = Integer.parseInt(Port);
            }
            catch (Exception e)
            {
                FS_PORT = 8766;
            }

            System.out.println("Connect to " + FS_ADDRESS + ":" + FS_PORT);
            if (!connect(FS_ADDRESS,FS_PORT))
            {
                System.out.println("Connection failed");
                return;
            }

            // Get UserToken
            /*
            List<String> Group = new ArrayList<>();
            Group.add("group");
            Token = new UserTokenImp("localhost", "admin", Group);
            */

            System.out.println("Connected.");
        }
    };
    
    static void disconnect()
    {
        try {
            Input.close();
        } catch (Exception ex) {
        }
        Input = null;
        
        try {
            Output.close();
        } catch (Exception ex) {
        }
        Output = null;
        
        try {
            Server.close();
        } catch (Exception ex) {
        }
        Server = null;
    }
    static ClientFramework Disconnect = new ClientFramework("Disconnect")
    {
        @Override
        public void run()
        {
            // Disconnect
            disconnect();
            System.out.println("Disconnected.");
        }
    };
    
    static List<String> listFile(UserToken token, String group)
    {
         Message Upload = new Message("list");
        
        // Create Message header
        Upload.addObject((UserToken) token);
        Upload.addObject((String) group);
        
        //  Send message
        try {
            Output.writeObject(Upload);
            Output.flush();
        } catch (Exception ex) {
            return null;
        }
        
        //  Receive response
        Message Response;
        try {
            Response = (Message) Input.readObject();
        } catch (Exception ex) {
            return null;
        }
        
        if(!"view".equals(Response.getMessage()))
        {
            return null;
        }
        else
        {
            ArrayList<Object> Content = Response.getObjCont();
            return (List<String>)Content.get(2);
        }
    }
    static ClientFramework ListFile = new ClientFramework("List File")
    {
        @Override
        public void run()
        {
            Scanner Input = new Scanner(System.in);

            System.out.print("Please enter group name:");
            String Group = Input.nextLine();

            List<String> FileList = listFile(Token,Group);
            if (FileList == null)
            {
                System.out.println("List failed");
            }
            else
            {
                System.out.println("List:");
                for (int i=0; i<FileList.toArray().length; i++)
                {
                    System.out.println(FileList.toArray()[i]);
                }
                System.out.println("-------");
            }
        }
    };
    
    static boolean upload(UserToken token, String group, String remoteFile, String localFile)
    {
        Message Upload = new Message("upload");
        
        try
        {
            // Create Message header
            Upload.addObject((UserToken) token);
            Upload.addObject((String) group);
            Upload.addObject((String) remoteFile);

            // Attach FileContent
            File FileHandle = new File((String) localFile);
            byte[] FileContent = new byte[(int) FileHandle.length()];
            FileInputStream FileStream = new FileInputStream(FileHandle);
            BufferedInputStream FileBuff = new BufferedInputStream(FileStream);
            FileBuff.read(FileContent, 0, FileContent.length);

            Upload.addObject((byte[]) FileContent);

            // Free resources
            FileBuff.close();
            FileStream.close();
        }
        catch (Exception e)
        {
            return false;
        }
        
        //  Send message
        try {
            Output.writeObject(Upload);
            Output.flush();
        } catch (Exception ex) {
            return false;
        }
        
        //  Receive response
        Message Response;
        try {
            Response = (Message) Input.readObject();
        } catch (Exception ex) {
            return false;
        }
        
        return ("success".equals(Response.getMessage()));
    }
    static ClientFramework Upload = new ClientFramework("Upload")
    {
        @Override
        public void run()
        {
            // Create an upload message
            Scanner Input = new Scanner(System.in);

            System.out.print("Please enter group name:");
            String Group = Input.nextLine();
            System.out.print("Please enter local file name:");
            String Local = Input.nextLine();
            System.out.print("Please enter remote file name:");
            String Remote = Input.nextLine();

            String localFile = System.getProperty("user.dir") + File.separator + "FileClient" + File.separator;

            if (!upload(Token,Group,Remote,localFile + Local))
            {
                System.out.println("Upload failed");
            }
            else
            {
                System.out.println("Upload Succeed");
            }
        }
    };

    static boolean download(UserToken token, String group, String remoteFile, String localFile)
    {
        Message Download = new Message("download");
        
         // Create Message header
        Download.addObject((UserToken) token);
        Download.addObject((String) group);
        Download.addObject((String) remoteFile);
        
        // Send the message
        try {            
            Output.writeObject(Download);
            Output.flush();
        } catch (Exception ex) {
            return false;
        }
        
        //  Receive response
        Message Response;
        try {
            Response = (Message) Input.readObject();
        } catch (Exception ex) {
            return false;
        }
        
        // Check response
        if (!"upload".equals(Response.getMessage()))
        {
            return false;
        }
        
        // Save file
        try
        {
            ArrayList<Object> Content = Response.getObjCont();
            byte[] FileContent = (byte[]) Content.get(3);
            File FileHandle = new File(localFile);
            FileHandle.getParentFile().mkdirs();
            FileOutputStream FileStream = new FileOutputStream(FileHandle);
            BufferedOutputStream FileBuff = new BufferedOutputStream(FileStream);
            FileBuff.write(FileContent, 0, FileContent.length);
            FileBuff.flush();

            // Free resources
            FileBuff.close();
            FileStream.close();
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }
    static ClientFramework Download = new ClientFramework("Download")
    {
        @Override
        public void run()
        {
            // Create an download message
            Scanner Input = new Scanner(System.in);

            System.out.print("Please enter group name:");
            String Group = Input.nextLine();
            System.out.print("Please enter local file name:");
            String Local = Input.nextLine();
            System.out.print("Please enter remote file name:");
            String Remote = Input.nextLine();

            String localFile = System.getProperty("user.dir") + File.separator + "FileClient" + File.separator;

            if(!download(Token,Group,Remote,localFile + Local))
            {
                System.out.println("Download failed");
            }
            else
            {
                System.out.println("Download Succeed");
            }
        }
    };
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        ClientFramework MainMenu = new ClientFramework("Main Menu");

        MainMenu.RegisterItem(Connect);
        MainMenu.RegisterItem(Disconnect);
        MainMenu.RegisterItem(ListFile);
        MainMenu.RegisterItem(Upload);
        MainMenu.RegisterItem(Download);
        MainMenu.run();
    }
}
