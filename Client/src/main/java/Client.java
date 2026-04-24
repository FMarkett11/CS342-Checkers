import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.function.Consumer;



public class Client extends Thread{

	
	Socket socketClient;
	
	ObjectOutputStream out;
	ObjectInputStream in;

	boolean isHost = false;

	boolean isBlack;

	public String uname;

	private Consumer<Serializable> callback;
	
	Client(Consumer<Serializable> call){
		callback = call;
	}

	//The function the client thread runs on start
	public void run() {

		//Try connecting to the server
		try {
		socketClient= new Socket("127.0.0.1",5555);
		//Create an input and output stream
	    out = new ObjectOutputStream(socketClient.getOutputStream());
	    in = new ObjectInputStream(socketClient.getInputStream());
		//Remove tcp delay
	    socketClient.setTcpNoDelay(true);
		}
		//Catch any exceptions and ignore them
		catch(Exception e) {}

		//Main thread loop
		while(true) {

			try {
				//Read in a message
				Message message = (Message) in.readObject();
				//Accept the message and send it to the scene manager
				callback.accept(message);
			}
			//Catch exceptions and do nothing with them
			catch(Exception e) {}
		}
	
    }

	//Send a message
	public void send(Message data) {
		
		try {
			//Write a message to the server
			out.writeObject(data);
		} catch (IOException e) {
			//Print the stack trace on error
			e.printStackTrace();
		}
	}

	//Send a login message
	public void sendLogin(String username, String password){
		send(new Message("login", username, null, password));
	}

	//Create a new group (currently unused)
	public void createGroup(String groupID, String groupMembers){
		send(new Message("group_create", uname, groupID, groupMembers.substring(1, groupMembers.length() - 1)));
	}

	//Send a chat
	public void sendChat(String msg, String recipient) {
		send(new Message("message", uname, recipient, msg));
	}

	//Send a solo chat
	public void sendSoloChat(String msg) {
		send(new Message("solo_message", uname, "", msg));
	}

	//Send a chat to a group (currently unused)
	public void groupSend(String msg, String group) {
		send(new Message("group_send", uname, group, msg));
	}

	//Create a new account
	public void createAccount(String username, String password){ send(new Message("creation", username, null, password));}

	//Begin hosting a lobby
	public void createLobby(){ send(new Message("host", uname, null, ""));}

	//Join a host's lobby
	public void joinLobby(String host){ send(new Message("join", uname, host, ""));}

	//Leave  a lobby
	public void leaveLobby(){
		if(isHost){
			//If the user is a host, send an unhost message
			send(new Message("unhost", uname, null, ""));
			return;
		}
		//Otherwise send a leave lobby message
		send(new Message("leave_lobby", uname, null, ""));
	}

	public void reqDraw(){send(new Message("request_draw", uname, null, ""));}

	//Request a rematch
	public void reqRematch(){send(new Message("rematch", uname, null, ""));}

}
