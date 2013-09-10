import java.net.*;
import java.util.*;

/**
*Thread to scan individual hosts
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

public class ScanHost extends Observable implements Observer, Runnable {
	protected static int MAX_THREADS = 20;
	// max threads to run per host on windows
	protected static int WINDOWS_MAX_THREADS = 12; 

	private String hostname;
	private InetAddress victim;
	private boolean tcp;
	private boolean udp;
	private int[] portsToScan;
	private PortLookup portService;

	private Status statusReg;
	private int portsScanned = 0;
	private int resultsPending = 0;

	private boolean stopped = false;

	/**
	*Constructs thread to Scan an individual host, on set ports and protocol
	*@param String victim - the host to scan
	*@param boolean TCP/IP scanning enabed
	*@param boolean UDP scanning enabled
	*@param int[] the list of ports to be scanned
	*@param PortLookup the service defining names for the ports
	*/
	public ScanHost(String hostname, 
					boolean tcp, 
					boolean udp, 
					int[] portsToScan, 
					PortLookup portService){
		this.hostname = hostname;
		this.tcp = tcp;
		this.udp = udp;
		this.portsToScan = portsToScan;
		this.portService = portService;

		statusReg = new Status(tcp, udp, hostname);

		// if running Windows, set number of threads accordingly
		if (System.getProperty("os.name").startsWith("Windows")){
			this.MAX_THREADS = this.WINDOWS_MAX_THREADS;
		}

        try{
            victim = InetAddress.getByName(hostname);
        }
        catch(UnknownHostException e){
			victim = null;
		}
	}

	public String getHostname(){
		return this.hostname;
	}

	public void run(){
		if (victim == null){
			notifyObservers(statusReg);
			setChanged();
		}

        int p;
        for (int i=0; i<portsToScan.length; i++){
            p = portsToScan[i];

			if (resultsPending - portsScanned > this.MAX_THREADS){
				// we currently have too many threads running

				// wait until at least half of the threads have finished
				while (resultsPending - portsScanned > (this.MAX_THREADS/2)){
					try{
						Thread.sleep(5000);
					}
					catch(Exception e){
					}
				}
			}

            //for updating the progress bar
            setChanged();
            if (i == portsToScan.length-1){
                notifyObservers("       Waiting for responces       ");
            }
            else{
                notifyObservers("Probing:  " + hostname + "   PORT: "
                            + p + "    " + portService.getService(p, "tcp"));
            }

            if (stopped){
                break;
            }

            if (tcp){
                ScanTCP scan = new ScanTCP(victim, p);
                scan.addObserver(this);
                Thread scanThread = new Thread(scan);
				//scanThread.setPriority(Thread.MAX_PRIORITY);
                scanThread.start();

				//increment the results that we are waiting for
				resultsPending++;

				// pause before starting new thread
				try{
					Thread.sleep(100);
				}
				catch(Exception e){
				}
            }

            if (udp){
                ScanUDP scan = new ScanUDP(victim, p);
                scan.addObserver(this);
                Thread scanThread = new Thread(scan);
				//scanThread.setPriority(Thread.MAX_PRIORITY);
                scanThread.start();
				//increment the results that we are waiting for
				resultsPending++;
                try{
                    Thread.sleep(1100); //pause before starting next thread
                }
                catch(Exception e){
                    ;
                }
            }
        }

        while (!this.stopped && !statusReg.isUnreachable()
          && portsScanned < resultsPending){
            try{
                Thread.sleep(500); //all results havent arrived yet
            }
            catch(Exception e){
                ;
            }
        }

        String returnString = new String();
        try{
            returnString =
                 InetAddress.getByName(hostname).getHostName() + "(" +
                 InetAddress.getByName(hostname).getHostAddress() + ")\n";
        }
        catch(UnknownHostException e){
            returnString = hostname + "\n";
        }
        returnString += statusReg.toString();

		setChanged();
        notifyObservers(statusReg);
    }

    public void update(Observable o, Object arg){
        portsScanned++;

        if (o instanceof ScanTCP){
            int port = ((ScanTCP)o).getPort();

            if (arg == null){ //host is unreachable
                statusReg.setUnreachable();
            }

            String result = (String)arg;

            if (result.equals("CLOSED"))
                statusReg.addStatus(port, Status.CLOSED, Status.TCP);
            else if (result.equals("DROPPED"))
                statusReg.addStatus(port, Status.DROPPED, Status.TCP);
            else if (result.equals("OPEN"))
                statusReg.addStatus(port, Status.OPEN, Status.TCP);
        }
        else if (o instanceof ScanUDP){
            int port = ((ScanUDP)o).getPort();
            String result = (String)arg;

            if (result.equals("CLOSED"))
                statusReg.addStatus(port, Status.CLOSED, Status.UDP);
            else if (result.equals("OPEN"))
                statusReg.addStatus(port, Status.OPEN, Status.UDP);
        }
    }

    /**
    *Stops the scanning of a host
    */
    public void stop(){
        stopped = true;
    }
}
