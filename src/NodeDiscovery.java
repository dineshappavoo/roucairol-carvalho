/**
 * 
 */


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * @author Dinesh Appavoo
 *
 */
public class NodeDiscovery{

	private int noOfVertices;
	private static HashMap<Integer, Host> nodeMap;
	private static int nodeId;
	private static int nWaitingForResponseCount;

	public void initiateDiscovery()
	{
		SctpServer sctpServer = new SctpServer(nodeMap, nodeId, nWaitingForResponseCount);

		new Thread(sctpServer).start();
	}

	public HashMap<Integer, Host> constructGraph(String fileName, int nodeId) throws FileNotFoundException
	{
		nodeMap = new HashMap<Integer, Host>();
		File file = new File(fileName);
		Scanner scanner=new Scanner(file);
		int noOfEdges;
		int u, v;
		int hostId, hostPort;
		String hostName="";
		String checker="";

		while(scanner.hasNext())
		{
			if((checker=scanner.next()).equals("p") && (!(checker.equals("#"))))
			{
				noOfVertices=scanner.nextInt();
				noOfEdges=scanner.nextInt();
				for(int i=0;i<noOfEdges;i++)
				{
					if((checker=scanner.next()).equals("e"))
					{
						u=scanner.nextInt();
						v=scanner.nextInt();
						//System.out.println("u: "+u+" v : "+v);
						if (u == nodeId)
						{
							//System.out.println("u: "+u+" v : "+v);
							nodeMap.put(v, new Host());
						}
					}
				}

				//System.out.println(nodeMap.toString());
				//To add the node information from config file [FORMAT : n  0	dc01		3332]
				for(int j=0;j<noOfVertices;j++)
				{
					if((checker=scanner.next()).equals("n")){
						hostId = scanner.nextInt();
						hostName = scanner.next()+".utdallas.edu";
						hostPort = scanner.nextInt();

						if(nodeMap.get(hostId)!=null || hostId == nodeId)
						{
							nodeMap.put(hostId, new Host(hostId, hostName, hostPort));
						}
					}
				}
			}
		}
		return nodeMap;
	}

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {

		if(args.length > 0) {
			nodeId = Integer.parseInt(args[0]);
		}
		NodeDiscovery sctpClientServer = new NodeDiscovery();		

		//Scanner scanner = new Scanner(System.in);
		//nodeId = scanner.nextInt();

		HashMap<Integer, Host> nMap = sctpClientServer.constructGraph("/home/004/d/dx/dxa132330/advanced-operating-system/projects/node-discovery/config.txt", nodeId);
		//System.out.println("INITIAL WAITING COUNT : "+nWaitingForResponseCount);
		sctpClientServer.initiateDiscovery();
	}

}
