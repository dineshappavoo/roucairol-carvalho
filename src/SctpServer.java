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
import java.util.ArrayList;
import java.util.HashMap;
public class SctpServer implements Runnable{
	public static final int MESSAGE_SIZE = 1000;


	private HashMap<Integer, Host> nodeMap;
	private int nodeId;
	private int nWaitingForResponseCount;

	public SctpServer(HashMap<Integer, Host> nodeMap, int nodeId, int nWaitingForResponseCount)
	{
		this.nodeMap = nodeMap; 
		this.nodeId = nodeId;
		this.nWaitingForResponseCount = nWaitingForResponseCount;
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
			//Create a socket addess in the current machine at port 5000
			InetSocketAddress serverAddr = new InetSocketAddress(nodeMap.get(nodeId).hostPort);
			//Bind the channel's socket to the server in the current machine at port 5000
			sctpServerChannel.bind(serverAddr);

			System.out.println("SERVER STARTED");

			Thread.sleep(5000);

			//Initial discovery for its own adjacency list
			ArrayList<Host> initialAdjList = new ArrayList<Host>();
			for(int nodeID : nodeMap.keySet())
			{
				initialAdjList.add(nodeMap.get(nodeID));
			}
			startDiscovery(initialAdjList, "REQUEST");

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

				for(Host host : messageObj.knownHosts)
				{
					System.out.println("Message Type : "+messageObj.messageType+" From : "+messageObj.nodeInfo.hostName+" Known Host : "+ host.hostName);

				}
				
				System.out.println("WAITING COUNT : "+nWaitingForResponseCount);

				Thread.sleep(8000);

				ArrayList<Host> knownNodes = messageObj.knownHosts;
				ArrayList<Host> newHosts = new ArrayList<Host>();

				//To identify the new hosts from the request/response
				for(Host oHost : knownNodes)
				{
					if(!nodeMap.containsKey(oHost.hostId))
					{
						nodeMap.put(oHost.hostId, oHost);
						newHosts.add(oHost);
					}
				}
				//System.out.println("SET OF NEW HOST : "+newHosts.size());

				if(messageObj.messageType.equals("RESPONSE"))
				{
					nWaitingForResponseCount--;
					startDiscovery(newHosts, "REQUEST");

				}else if(messageObj.messageType.equals("REQUEST"))
				{
					Host host = messageObj.nodeInfo;
					startDiscovery(newHosts, "REQUEST");

					//Reponse to the node. Only one node. To utilize the function which exists already we used arraylist
					ArrayList<Host> responseNode = new ArrayList<Host>();
					responseNode.add(host);
					startDiscovery(responseNode, "RESPONSE");
				}

				//Verify whether we found all nodes in this network
				System.out.println("WAITING COUNT FINAL: "+nWaitingForResponseCount);

				if(nWaitingForResponseCount==0)
				{
					System.out.println("DISCOVERED ALL NODES IN THE NETWORK");
					writeOutputToFile();
					System.exit(0);
				}
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

	public void startDiscovery(ArrayList<Host> hostList, String sMessageType)
	{		
		int size = hostList.size();
		int nNumOfThreads=size;
		Thread[] tThreads = new Thread[nNumOfThreads];
		SctpClient  sctpClient;
		Message message;

		//Already known hosts
		ArrayList<Host> currentAdjList = new ArrayList<Host>();
		for(int nodeID : nodeMap.keySet())
		{
			currentAdjList.add(nodeMap.get(nodeID));
		}

		for(int i=0;i<size;i++)
		{
			if (nodeId!=hostList.get(i).hostId)
			{
				//System.out.println("CLIENT CALL TYPE : "+sMessageType);
				//Increment the count only on requests
				if(sMessageType.equals("REQUEST"))
				{
					nWaitingForResponseCount++;
				}
				message = new Message(sMessageType, nodeMap.get(nodeId), currentAdjList);
				sctpClient = new SctpClient(hostList.get(i), message);
				tThreads[i] = new Thread(sctpClient);
				tThreads[i].start();
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