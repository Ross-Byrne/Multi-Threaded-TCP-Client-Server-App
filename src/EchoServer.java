

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
	    
	    // load login details into map from login.txt
	    
	    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(LOGIN_FILE)));
		String line = null;
		
		// read a line at a time
		while((line = br.readLine()) != null){
			
			// split the line at the space
			String [] login = line.split(" ");
			
			// add the username and password to the login details
			loginDetails.put(login[0].toString(), login[1].toString());
		
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
	
	Socket clientSocket;
	String message;
	int clientID = -1;
	boolean running = true;
	ObjectOutputStream out;
	ObjectInputStream in;
	Map<String, String> loginDetails;
	
	boolean clientIsLoggedIn = false;
	
	// Constructor
	ClientServiceThread(Socket s, int i, Map<String, String> loginMap) {
		
		clientSocket = s;
	    clientID = i;
	    loginDetails = new HashMap<String, String>(loginMap);
	    
	} // ClientServiceThread()

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
					
				} catch(ClassNotFoundException classnot){
					
					System.err.println("Data received in unknown format");
				} // try catch
			
			} // while
      
			System.out.println("Ending Client : ID - " + clientID + " : Address - "
		        + clientSocket.getInetAddress().getHostName());
		} catch (Exception e) {
    	
			e.printStackTrace();
		} // try catch
		
	} // run()
	
	// login the client in
	// returns true if client login is correct, false if wrong
	boolean clientlogin(){
		
		String user, pass;
		
		try {
			
			// tell client to enter username
			sendMessage("Login To Server.\nEnter Username.");
			
			// server finished send message
			sendMessage(SERVER_FINISHED_MSG);
			
			// read username in
			user = (String)in.readObject();
			
			System.out.println("Client to Server >  " + user);
			
			// tell user to enter password
			sendMessage("Enter Password.");
			
			// server finished send message
			sendMessage(SERVER_FINISHED_MSG);
			
			// read password in
			pass = (String)in.readObject();
			
			System.out.println("Client to Server >  " + pass);
			
			System.out.println(loginDetails.get("user1"));
			
			
			// validate login
			if(loginDetails.get(user).equals(pass)){ // login successful
				
				// tell client login successful
				sendMessage("Login Successful!");
				
				// server finished send message
				sendMessage(SERVER_FINISHED_MSG);
				
				return true;
				
			} else { // login failed
				
				// tell client login failed
				
				sendMessage("Login Failed! Username Or Password Incorrect!");
				
				// server finished send message
				sendMessage(SERVER_FINISHED_MSG);
				
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
