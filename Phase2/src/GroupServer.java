import java.util.List;

public class GroupServer
{
	List<UserTokenImp> users;
	List<String> groups;
	
	boolean connect(String server, int port)
	{}
	void disconnect()
	{}
	UserTokenImp getToken(String username)
	{}
	boolean createUser(String username, UserTokenImp token)
	{}
	boolean createGroup(String groupname, UserToken token)
	{}
	boolean addUserToGroup(String user, String group, UserToken token)
	{}
	boolean deleteUserFromGroup(String user, String group, UserToken token)
	{}
	List<String> listMembers(String group, UserToken token)
	{}
	
}