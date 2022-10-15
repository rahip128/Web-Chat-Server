# Web-Chat-Server

This a build of a basic chat server in Java that allows users to login with a username and password, and post messages to a chat log. The server uses HTTP as the base protocol. Which uses strings as the base data-type and message have a structure with a header consisting of variable-value pairs, followed by a body with data.

  
  These are the steps taken to complete this program.

	Write a very simple server that creates a server socket or servlet object.
	Connect the python3 client to the server.
	When the client connects, it reads the input HTTP message from the socket.
	Parses the message into lines and words. 
	Get the HTTP commands from the parsed message.
	Calls a method to handle the HTTP command.
	Builds a string of the HTTP response to the command.
	Writes the response back to the client on the socket.
