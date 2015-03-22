import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

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
	AtomicInteger timeStamp;
	MessageType messageType;
	Host nodeInfo;
	public Message(AtomicInteger timeStamp, MessageType messageType, Host nodeInfo)
	{
		this.timeStamp = timeStamp;
		this.messageType = messageType;
		this.nodeInfo = nodeInfo;
	}
}
