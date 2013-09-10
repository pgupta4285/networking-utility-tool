import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.jar.*;

/**
*Looks up information for given port
*
*Copyright (C) 2002 Tom Salmon tom@slashtom.org
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

public class PortLookup{
	protected String defsFile;
	protected String [] portsList;
	protected final String JAR_FILE_NAME = "jmap.jar";

	/**
	*Constructs port lookup object, to lookup service usually associated with
	*a given port number
	*@param String definitions file, similar in style to the one in 
	* /etc/services
	*@throws NullPointerException error getting to file
	*/
	public PortLookup(String defsFile) throws NullPointerException{
		this.defsFile = defsFile;
		String ports = new String();
		String temp = new String();

		try{
			JarFile arch = new JarFile(this.JAR_FILE_NAME);
			BufferedReader in = new BufferedReader(new InputStreamReader(
								arch.getInputStream(arch.getEntry(defsFile))));
			while(temp != null){
				temp = in.readLine();
				ports += temp + "\n";
			}
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
		portsList = ports.split("\n"); //split line into array
	}

	/**
	*Gets the service normally associated with the given port, if known
	*@param int port to find service name for
	*@param String protocol, such as tcp or udp
	*@return String the service name, or Unknown
	*/
	public String getService(int port, String protocol){
		for (int i=0; i<portsList.length; i++){
			if (portsList[i].matches(".*\t" + port + "\\/" + protocol + ".*")){ 
				if (portsList[i].indexOf("\t") > 0){
					return portsList[i].substring(0,portsList[i].indexOf("\t"));
				}
			}
		}
		return "UNKNOWN";
	}

	/**
	*Gets all port numbers that are associated with a service
	*@returns int array the list of ports
	*/
	public int[] getActivePorts(){
		Vector resultStrings = new Vector();
		int start, stop;
		
		for (int i=0; i<portsList.length; i++){
			if (portsList[i].startsWith("#")){ //comment
				continue;
			}
			start = portsList[i].indexOf("\t");
			stop = portsList[i].indexOf("/tcp");
			if (start < stop && start > 0){
				resultStrings.add(portsList[i].substring(start+1, stop).trim());
			}
		}

		int [] results = new int[resultStrings.size()];
		for (int i=0; i<resultStrings.size(); i++){
			results[i] = Integer.parseInt((String)resultStrings.get(i));
		}
		return results;
	}

	public static void main(String [] args){
		if (args.length != 1){
			System.err.println("USAGE: portLookup <port_number>");
			System.exit(0);
		}

		PortLookup p;
		try{
			p = new PortLookup("ports.defs");
			System.out.println( p.getService(Integer.parseInt(args[0]), "tcp"));
		}
		catch(Exception e){
			System.err.println("Couldn't open port definitions file");
			System.exit(-1);
		}
	}
}
