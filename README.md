# Multi-Threaded-TCP-Client-Server-App
A multi-threaded TCP client and server application coded in Java in Eclipse 4.5.1 (Mars.1)

A Third Year college project for Operating Systems Module.

## Overview
The client connects to the server on port 2004. The options of entering either an IP Address or Domain name for a DNS lookup are available.

When the server starts, it reads login.txt to read the list of usernames and passwords. It adds these to a map so it can validate a users login.

Then, the server creates a folder called Users. This folder will be where all the users folders are stored. The server then loops through the list of usernames in the loginDetails map and creates a folder for each user, named the same as the username.

After a user logs in successfully, their username is stored and their current directory is set to that user's folder in the Users folder on the server.

The server/client is modelled after bash in the linux terminal. 

The server should transfer any file type (.txt, .md and .pdf are the only tested file formats).
Java converts the files into a byte array and sents the array. The other end receives the byte array and creates a file out of it.

### Desgin
Communication between the server and client is as follows:
After login, the client sends a command to the server and then waits until the server is finished. It knows this because of the set SERVER_FINISHED_MSG message.

When the client requests a file, the server sends a SERVER_SENDING_FILE_MSG message to the client to tell it to get ready to receive the file.

When the client is going to send a file to the server, the client sends a CLIENT_SENDING_FILE_MSG message to the server to tell it to get ready to receive a file.

The clients current directory is stored on the thread that is created by the server to manage the client.
The current directory is split up into strings and stored in a List of Strings. When the current directory is needed, it is generated using a string builder.

When the client moves into a new directory, a the directory name is added to the end of the current directory List. When the client moves back one directory, the last item in the List is deleted.

### Controls

##### ls	
Lists all of the files and directories in the clients folder when the client in run from.
Also lists all of the files and directories in the clients current directory on the server.

##### cd .. 
Moves the client back a directory on the server unless the client is already in their home 		directory.

##### cd
Typing cd followed by the name of a directory that is in your current directory, will move you to that directory.

##### mkdir
Like bash, mkdir followed by the name of the directory you want to create, will create that directory.

##### pwd
Like in bash, pwd prints out your current directory.

##### get
Typing get followed by the name of a file eg "file1.txt" or "slides.pdf", will send the file from the server and into client folder that the client is running from. Might crash with files bigger then 1GB.

##### send
Typing send followed by the name of a file will send the file from the client and place it in the clients current directory on the server. Might crash with files bigger then 1GB.
			
