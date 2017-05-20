/* Name: Likitha Seeram
 * Student ID: 1001363714
 * References: 
 * 1) http://stackoverflow.com/questions/16621169/scroll-down-automatically-jtextarea-for-show-the-last-lines-added
 * 2) https://github.com/sambenison66/Ring-Coordinator-Election-Algorithm-using-Java/blob/master/RingAlgorithm.java
 * 3) http://stackoverflow.com/questions/10790582/multi-threaded-server-for-each-indiviual-clients
 * 4) http://stackoverflow.com/questions/10131377/socket-programming-multiple-client-to-one-server
 * 5) http://stackoverflow.com/questions/9707938/calculating-time-difference-in-milliseconds
 * 6) http://stackoverflow.com/questions/5109654/creating-a-socket-server-which-allows-multiple-connections-via-threads-and-java
 * 7) http://stackoverflow.com/questions/40197514/java-how-can-i-check-if-the-serversocket-accepted-the-connection
 */

package com;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.ServerSocket;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import javax.swing.JCheckBox;

/*
 * This class is the GUI of the Process class where all 
 * buttons and text areas are initialized and event listeners
 * are added to the buttons
 */
public class ProcessGUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JPanel panel;  //a GUI panel
	public JButton btnStart, btnShutdown, btnCommunicate; //Buttons available on the GUI
	private JTextArea text = new JTextArea(); //TextArea for displaying messages
	private JLabel pno, cno;  //Labels for process number and coordinator
	private JTextArea processNo, coordNo;   //TextaArea for process number and coordinator
	private static Process process;  //Process class object to call its methods
	private static int defPort = 1500;  //Default port address
	private static String host = "localhost";  //Default host name
	public static int portNo = 0, procNo = 0;  //Port Number and Process Number of clients (or processes) initialized to 0
	public int coordinator = 0;  //Coordinator of the process
	private static boolean portFlag = false;  //A boolean value to track the status of port availability
	public boolean isCoordinator = false, isCrashed = false, isIdle = false;  //Boolean values to keep track of process type
	public static int counter = 1;  //Counter to keep track of number of processes
	private JCheckBox chckbxSimultaneousElection;  //A check box to start simultaneous election
	
	/*
	 * Class Constructor to create and initialize GUI data
	 */
	public ProcessGUI(int defPort, String host, int processNumber) {
		
		//GUI Panel
		panel = new JPanel();
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(panel);
		panel.setLayout(null);

		//Panel contains a text area in the middle to display messages
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(text);
		scrollPane.setBounds(30, 75, 475, 200);
		DefaultCaret caret = (DefaultCaret) text.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		panel.add(scrollPane);

		//Adding label for process number
		pno = new JLabel("Process Number");
		pno.setBounds(50, 10, 149, 25);
		panel.add(pno);

		//Adding label for coordinator
		cno = new JLabel("Coordinator");
		cno.setBounds(50, 40, 149, 25);
		panel.add(cno);

		//Adding text area for process number
		processNo = new JTextArea();
		processNo.setBounds(200, 10, 45, 25);
		processNo.setEditable(false);
		panel.add(processNo);

		//Adding text area for coordinator
		coordNo = new JTextArea();
		coordNo.setBounds(200, 40, 45, 25);
		coordNo.setEditable(false);
		panel.add(coordNo);

		//Button to initiate communicataion among processes
		btnCommunicate = new JButton("Communicate");
		btnCommunicate.setBounds(311, 26, 129, 25);
		btnCommunicate.addActionListener(this);
		panel.add(btnCommunicate);

		//Button to reset coordinator after crash
		btnStart = new JButton("Reset");
		btnStart.setBounds(407, 302, 77, 25);
		btnStart.addActionListener(this);
		panel.add(btnStart);
		btnStart.setEnabled(false);

		//Button to crash a process
		btnShutdown = new JButton("Crash");
		btnShutdown.setBounds(287, 302, 90, 25);
		btnShutdown.addActionListener(this);
		panel.add(btnShutdown);

		//Check box to initialize simultaneous election
		chckbxSimultaneousElection = new JCheckBox("Simultaneous Election");
		chckbxSimultaneousElection.setBounds(40, 298, 192, 29);
		panel.add(chckbxSimultaneousElection);
		chckbxSimultaneousElection.setEnabled(false);

		setVisible(true);
		setTitle("RingCoordinator Election Algorithm");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(200, 200, 570, 400);

		//Adding a listener for window closing
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent event) {
				isIdle = false;
				String currToken = "EXIT " + procNo;
				Process.processExit(currToken, procNo - 1);
				System.out.println("Window Closed, Process " + procNo);
			}
		});
	}

	/*
	 * Listener class for action items on GUI
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource(); //To get the action object

		//If the action item is communicate
		if (o == btnCommunicate) {
			text.append("\nSending Token: MESSAGE " + procNo);
			String currToken = "MESSAGE " + procNo;
			Process.communication(currToken, procNo + 1);
		}

		// when a process crashes
		if (o == btnShutdown) {
			isCrashed = true;
			btnShutdown.setEnabled(false);
			btnCommunicate.setEnabled(false);
			btnStart.setEnabled(true);
			process.suspendProcess(procNo);
		}

		// when a process resets/starts after crash
		if (o == btnStart) {
			isCrashed = false;
			btnShutdown.setEnabled(true);
			btnCommunicate.setEnabled(true);
			btnStart.setEnabled(false);
			if (chckbxSimultaneousElection.isSelected()) {
				process.resumeWithElection();  //When checkbox is selected
			} else
				process.resumeProcess();  //When checkbox is not selected
		}
	}
	
	/*
	 * This method is used to append any message/ text to the GUI
	 */
	public void append(String msg) {
		text.append(msg);
	}
	
	/*
	 * This method is used to set the coordinator value to the GUI
	 * after the coordinator election completes
	 */
	public void electedCoordinator(int id) {
		coordinator = id;
		String value = Integer.toString(id);
		coordNo.setText(value);
	}
	
	//This method is used to enable simultaneous election checkbox
	public void enableElection() {
		chckbxSimultaneousElection.setEnabled(true);
	}
	
	//This method is used to disable simultaneous election checkbox
	public void disableElection() {
		chckbxSimultaneousElection.setEnabled(false);
	}
	
	/*
	 * Main method of GUI class
	 */
	public static void main(String[] args) {
		ProcessGUI pg = null;
		//A maximum number of 8 processes can be created
		for (; counter <= 8; counter++) {
			ServerSocket soc;
			portNo = defPort + counter;  //Every process has its own server. Hence server socket address is determined using process id
			try {
				//Checking the server socket available to create it in Process class 
				soc = new ServerSocket(portNo);
				procNo = portNo - defPort;
				pg = new ProcessGUI(portNo, host, procNo);
				String number = Integer.toString(procNo);
				pg.processNo.append(number);  //appending the process number to GUI
				soc.close(); 
				portFlag = true;  //If the socket is available then the flag  is set to true
				break;
			} catch (Exception e) {
				System.out.println("Socket is already running");
			}
		}
		
		//Checking the maximum number of possible processes has exceeded
		if (portFlag == false) {
			System.out.println("Exceeded the maximum number of ports");
			return;
		}
		
		//Once the port value is available, a process object is instantiated
		if (portFlag == true) {
			process = new Process(portNo, host, procNo, pg);
			process.start();  //Process method is called to start the thread
		}
	}
}
