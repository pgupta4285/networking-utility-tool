import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

/**
*Front end graphical user interface to JMap
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

public class JMapFE extends JFrame {
	protected Scan scan;
	protected Scanning sh;

	protected final String version = "version 0.3.1";
	protected final int FRAME_WIDTH = 455;
	protected final int FRAME_HEIGHT = 530;

	protected JFrame frame;

	protected JPanel mainPanel = new JPanel(new BorderLayout());
		protected JTextArea resultsText = new JTextArea();
	protected JScrollPane resultsScroll = new JScrollPane(resultsText);
		protected JPanel buttonsPanel = new JPanel(new GridLayout(1,5));
			protected JButton scanButton = new JButton("Scan");
			protected JButton quitButton = new JButton("Quit");
	protected JPanel titlePanel = new JPanel(new GridLayout(8,3));
		protected JLabel titleLabel = new JLabel(
										  "JMap");
		protected JLabel hostnameLabel = new JLabel("Hostname:  ");
		protected JTextField hostnameInput = new JTextField();
		protected JCheckBox subnetOption = new JCheckBox("Subnet Scan");
		protected JLabel subnetLabel = new JLabel("Subnet:  ");
		protected JTextField subnetInput = new JTextField("255.255.255.0");
		protected JCheckBox scanSubnetForOption = new JCheckBox("Scan Subnet for");
		protected JComboBox scanSubnetForCombo = new JComboBox();
		protected JCheckBox scanTCPOption = new JCheckBox("TCP", true);
		protected JCheckBox scanUDPOption = new JCheckBox("UDP", false);
		protected JCheckBox portsSelected = new JCheckBox(
												   "Port Range", false);
		protected JTextField lowestPortInput = new JTextField("1");
		protected JLabel portRangeLabel = new JLabel("to");
		protected JTextField highestPortInput = new JTextField("1024");

	//used in (process bar) dialog box to stop it all
	protected JButton stopProcessButton;

	protected JMenuBar menu = new JMenuBar();
	protected JMenu scanMenu = new JMenu("Scan");
		protected JMenuItem scanMenuItem = new JMenuItem("Scan");
		protected JMenuItem exitMenuItem = new JMenuItem("Exit");
	protected JMenu helpMenu = new JMenu("Help");
		protected JMenuItem usageMenuItem = new JMenuItem("Usage");
		protected JMenuItem aboutMenuItem = new JMenuItem("About");
		protected JMenuItem versionMenuItem = new JMenuItem("Version");
		protected JMenuItem bugsMenuItem = new JMenuItem("Bugs");

	public JMapFE(){
		super("JMap - GNU Java Port Scanner");
		this.getContentPane().add(mainPanel);
		this.setBounds(0,0,this.FRAME_WIDTH,this.FRAME_HEIGHT);
		mainPanel.add(titlePanel, BorderLayout.NORTH);
		mainPanel.add(resultsScroll, BorderLayout.CENTER);
			resultsText.setLineWrap(true);
			resultsText.setWrapStyleWord(true);
		mainPanel.add(buttonsPanel, BorderLayout.SOUTH);

		//set frame to this so to referrence in child and inner classes
		frame = this; 

		titlePanel.add(new JLabel());
		titlePanel.add(titleLabel);
			titleLabel.setHorizontalAlignment(JLabel.CENTER);
		titlePanel.add(new JLabel());

		titlePanel.add(new JLabel());
		titlePanel.add(hostnameLabel);
			hostnameLabel.setHorizontalAlignment(JLabel.RIGHT);
		titlePanel.add(hostnameInput);
			hostnameInput.setToolTipText("Hostname or IP address of " 
										 + "system to scan");

		titlePanel.add(subnetOption);
			subnetOption.setToolTipText("Enable subnet scanning");
		titlePanel.add(subnetLabel);
			subnetLabel.setHorizontalAlignment(JLabel.RIGHT);
		titlePanel.add(subnetInput);
			subnetInput.setToolTipText("Enter subnet to scan");
		titlePanel.add(new JLabel());
		titlePanel.add(scanSubnetForOption);
		titlePanel.add(scanSubnetForCombo);

		titlePanel.add(new JLabel());
		titlePanel.add(new JLabel());
		titlePanel.add(new JLabel());

		titlePanel.add(scanTCPOption);
		titlePanel.add(scanUDPOption);
		/*
		titlePanel.add(new JLabel());
		titlePanel.add(new JLabel());
		*/

		titlePanel.add(new JLabel());
		titlePanel.add(portsSelected);
		titlePanel.add(new JLabel());
		titlePanel.add(new JLabel());
		titlePanel.add(lowestPortInput);
			lowestPortInput.setToolTipText("Network Port " 
										   + "to start scanning from");
		titlePanel.add(portRangeLabel);
		titlePanel.add(highestPortInput);
			highestPortInput.setToolTipText("Network Port to scan upto");

		buttonsPanel.add(new JLabel());
		buttonsPanel.add(scanButton);
			scanButton.setToolTipText("Start Scanning");
		buttonsPanel.add(new JLabel());
		buttonsPanel.add(quitButton);
			quitButton.setToolTipText("Close JMap");
		buttonsPanel.add(new JLabel());

		subnetInput.setEditable(false);
		scanSubnetForOption.setEnabled(false);
		scanSubnetForCombo.setEnabled(false);
		portsSelected.setSelected(false);
		lowestPortInput.setEditable(false);
		highestPortInput.setEditable(false);
		resultsText.setEditable(false);

		resultsText.setText(this.getUsage());

		//create stop button for later usage in (process bar) dialog box
		stopProcessButton = new JButton("Stop");

		this.setupCombo(); //set the choice options
		this.displayMenu();
		this.addListeners();

		this.setVisible(true);
	}

	/**
	*Returns a String containing basic usage of the client
	*/
	protected String getUsage(){
		return "GNU Java Network Port Scanner - JMap\n\n\n" +
					"Hostname:\tIP address or valid name lookup\n" +
					"Subnet:\tRange of addresses/hosts to scan\n" +
					"TCP/UDP:\tProtocol to scan for, both may be " +
					"selected\n" +
					"Port Range:\t Enter port range to scan, " +
					"otherwise defaults will be used\n" +
					"\nWhen scanning subnet, can select to scan for "+
					"a specified service\n" +
					"\n\n" +
					"Provided without warranty, and distributed under "+
					"the terms of the GNU General Public License\n\n" +
					"Written by Tom Salmon\n" +
					"tom@slashtom.org";
	}

	protected void displayMenu(){
		menu.add(scanMenu);
		menu.add(helpMenu);

		scanMenu.setMnemonic('s');
		helpMenu.setMnemonic('h');

		scanMenu.add(scanMenuItem);
		scanMenu.addSeparator();
		scanMenu.add(exitMenuItem);

		scanMenuItem.setMnemonic('s');
		exitMenuItem.setMnemonic('x');

		helpMenu.add(usageMenuItem);
		helpMenu.addSeparator();
		helpMenu.add(versionMenuItem);
		helpMenu.add(aboutMenuItem);
		helpMenu.add(bugsMenuItem);

		usageMenuItem.setMnemonic('u');
		versionMenuItem.setMnemonic('v');
		aboutMenuItem.setMnemonic('t');
		bugsMenuItem.setMnemonic('u');

		this.setJMenuBar(menu);
	}

	protected void setupCombo(){
		//get list of ports, and services
		String [] portList = Scan.getPortList();

		for (int i=0; i<portList.length; i++){
			scanSubnetForCombo.addItem(portList[i]);
		}
	}

	protected void addListeners(){
		this.addWindowListener(new JMapFEWindowAdapter());

		scanButton.addActionListener(new JMapFEActionListener());
		quitButton.addActionListener(new JMapFEActionListener());
		portsSelected.addActionListener(new JMapFEActionListener());
		subnetOption.addActionListener(new JMapFEActionListener());
		scanSubnetForOption.addActionListener(new JMapFEActionListener());

		stopProcessButton.addActionListener(new JMapFEActionListener());

		hostnameInput.addKeyListener(new JMapFEKeyAdapter());
		subnetInput.addKeyListener(new JMapFEKeyAdapter());
		lowestPortInput.addKeyListener(new JMapFEKeyAdapter());
		highestPortInput.addKeyListener(new JMapFEKeyAdapter());

		scanMenuItem.addActionListener(new JMapFEMenuListener());
		exitMenuItem.addActionListener(new JMapFEMenuListener());
		usageMenuItem.addActionListener(new JMapFEMenuListener());
		aboutMenuItem.addActionListener(new JMapFEMenuListener());
		versionMenuItem.addActionListener(new JMapFEMenuListener());
		bugsMenuItem.addActionListener(new JMapFEMenuListener());
	}

	/**
	*Enables of disables input options
	*@param boolean true to enable
	*/
	protected void enableInputs(boolean enable){
		if (portsSelected.isSelected()){
			lowestPortInput.setEditable(enable);
			highestPortInput.setEditable(enable);
		}

		if (subnetOption.isSelected()){
			subnetInput.setEditable(enable);
			scanSubnetForOption.setEnabled(enable);

			if (scanSubnetForOption.isSelected()){
				scanSubnetForCombo.setEnabled(enable);
				portsSelected.setSelected(false); //deselect the ports selected
			}
		}

		portsSelected.setEnabled(enable);
		scanTCPOption.setEnabled(enable);
		scanUDPOption.setEnabled(enable);
		hostnameInput.setEditable(enable);

		scanMenuItem.setEnabled(enable);

		if (enable){
			scanButton.setText("Scan");
			stopProcessButton.setText("Stop");
			scanButton.setToolTipText("Start Scanning");
		}
		else{
			scanButton.setText("Stop");
			scanButton.setToolTipText("Stop Scanning");
		}
	}

	class Scanning extends Thread implements Observer{
		JDialog progressDialog; //dialog to show progress bar
		JProgressBar progress;
		JPanel dialogPanel;
		JLabel portDescription;

		Scanning(){
			enableInputs(false);
			resultsText.setText("");

			//get subnet
			String subnet;
			if (subnetOption.isSelected()){
				subnet = subnetInput.getText();
			}
			else{
				subnet = null;
			}

			if (hostnameInput.getText().length() == 0){
				resultsText.setText("You have not entered a hostname\n"
									+ "YOU FOOL!!!\n");
				enableInputs(true); //reactivate the inputs
				return; //they havent entered a hostname
			}

			try{
				if (portsSelected.isSelected()){
					scan = new Scan(hostnameInput.getText(), 
								  subnet,
								  Integer.parseInt(lowestPortInput.getText()), 
								  Integer.parseInt(highestPortInput.getText()));
				}
				else if (scanSubnetForOption.isSelected()){
					//work out what part number has been selected
					String selectedPortName = 
								 (String)scanSubnetForCombo.getSelectedItem();
					int selectedPortNumber = Integer.parseInt(
											selectedPortName.substring(0, 
											   selectedPortName.indexOf(' ')));
					scan = new Scan(hostnameInput.getText(), 
								  subnet,
								  selectedPortNumber,
								  selectedPortNumber);
				}
				else{
					scan = new Scan(hostnameInput.getText(), subnet);
				}
			}
			catch(Exception e){
				resultsText.setText(e.getMessage());
				enableInputs(true);

				scan = null;
				return;
			}

			//add this as one of the observers of scan
			scan.addObserver(this);


			//setup progress dialog - set non-model
			progressDialog = new JDialog(frame, "Scanning....", false);
			progressDialog.addWindowListener(new DialogWindowListener());

			dialogPanel = new JPanel(new BorderLayout());
			progress = new JProgressBar(0, scan.getNumberPorts());
			portDescription = new JLabel(
					"_____________________PLEASE WAIT_______________________");
					//forces a wider progress window

			//add swing components
			progressDialog.getContentPane().add(dialogPanel);
			progressDialog.setBounds(FRAME_WIDTH/10, FRAME_HEIGHT/4, 
									 FRAME_WIDTH/2, FRAME_HEIGHT/4);

			dialogPanel.add(portDescription, BorderLayout.NORTH);
			dialogPanel.add(stopProcessButton, BorderLayout.SOUTH);
			dialogPanel.add(progress, BorderLayout.CENTER);
			progressDialog.pack();
		}

		public void update(Observable o, Object arg){
			if (arg instanceof Integer){
				//we should update our progress by this number
				progress.setValue( progress.getValue() +
								 ((Integer)arg).intValue() );
				return;
			}
			else{
				//increment the progress bar's value
				progress.setValue(progress.getValue()+1);

				portDescription.setText((String)arg);
			}
		}

		public void run(){
			if (scan == null){
				return;
			}

			progressDialog.setVisible(true);
			progress.setStringPainted(true);
			progress.setValue(0);

			// if UDP scanning, warn of length of time
			if (scanUDPOption.isSelected()){
				resultsText.setText(
					"UDP Scanning takes some time\n" +
					"go have a coffee :)\n");
			}

			resultsText.setText( "\n" + scan.scan(
											 scanTCPOption.isSelected(), 
											 scanUDPOption.isSelected()));
			enableInputs(true);

			progressDialog.setVisible(false);
		}

		public void destroy(){
			scan.stop();
		}

		//handles this dialog window closing
		class DialogWindowListener extends WindowAdapter{
			public void windowClosing(WindowEvent e){
				scanButton.setText("Halting");
				stopProcessButton.setText("Halting");
				sh.destroy();
			}
		}
	}

	class JMapFEWindowAdapter extends WindowAdapter{
		public void windowClosing(WindowEvent e){
			System.exit(0); //kill everything
		}
	}

	class JMapFEActionListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			String action = e.getActionCommand();

			if (action.equals(scanButton.getText())){
				if (action.equals("Stop")){
					scanButton.setText("Halting");
					stopProcessButton.setText("Halting");
					sh.destroy();
				}
				else{
					sh = new Scanning();
					sh.start();
				}
			}
			else if (action.equals(quitButton.getText())){
				System.exit(0); //quit
			}
			else if (action.equals(portsSelected.getText())){
				lowestPortInput.setEditable(portsSelected.isSelected());
				highestPortInput.setEditable(portsSelected.isSelected());

				if (portsSelected.isSelected()){
					//disable scanSubnetForOption
					scanSubnetForOption.setSelected(false);
					scanSubnetForCombo.setEnabled(false);
				}
			}
			else if (action.equals(scanSubnetForOption.getText())){
			  scanSubnetForCombo.setEnabled(scanSubnetForOption.isSelected());
				if (scanSubnetForOption.isSelected()){
					portsSelected.setSelected(false);

					//disable port range, if necessary
					lowestPortInput.setEditable(false);
					highestPortInput.setEditable(false);
				}
			}
			else if (action.equals(subnetOption.getText())){
				subnetInput.setEditable(subnetOption.isSelected());
				scanSubnetForOption.setEnabled(subnetOption.isSelected());

				if (!subnetOption.isSelected()){
					//if deselecting subnet scanning, disable scan subnet
					//for server
					scanSubnetForOption.setSelected(false);
					scanSubnetForCombo.setEnabled(false); //disable combo
				}
			}
			else if (action.equals(stopProcessButton.getText())){
				scanButton.setText("Halting");
				stopProcessButton.setText("Halting");
				sh.destroy();
			}
			else{
				//System.err.println("Unknown action: " + action);
			}
		}
	}

	class JMapFEKeyAdapter extends KeyAdapter{
		public void keyPressed(KeyEvent e){
			if (e.getKeyCode() == KeyEvent.VK_ENTER){
				sh = new Scanning();
				sh.start();
			}
		}
	}

	class JMapFEMenuListener implements ActionListener {
		public void actionPerformed(ActionEvent e){
			String event = e.getActionCommand();

			if (event.equals(scanMenuItem.getText())){
				sh = new Scanning();
				sh.start();
			}
			else if (event.equals(exitMenuItem.getText())){
				System.exit(0);
			}
			else if (event.equals(usageMenuItem.getText())){
				resultsText.setText(getUsage());
			}
			else if (event.equals(aboutMenuItem.getText())){
				resultsText.setText("JMap - GNU Java Port Scanner\n"
						   + "written by Tom Salmon tom@slashtom.org\n"
						   + "http://slashtom.org\n\n"
+ "This program is free software; you can redistribute it and/or\n"
+ "modify it under the terms of the GNU General Public License\n"
+ "as published by the Free Software Foundation; version 2.\n"
+ "\n"
+ "This program is distributed in the hope that it will be useful,\n"
+ "but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
+ "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"
+ "GNU General Public License for more details.\n"
+ "\n"
+ "You should have received a copy of the GNU General Public License\n"
+ "along with this program; if not, write to the Free Software\n"
+ "Foundation, Inc., 59 Temple Place - \n"
+ "Suite 330, Boston, MA  02111-1307, USA.");
			}
			else if (event.equals(versionMenuItem.getText())){
				resultsText.setText("JMap - " + version + "\n");
			}
			else if (event.equals(bugsMenuItem.getText())){
				resultsText.setText("JMap\n\n" +
									" Report bugs to: jmap@slashtom.org" +
									"\n");
			}
			else {
				//System.err.println("Unknown event: " + event);
			}
		}
	}

	public static void main(String [] args){
		JMapFE jmap = new JMapFE();
	}
}
