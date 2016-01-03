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
 	Scanner stdin;
 	
 	// when client receives server finished message, client can send message to server
 	static final String SERVER_FINISHED_MSG = "_server+finished_";
	
 	public static void main(String args[]){
 		
 		Requester client = new Requester();
 		client.run();
 		
 	} // main()
	
	public void run(){
		
		stdin = new Scanner(System.in);
		
		boolean canSendMessage = false;
		
		try{
			//1. creating a socket to connect to the server
			
			System.out.println("Please Enter your IP Address");
			ipaddress = stdin.next();
			
			// DNS Lookup "rbdevelop.cloudapp.net"
			
			//System.out.println("Performing DNS Lookup On 'rbdevelop.cloudapp.net'");
			
			// perform DNS lookup on "rbdevelop.cloudapp.net"
			//inetAddress = InetAddress.getByName("rbdevelop.cloudapp.net");
			
			// get ip address from lookup
			//ipaddress = inetAddress.getHostAddress();
			
			requestSocket = new Socket(ipaddress, 2004);
			System.out.println("Connected to "+ipaddress+" in port 2004");
			
			//2. get Input and Output streams
			
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(requestSocket.getInputStream());
			System.out.println("Hello");
			
			//3: Communicating with the server
			
			do{
				try{
						
					message = (String)in.readObject();
					
					if(!message.equals(SERVER_FINISHED_MSG))
						System.out.print("\nServer > " + message);
					
					// if the server is finsihed sending messages
					if(message.equals(SERVER_FINISHED_MSG)){
						
						// the client can now send a message
						canSendMessage = true;
						message = "";
						stdin.nextLine(); // flush buffer
					} // if
					
					// if the client is allowed send a message
					if(canSendMessage){
					
						// send message to server
						
						System.out.print("\nClient > ");
						message = stdin.next();
						
						sendMessage(message);
					
					} // if
					
				} catch(ClassNotFoundException classNot){
					
					System.err.println("data received in unknown format");
				} // try catch
				
			}while(!message.equals("bye")); // do while
			
		} catch(UnknownHostException unknownHost){
			
			System.err.println("You are trying to connect to an unknown host!");
			
		} catch(IOException ioException){
			
			ioException.printStackTrace();
		} finally{
			
			//4: Closing connection
			
			try{
				
				in.close();
				out.close();
				requestSocket.close();
				
			} catch(IOException ioException){
				
				ioException.printStackTrace();
			} // try catch
			
		} // try catch
		
	} // run()
	
	void sendMessage(String msg){
		
		try{
			out.writeObject(msg);
			out.flush();
			System.out.println("client>" + msg);
		} catch(IOException ioException){
			
			ioException.printStackTrace();
		} // try catch
		
	} // sendMessage()
	
} // class