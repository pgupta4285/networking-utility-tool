import java.net.*;

 public class Scanner

{

        public static void main(String args[])
       {
        int startPortRange=0;
        int stopPortRange=0;
        startPortRange = Integer.parseInt(args[0]);
        stopPortRange = Integer.parseInt(args[1]);

        for(int i=startPortRange; i <=stopPortRange; i++)
       {
                       try
                       {
                        Socket ServerSok = new Socket("127.0.0.1",i);

                 System.out.println("Port in use: " + i );

                        ServerSok.close();
                       }
                catch (Exception e)
                       {
                       }
        System.out.println("Port not in use: " + i );
       }
       }
}
