
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class SecureSocket {
    final int SS_METHOD_NONE = 0;
    final int SS_METHOD_AES = 1;
    final int SS_METHOD_RSA = 2;
    
    Socket Connection;
    ObjectOutputStream Output;
    ObjectInputStream Input;
    
    SecretKey AESKey;
    

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
    
    boolean connect()
    {
        try
        {
            Crypto crypto = new Crypto();
            KeyPair Pair = crypto.createKeyPair();
            
            //1a. client send public_c
            Output.writeUnshared(Pair.getPublic());
            
            //1b. server send public_c(public_s)
            byte[] EncryptedData = (byte[]) Input.readUnshared();
            PublicKey RemoteKey = (PublicKey) crypto.convertFromBytes(crypto.RSA(Cipher.DECRYPT_MODE, Pair.getPrivate(), EncryptedData));
            
            //2a. generate aes key
            KeyGenerator generator = KeyGenerator.getInstance("AES","BC");
            // Initialize the generator for 128-bit key size
            generator.init(128);
            AESKey = generator.generateKey();
            EncryptedData = crypto.RSA(Cipher.ENCRYPT_MODE, RemoteKey, crypto.convertToBytes(AESKey));
            Output.writeUnshared(EncryptedData);
            
            //3b. shake hands
            EncryptedData = (byte[]) Input.readUnshared();
            PublicKey Hand = (PublicKey) crypto.convertFromBytes(crypto.AES(Cipher.DECRYPT_MODE, AESKey, EncryptedData));
            if (!Hand.equals(Pair.getPublic()))
            {
                throw new Exception("hand shaking failed");
            }
            
            //3c. final
            EncryptedData = crypto.AES(Cipher.ENCRYPT_MODE, AESKey, crypto.convertToBytes("success"));
            Output.writeUnshared(EncryptedData);
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }
    
    boolean listen()
    {
        try
        {
            Crypto crypto = new Crypto();
            KeyPair Pair = crypto.createKeyPair();
            
            //1a. client send public_c
            PublicKey RemoteKey = (PublicKey) Input.readUnshared();
            
            //1b. server send public_c(public_s)
            byte[] EncryptedData = crypto.RSA(Cipher.ENCRYPT_MODE, RemoteKey, crypto.convertToBytes(Pair.getPublic()));
            Output.writeUnshared(EncryptedData);
            
            //2b. receive aes key
            EncryptedData = (byte[]) Input.readUnshared();
            AESKey = (SecretKey) crypto.convertFromBytes(crypto.RSA(Cipher.DECRYPT_MODE, Pair.getPrivate(), EncryptedData));
            
            //3a. shake hands
            EncryptedData = crypto.AES(Cipher.ENCRYPT_MODE, AESKey, crypto.convertToBytes(RemoteKey));
            Output.writeUnshared(EncryptedData);
            
            //3c. final
            EncryptedData = (byte[]) Input.readUnshared();
            String Hand = (String) crypto.convertFromBytes(crypto.AES(Cipher.DECRYPT_MODE, AESKey, EncryptedData));
            
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

    boolean send(Object o)
    {
        try
        {
            Crypto crypto = new Crypto();
            byte[] EncryptedData = crypto.AES(Cipher.ENCRYPT_MODE, AESKey, crypto.convertToBytes(o));
            Output.writeUnshared(EncryptedData);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
    
    Object receive()
    {
        Object Response = null;
        try
        {
            byte[] EncryptedData = (byte[]) Input.readUnshared();
            Crypto crypto = new Crypto();
            Response = crypto.convertFromBytes(crypto.AES(Cipher.DECRYPT_MODE, AESKey, EncryptedData));
        }
        catch (Exception e)
        {
            
        }
        return Response;
    }
}
