import java.net.*;
import java.io.IOException;


public class PortScanner {

  public static void main(String[] args) {

    for (int i = 0; i < args.length; i++) {
      try {
        InetAddress ia = InetAddress.getByName(args[i]);
        scan(ia);
      }
      catch (UnknownHostException ex) {
        System.err.println(args[i] + " is not a valid host name.");
      }
    }

  }

  public static void scan(InetAddress remote) {

    // Do I need to synchronize remote?
    // What happens if someone changes it while this method
    // is running?

    String hostname = remote.getHostName();
    for (int port = 0; port < 65536; port++) {
      try {
        Socket s = new Socket(remote, port); 
        System.out.println("A server is listening on port " + port
         + " of " + hostname);
        s.close();
      }
      catch (IOException ex) {
        // The remote host is not listening on this port
      }
    }

  }

  public static void scan(String remote) throws UnknownHostException {

    // Why throw the UnknownHostException? Why not catch it like I did
    // in the main() method?
    InetAddress ia = InetAddress.getByName(remote);
    scan(ia);

  }

}
