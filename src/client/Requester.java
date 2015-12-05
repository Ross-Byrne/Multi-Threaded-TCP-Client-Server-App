package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Requester{
	
	Socket requestSocket;
	ObjectOutputStream out;
 	ObjectInputStream in;
 	String message="";
 	String ipaddress;
 	Scanner stdin;
 	
 	// Constructor
	Requester(){
		
	} // Requester()
	
	void run(){
		
		stdin = new Scanner(System.in);
		try{
			//1. creating a socket to connect to the server
			System.out.println("Please Enter your IP Address");
			ipaddress = stdin.next();
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
					System.out.println("Please Enter the Message to send...");
					message = stdin.next();
					sendMessage(message);
					
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
	
	// Main
	
	public static void main(String args[]){
		
		Requester client = new Requester();
		client.run();
		
	} // main()
	
} // class