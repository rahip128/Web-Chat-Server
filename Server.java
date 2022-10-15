/* Notes: 
 * This code is modified from the original to work with 
 * the CS 352 chat client:
 *
 * 1. added args to allow for a command line to the port 
 * 2. Added 200 OK code to the sendResponse near line 77
 * 3. Changed default file name in getFilePath method to ./ from www 
 */ 

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// Read the full article https://dev.to/mateuszjarzyna/build-your-own-http-server-in-java-in-less-than-one-hour-only-get-method-2k02
public class Server {

	public static int iteration = 0;
	public static String curr_user = "";

	public static void main( String[] args ) throws Exception {

		if (args.length != 1) {
			System.err.println("Usage: java Server <port number>");
			System.exit(1);
		}
		//create server socket given port number
		int portNumber = Integer.parseInt(args[0]);
		try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
			while (true) {
				try (Socket client = serverSocket.accept()) {
					handleClient(client);
				}
			}
		}
	}

	private static void handleClient(Socket client) throws IOException {
		iteration ++;
		
		System.out.println("ITERATION IS : " + iteration);
		BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
		StringBuilder requestBuilder = new StringBuilder();
		String line;
		while (!(line = br.readLine()).isBlank()) {
			requestBuilder.append(line + "\r\n");
		}
		
		String str = "";
		if(iteration % 4 == 2){ //saving the username and password to pass into the validate login function
		
			while (br.ready()){ //use this to find the username and password - going to validate it.
				str+=(char)br.read();
			}
			
		}
		
		if(iteration % 4 == 3){
		
			while (br.ready()){ //use this to find the message to store it
				str+=(char)br.read();
			}
		}
		
		String request = requestBuilder.toString();
		System.out.printf("The request is: %s \n", request);
		String[] requestsLines = request.split("\r\n");
		String[] requestLine = requestsLines[0].split(" ");
		String method = requestLine[0];
		String path = requestLine[1];
		String version = requestLine[2];
		String host = requestsLines[1].split(" ")[1];
		
		// build the reponse here 
		List<String> headers = new ArrayList<>();
		for (int h = 2; h < requestsLines.length; h++) {
			String header = requestsLines[h];
			headers.add(header);
			System.out.println("PRINTING HEADERS: " + header);
		}
		
		String accessLog = String.format("Client %s, method %s, path %s, version %s, host %s, headers %s", client.toString(), method, path, version, host, headers.toString());
		System.out.println(accessLog);

		if(iteration % 4 == 1){ //we are just getting the login page for the first test
			path = path.concat("login.html");
			
			Path filePath = getFilePath(path);
			if (Files.exists(filePath)) {
				//System.out.println("FILE PATH IS: " + filePath);
				String contentType = guessContentType(filePath);
				sendResponse(client, "200 OK", contentType, Files.readAllBytes(filePath));
			} else {
				// 404
				byte[] notFoundContent = "<h1>Not found :(</h1>".getBytes();
				sendResponse(client, "404 Not Found", "text/html", notFoundContent);
			}
		} else if(iteration % 4 == 2){ //check login credentials before displaying the chat page if its a POST login request
			curr_user= validateLogin(client, path, str);
			
			System.out.println("HEREEEE THE CURRENT USER IS: " + curr_user);
		} else if(iteration % 4 == 3){ //store message if its a POST request for the chat page
			storeMessage(client, str, curr_user);
		} else if (iteration % 4 == 0){
			getMessages(client);
		}
	}
	
	private static String validateLogin(Socket client, String path, String str) throws IOException{
	
		String curr_user = "";
		String curr_pass = "";
		boolean isValid = false;
		String curr[] = str.split("=");
		String getting_user[] = curr[1].split("&");
		curr_user = getting_user[0];
		curr_pass = curr[2];
		
		//validate the username and password: 
		
		File file = new File("./login/credentials.txt");
		
		BufferedReader bufRead = new BufferedReader(new FileReader(file));
		String st;
		
		while ((st = bufRead.readLine()) != null){
		
			String cred[] = st.split(",");
			if(curr_user.equals(cred[0]) && curr_pass.equals(cred[1]) && str != ""){
				System.out.println("CREDENTIALS ARE VALID");
				isValid = true;
			}
		}
		
		if(isValid == false){ //if the credentials are not valid, return the error page
		
			iteration = 0; //reset the number of iterations we have been through
			path = path.concat("error.html");
			Path filePath = getFilePath(path);
			
			if (Files.exists(filePath)) {
				String contentType = guessContentType(filePath);
				sendResponse(client, "200 OK", contentType, Files.readAllBytes(filePath));
			} else {
				// 404
				byte[] notFoundContent = "<h1>Not found :(</h1>".getBytes();
				sendResponse(client, "404 Not Found", "text/html", notFoundContent);
			}
		} else if (isValid == true){ // if credentials are valid, show the chat page.
		
			path = "/chat/chat.html";
			
			Path filePath = getFilePath(path);
			
			System.out.println("FILE PATH IS: " + filePath);
			if (Files.exists(filePath)) {
			
				String contentType = guessContentType(filePath);
				sendResponse(client, "200 OK", contentType, Files.readAllBytes(filePath));
			} else {
			// 404
				byte[] notFoundContent = "<h1>Not found :(</h1>".getBytes();
				sendResponse(client, "404 Not Found", "text/html", notFoundContent);
			}
		}
		
		return curr_user;
	}
	
	private static void storeMessage(Socket client, String str, String user) throws IOException{
	
	    	String getMessage[] = str.split("=");
	    	String message = getMessage[1];
	    	
	    	message = message.concat("\n");
	    	
	    	System.out.println("USER IS: " + user );
	    	user = user.concat(": ");
	    	
	    	String finalMessage = user.concat(message);
	    	
	    	System.out.println("USER IS: " + user + "FINAL MESSAGE IS: " + finalMessage);
	    	
	    	//Path messagePath = getFilePath("/messages.txt");
	    	
	    	File messagePath = new File("messages.txt");
	    	
	    	messagePath.createNewFile(); //create a file that stores all messages if it doesnt exist.
	    	
	    	//add the user and message to the messages.txt file
	    	
	    	final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("messages.txt", true));
		 bufferedWriter.write(finalMessage);
		 bufferedWriter.close();
	     	
	    	String path = "/chat/chat.html";

		Path filePath = getFilePath(path);
			
		if (Files.exists(filePath)) {
		    		
		    	String contentType = guessContentType(filePath);
		    	sendResponse(client, "200 OK", contentType, Files.readAllBytes(filePath));
		} else {
		    	// 404
		    	byte[] notFoundContent = "<h1>Not found :(</h1>".getBytes();
		    	sendResponse(client, "404 Not Found", "text/html", notFoundContent);
		}
		
	}
	
	private static void getMessages(Socket client) throws IOException{
	
	    	String path = "/chat/chat.html";
		Path filePath = getFilePath(path);

		
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		
		String firstHalf = "";
		String secondHalf = "";
   		try {
			BufferedReader in = new BufferedReader(new FileReader(path));
			String str;
			
			while ((str = in.readLine()) != "<div id=chat-window>") {
				firstHalf += str;
			}
			in.close();
		} catch (IOException e) {
    		}
    		
    		byte[] fHalf = firstHalf.getBytes();
    		try {
			BufferedReader in = new BufferedReader(new FileReader(path));
			String str;
			
			while ((str = in.readLine()) != null) {
				secondHalf += str;
			}
			in.close();
		} catch (IOException e) {
    		}
    		
    		byte[] sHalf = secondHalf.getBytes();
    		
 		//implement reading of message.txt
 		String messages = "";
 		try {
			BufferedReader in = new BufferedReader(new FileReader(path));
			String str;
			
			while ((str = in.readLine()) != null) {
				messages += str;
			}
			in.close();
		} catch (IOException e) {
    		}
    		
    		byte[] m = messages.getBytes();
    		
    		clientOutput.write(m);
    		
    		
		byte[] editedHTML;//  fHald + chat messages + sHalf;
		
		
		if (Files.exists(filePath)) {		
			String contentType = guessContentType(filePath);
			
			//
			//pass editedHTML into sendResponse instead of Files.readAllBytes(filePath))
			// 
			sendResponse(client, "200 OK", contentType, fHalf);
			
			
			
		} else {
			// 404
			byte[] notFoundContent = "<h1>Not found :(</h1>".getBytes();
			sendResponse(client, "404 Not Found", "text/html", notFoundContent);
		}
		
	}
	
	
	private static void sendResponse(Socket client, String status, String contentType, byte[] content) throws IOException {
		System.out.println("THE CLIENT IS: " + client);
		OutputStream clientOutput = client.getOutputStream();
		//clientOutput.write(("HTTP/1.1 200 OK\r\n" + status).getBytes());
		clientOutput.write(("HTTP/1.1 200 OK" + status + "\r\n").getBytes());
		clientOutput.write(("ContentType: " + contentType + "\r\n").getBytes());
		if(iteration % 4 == 2){
			String cookie = "123456";
			clientOutput.write(("Set-Cookie: userID:=7625145\r\n").getBytes());
		}
		
		clientOutput.write("\r\n".getBytes());
		clientOutput.write(content); //this is the line that writes the html
		clientOutput.write("\r\n\r\n".getBytes());
		clientOutput.flush();
		client.close();
	}
	
	private static Path getFilePath(String path) {
		if ("/".equals(path)) {
			path = "/index.html";
		}

		return Paths.get("./", path);
	}
	
	private static String guessContentType(Path filePath) throws IOException {
		return Files.probeContentType(filePath);
	}
}
