
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
}
