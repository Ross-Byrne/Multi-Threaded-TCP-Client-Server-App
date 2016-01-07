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
 	
 	// to receive files
 	FileOutputStream fos;
 	
 	// when client receives server finished message, client can send message to server
 	static final String SERVER_FINISHED_MSG = "_server+finished_";
 	static final String SERVER_SENDING_FILE_MSG = "_server+sending+file_";
	
 	public static void main(String args[]){
 		
 		Requester client = new Requester();
 		client.run();
 		
 	} // main()
 	
 	void receiveFile(String fileName, int fileSize){
 		
 		// create the downloads directory file
 		File file = new File("Downloads");
 		
 		// make the file a directory
 		file.mkdirs();
 		
 		try {
 			
 			// create an array of btyes
 			// receive an array of bytes that make up the file and place them in fileBytes
 		    byte[] fileBytes = (byte[]) in.readObject();
 		    
 		    // create a file output stream
 		    fos = new FileOutputStream("Downloads" + File.separator + fileName);
 		    
 		    // write the received file bytes to the file
 		    fos.write(fileBytes);
 		    
 		    // close the file output stream
 			fos.close();
 			
		} catch (FileNotFoundException e) {

			System.out.println("ERROR, File not found!");
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // try catch
 		
 	} // receiveFile()
	
	public void run(){
		
		stdin = new Scanner(System.in);
		
		boolean canSendMessage = false;
		
		try{
			//1. creating a socket to connect to the server
			
			System.out.println("Please Enter your IP Address");
			ipaddress = stdin.next();
			stdin.nextLine(); // flush buffer
			
			
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
					
					// read message
					message = (String)in.readObject();
					
					// only show message from server if it's not server finished message
					if(!message.equals(SERVER_FINISHED_MSG))
						System.out.println("Server > " + message);
					
					// if the server is finsihed sending messages
					if(message.equals(SERVER_FINISHED_MSG)){
						
						// the client can now send a message
						canSendMessage = true;
						message = "";
					} // if
					
					// if the client is allowed send a message
					if(canSendMessage){
					
						// send message to server
						
						System.out.print("Client > ");
						message = stdin.nextLine();
						
						sendMessage(message);
						
						// message sent, cant send again until server says so
						canSendMessage = false;
					
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
			//System.out.println("client>" + msg);
		} catch(IOException ioException){
			
			ioException.printStackTrace();
		} // try catch
		
	} // sendMessage()
	
} // class