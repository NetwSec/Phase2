
import java.security.MessageDigest;

/**
 * A general Crypto class
 */
public class Crypto {
    static byte[] getHash(String input)
    {
        byte[] toHash = input.getBytes();
        // TODO: other algorithm?
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA1", "BC");
            digest.update(toHash);
            return digest.digest();
        }
        catch(Exception e)
        {
            e.printStackTrace(System.err);
            return null;
        }
    }
}
