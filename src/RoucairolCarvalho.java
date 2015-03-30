import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Dany
 *
 */
public class RoucairolCarvalho {

	private static int noOfNodes;
	protected static int nodeId;
	private static Random rand;//= new Random();
	protected static int noOfCriticalSectionRequests;
	private static int meanDelayInCriticalSection;
	private static int durationOfCriticalSection;
	protected static Boolean isTerminationSent = false;

	protected static HashMap<Integer, Host> nodeMap;
	protected static int nWaitingForTerminationResponseCount=0;
	protected static boolean isInCriticalSection = false;
	protected static boolean requestForCriticalSection = false;
	protected static PriorityQueue<Message> minHeap = getPriorityQueue();
	protected static AtomicInteger currentNodeCSEnterTimestamp = new AtomicInteger(0);
	protected static int count = 0;
	protected static  ApplicationClient oAppClient;
	static RCServer rCServer = new RCServer();
	public void startServer()
	{
		//rCServer = new RCServer();
		new Thread(rCServer).start();
		oAppClient = new ApplicationClient(noOfCriticalSectionRequests, meanDelayInCriticalSection, durationOfCriticalSection);
		new Thread(oAppClient).start();
	}

	public void cs_enter()
	{
		System.out.println("[INFO]	["+sTime()+"]	Node Id "+nodeId+"  Request arrived for Entering into Critical Section");
		requestForCriticalSection = true;

		//currentNodeCSEnterTimestamp.incrementAndGet();	
		rCServer.requestAllKeys();
		while(!rCServer.isAllNodeKeysKnown())
		{
			try {
				Thread.sleep(2000);
			}catch(InterruptedException ex)
			{
				ex.printStackTrace();
			}
		}
		//System.out.println("[INFO]	["+sTime()+"]	****=====?????	|	***************************	| 	?????=====****");
		//System.out.println("[INFO]	["+sTime()+"]	****=====	NODE "+nodeId+" IS GETTING INTO CRITICAL SECTION	=====****");
		isInCriticalSection = true;
		displayCSMessage(nodeId);
		return;
	}

	public void cs_leave()
	{
		System.out.println("[INFO]	["+sTime()+"]	Node Id "+nodeId+"  Request arrived to leave from Critical Section");
		//make the isInCriticalSection boolean as false
		isInCriticalSection = false;
		requestForCriticalSection = false;
		rCServer.startRCClients(minHeap, MessageType.RESPONSE_KEY);
		minHeap = getPriorityQueue();
		System.out.println("[INFO]	["+sTime()+"]	Node Id "+nodeId+"  left Critical Section");
		count++;
		System.out.println("[INFO]	["+sTime()+"]	Count Value - "+count+"   Total CS Requests = "+noOfCriticalSectionRequests);
		if(count == noOfCriticalSectionRequests ){
			rCServer.sendTermination();
		}
		return;
	}
	
	public void displayCSMessage(int nodeId)
	{
		System.out.print("[INFO]	["+sTime()+"]\t");

		for(int i=0;i<noOfNodes;i++)
		{
			if(i!=nodeId)
			{
				System.out.print("|\t");
			}else
			{
				System.out.print("|  "+nodeId+" CS ");
			}
		}
		System.out.print("|");
		System.out.println();
	}


	public static PriorityQueue<Message> getPriorityQueue()
	{
		PriorityQueue<Message> queue = new PriorityQueue<Message>(11, new Comparator<Message>()
				{
			public int compare(Message o1, Message o2)
			{
				AtomicInteger t1 = o1.timeStamp;
				AtomicInteger t2 = o2.timeStamp;
				if(t1.get()>=t2.get())
					return 1;
				else
					return -1;
			}
				}
				);
		return queue;	
	}


	public HashMap<Integer, Host> constructGraph(String fileName, int nodeId) throws FileNotFoundException
	{
		nodeMap = new HashMap<Integer, Host>();
		rand = new Random();
		File file = new File(fileName);
		Scanner scanner=new Scanner(file);
		int hostId, hostPort;
		String hostName="";
		String checker="";

		//To Generate random numbers between a range from 0 to 9
		int start = randInt(0, 9);
		int end = randInt(0, 9);		
		int startNode = start<=end?start:end;
		int endNode = start>end?start:end;
		//System.out.println("Start : "+startNode+" End : "+endNode);

		//Read input from config file
		while(scanner.hasNext())
		{
			if((checker=scanner.next()).equals("p") && (!(checker.equals("#"))))
			{
				noOfNodes=scanner.nextInt();
				//To add the node information from config file [FORMAT : n  0	dc01		3332]
				for(int j=0;j<noOfNodes;j++)
				{
					if((checker=scanner.next()).equals("n")){
						hostId = scanner.nextInt();
						hostName = scanner.next()+".utdallas.edu";
						hostPort = scanner.nextInt();

						if(nodeMap.get(hostId)==null)// || hostId != nodeId)
						{
							if(hostId<=endNode && hostId >=startNode)
							{
								nodeMap.put(hostId, new Host(hostId, hostName, hostPort, false, false));
							}else
							{
								nodeMap.put(hostId, new Host(hostId, hostName, hostPort, false, false));
							}
						}
					}
				}

				if((checker=scanner.next()).equals("cscount") && (!(checker.equals("#"))))
				{
					noOfCriticalSectionRequests = scanner.nextInt();
				}

				if((checker=scanner.next()).equals("meandelay") && (!(checker.equals("#"))))
				{
					meanDelayInCriticalSection = scanner.nextInt();
				}

				if((checker=scanner.next()).equals("duration") && (!(checker.equals("#"))))
				{
					durationOfCriticalSection = scanner.nextInt();
				}
			}
		}
		//printNodeMap();
		return nodeMap;	
	}

	//For Testing - Not used
	public void getRange()
	{
		int start = randInt(0, 9);
		int end = randInt(0, 9);		
		int startNode = start<=end?start:end;
		int endNode = start>end?start:end;
		System.out.println("Start : "+startNode+" End : "+endNode);
	}

	public static int randInt(int min, int max) {

		// NOTE: Usually this should be a field rather than a method
		// variable so that it is not re-seeded every call.
		rand = new Random();

		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		int randomNum = rand.nextInt((max - min) + 1) + min;

		return randomNum;
	}

	public String sTime()
	{
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		return timeStamp;
	}

	public void printNodeMap()
	{
		Host host;
		for(int nodeId : nodeMap.keySet())
		{
			host = nodeMap.get(nodeId);
			System.out.println("[INFO]	["+sTime()+"]	Host Id "+nodeId+"  Name : "+host.hostName+"  port : "+host.hostPort+"  Key Known?  "+host.keyKnown);
		}
	}

	public void simulateRoucairolCarvalho() throws FileNotFoundException
	{
		//HashMap<Integer, Host> nMap = constructGraph("/Users/Dany/Documents/FALL-2013-COURSES/Imp_Data_structures/workspace/roucairol-carvalho/src/config.txt", nodeId);
		HashMap<Integer, Host> nMap = constructGraph("config.txt", nodeId);

		//System.out.println("[INFO]	["+sTime()+"]	No Of CS : "+noOfCriticalSectionRequests+"  Mean Delay : "+meanDelayInCriticalSection+"  Duration Of CS : "+durationOfCriticalSection);
		startServer();
	}

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		if(args.length > 0) {
			nodeId = Integer.parseInt(args[0]);
		}
		RoucairolCarvalho rcObject = new RoucairolCarvalho();
		rcObject.simulateRoucairolCarvalho();
	}

}
