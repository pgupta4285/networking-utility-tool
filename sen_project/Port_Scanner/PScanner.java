//NOT THAT WHICH IS REQUIRED
//ITSOVER.


import java.net.*;
import java.io.IOException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

    public class PScanner {
    
         public static void main(String[] args) {
         InetAddress ia=null;
         String host=null;
             try {
            
             host=JOptionPane.showInputDialog("Enter the Host name to scan:\n example: zzz.com");
                 if(host!=null){
                 ia = InetAddress.getByName(host);
             scan(ia); }
         }
             catch (UnknownHostException e) {
             System.err.println(e );
         }
         System.out.println("Bye from NFS");
         //System.exit(0);
     }
    
        public static void scan(final InetAddress remote) {
        //variables for menu bar
        
        int port=0;
        String hostname = remote.getHostName();
        
             for ( port = 0; port < 65536; port++) {
                 try {
                 Socket s = new Socket(remote,port);
                 System.out.println("Server is listening on port " + port+ " of " + hostname);
                 s.close();
             }
                 catch (IOException ex) {
                 // The remote host is not listening on this port
                 System.out.println("Server is not listening on port " + port+ " of " + hostname);
             }
         }//for ends
     }
}

