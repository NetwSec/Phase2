
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.SecureRandom;

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
    
    static KeyPair createKeyPair()
    {
        KeyPair Key = null;
        final int RSAKEYSIZE = 2048;
        try {
                KeyPairGenerator keyGenRSA = KeyPairGenerator.getInstance("RSA", "BC");
                SecureRandom keyGenRandom = new SecureRandom();
                byte bytes[] = new byte[20];
                keyGenRandom.nextBytes(bytes);
                keyGenRSA.initialize(RSAKEYSIZE, keyGenRandom);
                Key = keyGenRSA.generateKeyPair();
        }
        catch (Exception e) {
                Key = null;
        }
        return Key;
    }
}
