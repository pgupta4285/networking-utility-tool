import java.net.*;
import java.io.*;
import java.util.*;

/**
*Thread to scan TCP port on host
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

public class ScanTCP extends Observable implements Runnable {
	private InetAddress IP;
	private int port;
	private int timesDropped = 0;
	private int timesClosed = 0;
	private static int SOCKET_TIMEOUT = 2000;
	private static int MAX_TIMES_DROPPED = 3;
	private static int MAX_TIMES_CLOSED = 2;

	/*
	*Constructs new thread to Scan host on TCP port
	*@param InetAddress the IP address of host to scan
	*@param int port number to scan
	*/
	public ScanTCP(InetAddress IP, int port){
		this.IP = IP;
		this.port = port;

		// if we are running on Windows, increase timeout and no. of retries
		if (System.getProperty("os.name").startsWith("Windows")){
			this.SOCKET_TIMEOUT = 2500;
			this.MAX_TIMES_DROPPED = 4;
			this.MAX_TIMES_CLOSED = 3;
		}
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

	/**
	*Scans host/port using the TCP protocol
	*/
	public void run() {
        try{
			String portsStatus = this.scanTCP();
			setChanged();
			notifyObservers(portsStatus);
        }
        catch(NoRouteToHostException e){
			setChanged();
            notifyObservers("null"); //null to signify error contacting host
			return;
        }
	}

    /**
    *Scans single port using specified TCP hoost/port
    *@return String - either OPEN CLOSED DROPPED
    */
    public String scanTCP() throws NoRouteToHostException{
        try{
            Socket s = new Socket();

			//set the socket to timeout, multiply the timeout by number of retry
            s.connect(new InetSocketAddress(IP, port), 
					  this.SOCKET_TIMEOUT * ((timesDropped | timesClosed)+1));
            s.close();
        }
        catch(NoRouteToHostException e){
            throw e; //throw to calling
        }
        catch(SocketTimeoutException e){
			/*
			* Sometimes a socket times out in error (due to intensive scanning)
			*check again, to make sure
			*/
			if (this.timesDropped < this.MAX_TIMES_DROPPED){
				this.timesDropped++;
				try{
					Thread.sleep(2500); // wait before trying again
				}
				catch(Exception f){
				}
				// scan again!
				return this.scanTCP();
			}
			else{
            	return "DROPPED";
			}
        }
        catch(IOException e){
			if (this.timesClosed < this.MAX_TIMES_CLOSED){
				this.timesClosed++;
				try{
					Thread.sleep(2500);
				}
				catch(Exception f){
				}
				return this.scanTCP();
			}
			else{
            	return "CLOSED";
			}
        }
        //if got this far, is opem
        return "OPEN";
    }

}
