import java.util.List;

public class FileServer
{
	boolean connect(String server, int port)
	{
            return false;
        }
	void disconnect()
	{}
	List<String> listFiles(UserToken token) // Move to GroupServer?
	{
            return null;
        }
	boolean upload(String sourceFile, String destFile, String group, UserToken token)
	{
            return false;
        }
	boolean download(String sourceFile, String destFile, UserToken token)
	{
            return false;
        }
}