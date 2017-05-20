# Ring-Coordinator-Election-Algorithm-using-Socket-programming
This project implements an concept of Distributed Systems - Election Algorithm. There are 8 nodes that participate in the election. A token is passed around all the nodes and the process/node with highest ID is elected as coordinator. If the coordinator fails and a process in the ring doesn't receive the token in the specified time limit, it starts the election and elects a new coordinator and informs other processes. 

Steps to run the application:

•	Start 8 processes by running the file ‘ProcessGUI.java’
•	Click on ‘Communicate’ button on the Process Number 1 
•	Communication and initial election are seen after this
•	After the coordinator is elected, crash the coordinator by clicking on ‘Crash’ button of the coordinator (process 8)
•	New coordinator is elected. After this, reset the previously crashed coordinator by clicking on the ‘Reset’ button of the crashed coordinator (process 8)
•	Election process starts again and a new coordinator is elected which has highest process ID

Limitation:
Simultaneous election is not implemented
