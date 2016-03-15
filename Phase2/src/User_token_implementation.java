public class UserTokenImp implements UserToken
{
	String tokenIssuer;
	String tokenSubject;
	List<String> groupMemberships;
	
	public String getIssuer()
	{
		return tokenIssuer;
	}
	public String getSubject()
	{
		return tokenSubject;
	}
	public List<String> getGroups()
	{
		return groupMemberships;
	}
		
}