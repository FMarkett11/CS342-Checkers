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

	public String uname;

	private Consumer<Serializable> callback;
	
	Client(Consumer<Serializable> call){
	
		callback = call;
	}
	
	public void run() {
		
		try {
		socketClient= new Socket("127.0.0.1",5555);
	    out = new ObjectOutputStream(socketClient.getOutputStream());
	    in = new ObjectInputStream(socketClient.getInputStream());
	    socketClient.setTcpNoDelay(true);
		}
		catch(Exception e) {}
		
		while(true) {
			 
			try {
			Message message = (Message) in.readObject();
			callback.accept(message);
			}
			catch(Exception e) {}
		}
	
    }
	
	public void send(Message data) {
		
		try {
			out.writeObject(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendUsername(String username){
		send(new Message("login", username, null, ""));
	}

	public void createGroup(String groupID, String groupMembers){
		send(new Message("group_create", uname, groupID, groupMembers.substring(1, groupMembers.length() - 1)));
	}

	public void sendChat(String msg, String recipient) {
		send(new Message("message", uname, recipient, msg));
	}

	public void groupSend(String msg, String group) {
		send(new Message("group_send", uname, group, msg));
	}

}
