import java.net.*;
import java.io.*;
import java.util.*;

/**
*Module for scanning tcp/ip ports on a given host or subnet
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

public class Scan extends Observable implements Observer {
	public static final String PORT_DEFINITIONS = "ports.defs";
	protected static int MAX_THREADS = 10; // hosts to scan at once
	protected static int WINDOWS_MAX_THREADS = 3; //max threads to run under Win

	protected InetAddress host;
		private int [] hostParts = new int[4];
	protected InetAddress subnet;
		private int [] subnetParts = new int[4];
	protected int[] portsToScan;
	private PortLookup portService;
	private boolean stopped = false; //true to stop scanning

	protected String resultsString = new String();
	protected String subnetResults;
	private Vector hostsScanning = new Vector(); //stores handle to threads scanning each host
	private int hostsToScan = 0;
	private int hostsScanned = 0;

	//for use when only scanning one port on each host
	private int portsRequired = 0;;
	private int portsScanned = 0;

	/*
	*Scan specified hostname
	*This constructor always gets called, by the other constructors
	*@param String hostname
	*@throws ScanningException - initialisation error
	*/
	public Scan(String hostname) throws ScanningException{
		try{
			host = InetAddress.getByName(hostname);
			portService = new PortLookup(PORT_DEFINITIONS);

			if (portsToScan == null){ //define it
				portsToScan = portService.getActivePorts();
			}

			//get the component parts
			String [] hostStrings = host.getHostAddress().split("\\.");

			if (hostStrings.length != 4){
				throw new ScanningException("Error forming hostname");
			}

			for (int i=0; i<hostStrings.length; i++){
				hostParts[i] = Integer.parseInt(hostStrings[i]);
			}


			// check our operating system, if windows then reduce no of threads
			if (System.getProperty("os.name").startsWith("Windows")){
				this.MAX_THREADS = this.WINDOWS_MAX_THREADS;
			}
		}
		catch(UnknownHostException e){
			throw new ScanningException("Host: " + hostname + " unknown :(");
		}
		catch(NullPointerException e){
			throw new ScanningException("Could not open port services"
										+ " definitions file");
		}
		catch(Exception e){
			throw new ScanningException(e.getMessage());
		}
	}

	/**
	*Scan subnet, probing default ports
	*@param String hostname
	*@param String subnet, eg 255.255.255.0
	*/
	public Scan(String hostname, String subnet) throws ScanningException{
		this(hostname); //call main constructor

		if (subnet == null){
			return;
		}

		try{
			//sanity check of subnet
			String [] subnetStrings = subnet.split("\\.");
	
			if (subnetParts.length != 4){
				//should be four sectios
				throw new ScanningException("Subnet invalid:\n" +
											"should be specified in form:\n" +
											"\t255.255.255.0\tor similar");
			}
	
			//form the subnetParts array of ints
			for (int i=0; i<subnetParts.length; i++){
				subnetParts[i] = Integer.parseInt(subnetStrings[i]);
			}
	
			boolean zeroFlag = false; //raised after which bits should be 0
			boolean badSubnet = false; //signals a bad subnet
	
			for (int i=0; i<subnetParts.length; i++){
				if (zeroFlag){
					if (subnetParts[i] != 0){
						badSubnet = true;
						break;
					}
				}
				else{
					if (subnetParts[i] < 255){
						zeroFlag = true; //remainder should be '0'
						continue;
					}
					else if (subnetParts[i] > 255){
						badSubnet = true; //illegal range
						break;
					}
				}
			}
	
			if (badSubnet){
				throw new ScanningException("Illegal subnet address\n" +
											"Subnet should be in form:\n" +
											"\t255.255.255.0 for example");
			}

			this.subnet = InetAddress.getByName(subnet);
		}
		catch(UnknownHostException e){
			throw new ScanningException("Subnet: " + subnet + " invalid");
		}
	}

	/*
	*Scan specified ports on given hostname, to scan subnet for just one port,
	*enter the same values for lowestPort and highestPort
	*@param String hostname
	*@param String subnet in form 255.255.255.0 (for example)
	*@param int lowestPort - port to start scanning from
	*@param int highestPort - port to scan up to
	*@throws ScanningException - initialisation error
	*/
	public Scan(String hostname, String subnet,
					int lowestPort, int highestPort) 
	                                  throws ScanningException{
		this(hostname, subnet);

		if (lowestPort <= highestPort && lowestPort > 0){
			portsToScan = new int[highestPort - lowestPort + 1];

			//define ports to scan
			int i=0;
			while (lowestPort <= highestPort){
				portsToScan[i++] = lowestPort++;
			}
		}
		else{
			//illegal port range
			throw new ScanningException("Illegal port range specified");
		}
	}

	/*
	*Scan specified ports on given hostname
	*@param String hostname
	*@param int lowestPort - port to start scanning from
	*@param int highestPort - port to scan up to
	*@throws ScanningException - initialisation error
	*/
	public Scan(String hostname, int lowestPort, int highestPort) 
													throws ScanningException{
		this(hostname, null, lowestPort, highestPort);
	}

	/**
	*Returns the number of ports which are to be scanned
	*@return int the total number of ports to scan
	*/
	public int getNumberPorts(){
		if (subnet == null){
			return portsToScan.length;
		}
		else{
			return portsToScan.length * this.getNumberHosts();
		}
	}

	/**
	* calculates the maximum number of possible hosts on this subnet
	*@return int the maximum number of hosts on subnet
	*/
	protected int getNumberHosts(){
		// use exclusive OR bitwise operation on subnet parts
		int partOne = subnetParts[0] ^ 0xff;
		int partTwo = subnetParts[1] ^ 0xff;
		int partThree = subnetParts[2] ^ 0xff;
		int partFour = subnetParts[3] ^ 0xff;

		// scale the return value appropriatly
		return partFour + (partThree*0xff) + (partTwo*0xff*0xff) + 
						  (partOne*0xff*0xff*0xff) + 1; //add 1 for '0'
	}

	/**
	*Returns list of services in format: PORT_NUMBER - DEFINITION
	*@return String array of port data
	*/
	public static String [] getPortList(){
		PortLookup portsList = new PortLookup(PORT_DEFINITIONS);
		int [] portNums = portsList.getActivePorts();
		String [] retrStr = new String[portNums.length];

		for (int i=0; i<portNums.length; i++){
			retrStr[i] = portNums[i] + " - " + 
									 portsList.getService(portNums[i], "tcp");
		}
		return retrStr;
	}

	/**
	*Default protocol used for scanning (TCP/IP)
	*@returns String list of open ports
	*/
	public String scan(){
		return this.scan(true, false); //tcp scan only
	}

	/**
	*Scans given host/subnet
	*@param boolean TCP/IP scanning enabled
	*@param boolean UDP scanning enabled
	*/
	public String scan(boolean tcp, boolean udp){
		if (subnet == null){
			// if we are only scanning one host
			this.scanHost(host.getHostName(), tcp, udp);
		}
		else{
			int [] subnetHostParts = new int[4];
			String subnetHost;
			subnetResults = new String();
			InetAddress returnedHost;
			InetAddress tmpHost;
			String displayHost; //to display hostname
			int port = -1; //used only if there is one port only

			//if there is only one port to scan, select it
			if (portsToScan.length == 1){
				port = portsToScan[0];
			}

			for (int i=0; i<subnetHostParts.length; i++){
				// bitwise AND operation to get the starting address
				subnetHostParts[i] = hostParts[i] & subnetParts[i];
			}

			for (int i=0; i<this.getNumberHosts(); i++){
				if (stopped){
					resultsString = "Scanning Aborted, results so far:\n\n" 
						 	 		+ resultsString;
					return resultsString;
				}

				//form the host to scan from the individual parts of this 
				//subnet host
				subnetHost = subnetHostParts[0] + "." 
							  + subnetHostParts[1] + "."
							  + subnetHostParts[2] + "."
							  + subnetHostParts[3];

				try{
					returnedHost = InetAddress.getByName(subnetHost);
					displayHost = returnedHost.getHostName() 
							+ "(" + subnetHost + ")";
				}
				catch(UnknownHostException e){
					displayHost = subnetHost;
				}

				try{
					//dont scan network of broadcast address
					if (subnetHostParts[3] != 0 && subnetHostParts[3] != 0xff){
						//retrived less output if only scanning one port
						if (port > 0){
							setChanged();
							notifyObservers("Probing:  " + subnetHost + 
										   "   PORT: " + port + "    " + 
										   portService.getService(port, "tcp"));

							tmpHost = InetAddress.getByName(subnetHost);

							if (tcp){
								this.scanTCP(tmpHost, port);
							}
							if (udp){
								this.scanUDP(tmpHost, port);
							}
						}
						else{
							scanHost(subnetHost, tcp, udp);

							//sleep for a while
							try{
								Thread.sleep(500); //5 sec
							}
							catch(Exception e){
								;
							}
						}
					}
					else{ 
					}
				}
				catch(UnknownHostException e){
					subnetResults += displayHost + " not valid\n";
				}

				//increment the host to scan
				subnetHostParts[3]++;

				if (subnetHostParts[3] == 0x100){
					subnetHostParts[3] = 0;

					subnetHostParts[2]++;
					if (subnetHostParts[2] == 0x100){
						subnetHostParts[2] = 0;

						subnetHostParts[1]++;
						if (subnetHostParts[1] == 0x100){
							//you shouldn't really be scanning this far
							//but who am i to stop you?
							subnetHostParts[1] = 0;

							subnetHostParts[0]++;
							if (subnetHostParts[0] == 0x100){
								//you really have gone too far
								break;
							}
						}
					}
				}
			}
			if (port > 0){
				while (this.portsScanned < this.portsRequired && !stopped){
					try{
						Thread.sleep(1000);
					}
					catch(Exception e){
						;
					}
				}
				stopped = false; //reset switch
				resultsString = new String();
				return subnetResults;
			}
		}

		//wait for all results to come back
		while(this.hostsScanned < this.hostsToScan && !stopped){
			try{
				Thread.sleep(1000);
			}
			catch(Exception e){
				;
			}
		}
		if (stopped){
			resultsString = "Scanning stopped, results so far:\n\n" 
						   + resultsString;
		}
		stopped = false; //reset switch
		return resultsString; //this final results
	}

    /**
    *Scans single port using specified TCP hoost/port
    *@param InetAddress host to probe
    *@param int port number
    */
    protected void scanTCP(InetAddress IP, int port){
		portsRequired++;
		ScanTCP s = new ScanTCP(IP, port);
		s.addObserver(this);
		Thread scanPortThread = new Thread(s);
		//scanPortThread.setPriority(Thread.MAX_PRIORITY);
		scanPortThread.start();
    }

    /**
    *Scans single UDP port on host/port
    *@param InetAddress IP address of the victim
    *@param int port number
    */
    protected void scanUDP(InetAddress IP, int port){
		portsRequired++;

		ScanUDP s = new ScanUDP(IP, port);
		s.addObserver(this);
		Thread scanPortThread = new Thread(s);
		//scanPortThread.setPriority(Thread.MAX_PRIORITY);
		scanPortThread.start();
    }


	/**
	*Scans a given host, protected so that external classes can only scan
	*using options set in constructor
	*@param String victim - the host to scan
	*@param boolean TCP/IP scanning enabed
	*@param boolean UDP scanning enabled
	*/
	protected void scanHost(String hostname, boolean tcp, boolean udp){
		if (hostsToScan - hostsScanned > this.MAX_THREADS){
			// we are running too many threads

			// wait until half threads have done
			while (hostsToScan - hostsScanned > this.MAX_THREADS/2){
				try{
					Thread.sleep(5000);
				}
				catch(Exception e){
				}
			}
		}

		//increment counter of number of hosts we're waiting for results from
		hostsToScan++; 

		ScanHost s = new ScanHost(hostname, tcp, udp, portsToScan, portService);
		hostsScanning.add(s); //store reference to this thread
		s.addObserver(this);

		Thread scanThread = new Thread(s);
		scanThread.setPriority(Thread.MAX_PRIORITY);
		scanThread.start();

		// pause before attempting to start another thread
		try{
			Thread.sleep(2000);
		}
		catch(Exception e){
		}
	}

	public void update(Observable o, Object arg){
		if (o instanceof ScanTCP){
			portsScanned++;
			//responce to only scanning for one port per host
			if ( ((String)arg).equals("OPEN")){
				ScanTCP s = (ScanTCP)o;
            	subnetResults += s.getIP().getHostName() +
                    "\t" + s.getPort() + " tcp  " + 
					portService.getService(s.getPort(), "tcp") +
                    "\tOPEN\n";
			}
		}
		else if (o instanceof ScanUDP){
			portsScanned++;
			//responce to only scanning for one port per host
			if ( ((String)arg).equals("OPEN")){
				ScanUDP s = (ScanUDP)o;
            	subnetResults += s.getIP().getHostName() +
                    "\t" + s.getPort() + " udp  " + 
					portService.getService(s.getPort(), "udp") +
                    "\tOPEN\n";
			}
		}
		else if (arg instanceof String){
			//is a status update, update our observer
			setChanged();
			notifyObservers(arg);
		}
		else if (arg instanceof Status){
			hostsScanned++;

			//attempt to remove thread from list of scanning threads
			hostsScanning.remove((ScanHost)o);

	        try {
	            resultsString +=
	                 InetAddress.getByName(
                     ((ScanHost)o).getHostname()).getHostName() + "(" +
	                 InetAddress.getByName(((ScanHost)o).getHostname())
                     .getHostAddress() + ")\n";
	        }
	        catch(UnknownHostException e){
	            resultsString += ((ScanHost)o).getHostname() + "\n";
	        }
	        resultsString += ((Status)arg).toString() + "\n\n\n";
		}
	}

	/**
	*Stops the scanning of a host
	*/
	public void stop(){
		stopped = true;

		//call stop for all threads left scanning
		for (int i=0; i<hostsScanning.size(); i++){
			((ScanHost)hostsScanning.get(i)).stop();
		}
	}

	public static void main(String [] args){
		if (args.length != 1){
			System.out.println("USAGE: scanTCP <hostname>");
			System.exit(0);
		}

		try{
			Scan scan = new Scan(args[0]);
			System.out.println(scan.scan());
		}
		catch(ScanningException e){
			System.err.println(e.getMessage());
		}
	}
}
