import java.io.Serializable;
import java.util.HashSet;

public class Message implements Serializable {
    static final long serialVersionUID = 42L;
    public String type;
    public String sender;
    public String recipient;
    public String message;
    public boolean myTurn;
    public boolean isKing;
    public String prevLoc;
    public String newLoc;


    public Message(String type, String sender, String recipient, String str){
        this.type = type;
        this.sender = sender;
        this.recipient = recipient;
        message = str;
    }

    public Message(String type, String sender, String recipient, String str, boolean myTurn, boolean isKing, String prevLoc, String newLoc){
        this.type = type;
        this.sender = sender;
        this.recipient = recipient;
        message = str;
        this.myTurn = myTurn;
        this.isKing = isKing;
        this.prevLoc = prevLoc;
        this.newLoc = newLoc;
    }

    public String toString(){

        if(recipient != null) return "{" + type + "}" + sender + "to" + recipient + ": " + message; else return "{" + type + "}" + sender + ": " + message;
    }
}
