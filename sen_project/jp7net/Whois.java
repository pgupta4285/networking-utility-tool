import java.io.*; import java.net.*;
public class Whois
{
	public static void main(String args[]) throws Exception
	{
                int c=-1; //default to no data to display
                Socket s = new Socket("whois.internic.net",43); //port reserved for whois
                s.setSoTimeout(20000); //give up after 20 seconds
		InputStream in = s.getInputStream();
		OutputStream out = s.getOutputStream();
		if (args.length==0)
			{System.out.println("Usage: java Whois hostname");return;}
		String host = args[0] + "\n";
		byte buf[] = host.getBytes();
                out.write(buf);
		while ((c=in.read())!=-1) {System.out.print((char)c);}
		s.close();
	}
}
