import java.util.*;

/**
*Records the status of ports for a particular host
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

public class Status{
	public static final int TCP = 0;
	public static final int UDP = 1;

	public static final int OPEN = 0;
	public static final int CLOSED = 1;
	public static final int DROPPED = 2;
	private static final int INVALID = -1;

	private boolean TCPScan; 
	private boolean UDPScan;
	private int[] portsTCP = new int[0xffff]; //port status, indexed by port no
	private int[] portsUDP = new int[0xffff]; //port status, indexed by port no

	// to keep count
	private int open = 0;
	private int closed = 0;
	private int dropped = 0;
	private PortLookup service = new PortLookup(Scan.PORT_DEFINITIONS);

	// flag if unreachable
	private boolean isUnreachable = false;

	private String host;

	/**
	*Constructs status for a specified host and protocol
	*@param boolean true for TCP
	*@param boolean true for UDP
	*@param String host to scan
	*/
	public Status(boolean TCPScan, boolean UDPScan, String host){
		this.TCPScan = TCPScan;
		this.UDPScan = UDPScan;
		this.host = host;

		// set all ports to invalid status
		for (int i=0; i<portsTCP.length; i++){
			portsTCP[i] = this.INVALID;
		}
		for (int i=0; i<portsUDP.length; i++){
			portsUDP[i] = this.INVALID;
		}
	}

	/**
	*Returns the hostname of system being scanned
	*/
	public String getHost(){
		return this.host;
	}

	/**
	*Called if host is unreachable
	*/
	public void setUnreachable(){
		this.isUnreachable = true;
	}

	/**
	*Returns true is host is unreachable
	*/
	public boolean isUnreachable(){
		return this.isUnreachable;
	}

	/**
	*Update the status on a specified port
	*@param int port number which is being reported
	*@param int status of port, eg. Status.OPEN
	*@param int the protocol, eg. Status.TCP
	*/
	public void addStatus(int port, int status, int proto){
		if (proto == this.TCP)
			this.portsTCP[port] = status;
		else if (proto == this.UDP)
			this.portsUDP[port] = status;
		else
			return;

		if (status == this.OPEN)
			this.open++;
		if (status == this.CLOSED)
			this.closed++;
		if (status == this.DROPPED)
			this.dropped++;
	}

	/**
	*Returns full status of ports on this host
	*/
	public String toString(){
		if (isUnreachable){
			return "UNREACHABLE";
		}

		// check for signs host may be down
		if (TCPScan && !UDPScan && this.open == 0 && this.closed == 0){
			return "Host appears to be down, or ignoring us";
		}
		else if (TCPScan && UDPScan && 
		  this.open > 10 && this.dropped > 10 && this.closed == 0){
			//10 as a rough mark, 10 consecutive ports unlikely to be open
			return "Host appears to be down, or ignoring us";
		}
		else if (!TCPScan && UDPScan && this.closed == 0 && this.OPEN > 10){
			//10 as a rough mark, 10 consecutive ports unlikely to be open
			return "Host appears to be down, or ignoring us";
		}

		String s = new String();

		for (int i=0; i<portsTCP.length; i++){
			if (this.portsTCP[i] == this.INVALID 
			  && this.portsUDP[i] == this.INVALID){
				continue; //this port has not been scanned
			}

			if (this.TCPScan){
				if (this.portsTCP[i] == this.OPEN){
					s += "\t" + i + "\ttcp\t" + service.getService(i,"tcp")
						  + "\tOPEN\n";
				}
				else if (this.portsTCP[i] == this.DROPPED){
					s += "\t" + i + "\ttcp\t" + service.getService(i,"tcp")
						  + "\tDROPPED\n";
				}
			}
			if (this.UDPScan){
				if (this.portsUDP[i] == this.OPEN){
					s += "\t" + i + "\tudp\t" + service.getService(i,"udp")
						  + "\tOPEN\n";
				}
			}
		}

		s += "\n" + (this.open + this.closed + this.dropped)+"\tPorts Scanned\n"
			 + this.open + "\tports open\n"
			 + this.dropped + "\tports dropped\n"
			 + this.closed + "\tports closed\n";

		return s;
	}
}

