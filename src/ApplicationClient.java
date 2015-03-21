/**
 * 
 */
package com.aos.rcprotocol;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;

/**
 * @author Rahul
 *
 */
public class ApplicationClient {
	//Hardcoded for now
	//TODO: Set the below variables from the config file
	private static int noOfCriticalSectionRequests;
	private static int meanDelayInCriticalSection;
	private static int durationOfCriticalSection;

	public void csEnter()
	{
		try
		{
			for(int i=0;i<=noOfCriticalSectionRequests;i++)
			{
				//TODO: call server csEnter

				csExecute();
				Thread.sleep(meanDelayInCriticalSection);
			}
		} catch(Exception e)
		{
			e.printStackTrace();
		}

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
				System.out.println("Yes, one or more violations were found");
			}
			else
			{
				long startTime = System.currentTimeMillis();
				long currentTime = System.currentTimeMillis();
				BufferedOutputStream bw = new BufferedOutputStream(out);
				Integer lineNumber = 0;
				while(currentTime-startTime<durationOfCriticalSection)
				{
					bw.write(lineNumber.toString().getBytes());
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
				lock.release();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				csLeave();
			}

		}
	}
	
	public void csLeave()
	{
		//TODO: call server csLeave
	}
}
