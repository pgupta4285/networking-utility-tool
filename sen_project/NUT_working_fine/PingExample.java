import java.net.InetAddress;
 
public class PingExample
{
    public static void main(String[] args)
    {
        try
        {
            InetAddress address = InetAddress.getByName(args[0]);
 
            // Try to reach the specified address within the timeout
            // periode. If during this periode the address cannot be
            // reach then the method returns false.
            boolean reachable = address.isReachable(10000);
 
            System.out.println("Is host reachable? " + reachable);
        } catch (Exception e)
        {
          e.printStackTrace();
        }
    }
}
