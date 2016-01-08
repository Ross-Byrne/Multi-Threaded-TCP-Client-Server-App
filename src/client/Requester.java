package client;

import java.io.*;
import java.net.*;
import java.util.*;

public class Requester {
	
	Socket requestSocket;
	ObjectOutputStream out;
 	ObjectInputStream in;
 	String message = "";
 	String ipaddress;
 	InetAddress inetAddress;
 	Scanner scanner;
 	
 	// for sending files
 	FileInputStream fis;
 	
 	// to receive files
 	FileOutputStream fos;
 	
 	// when client receives server finished message, client can send message to server
 	static final String SERVER_FINISHED_MSG = "_server+finished_";
 	static final String SERVER_SENDING_FILE_MSG = "_server+sending+file_";
 	static final String CLIENT_SENDING_FILE_MSG = "_client+sending+file_";
 	
 	StringBuilder fileListSB = new StringBuilder();
	StringBuilder directoryListSB = new StringBuilder();
	
 	String[] clientInput;
 	int inputLength = 0;
	
 	public static void main(String args[]){
 		
 		Requester client = new Requester();
 		client.run();
 		
 	} // main()
 	
 	
 	void sendFile(File file){
		
		int bytesRead = 0;
		
		try {
			
			// create a file input stream for the file being sent
			fis = new FileInputStream(file);
			
			// create a byte array to hold the bytes of the file
		    byte[] fileBytes = new byte[fis.available()]; // can't handle files bigger then 1GB
		    
		    // track the number of bytes read
		    bytesRead = fis.read(fileBytes);

		    System.out.println("bytes read: " + bytesRead);
		    
		    // send the byte array to client
		    out.writeObject(fileBytes);
			
		} catch (FileNotFoundException e) {

			System.out.println("ERROR, File not found!");
			
		} catch (IOException e) {

			e.printStackTrace();
			
		} finally {
			
		    try {
		    	
		    	// close file input stream
				fis.close();
				
			} catch (IOException e) {

				e.printStackTrace();
			} // try catch
			
		} // try catch
		
	} // sendFile()


 	void receiveFile(String fileName, int fileSize){
 		
 		// create the downloads directory file
 		/*File file = new File("Downloads");
 		
 		// make the file a directory
 		file.mkdirs();*/
 		
 		try {
 			
 			// create an array of btyes
 			// receive an array of bytes that make up the file and place them in fileBytes
 		    byte[] fileBytes = (byte[]) in.readObject();
 		    
 		    // create a file output stream
 		    fos = new FileOutputStream(fileName);
 		    
 		    // write the received file bytes to the file
 		    fos.write(fileBytes); 
 			
		} catch (FileNotFoundException e) {

			System.err.println("ERROR, File not found!");
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		} catch (ClassNotFoundException e) {
			
			System.err.println("data received in unknown format");
			
		} finally {
			
		    try {
		    	
		    	// close the file output stream
	 			fos.close();
				
			} catch (IOException e) {

				e.printStackTrace();
			} // try catch
			
		} // try catch
 		
 	} // receiveFile()
	
	public void run(){
		
		scanner = new Scanner(System.in);
		
		boolean canSendMessage = false;
		boolean clientIsFinished = true;
		int menuChoice = 0;
		
		try{
			//1. creating a socket to connect to the server
			
			do{
				
				// Print out Menu Options
				System.out.println("\n1.) Type IP Address.");
				System.out.println("2.) Type Domain Name To Perform DNS Lookup On. (May Crash If TimeOut Occurs!)");
				
				System.out.print("\nEnter Option: ");
				
				while(!scanner.hasNextInt()){
					
					System.out.print("Enter Option: ");
					scanner.next(); // to advance Scanner past input
					
				} // while
				
				// get users input
				menuChoice = scanner.nextInt();
			
			}while(menuChoice < 1 || menuChoice > 2); // do while
			
			switch(menuChoice){
			case 1: // type IP address in
				
				System.out.print("Client > Please Enter your IP Address: ");
				ipaddress = scanner.next();
				scanner.nextLine(); // flush buffer
				
				break;
			case 2: // type domain name for DNS Lookup
				String domain = "";
				
				// flush buffer
				scanner.nextLine();
				
				// get Domain from user for the DNS Lookup
				System.out.print("Client > Please Enter Domain Name For DNS Lookup: ");
				domain = scanner.nextLine();
				
				System.out.println("Client > Performing DNS Lookup On '" + domain + "'.");
				
				// perform DNS lookup on domain
				inetAddress = InetAddress.getByName(domain);
		
				
				// get ip address from lookup
				ipaddress = inetAddress.getHostAddress();
				
				break;
			} // switch
						
			// create a socket to the server
			requestSocket = new Socket(ipaddress, 2004);
			System.out.println("Connected to "+ipaddress+" in port 2004");
			
			//2. get Input and Output streams
			
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(requestSocket.getInputStream());		
			
			//3: Communicating with the server
			
			do{
				try{
					
					// if the client is finished
					if(clientIsFinished){
						
						// read message from server
						message = (String)in.readObject();
						
					} // if
					
					// only show message from server if it's not server finished message
					if(!message.equals(SERVER_FINISHED_MSG)){
						
						System.out.println("Server > " + message);
					} // if
					
					// if the server is finished sending messages
					if(message.equals(SERVER_FINISHED_MSG)){
						
						// the client can now send a message
						canSendMessage = true;
						
						// client is not finished
						clientIsFinished = false;
						message = "";
					} // if
					
					// check if the server is sending a file
					if(message.equals(SERVER_SENDING_FILE_MSG)){
						
						String fileName = "";
						int fileSize = 0;
						
						// receive file name 
						fileName = (String)in.readObject();
						
						// receive file size
						fileSize = Integer.parseInt((String)in.readObject());
						
						// receive file
						receiveFile(fileName, fileSize);
						
						System.out.println("File recieved!");
						
					} // if
					
					// if the client is allowed send a message
					if(canSendMessage || !clientIsFinished){
					
						// get message from client
						System.out.print("Client > ");
						message = scanner.nextLine();
						
						// client is finished unless stated otherwise later
						clientIsFinished = true;
						
						// splits string to get command and parameter
						clientInput = message.split(" ");
						
						// get the number of parameters
						inputLength = clientInput.length;
					
						// if the user input is not greater then 2 commands or shorter then one command
						if(!(inputLength > 2 || inputLength < 1) && !(clientInput[0].equals(""))){
						
							switch(clientInput[0]){ // get command
							case "ls": // list the files on clients pc and files on the server
								
								// create a file to track current directory
								File currentDirectory = new File(".");
								
								// create an array of all the files in current directory
								File[] listOfFiles = currentDirectory.listFiles();
								
								fileListSB.append("Files:");
								directoryListSB.append("Directories:");
								
								// loop through the list of files in the directory
							    for (int i = 0; i < listOfFiles.length; i++) {
							    	
							    	// if the file is a file
							    	if (listOfFiles[i].isFile()) {
							    		
							    		// add name to list of files string
							    		fileListSB.append("  ").append(listOfFiles[i].getName());
							    		
							    	} else if (listOfFiles[i].isDirectory()) { // if the file is a directory
							    	  
							    		// add name to list of directories string
							    		directoryListSB.append("  ").append(listOfFiles[i].getName());
							    
							    	} // if
						    	} // for
							    
							    // print out files in home directory
							    System.out.println("Client > Client's Files.");
							    System.out.println("Client > " + directoryListSB.toString());
							    System.out.println("Client > " + fileListSB.toString());
							    
							    // send list command to server too
							    sendMessage(message);
							    
							    // clear string builders
							    directoryListSB.setLength(0);
							    fileListSB.setLength(0);
							    
							    // client is finished
							    clientIsFinished = true;
							    
							    // can't send any messages until server is finished
							    canSendMessage = false;
							    
								break;
							case "send": // send a file to the server
								
								if(clientInput.length == 2){
									
									File file = new File(clientInput[1]);
									
									// if the file is a Directory
									if(file.isDirectory()){
										
										// Tell the client
										System.out.println("Client > " + clientInput[1] + " is a Directory! Cannot Get!");
										
										// action failed so client is not finished
										clientIsFinished = false;
										break;
									} // if
									
									// if the file does not exist
									if (!file.exists()) {
										
										// Tell the client
										System.out.println("Client > Sorry, file does not exist!");
										
										// action failed so client is not finished
										clientIsFinished = false;
								
										break;
										
									} else { // if the file exists
										
										System.out.println("File Found!");
										
										// send message to server telling it to prepare to receive file
										sendMessage(CLIENT_SENDING_FILE_MSG);
										
										// send file name 
										sendMessage(file.getName());
										
										// send file size
										sendMessage(String.valueOf(file.length()));
										
										// send file
										sendFile(file);
										
										System.out.println("File sent!");
										
										// the client is finished
										clientIsFinished = true;
										
										// wait for server to finish
										canSendMessage = false;
										
									} // if
									
								} else { // if there isn't a name for file after Get
									
									// tell the client
									System.out.println("Client > ERROR, Command Get must be followed by a file name that is in the current directory!");
									
								} // if
								
								break;
							default:
								
								// if the client is finished, send message to the server
								if(clientIsFinished){
									
									// send message to the server
									sendMessage(message);
									
									// message sent, cant send again until server says so
									canSendMessage = false;
									
								} // if
								
								break;
								
							} // switch
						
						} // if
					} // if
					
				} catch(ClassNotFoundException classNot){
					
					System.err.println("data received in unknown format");
					
				} catch(EOFException e){
					
					System.err.println("Connection To Server Was Lost!");
					
					// Closing connection
					
					try{
						
						in.close();
						out.close();
						requestSocket.close();
						
					} catch(IOException ioException){
						
						ioException.printStackTrace();
					} // try catch
					
					// make message "bye" to exit the while loop
					message = "bye";
					
				} // try catch
				
			}while(!message.equals("bye")); // do while
			
		} catch(UnknownHostException unknownHost){
			
			System.err.println("You are trying to connect to an unknown host!");
			
		} catch(ConnectException e){
			
			System.err.println("Client > Connection Timed Out!");
			
		} catch(EOFException e){
			
			System.err.println("Connection To Server Was Lost!");
			
		} catch(IOException ioException){
			
			ioException.printStackTrace();
		} finally{
			
			//4: Closing connection
			
			try{
				
				in.close();
				out.close();
				requestSocket.close();
				
				System.out.println("Client > Program Exiting.");
				
			} catch(IOException ioException){
				
				ioException.printStackTrace();
			} // try catch
			
		} // try catch
		
	} // run()
	
	void sendMessage(String msg){
		
		try{
			out.writeObject(msg);
			out.flush();
			//System.out.println("client>" + msg);
		} catch(IOException ioException){
			
			ioException.printStackTrace();
		} // try catch
		
	} // sendMessage()
	
} // class