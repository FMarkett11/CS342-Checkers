import java.io.*;
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
	HashMap<String, User> users = new HashMap<>();;
	HashMap<String, ClientThread> loggedIn = new HashMap<>();
	HashMap<String, ArrayList<String>> groups = new HashMap<>();
	
	
	Server(Consumer<Serializable> call){
		callback = call;
		server = new TheServer();
		server.start();
	}
	

	public class TheServer extends Thread{

		public void loadUsers() {
			try {
				ObjectInputStream in = new ObjectInputStream(new FileInputStream("users.dat"));
				users = (HashMap<String, User>) in.readObject();
				in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void run() {
			loadUsers();
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
				ClientThread t = loggedIn.get(message.recipient);
				ClientThread s = loggedIn.get(sender);
				try {
					t.out.writeObject(message);
					s.out.writeObject(message);
				}
					catch(Exception e) {
					}
			}
			public void updateSingleClient(Message message){
				ClientThread t = loggedIn.get(message.recipient);
				try {
					t.out.writeObject(message);
				}
				catch(Exception e) {
				}
			}


			//THIS NEEDS TO BE CALLED WHENEVER STATS ARE UPDATED IN ANY WAY.
			public void saveUsers() throws Exception{
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("users.dat"));
				out.writeObject(users);
				out.close();
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

							//Handle a user logging in
							if(data.type.equals("login")) {
								callback.accept("client #" + count + "is trying to log into " + data.sender);
								if(!users.containsKey(data.sender)){
									out.writeObject(new Message("login_error", "server", data.sender, "User not found."));
									continue;
								}
								if(!users.get(data.sender).password.equals(data.message)){
									out.writeObject(new Message("login_error", "server", data.sender, "Incorrect password."));
									continue;
								}
								if(loggedIn.containsKey(data.sender)){
									out.writeObject(new Message("login_error", "server", data.sender, "User logged in elsewhere."));
									continue;
								}
								loggedIn.put(data.sender, this);
								this.uname = data.sender;

								out.writeObject(new Message("login_success", "server", uname, "Welcome!"));
								updateClients(new Message("notification","server", "ALL", "new client on server: " + this.uname));
								updateClients(new Message("user_list", "server", null, loggedIn.keySet().toString().substring(1, loggedIn.keySet().toString().length() - 1)));
							}

							//Handle a message being sent
							if(data.type.equals("message")){
								callback.accept(data.sender + " sent: " + data.message + " to " + data.recipient);
								if(data.recipient.equals("ALL")){
									updateClients(new Message("message", "server", data.recipient, data.sender +" said: "+data.message));
								} else {
									updateSingleClient(new Message("message", "server", data.recipient, data.sender + " said: " + data.message), data.sender);
								}
							}

							//Handle creating a group (also currently not in use)
							if(data.type.equals("group_create")){
								callback.accept(data.sender + " created a group: " + data.recipient + " with members " + data.message);
								if(groups.containsKey(data.recipient)){
									out.writeObject(new Message("errorg", "server", data.sender, "Group name taken"));
									return;
								}
								groups.put(data.recipient, new ArrayList<String>());
								groups.get(data.recipient).add(data.sender);
								for(String s : data.message.split(", ")){
									if(!loggedIn.containsKey(s)){
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

							//Handle sending messages to a group (currently not in use)
							if(data.type.equals("group_send")){
								callback.accept(data.sender + " sent: " + data.message + " to group " + data.recipient);
								for(String i : groups.get(data.recipient)){
									if(!loggedIn.containsKey(i)){
										return;
									}
									updateSingleClient(new Message("message", "server", i, data.sender + " said to group " + data.recipient + ": " + data.message));
								}
							}

							//Handle account creation
							if(data.type.equals("creation")){
								callback.accept("client #" + count + "has requested to create account: " + data.sender);
								if(users.containsKey(data.sender)) {
									callback.accept("client #" + count + " has failed to set their username to " + data.sender);
									out.writeObject(new Message("creation_error", "server", data.sender, "Username taken"));
								} else {
									User newUser = new User(data.sender, data.message);
									users.put(data.sender, newUser);
									try{
										saveUsers();
									} catch (Exception e){
										callback.accept("Error creating user" + data.sender);
										out.writeObject(new Message("creation_error", "server", data.sender, "Error saving user, try again."));
									}
									callback.accept("client #" + count + "has created their account: " + data.sender);
									out.writeObject(new Message("successful_creation", "server", data.sender, "You have successfully made your account, welcome " + data.sender + "!"));
								}

							}
					    	
					    	}
					    catch(Exception e) {
							callback.accept("Something wrong with the socket from client: " + count + "....closing down!");
							updateClients(new Message("notification", "server", "ALL", this.uname + " has left the server!"));
							loggedIn.remove(this.uname);
							clients.remove(this);
							updateClients(new Message("user_list", "server", null, loggedIn.keySet().toString().substring(1, loggedIn.keySet().toString().length() - 1)));
							break;
						}
					}
				}//end of run


			
		}//end of client thread
}


	
	

	
