
import java.util.ArrayList;

/**
 * *
 *
 * Message class serves the purpose as the courier who exchanges messages
 * between server and client. It is a means of communication.
 */
public class Message implements java.io.Serializable {

    //Declared Variables
    private String message; //Holds messages being sent or received b/w client and server
    private ArrayList<Object> arrObj = new ArrayList<Object>();

    //Constructor w/ 1 String Parameter
    public Message(String message) {
        this.message = message;
    }

    //returns message
    public String getMessage() {
        return message;
    }

    //Returns arrObj
    public ArrayList<Object> getObjCont() {
        return arrObj;
    }

    //Adds object to the ArrayList
    public void addObject(Object o) {
        arrObj.add(o);
    }
}
