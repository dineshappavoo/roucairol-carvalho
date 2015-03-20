import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

/**
 * @author Dany
 *
 */
public class RoucairolCarvalho {

	private static HashMap<Integer, Host> nodeMap;
	private static int noOfNodes;
	//private static int nodeId;

	public static HashMap<Integer, Host> constructGraph(String fileName, int nodeId) throws FileNotFoundException
	{
		nodeMap = new HashMap<Integer, Host>();
		File file = new File(fileName);
		Scanner scanner=new Scanner(file);
		int u, v;
		int hostId, hostPort;
		String hostName="";
		String checker="";

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
							nodeMap.put(hostId, new Host(hostId, hostName, hostPort, false));
						}
					}
				}
			}
		}
		printNodeMap();
		return nodeMap;	
	}
	
	public static void printNodeMap()
	{
		Host host;
		for(int nodeId : nodeMap.keySet())
		{
			host = nodeMap.get(nodeId);
			System.out.println("Host Id "+nodeId+"  Name : "+host.hostName+"  port : "+host.hostPort);
		}
	}
	
	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		HashMap<Integer, Host> nMap = RoucairolCarvalho.constructGraph("/Users/Dany/Documents/FALL-2013-COURSES/Imp_Data_structures/workspace/roucairol-carvalho/src/config.txt", 0);

	}

}
