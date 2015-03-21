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
	long timeStamp;
	MessageType messageType;
	Host nodeInfo;
	public Message(long timeStamp, MessageType messageType, Host nodeInfo)
	{
		this.timeStamp = timeStamp;
		this.messageType = messageType;
		this.nodeInfo = nodeInfo;
	}
}
