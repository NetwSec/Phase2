
import java.security.KeyPair;
import java.security.Signature;
import java.util.List;

public class UserTokenImp implements UserToken, java.io.Serializable {

    //Declared Variable
    private String issuer;  //Holds the issuer/server name
    private String subject; //Holds the subject/user requesting the token
    private List<String> group; //Holds list of memberships of the subject
    private byte[] signature;   //Holds signature of token issued by the server

    //Constructor with 2 parameters
    public UserTokenImp(String issuer, User UserInfo) {
        this.issuer = issuer;
        this.subject = UserInfo.getName();
        this.group = UserInfo.getGroups();
    }
    
    //Constructor with 3 parameters
    public UserTokenImp(String issuer, User UserInfo, byte[] signature) {
        this.issuer = issuer;
        this.subject = UserInfo.getName();
        this.group = UserInfo.getGroups();
        this.signature = signature;
    }

    @Override
    //return issuer of this token
    public String getIssuer() {
        return issuer;
    }

    @Override
    //return the subject of this token
    public String getSubject() {
        return subject;
    }
    
    public byte[] getSignature(){
        return signature;
    }
    
    public void setSignature(byte[] signature){
        this.signature = signature;
    }

    @Override
    //return the list of group memberships encoded in this token
    public List<String> getGroups() {
        return group;
    }
    
    public String getContents() {
        StringBuilder contents = new StringBuilder(issuer);
        contents.append(subject);
        for (int i = 0; i < group.size(); i++) {
                contents.append(group.get(i));
        }
        return contents.toString();
    }
    
    public boolean signToken(KeyPair key) {
        try {
            // Create the token's signature
            Signature tokenSign = Signature.getInstance("SHA1WithRSA", "BC");
            tokenSign.initSign(key.getPrivate());
            tokenSign.update(this.getContents().getBytes());
            this.setSignature(tokenSign.sign());
            
            return true;
        }
        catch (Exception e) {
            System.err.println("Signing Error: " + e.getMessage());
            e.printStackTrace(System.err);
        }
        return false;
    }
    
    public boolean authToken(KeyPair key) {
        
        // TODO: can just always check signature from UserList if we make sure to always update it there

        try {
                // Signature verification
                Signature signed = Signature.getInstance("SHA1WithRSA", "BC");
                signed.initVerify(key.getPublic());
                signed.update(this.getContents().getBytes());

                if (signed.verify(this.getSignature())) {
                    // RSA Signature verified
                    return true;
                }
            }
            catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace(System.err);
            }
            return false;  
    }
}
