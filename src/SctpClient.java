/**
 * 
 */


/**
 * @author Dany
 *
 */
import java.io.*;
import java.net.*;

import com.sun.nio.sctp.*;

import java.nio.*;
import java.io.*;
import java.net.*;

import com.sun.nio.sctp.*;

import java.nio.*;
import java.util.ArrayList;
import java.util.HashMap;
public class SctpClient implements Runnable{

	public static final int MESSAGE_SIZE = 1000;

	private Host hostToBeRequested;
	private Message messageForHost;

	public SctpClient(Host hostToBeRequested, Message messageForHost)
	{
		this.hostToBeRequested = hostToBeRequested;
		this.messageForHost = messageForHost;
	}

	public void go()
	{
		//Buffer to hold messages in byte format
		ByteBuffer byteBuffer = ByteBuffer.allocate(MESSAGE_SIZE);
		String responseMessageFromServer="";
		try
		{
			Thread.sleep(5000);
			//Create a socket address for  server at dc at port XXX
			SocketAddress socketAddress = new InetSocketAddress(hostToBeRequested.hostName,hostToBeRequested.hostPort);
			//Open a channel. NOT SERVER CHANNEL
			SctpChannel sctpChannel = SctpChannel.open();
			//Bind the channel's socket to a local port. Again this is not a server bind

			//sctpChannel.bind(new InetSocketAddress(4587));

			//Connect the channel's socket to  the remote server
			sctpChannel.connect(socketAddress);
			//Before sending messages add additional information about the message
			MessageInfo messageInfo = MessageInfo.createOutgoing(null, 0);
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream oout;
			try {
				oout = new ObjectOutputStream(bout);
				oout.writeObject(messageForHost);
				byteBuffer.put(bout.toByteArray());
				byteBuffer.flip();
				sctpChannel.send(byteBuffer, messageInfo);

			} catch (IOException e) {

				e.printStackTrace();
			} finally {
				byteBuffer.clear();
			}

		}catch(IOException ex)
		{
			ex.printStackTrace();

		}catch(InterruptedException ex)
		{
			ex.printStackTrace();
		}
	}

	public String byteToString(ByteBuffer byteBuffer)
	{
		byteBuffer.position(0);
		byteBuffer.limit(MESSAGE_SIZE);
		byte[] bufArr = new byte[byteBuffer.remaining()];
		byteBuffer.get(bufArr);
		return new String(bufArr);
	}

	public void run()
	{
		go();
	}
	public static void main(String args[])
	{
		//SctpClientdc01 SctpClientObj = new SctpClientdc01();
		//SctpClientObj.go();
	}
}