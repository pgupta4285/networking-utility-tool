import java.io.*; import java.util.*; import java.net.*;
public class Finger
{
	public static void main(String args[])
	{
		String user, host;
		if ((args.length==1)&&(args[0].indexOf('@')>-1))
		{
                        StringTokenizer split=new StringTokenizer(args[0],"@");
			user=split.nextToken(); host=split.nextToken();
		}
		else {System.out.println("Usage: java Finger user@host");return;}
		try
		{
			Socket digit=new Socket(host,79); //port reserved for finger
			digit.setSoTimeout(20000); //give up after 20 seconds
			PrintStream out=new PrintStream(digit.getOutputStream());
			out.print(user+"\015\012");
			BufferedReader in = new BufferedReader(
				new InputStreamReader(digit.getInputStream()));
			boolean eof=false;
			while (eof)
			{
				String line=in.readLine();
				if (line!=null) {System.out.println(line);}
				else {eof=true;}
			}
			digit.close();
		}
		catch (IOException e)
		{System.out.println("IO Error: "+e.getMessage());}
	}
}
