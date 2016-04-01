
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author Yuntian Zhang
 */
public class ClientFramework implements Runnable {

    // Using array to hold menu item
    private ArrayList<ClientFramework> ItemList = new ArrayList<ClientFramework>();
    private String ItemName;
    private String ItemDescription;

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
        if (ItemList.size() >= 9) {
            return false;
        }

        return ItemList.add(Item);
    }

    @Override
    public void run() {
        // Scanner object for input
        Scanner Input = new Scanner(System.in);
        while (true) {
            DisplayMenu();

            String UserInput = Input.nextLine();

            int UserChoice;

            try {
                UserChoice = Integer.parseInt(UserInput);
            } catch (NumberFormatException e) {
                UserChoice = -1;
            }

            if (UserChoice == 0) {
                break;
            } else if ((UserChoice >= 1) && (UserChoice <= ItemList.size())) {
                System.out.println();
                ItemList.get(UserChoice - 1).run();
            } else {
                System.out.println("Invalid input");
            }
            System.out.println();
        }
    }

}
