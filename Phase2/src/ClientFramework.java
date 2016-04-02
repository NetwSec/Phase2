
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * A general command line menu class
 * 
 * ClientFramework provides the main loop for a command line menu.
 * A menu usually can be divided into two parts: branch node which provides items,
 * and leaf node which provides features.
 * 
 * By default, a ClientFramework object is a branch node, and currently can have
 * 9 children using RegisterItem plus a default exit node to return to parent node.
 * Number 9 is choosen so a user can use his number pad to go to any node in 2
 * key press. ClientFramework will maintain the loop to ensure user won't leave
 * this node before he made a  vaild choice. When entering the node, ClientFramework
 * will display the title for the node and optionally, a description.
 * 
 * When a leaf node is required, override run() method with your own functions.
 * 
 * You can also override Exit() method to provide a custom action upon exiting
 * menu, such as free resources.
 */
public class ClientFramework implements Runnable {

    // Using array to hold menu item
    private ArrayList<ClientFramework> ItemList = new ArrayList<ClientFramework>();
    private String ItemName;
    private String ItemDescription;
    private ClientFramework Parent = null;
    private ClientFramework ExitHandler = this;

    ClientFramework(String Name) {
        ItemName = Name;
        ItemDescription = null;
    }

    ClientFramework(String Name, String Description) {
        ItemName = Name;
        ItemDescription = Description;
    }

    public String GetName() {
        return ItemName;
    }
    
    public void SetParent(ClientFramework Parent) {
        this.Parent = Parent;
    }
    
    public void SetExitHandler(ClientFramework ExitHandler) {
        this.ExitHandler = ExitHandler;
    }
    
    public void Exit()
    {
        
    }

    public void DisplayMenu() {
        System.out.println(ItemName);
        System.out.println();
        if (ItemDescription != null) {
            System.out.println(ItemDescription);
        }
        for (int i = 0; i < ItemList.size(); i++) {
            System.out.print(i + 1);
            System.out.print(". ");
            System.out.print(ItemList.get(i).GetName());
            System.out.println();
        }
        System.out.print(0);
        System.out.print(". Exit menu");
        System.out.println();
        
        System.out.print("Please enter your choice: ");
    }

    //  Add new menu item
    public boolean RegisterItem(ClientFramework Item) {
        if (ItemList.size() + 1 >= 10) {
            return false;
        }
        Item.SetParent(this);
        return ItemList.add(Item);
    }
    
    //  Add new menu items
    public boolean RegisterItem(ArrayList<ClientFramework> Item) {
        for (int i = 0; i < Item.size(); i++)
        {
            if (!RegisterItem(Item.get(i)))
            {
                while((i--) > 0)
                {
                    ItemList.remove(ItemList.size() - 1);
                }
                return false;
            }
        }
        return true;
    }
    
    public ArrayList<ClientFramework> UnregisterItem()
    {
        ArrayList<ClientFramework> OldMenu = ItemList;
        for (int i = 0; i < OldMenu.size(); i++)
        {
            OldMenu.get(i).SetParent(null);
        }
        ItemList = new ArrayList<ClientFramework>();
        return OldMenu;
    }

    @Override
    public void run() {
        // Scanner object for input
        Scanner Input = new Scanner(System.in);
        while (true) {
            DisplayMenu();

            String UserInput;
            try
            {
                UserInput = Input.nextLine();
                System.out.println();
            }
            catch (NoSuchElementException e)
            {
                // Ctrl+C handler
                break;
            }
            catch (Exception e)
            {
                // Something is wrong
                UserInput = "-1";
            }

            int UserChoice;

            try {
                UserChoice = Integer.parseInt(UserInput);
            } catch (Exception e) {
                UserChoice = -1;
            }

            if (UserChoice == 0) {
                ExitHandler.Exit();
                break;
            } else if ((UserChoice >= 1) && (UserChoice <= ItemList.size())) {
                ItemList.get(UserChoice - 1).run();
            } else {
                System.out.println("Invalid input");
            }
            System.out.println();

        }
    }

}
