import java.io.Serializable;
import java.util.ArrayList;

/**
 * 
 */

/**
 * @author Dany
 *
 */
public class Message implements Serializable{
	String messageType = "";
	Host nodeInfo;
	ArrayList<Host> knownHosts;
	public Message(String messageType, Host nodeInfo, ArrayList<Host> knownHosts)
	{
		this.messageType = messageType;
		this.nodeInfo = nodeInfo;
		this.knownHosts = knownHosts;
	}
}
