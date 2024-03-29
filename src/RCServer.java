
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
	public static boolean hasAllTerminated = false;
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
			System.out.println("[INFO]	["+sTime()+"]	==================================");
			
			Thread.sleep(7000);
			minHeap = getPriorityQueue();
			//preEmptQueue = new ArrayList<Host>();

			//Server goes into a permanent loop accepting connections from clients			
			while(true)
			{

				if(hasAllTerminated){
					if(!isTerminationSent)
					{
						if(count == noOfCriticalSectionRequests){
							while(isInCriticalSection);
							//System.out.println("Count Value - "+count+"   Total CS Requests = "+noOfCriticalSectionRequests);

							rCServer.sendTermination();
						}
					}
					else
					{
						//System.out.println("Terminating Server at node "+nodeId);
						System.exit(0);
					}

				}
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
				int hostId;

				if(messageObj.messageType  == MessageType.REQUEST_KEY)
				{
					//out.println("[INFO]	["+sTime()+"]	["+messageObj.messageType+"]	Node ID : "+nodeId+"	CurrentNode Timestamp : "+currentNodeCSEnterTimestamp.get()+"	Message Timestamp : "+messageObj.timeStamp+"	Got Request from Node Id "+messageObj.nodeInfo.hostId);

					if(isInCriticalSection)
					{
						minHeap.add(messageObj);

					}else if(requestForCriticalSection)
					{
						if(messageObj.timeStamp.get()>=currentNodeCSEnterTimestamp.get())
						{
							if(messageObj.timeStamp.get()==currentNodeCSEnterTimestamp.get())
							{
								if(nodeId<messageObj.nodeInfo.hostId)
								{
									minHeap.add(messageObj);
								}else
								{
									hostId = messageObj.nodeInfo.hostId;
									nodeMap.get(hostId).keyKnown = false;
									//currentNodeCSEnterTimestamp.incrementAndGet(); //Timestamp is getting incremented since it is RESPONSE_AND_REQUEST_KEY
									startRCClient(messageObj.nodeInfo, MessageType.RESPONSE_AND_REQUEST_KEY);   //Response_and_request key in case there is a CS request pending
								}
							}else
							{
								minHeap.add(messageObj);
							}
						}						
						else
						{
							hostId = messageObj.nodeInfo.hostId;
							nodeMap.get(hostId).keyKnown = false;
							//currentNodeCSEnterTimestamp.incrementAndGet(); //Timestamp is getting incremented since it is RESPONSE_AND_REQUEST_KEY
							startRCClient(messageObj.nodeInfo, MessageType.RESPONSE_AND_REQUEST_KEY);   //Response_and_request key in case there is a CS request pending
						}
					}				
					else
					{
						//preEmptQueue.add(messageObj.nodeInfo);
						hostId = messageObj.nodeInfo.hostId;
						nodeMap.get(hostId).keyKnown = false;
						//nodeMap.get(hostId).isRequested = false;  //-Dont think this is required
						startRCClient(messageObj.nodeInfo, MessageType.RESPONSE_KEY);   //Response_and_request key in case there is a CS request pending
					}
				}else if(messageObj.messageType  == MessageType.RESPONSE_KEY)
				{
					//out.println("[INFO]	["+sTime()+"]	["+messageObj.messageType+"]	Node ID : "+nodeId+"	CurrentNode Timestamp : "+currentNodeCSEnterTimestamp.get()+"	Message Timestamp : "+messageObj.timeStamp+"	Got Response from Node Id "+messageObj.nodeInfo.hostId);

					hostId = messageObj.nodeInfo.hostId;
					nodeMap.get(hostId).keyKnown = true;
					nodeMap.get(hostId).isRequested = false;

					if(!isAllNodeKeysKnown())
					{
						;
					}

				}else if(messageObj.messageType == MessageType.RESPONSE_AND_REQUEST_KEY)
				{
					//out.println("[INFO]	["+sTime()+"]	["+messageObj.messageType+"]	Node ID : "+nodeId+"	CurrentNode Timestamp : "+currentNodeCSEnterTimestamp.get()+"	Message Timestamp : "+messageObj.timeStamp+"	Got Response and Request from Node Id "+messageObj.nodeInfo.hostId);

					//This case we don't have to compare the timestamp because the other node is sending 'RESPONSE_AND_REQUEST_KEY' which means the timestamp of other node is greater than the current
					minHeap.add(messageObj);
					hostId = messageObj.nodeInfo.hostId;
					nodeMap.get(hostId).keyKnown = true;
					nodeMap.get(hostId).isRequested = false;


				}else if(messageObj.messageType == MessageType.TERMINATION_MESSAGE)
				{

					//out.println("[INFO]	["+sTime()+"]	["+messageObj.messageType+"]	Node ID : "+nodeId+"	CurrentNode Timestamp : "+currentNodeCSEnterTimestamp.get()+"	Message Timestamp : "+messageObj.timeStamp+"	Got Termination message from Node Id "+messageObj.nodeInfo.hostId);
					hostId = messageObj.nodeInfo.hostId;
					nodeMap.get(hostId).isTerminated = true;

					hasAllTerminated = true;
					for(int i : nodeMap.keySet()){
						//System.out.println("[INFO]	["+sTime()+"]	Termination Check  :: Node: "+nodeMap.get(i).hostName);
						if(!nodeMap.get(i).isTerminated){
							hasAllTerminated = false;
							//break;
						}
					}


					if(hasAllTerminated)
					{
						if(!isTerminationSent)
						{
							if(count == noOfCriticalSectionRequests){
								while(isInCriticalSection);
								//out.println("[INFO]	["+sTime()+"]	Count Value - "+count+"   Total CS Requests = "+noOfCriticalSectionRequests);

								rCServer.sendTermination();
							}
						}
						else
						{
							System.out.println("[INFO]	["+sTime()+"]	Terminating Server at node "+nodeId);
							System.exit(0);
						}

					}


				}
				Thread.sleep(6000);
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


	public synchronized void requestAllKeys()
	{
		Host host;
		for(int nId : nodeMap.keySet())
		{
			host = nodeMap.get(nId);
			if(!host.keyKnown && nodeId != host.hostId && nodeMap.get(host.hostId).isRequested != true )
			{
				startRCClient(host, MessageType.REQUEST_KEY);
				nodeMap.get(host.hostId).isRequested=true;
			}
		}
	}

	public void sendTermination()
	{
		Host host = null;
		nodeMap.get(nodeId).isTerminated=true;
		Thread a[] = new Thread[nodeMap.keySet().size()];
		int index=0;
		//currentNodeCSEnterTimestamp.incrementAndGet();

		for(int nId : nodeMap.keySet())
		{
			if(nId != nodeId)
			{
				host = nodeMap.get(nId);
				RCClient  rCClient;
				Message message;
				//out.println("[INFO]	["+sTime()+"]	Node Id "+nodeId+"  Starting the client to send termination message to "+host.hostName+" at port "+host.hostPort);

				message = new Message(currentNodeCSEnterTimestamp, MessageType.TERMINATION_MESSAGE, nodeMap.get(nodeId));
				rCClient = new RCClient(host, message);
				a[index]= new Thread(rCClient);
				a[index].start();
				index++;
			}
			//startRCClients(minHeap, MessageType.TERMINATION_MESSAGE);
		}

		Boolean isAllDone = true;
		for(int i : nodeMap.keySet()){
			if(i != nodeId)
			{
				//out.println("[INFO]	["+sTime()+"]	Node: "+nodeMap.get(i).hostName);
				if(!nodeMap.get(i).isTerminated){
					isAllDone = false;
				}
			}
		}
		if(isAllDone)
		{
			for(int j=0;j<a.length;j++)
			{

				while(a[j] != null && a[j].isAlive());
			}
			System.out.println("[INFO]	["+sTime()+"]	Terminating Server at node "+nodeId);
			System.exit(0);
		}
		isTerminationSent = true;

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

	public synchronized void startRCClient(Host host, MessageType sMessageType)
	{		
		if(host.hostId != nodeId)
		{
			if(sMessageType == MessageType.RESPONSE_AND_REQUEST_KEY && nodeMap.get(host.hostId).isRequested == true)
			{
				sMessageType = MessageType.RESPONSE_KEY; 
			}

			if(sMessageType == MessageType.REQUEST_KEY && (nodeMap.get(host.hostId).isRequested == true || nodeMap.get(host.hostId).keyKnown == true) )
			{
				return;
			}
			
			if(sMessageType == MessageType.REQUEST_KEY)
			{
				nodeMap.get(host.hostId).isRequested = true;
			}
			RCClient  rCClient;
			Message message;
			//out.println("[INFO]	["+sTime()+"]	Node Id "+nodeId+"  Starting the client to request for a key to "+host.hostName+" at port "+host.hostPort);

			message = new Message(currentNodeCSEnterTimestamp, sMessageType, nodeMap.get(nodeId));
			rCClient = new RCClient(host, message);
			new Thread(rCClient).start();
			//System.out.println("[INFO]	["+sTime()+"]	Node Id "+nodeId+"  client requested for a key to "+host.hostName);
		}

	}

	public synchronized void startRCClients(PriorityQueue<Message> minHeap, MessageType sMessageType)
	{		
		int size = minHeap.size();
		//out.println("Min Heap Size : "+size);
		int nNumOfThreads=size;
		Thread[] tThreads = new Thread[nNumOfThreads];
		RCClient  rCClient;
		Message message;

		//Already known hosts - MAY BE USEFUL FOR LIST OF GOT RESPONSE NODES
		/*ArrayList<Host> currentAdjList = new ArrayList<Host>();
		for(int nodeID : nodeMap.keySet())
		{
			currentAdjList.add(nodeMap.get(nodeID));
		}*/
		int i=0;
		currentNodeCSEnterTimestamp.incrementAndGet();

		while(minHeap.size()>0)
		{
			//System.out.println("Size inside while loop : "+size);
			Message m = minHeap.poll();
			if(m!=null)
			{
				Host host = m.nodeInfo;
				//out.println("[INFO]	["+sTime()+"]	WAITING-LIST	Node Name : "+host.hostName);
				if (nodeId!=host.hostId)
				{

					if(sMessageType == MessageType.REQUEST_KEY && nodeMap.get(host.hostId).keyKnown == true)
					{
						return;
					}
					//Increment the count only on requests
					if(sMessageType == MessageType.RESPONSE_KEY || sMessageType == MessageType.RESPONSE_AND_REQUEST_KEY)
					{
						nodeMap.get(host.hostId).keyKnown = false;
						if(sMessageType == MessageType.RESPONSE_AND_REQUEST_KEY)
						{
							nodeMap.get(host.hostId).isRequested=true;
						}
					}
					if(sMessageType == MessageType.REQUEST_KEY)
					{
						nodeMap.get(host.hostId).isRequested = true;
					}
					message = new Message(currentNodeCSEnterTimestamp, sMessageType, nodeMap.get(nodeId));
					//System.out.println("[INFO]	["+sTime()+"]	Message Type : "+message.messageType);

					rCClient = new RCClient(host, message);
					tThreads[i] = new Thread(rCClient);
					tThreads[i].start();
					i++;
				}
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