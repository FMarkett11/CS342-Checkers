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

	//Stores the number of clients connected
	int count = 1;
	//An list of the connected clients
	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	//The actual server thread which start will be called on
	TheServer server;
	//The callback which just prints to a log
	private Consumer<Serializable> callback;
	//A thread safe hashmap that stores a username and its respective User object
	ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
	//A thread safe hashmap that stores the username of currently logged in users and its respective clientThread
	ConcurrentHashMap<String, ClientThread> loggedIn = new ConcurrentHashMap<>();
	//A thread safe hashmap that stores a groupname and the usernames of members in that group
	//(Currently unused)
	ConcurrentHashMap<String, ArrayList<String>> groups = new ConcurrentHashMap<>();
	//A Hashmap that stores a gameboard in relation to a host
	ConcurrentHashMap<User, checkersBoard> boards = new ConcurrentHashMap<>();
	//A thread safe set that stores the current hosts
	Set<User> hosts = Collections.synchronizedSet(new HashSet<>());
	//A thread safe hashmap that stores a match with a host -> a user who joined the host
	ConcurrentHashMap<User, User> matchesh2j = new ConcurrentHashMap<>();
	//A thread safe hashmap that stores the opposite of the hashmap above user -> host
	ConcurrentHashMap<User, User> matchesj2h = new ConcurrentHashMap<>();
	//A lock which synchronizes anything that alters the three datatypes above
	private final Object matchLock = new Object();
	//Stores whos turn it is
	ConcurrentHashMap<User, String> turns = new ConcurrentHashMap<>();
	Set<User> rematch = Collections.synchronizedSet(new HashSet<>());
	Set<User> draw = Collections.synchronizedSet(new HashSet<>());
	
	
	Server(Consumer<Serializable> call){
		//Setup the callback
		callback = call;
		//Create the serverthread
		server = new TheServer();
		//Start the server
		server.start();
	}


	//A class which stores the server thread itself
	public class TheServer extends Thread{

		//Loads in the users and their information from the users.dat file
		public void loadUsers() {
			try {
				//Create a new input stream that reads the file stream users.dat
				ObjectInputStream in = new ObjectInputStream(new FileInputStream("users.dat"));
				//Store the information thats read in the users concurrent hashmap (allowed since users is serializable)
				users = (ConcurrentHashMap<String, User>) in.readObject();
				//Close the file
				in.close();
				//On an exception print the stack trace
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		//Main thread that runs on server start
		public void run() {
			//Load in the users
			loadUsers();
			//Create a new socket that reads from port 5555
			try(ServerSocket mysocket = new ServerSocket(5555);){
			//Print to the terminal that the server is waiting for a client
		    System.out.println("Server is waiting for a client!");

			//While the server is active
		    while(true) {
				//Create a new client thread when a new client is accepted
				ClientThread c = new ClientThread(mysocket.accept(), count);
				//Print to the log that a client has connected to the server
				callback.accept("client has connected to server: " + "client #" + count);
				//Add the client to the client list
				clients.add(c);
				//Start up a new client thread
				c.start();

				//Iterate the count of clients on the server
				count++;
				
			    }
			}//end of try
			//On exception just print that the server socket didn't launch to the log
				catch(Exception e) {
					callback.accept("Server socket did not launch");
				}
			}//end of while
		}
	

		class ClientThread extends Thread{
			
			//The main connection
			Socket connection;
			//The count that this client is
			int count;
			//This client's input stream
			ObjectInputStream in;
			//This client's output stream
			ObjectOutputStream out;
			//This client's username
			String uname;
			//Determines what color piece the client is

			//Constructor
			ClientThread(Socket s, int count){
				this.connection = s;
				this.count = count;	
			}

			//Sends a message to every client on the server
			public void updateClients(Message message) {
				//For every client
				for(int i = 0; i < clients.size(); i++) {
					ClientThread t = clients.get(i);
					try {
					//Send a message to the client t
					 t.out.writeObject(message);
					}
					//Ignore exceptions
					catch(Exception e) {}
				}
			}

			//Send a message to a single client on the server, also sends it to the sender of that message
			public void updateSingleClient(Message message, String sender){
				//Get the client thread for the sender and the intended recipient
				ClientThread t = loggedIn.get(message.recipient);
				ClientThread s = loggedIn.get(sender);
				//Try to send the sender and recipient the message
				try {
					t.out.writeObject(message);
					s.out.writeObject(message);
				}
				//Ignore exceptions
					catch(Exception e) {}
			}

			//Send a message to a single client on the server
			public void updateSingleClient(Message message){
				//Set the client thread to the intended recipient by fetching the client thread of the recipient's username
				ClientThread t = loggedIn.get(message.recipient);
				//Send the message to the intended recipient
				try {
					t.out.writeObject(message);
				}
				catch(Exception e) {
				}
			}


			//THIS NEEDS TO BE CALLED WHENEVER STATS ARE UPDATED IN ANY WAY.
			//Saves the user information to the users.dat file
			public void saveUsers() throws Exception{
				//Create an output stream based on the file stream that opens users.dat
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("users.dat"));
				//Write the users hashmap to the file
				out.writeObject(users);
				//Close the file
				out.close();
			}

			//The main function that runs on thread startup
			public void run(){

				//Try to create an input and output stream
				try {
					in = new ObjectInputStream(connection.getInputStream());
					out = new ObjectOutputStream(connection.getOutputStream());
					connection.setTcpNoDelay(true);	
				}
				//If there is an exception print to the terminal that the streams are not open properly.
				catch(Exception e) {
					System.out.println("Streams not open");
				}

				//Main thread loop
				 while(true) {
					 //Try to read in a message from the client
					    try {
					    	Message data = (Message) in.readObject();

							//Handle a user logging in
							if(data.type.equals("login")) {
								//Print to the log that a client is trying to log into a user
								callback.accept("client #" + count + "is trying to log into " + data.sender);
								//If the user does not exist as a current user
								if(!users.containsKey(data.sender)){
									//Send a login error saying the user was not found
									out.writeObject(new Message("login_error", "server", data.sender, "User not found."));
									continue;
								}
								//Otherwise if the incorrect password was input
								if(!users.get(data.sender).password.equals(data.message)){
									//Display to the user that they input an incorrect password
									out.writeObject(new Message("login_error", "server", data.sender, "Incorrect password."));
									continue;
								}
								//If the user is already logged in
								if(loggedIn.containsKey(data.sender)){
									//Send a login error saying the user is logged in elsewhere
									out.writeObject(new Message("login_error", "server", data.sender, "User logged in elsewhere."));
									continue;
								}
								//Otherwise store the user in loggedIn
								loggedIn.put(data.sender, this);
								//Set the current clients username to the user that they logged into
								this.uname = data.sender;

								//Send to the user that they logged in successfully
								out.writeObject(new Message("login_success", "server", uname, "Welcome!"));
								//Tell every user that theres a new client on the server
								updateClients(new Message("notification","server", "ALL", "new client on server: " + this.uname));
								//Send to every client the list of people logged in
								updateClients(new Message("user_list", "server", null, loggedIn.keySet().toString().substring(1, loggedIn.keySet().toString().length() - 1)));
								//Send to every client the list of current hosts on the sever
								updateClients(new Message("host_list", "server", null, hosts.toString().substring(1, hosts.toString().length() - 1)));
								//Display to the log that the user logged in successfully
								callback.accept("client #" + count + "has logged into " + data.sender);
							}

							//Handle a message being sent
							if(data.type.equals("message")){
								//Store in the log that a user sent a message
								callback.accept(data.sender + " sent: " + data.message + " to " + data.recipient);
								//If the recipient is all
								if(data.recipient.equals("ALL")){
									//Send the message to everyone
									updateClients(new Message("message", "server", data.recipient, data.sender +" said: "+data.message));
								} else {
									//Otherwise just send it to an individual user
									updateSingleClient(new Message("message", "server", data.recipient, data.sender + " said: " + data.message), data.sender);
								}
							}

							//Handle a message being sent
							if(data.type.equals("solo_message")){
								User sender = users.get(data.sender);
								User host;
								User joiner;
								// If sender is a joiner get host
								if (matchesj2h.containsKey(sender)) {
									host = matchesj2h.get(sender);
									joiner = sender;
								} else {
									// otherwise sender is the host
									host = sender;
									joiner = matchesh2j.get(sender);
								}
								//Store in the log that a user sent a message
								callback.accept(data.sender + " sent: " + data.message + " to opponent");
								updateSingleClient(new Message("ind_message", "server", joiner.username, data.sender + " said: " + data.message));
								updateSingleClient(new Message("ind_message", "server", host.username, data.sender + " said: " + data.message));
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
							//END OF NOT IN USED GROUP CREATION

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
							//END OF NOT IN USE GROUP SEND

							//Handle account creation
							if(data.type.equals("creation")){
								//Display to the log that a user is trying to create an account
								callback.accept("client #" + count + "has requested to create account: " + data.sender);
								//If the username is already in the users map
								if(users.containsKey(data.sender)) {
									//Display to the log that the client failed to set their username
									callback.accept("client #" + count + " has failed to set their username to " + data.sender);
									//Send a creation error to the user
									out.writeObject(new Message("creation_error", "server", data.sender, "Username taken"));
								} else {
									//Otherwise create the user and store their password
									User newUser = new User(data.sender, data.message);
									//Put the new user in the user list
									users.put(data.sender, newUser);
									try{
										//Save the users into users.dat (SEE saveUsers for more info)
										saveUsers();
										//If theres an exception
									} catch (Exception e){
										//Send an error to the user and the log
										callback.accept("Error creating user" + data.sender);
										out.writeObject(new Message("creation_error", "server", data.sender, "Error saving user, try again."));
									}
									//Otherwise display to the log that the user successfully created their account
									callback.accept("client #" + count + "has created their account: " + data.sender);
									//Send a successful creation to the user
									out.writeObject(new Message("successful_creation", "server", data.sender, "You have successfully made your account, welcome " + data.sender + "!"));
								}

							}

							//If a user requests to host a lobby
							if(data.type.equals("host")){
								//Print to the log the user is hosting a lobby
								callback.accept(data.sender + " is hosting a lobby");
								//If the user holds the lock
								synchronized (matchLock){
									//Get the host
									User host = users.get(data.sender);
									//Add the user to the list of hosts
									hosts.add(host);
									//Send the list of hosts to every client
									updateClients(new Message("host_list", "server", null, hosts.toString().substring(1, hosts.toString().length() - 1)));
									//Tell the person who tried to host that they have started hosting
									updateSingleClient(new Message("hosting_started", "server", data.sender, ""));
									//Create a new checkersboard for the host.
								}
							}

							//If the user requests to stop hosting a lobby
							if(data.type.equals("unhost")){
								//Send to the log that the user is no longer hosting
								callback.accept(data.sender + " is no longer hosting a lobby");
								//Store get the user attached to the username
								User host = users.get(data.sender);
								//If the user holds the matchmaking lock
								synchronized(matchLock){
									//Remove the user from the list of hosts
									//Get the person who was in the host's lobby
									User joiner = matchesh2j.get(host);
									//If this joiner exists
									if (joiner != null) {
										//Send the joiner a message to leave the host's lobby
										updateSingleClient(new Message("leave_lobby", "server", joiner.toString(), data.sender + " has stopped hosting!"));
										//Cleanup the match between the host and joiner (SEE clearnupMatch() for more info)
										joiner.wins++;
										host.losses++;
										saveUsers();
										cleanupMatch(host, joiner);
									}
									hosts.remove(host);
									//Send the new host list to every client
									updateClients(new Message("host_list", "server", null, hosts.toString().substring(1, hosts.toString().length() - 1)));
								}
							}

							if(data.type.equals("request_draw")){
								callback.accept(data.sender + " has requested a draw");
								User sender = users.get(data.sender);
								User host;
								User joiner;
								// If sender is a joiner get host
								if (matchesj2h.containsKey(sender)) {
									host = matchesj2h.get(sender);
									joiner = sender;
								} else {
									// otherwise sender is the host
									host = sender;
									joiner = matchesh2j.get(sender);
								}
								if(draw.contains(sender)){
									draw.remove(sender);
									updateSingleClient(new Message("ind_message", "server", host.username, sender.username + " has revoked draw request."), joiner.username);
								} else{
									draw.add(sender);
									updateSingleClient(new Message("ind_message", "server", host.username, sender.username + " has requested a draw."), joiner.username);
								}
								if (draw.contains(host) && draw.contains(joiner)){
									callback.accept("Draw accepted for " + joiner.username + " and " + host.username);
									joiner.draws++;
									host.draws++;
									saveUsers();
									Message drawmsg = new Message("game_complete", "server", "both players", "draw");
									drawmsg.recipient = host.username;
									updateSingleClient(drawmsg);
									drawmsg.recipient = joiner.username;
									updateSingleClient(drawmsg);
									draw.remove(joiner);
									draw.remove(host);
								}
							}
							//If the user requests to leave a lobby
							if(data.type.equals("leave_lobby")){
								//Set the joiner to be the user attached to the username sent in
								User joiner = users.get(data.sender);
								//If the lock is held by this thread
								synchronized(matchLock){
									//Set the host to be the person hosting the lobby the joiner is in
									User host = matchesj2h.get(joiner);
									//If the host is null
									if (host == null) {
										//Print to the log that there was no host found (should never happen that's why the warning so big)
										callback.accept("WARNING: No host found for " + data.sender);
										return;
									}
									//Print to the log that the user left the hosts lobby
									callback.accept(data.sender + " has left " + host + "'s lobby.");

									host.wins++;
									joiner.losses++;
									saveUsers();

									//Tell the client to leave the lobby
									updateSingleClient(new Message("leave_lobby", "server", host.toString(), data.sender + " has left your lobby!"));

									//Cleanup the match between the host and joiner (SEE clearnupMatch() for more info)
									cleanupMatch(host, joiner);
									//Send the new host list to every client
									updateClients(new Message("host_list", "server", null, hosts.toString().substring(1, hosts.toString().length() - 1)));
								}
							}

							//If a user requests to join a lobby
							if(data.type.equals("join")){
								//Set the host to be the user attached to the recipient's username
								User host = users.get(data.recipient);
								//Set the joiner to be the user attached to the sender's username.
								User joiner = users.get(data.sender);
								//If the thread holds the matchmaking lock
								synchronized(matchLock){
									//If the host or joiner are null
									if (host == null || joiner == null) {
										//return
										return;
									}
									//If the host does not exist in the list of hosts
									if (!hosts.contains(host)) {
										//send an error saying the host is unavailable (likely someone joined before they did)
										updateSingleClient(new Message("error", "server", data.sender, "Host is not available"));
										//return
										return;
									}
									//If the joiner is already in a lobby (should never happen)
									if (matchesj2h.containsKey(joiner)) {
										//Tell the client that they are already in a lobby
										updateSingleClient(new Message("error", "server", data.sender, "Already in a lobby"));
										return;
									}
								}
								//Create the match (SEE makeMatch() for more info)
								makeMatch(host, joiner);
								//Print to the log that the joiner joined the host's lobby
								callback.accept(data.sender + " has joined " + data.recipient + "'s lobby!");
							}

							//If the user is requesting the host list
							if(data.type.equals("request_host_list")){
								//Print to the log that the user is requesting the host list
								callback.accept(data.sender + " has requested the host list of " + hosts.toString().substring(1, hosts.toString().length() - 1));
								//Send the host list to the user that requested it
								updateSingleClient(new Message("host_list", "server", data.sender, hosts.toString().substring(1, hosts.toString().length() - 1)));
							}

							//If the user wants to see correct moves for a specific piece
							if(data.type.equals("request_moves")){
								//Fetch the current board
								User sender = users.get(data.sender);
								User host;

								if (matchesj2h.containsKey(sender)) {
									host = matchesj2h.get(sender);
								} else {
									host = sender;
								}

								checkersBoard game = boards.get(host);

								//Get the valid moves for the piece given
								ArrayList<int[]> moves = game.Vmoves(data.row, data.col);

								//Send the valid moves back to the user
								updateSingleClient(new Message("valid_moves", "server", data.sender, "", game, data.row, data.col, moves));
							}
							//
							if(data.type.equals("rematch")){
								callback.accept(data.sender + " has requested a rematch");
								User sender = users.get(data.sender);
								User host;
								User joiner;
								// If sender is a joiner get host
								if (matchesj2h.containsKey(sender)) {
									host = matchesj2h.get(sender);
									joiner = sender;
								} else {
									// otherwise sender is the host
									host = sender;
									joiner = matchesh2j.get(sender);
								}
								rematch.add(sender);
								 if (rematch.contains(host) && rematch.contains(joiner)){
									callback.accept("Rematch accepted! " + host.username + " and " + joiner.username);
									rematch.remove(host);
									rematch.remove(joiner);
									boards.remove(host);
									checkersBoard newBoard = new checkersBoard();
									newBoard.populateBoard();
									boards.put(host, newBoard);
									turns.put(host, host.username);
									Message rematchOn = new Message("rematch_start", "server", "both", "Rematch accepted!", boards.get(host).copy(), -1, -1, null);
									rematchOn.recipient = joiner.username;
									updateSingleClient(rematchOn);
									rematchOn.recipient = host.username;
									updateSingleClient(rematchOn);
									String hostStats = host.username + "|" + host.wins + "W-" + host.losses + "L-" + host.draws + "D";
									String joinerStats = joiner.username + "|" + joiner.wins + "W-" + joiner.losses + "L-" + joiner.draws + "D";
									updateSingleClient(new Message("my_stats", "server", joiner.toString(), joinerStats, boards.get(host), -1, -1, null));
									updateSingleClient(new Message("my_stats", "server", host.toString(), hostStats, boards.get(host), -1, -1, null));
									updateSingleClient(new Message("opponent_stats", "server", joiner.toString(), hostStats, boards.get(host), -1, -1, null));
									updateSingleClient(new Message("opponent_stats", "server", host.toString(), joinerStats, boards.get(host), -1, -1, null));
								}
							}
							//If the user requests to make a move
							if(data.type.equals("make_move")){
								callback.accept(data.sender + " is attempting to move");
								//Get the board sent over
								User sender = users.get(data.sender);
								User host;
								User joiner;
								// If sender is a joiner get host
								if (matchesj2h.containsKey(sender)) {
									host = matchesj2h.get(sender);
									joiner = sender;
								} else {
									// otherwise sender is the host
									host = sender;
									joiner = matchesh2j.get(sender);
								}
								checkersBoard game = boards.get(host);
								String currentTurn = turns.get(host);
								if (!data.sender.equals(currentTurn)) {
									// Not this player's turn ignore
									updateSingleClient(new Message("error", "server", data.sender, "Not your turn"));
									return;
								}
								//Fetch the old row and column via the message
								int oldrow = Integer.parseInt(data.message.substring(0, data.message.indexOf(",")));
								int oldcol = Integer.parseInt(data.message.substring(data.message.indexOf(",") + 1));
								//Get the valid moves for the piece trying to be moved
								ArrayList<int[]> moves = game.Vmoves(oldrow, oldcol);
								//Set isValid to be false by default
								boolean isValid = false;
								String piece = game.getBoard()[oldrow][oldcol];
								if (piece == null) return;
								// enforce ownership
								boolean isWhite = piece.startsWith("w");
								boolean isBlack = piece.startsWith("b");
								boolean senderIsHost = sender.equals(host);
								boolean senderIsJoiner = !senderIsHost;
								// host = black, joiner = white
								if (senderIsHost && !isBlack) return;
								if (senderIsJoiner && !isWhite) return;
								//Check to see if the updated move shows up in the valid moves. if it does have isValid be true and break
								for(int[] i : moves){
									if(i[0] == data.row && i[1] == data.col){
										isValid = true;
										break;
									}
								}
								//If the move isn't valid do nothing (might make send an error message later)
								if(!isValid) {
									return;
								}
								//Move the piece requested
								game.move(data.row, data.col, oldrow, oldcol, game.getBoard()[oldrow][oldcol]);
								//Get the user who sent the message
								String player1 = data.sender;
								User p1 = users.get(player1);
								String player2;
								//Get the user who they are in a match with
								if(matchesj2h.containsKey(p1)) player2 = matchesj2h.get(p1).username;
								else player2 = matchesh2j.get(p1).username;
								boolean jumped = Math.abs(data.row - oldrow) == 2;
								String nextPlayer = data.sender;
								//if not a jump switch turn
								if (!jumped) {
									if (data.sender.equals(host.username)) {
										nextPlayer = matchesh2j.get(host).username;
									} else {
										nextPlayer = host.username;
									}
									turns.put(host, nextPlayer);
								} else {
									//check if more jumps exist if yes, same player continues
									ArrayList<int[]> moreMoves = game.Vmoves(data.row, data.col);
									boolean canJumpAgain = false;
									for (int[] m : moreMoves) {
										if (Math.abs(m[0] - data.row) == 2) {
											canJumpAgain = true;
											break;
										}
									}
									if (!canJumpAgain) {
										//no more jumps -> switch turn
										if (data.sender.equals(host.username)) {
											nextPlayer = matchesh2j.get(host).username;
										} else {
											nextPlayer = host.username;
										}
										turns.put(host, nextPlayer);
									}
								}
								//Create the message which sends the updated board
								Message newBoard = new Message("board_update", "server", player1 + " and " + player2, nextPlayer,  game.copy(), -1, -1, null);
								//Send the updated board to both clients in a match
								newBoard.recipient = player1;
								updateSingleClient(newBoard);
								newBoard.recipient = player2;
								updateSingleClient(newBoard);
								boards.put(host, game);
								int winner = game.checkWin();
								Message whoWon;
								if (winner != 0){
									if(winner == 1) {
										whoWon = new Message("game_complete", "server", "both players", "black");
										host.wins++;
										joiner.losses++;
										saveUsers();
									} else{
										whoWon = new Message("game_complete", "server", "both players", "white");
										host.losses++;
										joiner.wins++;
										saveUsers();
									}
									draw.remove(joiner);
									draw.remove(host);
									whoWon.recipient = player1;
									updateSingleClient(whoWon);
									whoWon.recipient = player2;
									updateSingleClient(whoWon);
								}
							}


						}
						catch(Exception e) {
							//uncomment for debugging
							//e.printStackTrace();
							//Print to the log that something went wrong and that their socket is closing down
							callback.accept("Something wrong with the socket from client: " + count + "....closing down!");
							//Get the user attached to the user thats disconnecting
							User disconnectingUser = users.get(this.uname);
							//If the disconnecting user exists (only if they logged in already)
							if (disconnectingUser != null) {
								//If the thread holds the matchmaking lock
								synchronized (matchLock) {
									//Get the host associated to the disconnecting user
									User host = matchesj2h.get(disconnectingUser);
									//If the host is not null
									if (host != null) {
										//Send a leave lobby message to the joiner
										updateSingleClient(new Message("leave_lobby", "server", host.toString(), this.uname + " has left your lobby!"));
										//Cleanup the match (SEE clearnupMatch() for more info)
										cleanupMatch(host, disconnectingUser);
									}
									// If user is a host, get the joiner associated to the host
									User joiner = matchesh2j.get(disconnectingUser);
									//If the joiner is not null
									if (joiner != null) {
										//tell the joiner to leave the lobby
										updateSingleClient(new Message("leave_lobby", "server", joiner.toString(), this.uname + " has stopped hosting!"));
										//CLean up the match (SEE clearnupMatch() for more info)
										cleanupMatch(disconnectingUser, joiner);
									}
									if(hosts.contains(disconnectingUser)){
										synchronized(matchLock){
											hosts.remove(disconnectingUser);
										}
									}
								}
								loggedIn.remove(this.uname);
							}
							break;
						}
					} //end of while loop
				}//end of run

			/*
				Cleans up the match
				Essentially removes any trace of the user from matches and hosts
			*/
			private void cleanupMatch(User host, User joiner) {
				//If the thrad holds the matchLock
				synchronized (matchLock) {
					rematch.remove(host);
					rematch.remove(joiner);
					draw.remove(host);
					draw.remove(joiner);
					//If the host is not null
					if (host != null) {
						//Remove the host from the current matches
						matchesh2j.remove(host);
						//Remove the board from the list of boards
						boards.remove(host);
						//Add the host back to the host list
						hosts.add(host);
						turns.remove(host);
					}
					//If the joiner is not null
					if (joiner != null) {
						//Remove the match the joiner was in
						matchesj2h.remove(joiner);
					}
				}
			}

			/*
				Makes the match
				Adds the host and joiner to any required data structures
			*/
			private void makeMatch(User host, User joiner) {
				//If the thread holds the lock
				synchronized (matchLock) {
					// Check validity
					if (host == null || joiner == null) return;
					if (!hosts.contains(host)) return;
					if (matchesj2h.containsKey(joiner)) return;
					// Create match by storing in proper data structures
					matchesh2j.put(host, joiner);
					matchesj2h.put(joiner, host);
					// Remove host from available pool
					hosts.remove(host);
					//host always starts
					turns.put(host, host.username);
					checkersBoard newBoard = new checkersBoard();
					newBoard.populateBoard();
					boards.put(host, newBoard);
				}
				// Notify both players
				String hostStats = host.username + "|" + host.wins + "W-" + host.losses + "L-" + host.draws + "D";
				String joinerStats = joiner.username + "|" + joiner.wins + "W-" + joiner.losses + "L-" + joiner.draws + "D";
				updateSingleClient(new Message("match_created", "server", joiner.toString(), joinerStats, boards.get(host), -1, -1, null));
				updateSingleClient(new Message("match_created", "server", host.toString(), hostStats, boards.get(host), -1, -1, null));
				updateSingleClient(new Message("opponent_stats", "server", joiner.toString(), hostStats, boards.get(host), -1, -1, null));
				updateSingleClient(new Message("opponent_stats", "server", host.toString(), joinerStats, boards.get(host), -1, -1, null));

				// Update host list for everyone
				updateClients(new Message("host_list", "server", null, hosts.toString().substring(1, hosts.toString().length() - 1)));
			}

			
		}//end of client thread
}


	
	

	
