import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
/*
 * Clicker: A: I really get it    B: No idea what you are talking about
 * C: kind of following
 */

public class Server{

	int count = 1;	
	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	TheServer server;
	private Consumer<Serializable> callback;
	ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();;
	ConcurrentHashMap<String, ClientThread> loggedIn = new ConcurrentHashMap<>();
	ConcurrentHashMap<String, ArrayList<String>> groups = new ConcurrentHashMap<>();
	Set<User> hosts = Collections.synchronizedSet(new HashSet<>());
	ConcurrentHashMap<User, User> matchesh2j = new ConcurrentHashMap<>();
	ConcurrentHashMap<User, User> matchesj2h = new ConcurrentHashMap<>();
	private final Object matchLock = new Object();
	
	
	Server(Consumer<Serializable> call){
		callback = call;
		server = new TheServer();
		server.start();
	}
	

	public class TheServer extends Thread{

		public void loadUsers() {
			try {
				ObjectInputStream in = new ObjectInputStream(new FileInputStream("users.dat"));
				users = (ConcurrentHashMap<String, User>) in.readObject();
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
								updateClients(new Message("host_list", "server", null, hosts.toString().substring(1, hosts.toString().length() - 1)));

								callback.accept("client #" + count + "has logged into " + data.sender);
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

							//If a user requests to host a lobby
							if(data.type.equals("host")){
								callback.accept(data.sender + " is hosting a lobby");
								synchronized (matchLock){
									hosts.add(users.get(data.sender));
									updateClients(new Message("host_list", "server", null, hosts.toString().substring(1, hosts.toString().length() - 1)));
									updateSingleClient(new Message("hosting_started", "server", data.sender, ""));
								}
							}

							if(data.type.equals("unhost")){
								callback.accept(data.sender + " is no longer hosting a lobby");
								User host = users.get(data.sender);
								synchronized(matchLock){
									hosts.remove(host);
									User joiner = matchesh2j.get(host);
									if (joiner != null) {
										updateSingleClient(new Message("leave_lobby", "server", joiner.toString(), data.sender + " has stopped hosting!"));
										cleanupMatch(host, joiner);
									}
									updateClients(new Message("host_list", "server", null, hosts.toString().substring(1, hosts.toString().length() - 1)));
								}
							}

							if(data.type.equals("leave_lobby")){
								User joiner = users.get(data.sender);
								synchronized(matchLock){
									User host = matchesj2h.get(joiner);
									if (host == null) {
										callback.accept("WARNING: No host found for " + data.sender);
										return;
									}
									callback.accept(data.sender + " has left " + host + "'s lobby.");

									updateSingleClient(new Message("leave_lobby", "server", host.toString(), data.sender + " has left your lobby!"));

									cleanupMatch(host, joiner);
									updateClients(new Message("host_list", "server", null, hosts.toString().substring(1, hosts.toString().length() - 1)));
								}
							}

							//If a user requests to join a lobby
							if(data.type.equals("join")){
								User host = users.get(data.recipient);
								User joiner = users.get(data.sender);
								synchronized(matchLock){
									if (host == null || joiner == null) {
										return;
									}
									if (!hosts.contains(host)) {
										updateSingleClient(new Message("error", "server", data.sender, "Host is not available"));
										return;
									}
									if (matchesj2h.containsKey(joiner)) {
										updateSingleClient(new Message("error", "server", data.sender, "Already in a lobby"));
										return;
									}
								}
								//Create the match (SEE makeMatch() for more info)
								makeMatch(host, joiner);
								callback.accept(data.sender + " has joined " + data.recipient + "'s lobby!");
							}

							if(data.type.equals("request_host_list")){
								callback.accept(data.sender + " has requested the host list");
								updateSingleClient(new Message("host_list", "server", data.sender, hosts.toString().substring(1, hosts.toString().length() - 1)));
							}
						}
						catch(Exception e) {
							//uncomment for debugging
							//e.printstacktrace()
							callback.accept("Something wrong with the socket from client: " + count + "....closing down!");
							User disconnectingUser = users.get(this.uname);
							if (disconnectingUser != null) {
								synchronized (matchLock) {
									//If user is a joiner
									User host = matchesj2h.get(disconnectingUser);
									if (host != null) {
										updateSingleClient(new Message("leave_lobby", "server", host.toString(), this.uname + " has left your lobby!"));
										cleanupMatch(host, disconnectingUser);
									}
									// If user is a host
									User joiner = matchesh2j.get(disconnectingUser);
									if (joiner != null) {updateSingleClient(new Message("leave_lobby", "server", joiner.toString(), this.uname + " has stopped hosting!"));
										cleanupMatch(disconnectingUser, joiner);
									}
								}
							}
						}
					}
				}//end of run

			private void cleanupMatch(User host, User joiner) {
				synchronized (matchLock) {
					if (host != null) {
						matchesh2j.remove(host);
						hosts.add(host);
					}
					if (joiner != null) {
						matchesj2h.remove(joiner);
					}
				}
			}

			private void makeMatch(User host, User joiner) {
				synchronized (matchLock) {
					// Check validity
					if (host == null || joiner == null) return;
					if (!hosts.contains(host)) return;
					if (matchesj2h.containsKey(joiner)) return;
					// Create match
					matchesh2j.put(host, joiner);
					matchesj2h.put(joiner, host);
					// Remove host from available pool
					hosts.remove(host);
				}
				// Notify both players
				updateSingleClient(new Message("match_created", "server", joiner.toString(), "You have successfully joined " + host));

				updateSingleClient(new Message("match_created", "server", host.toString(), joiner + " has joined your lobby."));

				// Update host list for everyone
				updateClients(new Message("host_list", "server", null, hosts.toString().substring(1, hosts.toString().length() - 1)));
			}


			
		}//end of client thread
}


	
	

	
