

import java.io.*;
import java.net.*;
import java.util.*;

public class EchoServer {
	
	public static void main(String[] args) throws Exception {
	  
	    ServerSocket m_ServerSocket = new ServerSocket(2004,10);
	    Map<String, String> loginDetails = new HashMap<String, String>();
	    final String LOGIN_FILE = "login.txt";
	    
	    boolean isRunning = true;
	    int id = 0;
	    
	    // create a Users directory
 		File file = new File("Users");
 		
 		// make the file a directory
 		file.mkdir();
 		
 		// gets reference for login file
 		file = new File(LOGIN_FILE);
 		
 		// checks if the file exists
 		if (!file.exists() || !file.isFile()){
 			
 			// print out warning message
 			System.out.println("Error, Login File missing. No Usernames or Passwords On Record!");
 			System.out.println("No Users will be able to login!");
 			
 			// if file doesnt exist or is a directory, create the file
 			file.createNewFile();
 		} // if
 		
	    // load login details into map from login.txt
	    
	    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(LOGIN_FILE)));
		String line = null;
		
		// read a line at a time
		while((line = br.readLine()) != null){
			
			// split the line at the space
			String [] login = line.split(" ");
			
			// add the username and password to the login details
			loginDetails.put(login[0].toString(), login[1].toString());
			
			// create a users folder and make a folder for all the users in the login file
			
			// make a file for the user in users folder
	 		file = new File("Users" + File.separator + login[0]);
	 		
	 		// make the file a directory
	 		file.mkdirs();
	
		} // while
		
		// close the buffered line reader
		br.close();
		
		// wait for connections
	    
	    while (isRunning) {
	    	
	    	System.out.println("Waiting for connection . . .");
	    	Socket clientSocket = m_ServerSocket.accept();
	    	ClientServiceThread cliThread = new ClientServiceThread(clientSocket, id++, loginDetails);
	    	cliThread.start();
	    	
	    } // while
	    
	    // close ServerSocket
	    m_ServerSocket.close();
	    
  } // main()
	
} // class

class ClientServiceThread extends Thread {
	
	static final String SERVER_FINISHED_MSG = "_server+finished_";
	static final String SERVER_SENDING_FILE_MSG = "_server+sending+file_";
	static final String CLIENT_SENDING_FILE_MSG = "_client+sending+file_";
	
	ServerSocket m_ServerSocket;
	Socket clientSocket;
	String message;
	int clientID = -1;
	boolean running = true;
	ObjectOutputStream out;
	ObjectInputStream in;
	Map<String, String> loginDetails;
	
	// for sending files
	FileInputStream fis;
	
	// to receive files
 	FileOutputStream fos;
	
	boolean clientIsLoggedIn = false;
	String clientsUsername = "";
	
	StringBuilder fileListSB = new StringBuilder();
	StringBuilder directoryListSB = new StringBuilder();
	
	StringBuilder clientsCurrentDirectory = new StringBuilder(); // to hold the clients current directory
	List<String> directoryHolder = new ArrayList<String>(); // to hold directory values
	
	String[] clientInput;
	int inputLength = 0;
	
	// Constructor
	ClientServiceThread(Socket s, int i, Map<String, String> loginMap) {
		
		clientSocket = s;
	    clientID = i;
	    loginDetails = new HashMap<String, String>(loginMap);
	    
	} // ClientServiceThread()
	
	void sendFile(File file){
		
		int bytesRead = 0;
		
		try {
			
			// create a file input stream for the file being sent
			fis = new FileInputStream(file);
			
			// create a byte array to hold the bytes of the file
		    byte[] fileBytes = new byte[fis.available()]; // cant handdle files bigger then 1GB
		    
		    // track the number of bytes read
		    bytesRead = fis.read(fileBytes);

		    System.out.println("Server > bytes read: " + bytesRead);
		    
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
 		
		// clear string builder
		clientsCurrentDirectory.setLength(0);
		
		// create a string to represent the clients current directory
		for(int i = 0; i < directoryHolder.size(); i++){
			
			// add the directory to the string builder followed by the separator for the system
			clientsCurrentDirectory.append(directoryHolder.get(i)).append(File.separator);
			
		} // for

		// create a file to track current directory
		new File(clientsCurrentDirectory.toString());
 		
 		try {
 			
 			// create an array of bytes
 			// receive an array of bytes that make up the file and place them in fileBytes
 		    byte[] fileBytes = (byte[]) in.readObject();
 		    
 		    // create a file output stream
 		    fos = new FileOutputStream(clientsCurrentDirectory.toString() + File.separator + fileName);
 		    
 		    // write the received file bytes to the file
 		    fos.write(fileBytes); 
 			
		} catch (FileNotFoundException e) {

			System.out.println("ERROR, File not found!");
			
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

	void sendMessage(String msg){
		
		try{
			out.writeObject(msg);
			out.flush();
			System.out.println("Server to Client > " + msg);
			
		} catch(IOException ioException){
			
			ioException.printStackTrace();
		} // try catch
		
	} // sendMessage()
	
	public void run() {
		
		System.out.println("Accepted Client : ID - " + clientID + " : Address - "
        + clientSocket.getInetAddress().getHostName());
		try {
			
			out = new ObjectOutputStream(clientSocket.getOutputStream());
			out.flush();
			
			in = new ObjectInputStream(clientSocket.getInputStream());
			
			System.out.println("Accepted Client : ID - " + clientID + " : Address - "
		        + clientSocket.getInetAddress().getHostName());
		
			sendMessage("Connection successful");
			
			// after connecting, user must login
			
			clientIsLoggedIn = clientlogin();
			
			// keep looping while the client is still logged in
			while(clientIsLoggedIn){ // main loop
				
				try{
				
					// read message in
					message = (String)in.readObject();
					
					// print out message
					System.out.println("Client " + clientID + " to Server > " + message);
					
					// check if the client is sending a file
					if(message.equals(CLIENT_SENDING_FILE_MSG)){
						
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
					
					// check if the client is logging out
					if(message.equals("bye")){
						
						System.out.println("Client " + clientID + " is Logging Out!");
						
						// set client to not logged in
						clientIsLoggedIn = false;
						
						// break out of loop
						break;
						
					} // if
					
					// splits string to get command and parameter
					clientInput = message.split(" ");
					
					// get the number of parameters
					inputLength = clientInput.length;
				
					// if the user input is not greater then 2 commands or shorter then one command
					if(!(inputLength > 2 || inputLength < 1) && !(clientInput[0].equals(""))){
					
						switch(clientInput[0]){ // get command
						case "ls": // listing the files and directories in the current directory
							
							// clear string builder
							clientsCurrentDirectory.setLength(0);
							
							// create a string to represent the clients current directory
							for(int i = 0; i < directoryHolder.size(); i++){
								
								// add the directory to the string builder followed by the separator for the system
								clientsCurrentDirectory.append(directoryHolder.get(i)).append(File.separator);
								
							} // for
				
							// create a file to track current directory
							File currentDirectory = new File(clientsCurrentDirectory.toString());
							
							// create an array of all the files in current directory
							File[] listOfFiles = currentDirectory.listFiles();
							
							fileListSB.append("Files:");
							directoryListSB.append("Directories:");
							
							// if the array of files is not empty
							if(listOfFiles != null){
								
								// loop through the list of files in the directory
							    for (int i = 0; i < listOfFiles.length; i++) {
							    	
							    	// if the file is a file
							    	if (listOfFiles[i].isFile()) {
							    		
							    		// add name to list of files string
							    		fileListSB.append("  ").append(listOfFiles[i].getName());
							    		
							    		//System.out.println("File " + listOfFiles[i].getName());
							    		
							    	} else if (listOfFiles[i].isDirectory()) { // if the file is a directory
							    	  
							    		// add name to list of directories string
							    		directoryListSB.append("  ").append(listOfFiles[i].getName());
							    		
							    		//System.out.println("Directory " + listOfFiles[i].getName());
							    	} // if
						    	} // for
							} // if
						    
						    // send results to the client
						    sendMessage("Server's Files @ " + clientsCurrentDirectory.toString());
						    sendMessage(directoryListSB.toString());
						    sendMessage(fileListSB.toString());
						    
						    // clear string builders
						    directoryListSB.setLength(0);
						    fileListSB.setLength(0);
						      
							break;
						case "get": // getting a file from the server
							
							if(clientInput.length == 2){
								
								// clear string builder
								clientsCurrentDirectory.setLength(0);
								
								// create a string to represent the clients current directory
								for(int i = 0; i < directoryHolder.size(); i++){
									
									// add the directory to the string builder followed by the separator for the system
									clientsCurrentDirectory.append(directoryHolder.get(i)).append(File.separator);
									
								} // for
								
								File file = new File(clientsCurrentDirectory.toString() + File.separator + clientInput[1]);
								
								// if the file is a Directory
								if(file.isDirectory()){
									
									sendMessage(clientInput[1] + " is a Directory! Cannot Get!");
									break;
								} // if
								
								// if the file does not exist
								if (!file.exists()) {
									
									sendMessage("Sorry, file does not exist!");
									
								} else { // if the file exsits
									
									sendMessage("File Found!");
									
									// send message to client telling it to prepare to receive file
									sendMessage(SERVER_SENDING_FILE_MSG);
									
									// send file name 
									sendMessage(file.getName());
									
									// send file size
									sendMessage(String.valueOf(file.length()));
									
									// send file
									sendFile(file);
									
									System.out.println("Server > File sent!");
									
								} // if
								
							} else { // if there isn't a name for file after Get
								
								sendMessage("ERROR, Command get must be followed by a file name that is in the current directory!");
							}
							
							break;
						case "cd": // changing directory
							
							// make sure client enters parameter for cd command
							if(clientInput.length == 2){
								
								// check if the client wants to go back to previous directory 
								if(clientInput[1].equals("..")){
									
									// check if the client can go back to parent directory
									if(directoryHolder.size() > 2){ // if client is not in user directory eg Users/[Username]
										
										// remove last directory entry from directoryHolder so client is moved back one directory
										directoryHolder.remove(directoryHolder.size()-1);
										
										// create string to represent current directory
										
										// clear string builder
										clientsCurrentDirectory.setLength(0);
										
										// create a string to represent the clients current directory
										for(int i = 0; i < directoryHolder.size(); i++){
											
											// add the directory to the string builder followed by the separator for the system
											clientsCurrentDirectory.append(directoryHolder.get(i)).append(File.separator);
											
										} // for
										
										// tell client they are moving back one directory
										sendMessage("Moving back to: " + clientsCurrentDirectory.toString());
										
									} else { // if the user is already in home directory 
										
										// tell the user they cannot move back
										sendMessage("Error, Already in User's Home Directory! Cannot Move Further Back!");
										
									} // if
									
									// break out because selected command is complete
									break;
									
								} // if
								
								// check that directory selected exists
								
								// clear string builder
								clientsCurrentDirectory.setLength(0);
								
								// create a string to represent the clients current directory
								for(int i = 0; i < directoryHolder.size(); i++){
									
									// add the directory to the string builder followed by the separator for the system
									clientsCurrentDirectory.append(directoryHolder.get(i)).append(File.separator);
									
								} // for
					
								// create a file to track current directory
								currentDirectory = new File(clientsCurrentDirectory.toString());
								
								// create a file to represent the directory the user wants to move to
								File targetDirectory = new File(clientsCurrentDirectory.toString() + File.separator + clientInput[1]);
								
								// check if the file exists
								if(targetDirectory.exists()){
									
									// check if the file that exists is a directory
									if(targetDirectory.isDirectory()){
										
										// tell the client their directory is being changed
										sendMessage("Moving to: " + clientsCurrentDirectory.toString() + clientInput[1]);
										
										// move the client's current directory to the new directory
										directoryHolder.add(clientInput[1]);
										
									} else { // if file is not a directory
										
										// tell client they cannot move, file is not directory
										sendMessage("Error! " + clientInput[1] + " is a File, not a Directory! Cannot Move!");
										
									} // if
									
								} else { // if the file does not exist
									
									// tell client the directory does not exist
									sendMessage("Error! Direcotry '" + clientInput[1] + "' Does Not Exist! Cannot Move!");
									
								} // if
								
							} else { // if no parameter after command
								
								sendMessage("ERROR, Command cd must be followed by a directory in your current directory Or '..' to move back one directory!");
							
							} // if
							
							break;
						case "mkdir": // making a directory
							
							if(clientInput.length == 2){
								
								// clear string builder
								clientsCurrentDirectory.setLength(0);
								
								// create a string to represent the clients current directory
								for(int i = 0; i < directoryHolder.size(); i++){
									
									// add the directory to the string builder followed by the separator for the system
									clientsCurrentDirectory.append(directoryHolder.get(i)).append(File.separator);
									
								} // for
					
								// create a file to track current directory
								currentDirectory = new File(clientsCurrentDirectory.toString());
								
								// create a file to represent the directory the user wants to create
								File newDirectory = new File(clientsCurrentDirectory.toString() + File.separator + clientInput[1]);
								
								// check if the directory already exists
								if(newDirectory.exists() && newDirectory.isDirectory()){
									
									// tell user the directory already exists
									sendMessage("Error! Directory '" + clientInput[1] + "' Already exists!");
									
								} else { // otherwise create the directory
									
									// create the new directory
									newDirectory.mkdirs();
									
									// tell the client that the directory is created
									sendMessage("Created Directory '" + clientInput[1] + "'");
									
								} // if
								
							} else { // if no parameter after command
								
								// tell the client they need to add a parameter after command
								sendMessage("ERROR, Command mkdir must be followed by the name of the directory you want to create!");
								
							} // if
							
							break;
						case "pwd": // print the current working directory
							
							// check if the client entered more then "pwd"
							if(inputLength > 1){
								
								sendMessage("Command 'pwd' is all that is needed to print working directory!");
							} // if
							
							// clear string builder
							clientsCurrentDirectory.setLength(0);
							
							// create a string to represent the clients current directory
							for(int i = 0; i < directoryHolder.size(); i++){
								
								// add the directory to the string builder followed by the separator for the system
								clientsCurrentDirectory.append(directoryHolder.get(i)).append(File.separator);
								
							} // for
							
							// tell the client what the current working directory is
							sendMessage("Current Directory: " + clientsCurrentDirectory.toString());
							
							break;
						default:
							
							// if the message from the client is not the CLIENT_SENDING_FILE_MSG message
							if(!message.equals(CLIENT_SENDING_FILE_MSG)){
								
								// send error message to client
								sendMessage("Incorrect Command! No action taken!");
							} // if
							
							break;
							
						} // switch
					
					} else {
						
						sendMessage("ERROR! Please Enter max of TWO parameters separated by a Space eg. [Command] [parameter]");
					} // if
					
					// server finished send message
					sendMessage(SERVER_FINISHED_MSG);
					
				} catch(ClassNotFoundException classnot){
					
					System.err.println("Data received in unknown format");
					
				} catch(EOFException e){
					
					System.err.println("Connection To Client " + clientID + " Was Lost!");	
					
					break; // to exit the while loop
					
				} // try catch
			
			} // while
      
			System.out.println("Ending Client : ID - " + clientID + " : Address - "
		        + clientSocket.getInetAddress().getHostName());
		} catch (Exception e) {
    	
			e.printStackTrace();
		} finally{
			
			// Closing connection
			
			try{
				
				in.close();
				out.close();
				clientSocket.close();
				
			} catch(IOException ioException){
				
				ioException.printStackTrace();
			} // try catch
			
		} // try catch
		
	} // run()
	
	// login the client in
	// returns true if client login is correct, false if wrong
	boolean clientlogin(){
		
		String user, pass;
		boolean isValidLogin = false;
		
		try {
			
			// tell client to enter username
			sendMessage("Login To Server.");
			sendMessage("Enter Username.");
			
			// send server finished message
			sendMessage(SERVER_FINISHED_MSG);
			
			// read username in
			user = (String)in.readObject();
			
			System.out.println("Client " + clientID + " to Server >  " + user);
			
			// tell user to enter password
			sendMessage("Enter Password.");
			
			// send server finished message
			sendMessage(SERVER_FINISHED_MSG);
			
			// read password in
			pass = (String)in.readObject();
			
			System.out.println("Client " + clientID + " to Server >  " + pass);
			
			if(loginDetails.get(user) != null){
			
				isValidLogin = loginDetails.get(user).equals(pass);
			} else {
				
				isValidLogin = false;
			} // if
			
			// validate login
			if(isValidLogin == true){ // login successful
				
				// tell client login successful
				sendMessage("Login Successful!");
				
				// send server finished message
				sendMessage(SERVER_FINISHED_MSG);
				
				// save the clients username
				clientsUsername = user;
				
				// set the clients home directory to the users folder
				directoryHolder.clear();
				directoryHolder.add("Users");
				directoryHolder.add(clientsUsername);
				
				return true;
				
			} else { // login failed
				
				// tell client login failed
				
				sendMessage("Login Failed! Username Or Password Incorrect!");
				
				// client is disconnected
				// send "bye" message to disconnect client
				sendMessage("bye");
				
				return false;
			} // if
			
		} catch (ClassNotFoundException e) {
			
			System.err.println("Data received in unknown format");
			
			return false;
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
			return false;
		} // try catch
		
	} // clientlogin()
	
} // class
