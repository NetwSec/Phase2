import java.util.List;

public class FileServer
{
	boolean connect(String server, int port)
	{}
	void disconnect()
	{}
	List<String> listFiles(UserToken token) // Move to GroupServer?
	{}
	boolean upload(String sourceFile, String destFile, String group, UserToken token)
	{}
	boolean download(String sourceFile, String destFile, UserToken token)
	{}
}