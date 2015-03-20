import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

/**
 * @author Dany
 *
 */
public class RoucairolCarvalho {

	private static HashMap<Integer, Host> nodeMap;
	private static int noOfNodes;
	//private static int nodeId;
	private static Random rand;//= new Random();


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
		System.out.println("Start : "+startNode+" End : "+endNode);

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

						if(nodeMap.get(hostId)!=null || hostId != nodeId)
						{
							if(hostId<=endNode && hostId >=startNode)
							{
								nodeMap.put(hostId, new Host(hostId, hostName, hostPort, true));
							}else
							{
								nodeMap.put(hostId, new Host(hostId, hostName, hostPort, false));
							}
						}
					}
				}
			}
		}
		printNodeMap();
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

	public void printNodeMap()
	{
		Host host;
		for(int nodeId : nodeMap.keySet())
		{
			host = nodeMap.get(nodeId);
			System.out.println("Host Id "+nodeId+"  Name : "+host.hostName+"  port : "+host.hostPort+"  Key Known?  "+host.keyKnown);
		}
	}

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		HashMap<Integer, Host> nMap = new RoucairolCarvalho().constructGraph("/Users/Dany/Documents/FALL-2013-COURSES/Imp_Data_structures/workspace/roucairol-carvalho/src/config.txt", 5);

	}

}
