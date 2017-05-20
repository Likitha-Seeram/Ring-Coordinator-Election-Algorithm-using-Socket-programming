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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.StringTokenizer;

/*
 * A process class for the GUI. It has methods to create a thread for each GUI and 
 * to control the processes basing on the actions
 */
public class Process {

	private static int defPort = 1500;  // Default port number
	private static String host;  //local host name
	private static processThread client = new processThread();  //A client thread
	private static int portNo = 0, procNo = 0;  //initial port number and process number
	private static ServerSocket servSocket;  //A socket for server
	public static int crashedPort;  //A variable to keep track of crashed port
	private static ProcessGUI pg;  //An instance of ProcessGUI class
	public static boolean statusFlag = false;  //To keep track of message status in a process

	/*
	 * A Constructor that takes initial values when Process class
	 * object is created in ProcessGUI class
	 */
	public Process(int port, String host, int processID, ProcessGUI pg) {
		Process.portNo = port;
		Process.host = host;
		Process.procNo = processID;
		Process.pg = pg;
	}

	/*
	 * This method is used to initiate election process among the threads (or processes).
	 * When next thread is not available, then the election token is passed to that next
	 */
	public static void initiateElection(String token, int processNo) {
		//If the process number exceeds 8, we subtract it from 8 to get the correct number
		if (processNo > 8) {
			processNo = processNo - 8;
		}
		try {
			//Creating a socket that writes to output stream of the process port
			Socket socket = new Socket(host, defPort + processNo);
			//Object stream object used to write to a socket
			ObjectOutputStream so = new ObjectOutputStream(socket.getOutputStream());
			//Writing to the socket
			so.writeObject(token);
			//Closing the output stream object after writing
			so.close();
			//Closing the socket after writing
			socket.close();
		} catch (Exception e) {
			//If the next process is not available, then pass the token that next process
			initiateElection(token, processNo + 1);
		}
	}

	/*
	 * This method is called when communication is initialized by a process.
	 * It writes the token to the next process.
	 */
	public static void communication(String token, int processNo) {
		//If the process number exceeds 8, we subtract it from 8 to get the correct number
		if (processNo > 8) {
			processNo = processNo - 8;
		}
		try {
			//Creating a socket that writes to output stream of the process port
			Socket socket = new Socket(host, defPort + processNo);
			//Object stream object used to write to a socket
			ObjectOutputStream so = new ObjectOutputStream(socket.getOutputStream());
			//Starting the timer for the process, once the communication is started by it
			processThread.before = Instant.now();
			//Writing to the socket
			so.writeObject(token);
			//Closing the output stream object after writing
			so.close();
			//Closing the socket after writing
			socket.close();
		} catch (Exception e) {  //When there is an exception writing to a port
			System.out.println("Next process is not available");
		}
	}

	/*
	 * This method is used to create a server for a process and starts a thread that 
	 * runs for each proccess created
	 */
	public boolean start() {
		try {
			//Creating a server pocket
			servSocket = new ServerSocket(portNo);
			//Starting a thread
			client.start();
			//Initializing values for client thread created
			client.init(portNo, procNo, pg, servSocket);
		} catch (Exception e) {  //Exception while creating a thread
			System.out.println("Exception : " + e);
			return false;
		}
		return true;
	}

	/*
	 * This method is called when a thread is crashed.
	 */
	@SuppressWarnings("deprecation")
	public void suspendProcess(int id) {
		try {
			//Suspending the client thread
			client.suspend();
			//Closing the socket
			servSocket.close();
			//Saving the port address of the crashed process
			crashedPort = defPort + id;
			client.display("\n Process Crashed");
			//Checking the timer for a process thread
			client.checkTimer(id + 1);
		} catch (IOException e) {  //When crashing fails
			pg.isCrashed = false;
			client.display("Unable to crash the process");
		} 
	}
	
	/*
	 * This method is called when a thread restarts
	 */
	@SuppressWarnings("deprecation")
	public void resumeProcess() {
		try {
			//Creating the server socket for the crashed port
			servSocket = new ServerSocket(Process.crashedPort);
			//Resetting the thread values again
			client.resetValues(Process.crashedPort, procNo, servSocket);
			//Resume the client thread
			client.resume();
			client.display("\n Thread Restarting..");
			//Crashed port is set to zero after resetting the process
			Process.crashedPort = 0;
		} catch (Exception ex) { // Exception to shut down the process when crashed port is unavailable
			System.out.println("\n Port not available for Process Restart..!! \n Process Restart failed..!!");
		}
		pg.isIdle = false; // boolean value change
		// Initiate the election whenever the process is restarted
		client.display("\n Token Out: " + "ELECTION " + procNo);
		String currToken = "ELECTION " + procNo;  //Election token to pass
		Process.initiateElection(currToken, procNo + 1);
	}
	
	/*
	 * This method is called when a thread restarts
	 */
	public void resumeWithElection() {
		try {
			//Creating the server socket for the crashed port
			servSocket = new ServerSocket(Process.crashedPort);
			//Resetting the thread values again
			client.resetValues(Process.crashedPort, procNo, servSocket); //updating the thread values to reset it
			//Resume the client thread
			client.resume();
			client.display("\n Thread restarted");
			//Crashed port is set to zero after resetting the process
			Process.crashedPort = 0;
		} catch (Exception ex) { // Exception to shut down the process when crashed port is unavailable
			System.out.println("\n Port not available for Process Restart..!! \n Process Restart failed..!!");
		}
		pg.isIdle = false; // boolean value change
		// Initiating the simultaneous election
		client.simultaneousElection(procNo + 1, procNo + 3);
	}
	
	/*
	 * This method is called when a GUI is closed by clicking the window exit button
	 */
	public static void processExit(String token, int procID) {
		client.processExit(token, procID);
	}
}

/*
 * Every process created has its own instance of process thread class
 */
class processThread extends Thread {
	public int processNumber;  //Process number of a thread
	public int portNumber;  //Port number on which the process's server is running
	public ProcessGUI pg;  //GUI of the thread
	public ServerSocket servSoc;  //Server socket on which the process runs
	public Socket socket;  //A Socket for thread to read/write messages
	public static int defPort = 1500;  //A default port number 
	public int coordinator = 0;  //Coordinator for the thread
	static String host = "localhost";  //Local host address
	//Input and output streams for a process thread
	private ObjectInputStream sInput;
	private ObjectOutputStream sOutput;
	
	//Timer values for each thread to keep track of messages being received in time
	public static Instant before = null, after = null;
	public static Instant coord_before = null, coord_after = null;
	
	//Method to initialize thread values
	public void init(int portNumber, int processNumber, ProcessGUI pg, ServerSocket servSoc) {
		this.portNumber = portNumber;
		this.processNumber = processNumber;
		this.pg = pg;
		this.servSoc = servSoc;
		display("Started Thread");
	}
	
	//Method to append text/data to the GUI text area panel
	public void display(String message) {
		if (pg == null)
			System.out.println(message);
		else
			pg.append(message);
	}

	//Method to reset thread values after resuming from crash
	public void resetValues(int portNumber, int processNumber, ServerSocket servSoc) {
		this.portNumber = portNumber; // assign the port number
		this.processNumber = processNumber; // assign the token
		this.servSoc = servSoc; // assign the socket
	}

	/*
	 * This method runs for each thread until the thread is crashed
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		String token = null; //Variable to keep track of token value being received by thread
		while (true) {
			//This block of try catch executes after the initial election timer times out. Typically
			//after the token goes around once in the ring
			try {
				if (before != null) {
					after = Instant.now();  //to get the current time
					long difference = Duration.between(before, after).toMillis();  //Difference is calculated to check the timer
					//If the thread times out, then election is started by the thread as it doesn't receive any response
					if (difference > 24000) {
						display("\n Initiating Election");
						before = null;
						after = null;
						String currToken = "ELECTION " + processNumber;
						Process.initiateElection(currToken, processNumber + 1);
					}
				}
			} catch (Exception e) {
				System.out.println("Exception : " + e);
			}
			//This block of try catch executes when a process doesn't receive a token that is being passed
			//around after coordinator election
			try {
				if (coord_before != null) {
					coord_after = Instant.now();  //to get the current time
					long difference = Duration.between(coord_before, coord_after).toMillis();  //Difference is calculated to check the timer
					if (difference > 24000 || Process.crashedPort != 0) {  //
						if (token.equalsIgnoreCase("timer")) { //If the token received is only timer check message 
							display("\nTimed out");
							display("\n Initiating Election again");
							Process.statusFlag = true;
							coord_before = null;
							coord_after = null;
							String currToken = "ELECTION " + processNumber;
							Process.initiateElection(currToken, processNumber + 1);
						} else {  //Starting the timer again, if tokens are received in time
							coord_before = Instant.now();
						}
					}
				}
			} catch (Exception e) {
				System.out.println("Exception : " + e);
			}
			
			//When a socket connection is received, following is executed
			try {
				socket = servSoc.accept();  //accepting the connection
				sInput = new ObjectInputStream(socket.getInputStream());  //Getting the input stream
				token = (String) sInput.readObject();  //reading the token from the stream
				Thread.sleep(3000);  //Making the thread to sleep to show the flow of messages
				StringTokenizer stringTokenizer = new StringTokenizer(token);  //To retrieve the message in the form tokens/words 
				//Basing on the token type, case executes
				switch (stringTokenizer.nextToken()) { 
				//If the token is for election
				case "ELECTION":
					display("\n Token In:  " + token);  //displaying the input on GUI
					pg.isIdle = false;
					try {
						//If the next token is the process number then token has been received by initiating process 
						if (Integer.parseInt(stringTokenizer.nextToken()) == processNumber) {
							//Taking all the alive processes in to the list array
							int list[] = new int[8];
							list[0] = processNumber;
							int count = 1;
							while (stringTokenizer.hasMoreTokens()) {
								list[count] = Integer.parseInt(stringTokenizer.nextToken());
								count++;
							}
							//Finding the coordinator from the list
							identifyCoordinator(list);
							//Informing about the coordinator to all the otherprocesses
							if (coordinator != 0) {
								informProcesses(coordinator, processNumber, processNumber);
							}
						} else {  //If the token has been received by a process, then pass it
							display("\nToken Out: " + token + " " + processNumber);
							sendToNextProcess(token + " " + processNumber, processNumber + 1);
						}
					} catch (Exception e) {
						System.out.println("Exception : " + e);
					}
					token = "read";  //As the token is processed, making the value as 'read'
					break;
				//If the token is for message passing
				case "MESSAGE":
					display("\n Token In:  " + token);  //displaying the input on GUI
					//If the token is rececived by the initiating process
					if (Integer.parseInt(stringTokenizer.nextToken()) == processNumber) {
						if (coord_before != null && Process.statusFlag == false) {
							//Sending the token again
							display("\nAll processes are alive, sending the token again");  
							String currToken = "MESSAGE " + processNumber;
							display("\nToken Out:" + currToken);  //displaying the output on GUI
							coord_before = Instant.now();  //Starting the timer
							passToken(currToken, processNumber + 1);  //Passing the token around
						}
					} else {
						//If the token has been received by a process, then pass it
						display("\nToken Out: " + token + " " + processNumber);
						passToken(token + " " + processNumber, processNumber + 1);
					}
					token = "read";  //As the token is processed, making the value as 'read'
					break;
				case "COORDINATOR":
					String token2 = stringTokenizer.nextToken(); // coordinator number
					String token3 = stringTokenizer.nextToken(); // identifier process
					try {
						Thread.sleep(2000);
						//If the token is received by the coordinator
						if (Integer.parseInt(token2) == processNumber) {
							pg.isCoordinator = true;
							display("\nThis is new Coordinator, elected by " + token3);
							coord_before = Instant.now();  //Starting the timer
							pg.enableElection();  //Enabling simultaneous election option for coordinator
							if (token2 == token3) {  //If coordinator is the process, then start passing the token
								String currToken = "MESSAGE " + processNumber;
								display("\nToken Out:" + currToken);
								passToken(currToken, processNumber + 1);  //Passing the token around
							}
						} else {
							//If the process is previous coordinator
							if (pg.isCoordinator == true) {
								display("\n" + processNumber + " is not coordinator anymore");
								coord_before = Instant.now();
								pg.isCoordinator = false;
								pg.disableElection();  //Disabling simultaneous election option
							}
						}
						//If token is received by initiator process
						if (Integer.parseInt(token3) == processNumber) {
							display("\nNew coordinator ---> " + token2 + " and informed all other processes");
							if (pg.isCrashed == false) {
								pg.btnShutdown.setEnabled(true);
							}
							pg.electedCoordinator(Integer.parseInt(token2));  //Setting the coordinator value on GUI
							pg.isIdle = true;

							// after coordinator election is done and informed
							// all other processes about it, start sending token again
							String currToken = "MESSAGE " + processNumber;
							display("\nToken Out:" + currToken);
							coord_before = Instant.now();  //Starting the timer
							passToken(currToken, processNumber + 1);  //Passing the token around
						} else {  //If the token is received by any other process
							System.out.println(pg.isCoordinator + " " + processNumber);
							if (!pg.isCoordinator) {
								display("\nNew coordinator ---> " + token2 + ", Elected by " + token3);
							}
							int coord = Integer.parseInt(token2);
							pg.electedCoordinator(coord);  //Setting the coordinator value on GUI
							coordinator = coord;
							coord_before = Instant.now();  //Starting the timer
							passToken(token, processNumber + 1);  //Passing the token around
							pg.isIdle = true;
							pg.btnShutdown.setEnabled(true);  //Enabling crash button
						}
					} catch (Exception e) { 
						System.out.println("Exception : " + e);
					}
					token = "read";  //As the token is processed, making the value as 'read'
					break;
				}

			} catch (Exception e) {
				System.out.println("Exception : " + e);
			} finally {
				try {  //Closing the input stream
					sInput.close();
				} catch (IOException e) {
					System.out.println("Exception : " + e);
				}
			}
		}
	}
	
	/*
	 * This method is used to identify the coordinator which is
	 * the process with highest process number
	 */
	private void identifyCoordinator(int[] list) {
		int newCoordinator = list[0];
		for (int i = 1; i < list.length; i++) {
			if (list[i] > newCoordinator)
				newCoordinator = list[i];
		}
		coordinator = newCoordinator;
	}
	
	//This method is used to pass election message
	private void sendToNextProcess(String token, int pid) {
		if (pid > 8)
			pid = pid - 8;
		try {
			//Creating a socket that writes to output stream of the process port
			Socket soc = new Socket(host, defPort + pid);
			//Object stream object used to write to a socket
			ObjectOutputStream s = new ObjectOutputStream(soc.getOutputStream());
			//Writing to the socket
			s.writeObject(token);
			//Closing the output stream object after writing
			s.close();
			//Closing the socket after writing
			soc.close();
		} catch (Exception e) { //When there is an exception writing to a port
			System.out.println("Exception : " + e);
			sendToNextProcess(token, pid + 1);
		}
	}
	
	//This method is used for simultaneous election 
	public void simultaneousElection(int p1, int p2) {
		if (p1 > 8)
			p1 = p1 - 8;
		if (p2 > 8)
			p2 = p2 - 8;
		try {
			String token1 = "ELECTION " + p1;
			Socket soc = new Socket(host, defPort + p1);
			ObjectOutputStream s = new ObjectOutputStream(soc.getOutputStream());
			s.writeObject(token1);
			s.close();
			soc.close();
		} catch (Exception e) {
			e.printStackTrace();
			simultaneousElection(p1 + 1, p2 + 1);
			return;
		}
		try {
			String token2 = "ELECTION " + p2;
			Socket soc = new Socket(host, defPort + p2);
			ObjectOutputStream s = new ObjectOutputStream(soc.getOutputStream());
			s.writeObject(token2);
			s.close();
			soc.close();
		} catch (Exception e) {
			e.printStackTrace();
			simultaneousElection(p1 + 1, p2 + 1);
			return;
		}
	}
	
	//This method is used to pass any token message around the ring
	private void passToken(String token, int pid) {
		if (pid > 8)
			pid = pid - 8;
		if (coordinator == processNumber) {
			pid = 1;
		}
		try {
			Socket soc = new Socket(host, defPort + pid);
			ObjectOutputStream s = new ObjectOutputStream(soc.getOutputStream());
			s.writeObject(token);
			s.close();
			soc.close();
		} catch (Exception e) {
			checkTimer(pid + 1);  //Checking the timer for the thread
			System.out.println("Exception : " + e);
		}
	}
	
	//This method is used to inform coordinator elected message to all processes
	private void informProcesses(int coordinator, int procNo, int electedProcess) {
		int temp;
		if (coordinator == procNo)
			temp = 1;
		else
			temp = procNo + 1;
		String token = "COORDINATOR " + coordinator + " " + electedProcess;  //Coordinator message
		try {
			//Creating a socket that writes to output stream of the process port
			Socket soc = new Socket(host, defPort + temp);
			//Object stream object used to write to a socket
			ObjectOutputStream s = new ObjectOutputStream(soc.getOutputStream());
			//Writing to the socket
			s.writeObject(token);
			//Closing the output stream object after writing
			s.close();
			//Closing the socket after writing
			soc.close();
		} catch (Exception e) {  //If next process is not available then inform to that next available process
			System.out.println("Exception : " + e);
			informProcesses(coordinator, temp, electedProcess); 
		}
	}
	
	//This method is used to check the timer for each thread by sending timer token
	public void checkTimer(int pid) {
		if (pid > 8)
			pid = pid - 8;
		try {
			//Creating a socket that writes to output stream of the process port
			Socket soc = new Socket(host, defPort + pid);
			//Object stream object used to write to a socket
			ObjectOutputStream s = new ObjectOutputStream(soc.getOutputStream());
			String token = "timer";
			Thread.sleep(2000);
			//Writing to the socket
			s.writeObject(token);
			//Closing the output stream object after writing
			s.close();
			//Closing the socket after writing
			soc.close();
		} catch (Exception e) {  //When there is an exception writing to a port
			checkTimer(pid + 1);
		}
	}

	public void processExit(String token, int id) {
		if (id == 0 || id < 0) {
			if (coordinator != 0) {
				id = coordinator;
			} else {
				id = 8;
			}
		}
		try {
			//Creating a socket that writes to output stream of the process port
			Socket soc = new Socket(host, defPort + id);
			//Object stream object used to write to a socket
			ObjectOutputStream s = new ObjectOutputStream(soc.getOutputStream());
			//Writing to the socket
			s.writeObject(token);
			//Closing the output stream object after writing
			s.close();
			//Closing the socket after writing
			soc.close();
		} catch (Exception ex) {
			Process.initiateElection(token, id - 1); // inform to previous of previous if previous
														// process is unavailable
		}
	}

}
