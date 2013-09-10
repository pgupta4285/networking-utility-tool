import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.channels.DatagramChannel; 

/**
*Thread to scan UDP port on host
*
*Copyright (C) 2003 Tom Salmon tom@slashtom.org
*
*This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; version 2.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*
*@author Tom Salmon tom@slashtom.org
*@version 0.3.1
*/

public class ScanUDP extends Observable implements Runnable {
	private InetAddress IP;
	private int port;

	/**
	*Constructs thread to scan a UDP port
	*@param InetAddress address of host to scan
	*@param int port number to scan
	*/
	public ScanUDP(InetAddress IP, int port){
		this.IP = IP;
		this.port = port;
	}

    /**
    *Returns the IP address being scanned
    */
    public InetAddress getIP(){
        return IP;
    }

    /**
    *Return the port being scanned
    */
    public int getPort(){
        return port;
    }

	public void run(){
		String portStatus = this.scanUDP();
		setChanged();
		notifyObservers(portStatus);
	}

    /**
    *Scans single UDP port on host/port
    *@return String either OPEN CLOSED
    */   
    protected String scanUDP(){
        DatagramSocket ds;
        DatagramPacket dp;
        DatagramChannel dChannel;
        try{
            byte [] bytes = new byte[128];
            ds = new DatagramSocket();
            dp = new DatagramPacket(bytes, bytes.length, IP, port);
            dChannel = DatagramChannel.open();
            dChannel.connect(new InetSocketAddress(IP, port));
            dChannel.configureBlocking(true);
            ds = dChannel.socket();
            ds.setSoTimeout(1000);
            ds.send(dp);
            dp = new DatagramPacket(bytes, bytes.length);
            Thread.sleep(1000);
            ds.receive(dp);

            //check datagram channel still connected
            if (!dChannel.isConnected() || !dChannel.isOpen()){
                ds.close();
                return "CLOSED";
            }

            ds.disconnect();
            dChannel.disconnect();
            dChannel.close();
            ds.close();
        }
        catch(PortUnreachableException e){
            return "CLOSED";
        }
        catch(InterruptedIOException e){
            return "CLOSED";
        }
        catch(IOException e){
            return "CLOSED";
        }
        catch(Exception e){
            return "CLOSED";
        }
        return "OPEN";
    }
}
