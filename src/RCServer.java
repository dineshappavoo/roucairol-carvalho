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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
public class RCServer extends RoucairolCarvalho implements Runnable{
	public static final int MESSAGE_SIZE = 1000;
	//private HashMap<Integer, Host> nodeMap;
	//private int nodeId;
	//private int nWaitingForTerminationResponseCount;

	public RCServer()
	{
		//this.nodeMap = nodeMap; 
		//this.nodeId = nodeId;
		//this.nWaitingForTerminationResponseCount = nWaitingForTerminationResponseCount;
	}

	public void go()
	{
		//Buffer to hold messages in byte format
		ByteBuffer byteBuffer = ByteBuffer.allocate(MESSAGE_SIZE);
		String message;

		try
		{
			//Open a server channel
			SctpServerChannel sctpServerChannel = SctpServerChannel.open();
			int port = nodeMap.get(nodeId).hostPort;
			System.out.println("[INFO]	["+sTime()+"]	Node Id "+nodeId+"	Port : "+port);
			//Create a socket addess in the current machine at port 5000
			InetSocketAddress serverAddr = new InetSocketAddress(port);
			//Bind the channel's socket to the server in the current machine at port 5000
			sctpServerChannel.bind(serverAddr);

			System.out.println("[INFO]	["+sTime()+"]	Node Id "+nodeId+"  SERVER STARTED");


			Thread.sleep(5000);
			minHeap = getPriorityQueue();
			//preEmptQueue = new ArrayList<Host>();

			//Server goes into a permanent loop accepting connections from clients			
			while(true)
			{
				//Listen for a connection to be made to this socket and accept it
				//The method blocks until a connection is made
				//Returns a new SCTPChannel between the server and client
				SctpChannel sctpChannel = sctpServerChannel.accept();
				//Receive message in the channel (byte format) and store it in buf
				//Note: Actual message is in byte format stored in buf

				//Receive Message from client
				byteBuffer.clear();
				MessageInfo messageInfo = sctpChannel.receive(byteBuffer, null, null);
				ByteArrayInputStream bin = new ByteArrayInputStream(byteBuffer.array());
				ObjectInputStream oin = new ObjectInputStream(bin);
				Message messageObj = (Message) oin.readObject();	

				System.out.println("[INFO]	["+sTime()+"]	["+messageObj.messageType+"]	Requested Node Id "+messageObj.nodeInfo.hostId);

				if(messageObj.messageType  == MessageType.REQUEST_KEY)
				{

					if(isInCriticalSection)
					{
						minHeap.add(messageObj);

					}else if(requestForCriticalSection)
					{
						if(messageObj.timeStamp.get()>=currentNodeCSEnterTimestamp.get())
						{
							if(messageObj.timeStamp.get()==currentNodeCSEnterTimestamp.get())
							{
								if(nodeId>messageObj.nodeInfo.hostId)
								{
									
								}
							}else
							{
							minHeap.add(messageObj);
							}
						}						
						else
						{
							startRCClient(messageObj.nodeInfo, MessageType.RESPONSE_AND_REQUEST_KEY);   //REsponse_and_request key in case there is a CS request pending
						}
					}				
					else
					{
						//preEmptQueue.add(messageObj.nodeInfo);
						int hostid = messageObj.nodeInfo.hostId;
						nodeMap.get(hostid).keyKnown = false;
						startRCClient(messageObj.nodeInfo, MessageType.RESPONSE_KEY);   //REsponse_and_request key in case there is a CS request pending
					}
				}else if(messageObj.messageType  == MessageType.RESPONSE_KEY)
				{
					int hostid = messageObj.nodeInfo.hostId;
					nodeMap.get(hostid).keyKnown = true;

					if(!isAllNodeKeysKnown())
					{
						;
					}

				}else if(messageObj.messageType == MessageType.RESPONSE_AND_REQUEST_KEY)
				{
					//This case we don't have to compare the timestamp because the other node is sending 'RESPONSE_AND_REQUEST_KEY' which mean the timestamp of other node is greater than the current
					minHeap.add(messageObj);
					int hostid = messageObj.nodeInfo.hostId;
					nodeMap.get(hostid).keyKnown = true;

				}else if(messageObj.messageType == MessageType.TERMINATION_REQUEST)
				{

				}else if(messageObj.messageType == MessageType.TERMINATION_RESPONSE)
				{

				}

				//System.out.println("WAITING COUNT : "+nWaitingForTerminationResponseCount);

				Thread.sleep(8000);
				/*
				//Verify whether we found all nodes in this network
				System.out.println("WAITING COUNT FINAL: "+nWaitingForTerminationResponseCount);

				if(nWaitingForTerminationResponseCount==0)
				{
					System.out.println("DISCOVERED ALL NODES IN THE NETWORK");
					writeOutputToFile();
					System.exit(0);
				}*/
			}
		}
		catch(IOException ex)
		{
			ex.printStackTrace();	
		}catch(InterruptedException ex)
		{
			ex.printStackTrace();
		}catch(ClassNotFoundException ex)
		{
			ex.printStackTrace();
		}
	}


	public void requestAllKeys()
	{
		Host host;
		for(int nId : nodeMap.keySet())
		{
			host = nodeMap.get(nId);
			if(!host.keyKnown && nodeId != host.hostId)
			{
				startRCClient(host, MessageType.REQUEST_KEY);
			}
		}
	}

	public boolean isAllNodeKeysKnown()
	{
		Host host;
		for(int nId : nodeMap.keySet())
		{
			host = nodeMap.get(nId);
			if(!host.keyKnown && host.hostId != nodeId)
			{
				return false;
			}
		}
		return true;
	}


	public String sTime()
	{
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		return timeStamp;
	}

	public String byteToString(ByteBuffer byteBuffer)
	{
		byteBuffer.position(0);
		byteBuffer.limit(MESSAGE_SIZE);
		byte[] bufArr = new byte[byteBuffer.remaining()];
		byteBuffer.get(bufArr);
		return new String(bufArr);
	}

	public void writeOutputToFile() throws FileNotFoundException, UnsupportedEncodingException
	{
		String fileName = "node"+nodeId+".txt";
		PrintWriter writer = new PrintWriter(fileName, "UTF-8");
		writer.println("================================================");
		writer.println("Discovered all nodes in this network");
		writer.println("Total No. of Nodes : "+nodeMap.size());
		writer.println("================================================");
		writer.println("               LIST OF NODES                    ");
		writer.println("================================================");
		for(int n : nodeMap.keySet())
		{
			Host hNode = nodeMap.get(n);
			writer.println(hNode.hostId+"	"+hNode.hostName+"	 "+hNode.hostPort);
		}
		writer.close();
	}

	public void startRCClient(Host host, MessageType sMessageType)
	{		
		RCClient  rCClient;
		Message message;
		currentNodeCSEnterTimestamp.incrementAndGet();
		System.out.println("[INFO]	["+sTime()+"]	Node Id "+nodeId+"  Starting the client to request for a key to "+host.hostName+" at port "+host.hostPort);

		message = new Message(currentNodeCSEnterTimestamp, sMessageType, nodeMap.get(nodeId));
		rCClient = new RCClient(host, message);
		new Thread(rCClient).start();
		System.out.println("[INFO]	["+sTime()+"]	Node Id "+nodeId+"  client requested for a key to "+host.hostName);

	}

	public void startRCClients(PriorityQueue<Message> minHeap, MessageType sMessageType)
	{		
		int size = minHeap.size();
		int nNumOfThreads=size;
		Thread[] tThreads = new Thread[nNumOfThreads];
		RCClient  rCClient;
		Message message;

		//Already known hosts - MAY BE USEFUL FO LIST OF GOT RESPONSE NODES
		/*ArrayList<Host> currentAdjList = new ArrayList<Host>();
		for(int nodeID : nodeMap.keySet())
		{
			currentAdjList.add(nodeMap.get(nodeID));
		}*/
		int i=0;
		while(size>0)
		{
			Host host = minHeap.poll().nodeInfo;
			if (nodeId!=host.hostId)
			{
				//System.out.println("CLIENT CALL TYPE : "+sMessageType);
				//Increment the count only on requests
				if(sMessageType == MessageType.TERMINATION_REQUEST)
				{
					nWaitingForTerminationResponseCount++;
				}
				currentNodeCSEnterTimestamp.incrementAndGet();
				message = new Message(currentNodeCSEnterTimestamp, sMessageType, nodeMap.get(nodeId));
				rCClient = new RCClient(host, message);
				tThreads[i] = new Thread(rCClient);
				tThreads[i].start();
				i++;
			}
		}
	}

	public void run()
	{
		go();
	}

	public static void main(String args[])
	{
		//SctpServerdc01 SctpServerObj = new SctpServerdc01();
		//SctpServerObj.go();
	}

}