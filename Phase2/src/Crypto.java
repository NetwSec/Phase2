
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

/**
 * A general Crypto class
 */
public class Crypto {
    public byte[] convertToBytes(Object object)
    {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutput out = new ObjectOutputStream(bos))
        {
            out.writeObject(object);
            return bos.toByteArray();
        }
        catch (Exception e)
        {
            return null;
        } 
    }
    
    public Object convertFromBytes(byte[] bytes)
    {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInput in = new ObjectInputStream(bis))
        {
            return in.readObject();
        }
        catch (Exception e)
        {
            return null;
        } 
    }
    
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

    byte[] AES(int EncryptMode, Key EncryptionKey, IvParameterSpec InitVector, byte[] Content) throws Exception
    {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "BC");
        cipher.init(EncryptMode, EncryptionKey, InitVector);
        return cipher.doFinal(Content);
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
