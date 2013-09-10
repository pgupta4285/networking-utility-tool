import java.io.*;

/**
*Command line interface to the java port scanner
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

public class JMap{
	public static void main(String [] args){
        System.out.println("GNU JMap - Java Port Scanner, Copyright 2002, " +
                           " Tom Salmon tom@slashtom.org\n"+
                           "GNU JMap comes with ABSOLUTELY NO WARRANTY\n" +
                           "This is free software, and you are welcome to " +
                           "redistribute it under certain\n conditions\n");

		Scan scan;

		try{
			if (args.length == 1){
				scan = new Scan(args[0]);
				System.out.println(scan.scan());
			}
			else if (args.length == 4 && args[0].equals("-p")){
				scan = new Scan(args[3], 
					   Integer.parseInt(args[1]), Integer.parseInt(args[2]));
				System.out.println(scan.scan());
			}
			else{
				System.err.println("USAGE: JMap [-p <start port> <end port>] "
								   + "HOSTNAME");
				System.exit(-1);
			}
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
	}
}
