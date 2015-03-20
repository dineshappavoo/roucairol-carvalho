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
	public boolean keyKnown;
	
	public Host(int hostId, String hostName, int hostPort, boolean keyKnown)
	{
		this.hostId = hostId;
		this.hostName = hostName;
		this.hostPort = hostPort;
		this.keyKnown = keyKnown;
	}
	
	public Host()
	{
		this.hostId = 0;
		this.hostName = "";
		this.hostPort = 0;	
		this.keyKnown = false;
	}
}

