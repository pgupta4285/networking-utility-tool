import java.io.*; import java.util.*; import java.net.*;

public class TimeServer extends Thread
{
	private ServerSocket sock;
	public TimeServer()
	{
		super();
		try
		{
			sock=new ServerSocket(4415); // private service port
			System.out.println("TimeServer running...");
		}
		catch (IOException e)
		{System.out.println("Error: Can't create socket");System.exit(1);}
	}
	public void run()
	{
		Socket client=null;
		while (true)
		{
			if (sock==null) {return;}
			try
			{
			client=sock.accept();
			BufferedOutputStream bos=new BufferedOutputStream(
				client.getOutputStream());
			PrintWriter os = new PrintWriter(bos,false);
			String outline; Date now=new Date();
			os.println(now);os.flush();os.close();client.close();
			}
  			catch (IOException e)
			{System.out.println("Error: Couldn't connect");System.exit(1);}
		}
	}
	public static void main(String args[])
	{TimeServer server = new TimeServer(); server.start();}
}