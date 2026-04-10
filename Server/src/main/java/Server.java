import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.scene.control.ListView;
/*
 * Clicker: A: I really get it    B: No idea what you are talking about
 * C: kind of following
 */

public class Server{

	int count = 1;	
	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	TheServer server;
	private Consumer<Serializable> callback;
	HashMap<String, ClientThread> usernames = new HashMap<>();
	HashMap<String, ArrayList<String>> groups = new HashMap<>();
	
	
	Server(Consumer<Serializable> call){
	
		callback = call;
		server = new TheServer();
		server.start();
	}
	
	
	public class TheServer extends Thread{
		
		public void run() {
		
			try(ServerSocket mysocket = new ServerSocket(5555);){
		    System.out.println("Server is waiting for a client!");
		  
			
		    while(true) {
		
				ClientThread c = new ClientThread(mysocket.accept(), count);
				callback.accept("client has connected to server: " + "client #" + count);
				clients.add(c);
				c.start();
				
				count++;
				
			    }
			}//end of try
				catch(Exception e) {
					callback.accept("Server socket did not launch");
				}
			}//end of while
		}
	

		class ClientThread extends Thread{
			
		
			Socket connection;
			int count;
			ObjectInputStream in;
			ObjectOutputStream out;
			String uname;
			
			ClientThread(Socket s, int count){
				this.connection = s;
				this.count = count;	
			}
			
			public void updateClients(Message message) {
				for(int i = 0; i < clients.size(); i++) {
					ClientThread t = clients.get(i);
					try {
					 t.out.writeObject(message);
					}
					catch(Exception e) {}
				}
			}

			public void updateSingleClient(Message message, String sender){
				ClientThread t = usernames.get(message.recipient);
				ClientThread s = usernames.get(sender);
				try {
					t.out.writeObject(message);
					s.out.writeObject(message);
				}
					catch(Exception e) {
					}
			}
			public void updateSingleClient(Message message){
				ClientThread t = usernames.get(message.recipient);
				try {
					t.out.writeObject(message);
				}
				catch(Exception e) {
				}
			}
			
			public void run(){
					
				try {
					in = new ObjectInputStream(connection.getInputStream());
					out = new ObjectOutputStream(connection.getOutputStream());
					connection.setTcpNoDelay(true);	
				}
				catch(Exception e) {
					System.out.println("Streams not open");
				}
					
				 while(true) {
					    try {
					    	Message data = (Message) in.readObject();
							if(data.type.equals("login")) {

								if(usernames.containsKey(data.sender)) {
									callback.accept("client #" + count + " has failed to set their username to " + data.sender);
									out.writeObject(new Message("error", "server", data.sender, "Username taken"));
								} else if(data.sender.contains(",")){
									callback.accept("client #" + count + " has failed to set their username to " + data.sender);
									out.writeObject(new Message("error", "server", data.sender, "Username may not contain ,"));
								} else if(data.sender.length() > 20){
									callback.accept("client #" + count + " has failed to set their username to " + data.sender);
									out.writeObject(new Message("error", "server", data.sender, "Username may not be longer than 20 characters buster."));
								}
								else {
									callback.accept("client #" + count + " has set their username to " + data.sender);
									usernames.put(data.sender, this);
									this.uname = data.sender;

									out.writeObject(new Message("login_success", "server", uname, "Welcome!"));
									updateClients(new Message("notification","server", "ALL", "new client on server: " + this.uname));
									updateClients(new Message("user_list", "server", null, usernames.keySet().toString().substring(1, usernames.keySet().toString().length() - 1)));
								}
							}
							if(data.type.equals("message")){
								callback.accept(data.sender + " sent: " + data.message + " to " + data.recipient);
								if(data.recipient.equals("ALL")){
									updateClients(new Message("message", "server", data.recipient, data.sender +" said: "+data.message));
								} else {
									updateSingleClient(new Message("message", "server", data.recipient, data.sender + " said: " + data.message), data.sender);
								}
							}
							if(data.type.equals("group_create")){
								callback.accept(data.sender + " created a group: " + data.recipient + " with members " + data.message);
								if(groups.containsKey(data.recipient)){
									out.writeObject(new Message("errorg", "server", data.sender, "Group name taken"));
									return;
								}
								groups.put(data.recipient, new ArrayList<String>());
								groups.get(data.recipient).add(data.sender);
								for(String s : data.message.split(", ")){
									if(!usernames.containsKey(s)){
										out.writeObject(new Message("errorg", "server", data.sender, "Member " + s + " does not exist"));
										continue;
									}
									groups.get(data.recipient).add(s);
									updateSingleClient(new Message("group_creation", "server", s, data.recipient));
									updateSingleClient(new Message("notification", "server", s, data.sender + " has added you to group " + data.recipient + " with " + data.message + ", " + data.sender));
								}
								updateSingleClient(new Message("group_creation", "server", data.sender, data.recipient));
								updateSingleClient(new Message("notification", "server", data.sender, "you have created " + data.recipient + " with members " + data.message));
							}
							if(data.type.equals("group_send")){
								callback.accept(data.sender + " sent: " + data.message + " to group " + data.recipient);
								for(String i : groups.get(data.recipient)){
									if(!usernames.containsKey(i)){
										return;
									}
									updateSingleClient(new Message("message", "server", i, data.sender + " said to group " + data.recipient + ": " + data.message));
								}
							}
					    	
					    	}
					    catch(Exception e) {
					    	callback.accept("Something wrong with the socket from client: " + count + "....closing down!");
					    	updateClients(new Message("notification" , "server", "ALL", this.uname + " has left the server!"));
							usernames.remove(this.uname);
					    	clients.remove(this);
							updateClients(new Message("user_list", "server", null, usernames.keySet().toString().substring(1, usernames.keySet().toString().length() - 1)));
					    	break;
					    }
					}
				}//end of run
			
			
		}//end of client thread
}


	
	

	
