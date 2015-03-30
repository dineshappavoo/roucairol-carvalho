import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * 
 */

/**
 * @author Rahul
 *
 */
public class TestModule {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) {

		try 
		{
			TreeMap<Long, String> treeMap = new TreeMap<Long, String>();
			File file = new File("config.txt");
			Scanner scanner_1=new Scanner(file);
			int noOfNodes = 0;
			String current ="";
			//Read number of nodes from config file
			while(scanner_1.hasNext())
			{
				if((current=scanner_1.next()).equals("p") && (!(current.equals("#"))))
				{
					noOfNodes=scanner_1.nextInt();
					break;
				}
			}

			scanner_1.close();
			int i=0;
			//Read through all the log files and put the lines in a TreeMap with the timestamp as key
			while(i<noOfNodes)
			{
				File logFile = new File("node"+i+".log");
				Scanner scanner_2 =null;
				try {
					scanner_2 = new Scanner(logFile);

					while(scanner_2.hasNext())
					{
						String currentLine = scanner_2.next();
						String[] keyValue = currentLine.split(":");
						treeMap.put(Long.valueOf(keyValue[0]), keyValue[1]);
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} finally {
					if(scanner_2 != null)
					{
						scanner_2.close();
					}
				}
				i++;

			}
			String prevValue = null;
			/**
			 * TreeMap sorts the entries based on the keys.
			 * Check if there are overlapping start and end pairs.
			 * If there are overlapping pairs, then there has been a violation of the protocol.
			 */

			int j=1;
			//treeMap.values() will give the values in ascending order of the keys (from Javadoc).
			for(String value : treeMap.values())
			{
				String[] currentValues;
				if(prevValue != null && j%2==0)
				{
					currentValues = value.split("-");
					String[] previous = prevValue.split("-");
					if(previous[0].equals(currentValues[0]) )
					{
						if(previous[1].equalsIgnoreCase("Start") 
								&& currentValues[1].equalsIgnoreCase("End"))
						{
							prevValue = null;
							j++;
							continue;
						}
					}
					else if ((previous[1].equalsIgnoreCase("Start") 
							&& currentValues[1].equalsIgnoreCase("Start")) || (previous[1].equalsIgnoreCase("End") 
									&& currentValues[1].equalsIgnoreCase("End"))) 
					{
						System.out.println("Yes, one or more violations found!");
						return;
					}
					else if (previous[1].equalsIgnoreCase("Start") 
							&& currentValues[1].equalsIgnoreCase("End"))
					{
						System.out.println("Yes, one or more violations found!");
						return;
					}
				}
				prevValue = value;
				j++;
			}
			System.out.println("No violations found!");
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
