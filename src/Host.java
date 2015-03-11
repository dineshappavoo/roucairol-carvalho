import java.io.Serializable;

/**
 * 
 */


/**
 * @author Dany
 *
 */
public class Host implements Serializable{
	
	public int hostId;
	public String hostName;
	public int hostPort;
	
	public Host(int hostId, String hostName, int hostPort)
	{
		this.hostId = hostId;
		this.hostName = hostName;
		this.hostPort = hostPort;
	}
	
	public Host()
	{
		this.hostId = 0;
		this.hostName = "";
		this.hostPort = 0;	
	}
}
