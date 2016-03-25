/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.List;

/**
 *
 *
 */

public class UserTokenImp implements UserToken, java.io.Serializable{
    //Declared Variable
    private String issuer;  //Holds the issuer/server name
    private String subject; //Holds the subject/user requesting the token
    private List<String> group; //Holds list of memberships of the subject
    
    //Constructor with 3 parameters
    public UserTokenImp(String issuer, String subject, List<String> group){
        this.issuer = issuer;   
        this.subject =  subject;
        this.group = group;
    }
        
    @Override
    public String getIssuer(){
       return issuer;
    }

    @Override
    public String getSubject(){
       return subject;
    }

    @Override
    public List<String> getGroups(){
        return group;
    }
}
