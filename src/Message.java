import java.io.Serializable;
import java.util.ArrayList;

/**
 * 
 */

/**
 * @author Dany
 *
 */
enum MessageType {
	REQUEST_KEY,RESPONSE_KEY,RESPONSE_AND_REQUEST_KEY,TERMINATION_REQUEST, TERMINATION_RESPONSE;
}
public class Message implements Serializable{
	
	MessageType messageType;
	Host nodeInfo;
	public Message(MessageType messageType, Host nodeInfo)
	{
		this.messageType = messageType;
		this.nodeInfo = nodeInfo;
	}
}
