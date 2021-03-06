
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class SecureSocket {
    final int SS_METHOD_NONE = 0;
    final int SS_METHOD_AES = 1;
    final int SS_METHOD_RSA = 2;
    
    Socket Connection;
    ObjectOutputStream Output;
    ObjectInputStream Input;
    
    SecretKey AESKey;
    IvParameterSpec InitVector;
    

    SecureSocket(String IP, int Port) throws Exception
    {
        Connection = new Socket(IP, Port);
        Output = new ObjectOutputStream(Connection.getOutputStream());
        Input = new ObjectInputStream(Connection.getInputStream());
    }
    
    SecureSocket(Socket OpenSocket) throws Exception
    {
        Connection = OpenSocket;
        Output = new ObjectOutputStream(Connection.getOutputStream());
        Input = new ObjectInputStream(Connection.getInputStream());
    }
    
    public void close()
    {
        try
        {
        Input.close();
        Output.close();
        Connection.close();
        }
        catch (Exception e)
        {
            
        }
    }
    
    public boolean connect()
    {
        try
        {
            Crypto crypto = new Crypto();
            KeyPair Pair = Crypto.createKeyPair();
            
            //1a. client send public_c
            Output.writeUnshared(Pair.getPublic());
            
            //2b. receive aes key
            byte[] EncryptedData = (byte[]) Input.readUnshared();
            Cipher cipher = Cipher.getInstance("RSA", "BC");
            cipher.init(Cipher.UNWRAP_MODE, Pair.getPrivate());
            AESKey = (SecretKey) cipher.unwrap(EncryptedData, "RSA", Cipher.SECRET_KEY);
            
            //3b. send iv
            byte[] iv = generateIV(16);
            InitVector = new IvParameterSpec(iv);
            EncryptedData = crypto.AES(Cipher.ENCRYPT_MODE, AESKey, null, iv);
            Output.writeUnshared(EncryptedData);
            
            // 4. end
            EncryptedData = (byte[]) Input.readUnshared();
            String Hand = (String) crypto.convertFromBytes(crypto.AES(Cipher.DECRYPT_MODE, AESKey, InitVector, EncryptedData));
            
            if (!Hand.equals("success"))
            {
                throw new Exception("hand shaking failed");
            }
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }
    
    public boolean listen()
    {
        try
        {
            Crypto crypto = new Crypto();
            
            //1a. client send public_c
            PublicKey RemoteKey = (PublicKey) Input.readUnshared();
            
            //2a. generate aes key
            KeyGenerator generator = KeyGenerator.getInstance("AES","BC");
            // Initialize the generator for 128-bit key size
            generator.init(128);
            AESKey = generator.generateKey();
            
            Cipher cipher = Cipher.getInstance("RSA", "BC");
            cipher.init(Cipher.WRAP_MODE, RemoteKey);
            byte[] EncryptedData = cipher.wrap(AESKey);
            Output.writeUnshared(EncryptedData);
            
            //3c. get iv
            EncryptedData = (byte[]) Input.readUnshared();
            byte[] iv = crypto.AES(Cipher.DECRYPT_MODE, AESKey, null, EncryptedData);
            InitVector = new IvParameterSpec(iv);

            // 4. send success
            EncryptedData = crypto.AES(Cipher.ENCRYPT_MODE, AESKey, InitVector, crypto.convertToBytes("success"));
            Output.writeUnshared(EncryptedData);
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }

    public void send(Object o) throws Exception
    {
        Crypto crypto = new Crypto();
        byte[] EncryptedData = crypto.AES(Cipher.ENCRYPT_MODE, AESKey, InitVector, crypto.convertToBytes(o));
        Output.writeUnshared(EncryptedData);
    }
    
    public Object receive() throws Exception
    {
        Object Response = null;
        byte[] EncryptedData = (byte[]) Input.readUnshared();
        Crypto crypto = new Crypto();
        Response = crypto.convertFromBytes(crypto.AES(Cipher.DECRYPT_MODE, AESKey, InitVector, EncryptedData));
        return Response;
    }
    
        /**
     * Generates a random initialization vector of size n
     * @param n
     * @return array of n random bytes
     */
    private byte[] generateIV(int n)
    {
        SecureRandom rand =new SecureRandom();
        byte [] randBytes = new byte[n];
        rand.nextBytes(randBytes);
        
        return randBytes;
    }
}
