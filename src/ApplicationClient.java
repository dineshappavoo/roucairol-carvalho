/**
 * 
 */
import java.awt.Toolkit;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author Rahul
 *
 */
public class ApplicationClient implements Runnable{
	//Hardcoded for now
	//TODO: Set the below variables from the config file
	private int noOfCriticalSectionRequests = 1;
	private int meanDelayInCriticalSection = 10000;
	private int durationOfCriticalSection = 10000;
	private RoucairolCarvalho rcObj = new RoucairolCarvalho();

	public ApplicationClient(int noOfCriticalSectionRequests, int meanDelayInCriticalSection, int durationOfCriticalSection)
	{
		//this.rCServer = rCServer;
		this.noOfCriticalSectionRequests = noOfCriticalSectionRequests;
		this.meanDelayInCriticalSection = meanDelayInCriticalSection;
		this.durationOfCriticalSection = durationOfCriticalSection;
	}

	public void csEnterInitiate()
	{
		try
		{	
			Thread.sleep(10000);

			for(int i=0;i<noOfCriticalSectionRequests;i++)
			{
				//TODO: call server csEnter
				rcObj.cs_enter();
				csExecute();
				rcObj.cs_leave();
				Thread.sleep(meanDelayInCriticalSection);

			}
		} catch(Exception e)
		{
			e.printStackTrace();
		}

	}

	public String sTime()
	{
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		return timeStamp;
	}

	public void csExecute()
	{
		File file = new File("temp.txt");
		FileOutputStream out = null;
		FileLock lock = null;
		try
		{
			out = new FileOutputStream(file);
			lock = out.getChannel().tryLock();
			if (lock == null)
			{
				int i=0;
				System.out.println("[FATAL]	["+sTime()+"]	Yes, one or more violations were found");
				while(i<10)
				{
					Toolkit.getDefaultToolkit().beep();
					i++;
				}
			}
			else
			{
				//System.out.println("[INFO]	["+sTime()+"]	****=====?????	|	NODE IS IN CRITICAL SECTION	| 	?????=====****");
				//System.out.println("[INFO]	["+sTime()+"]	****=====?????	|	***************************	| 	?????=====****");


				long startTime = System.currentTimeMillis();
				long currentTime = System.currentTimeMillis();
				BufferedOutputStream bw = new BufferedOutputStream(out);
				Integer lineNumber = 0;
				while(currentTime-startTime<durationOfCriticalSection)
				{
					bw.write(lineNumber.toString().getBytes());
					Thread.sleep(1000);
					currentTime = System.currentTimeMillis();
				}
				bw.close();
			}
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try {

				if(lock != null && lock.isValid())
				{
					lock.release();
				}
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void csLeave()
	{
		//TODO: call server csLeave
		rcObj.cs_leave();

	}

	public void run()
	{
		csEnterInitiate();
	}
}
