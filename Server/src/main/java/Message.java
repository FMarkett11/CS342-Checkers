import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

public class Message implements Serializable {
    static final long serialVersionUID = 42L;
    public String type;
    public String sender;
    public String recipient;
    public String message;
    public String[][] board;
    int row;
    int col;
    ArrayList<int[]> validMoves;


    public Message(String type, String sender, String recipient, String str){
        this.type = type;
        this.sender = sender;
        this.recipient = recipient;
        message = str;
    }

    public Message(String type, String sender, String recipient, String str, boolean myTurn, String[][] board, int row, int col, ArrayList<int[]> validMoves){
        this.type = type;
        this.sender = sender;
        this.recipient = recipient;
        message = str;
        this.board = board;
        this.row = row;
        this.col = col;
        this.validMoves = validMoves;
    }

    public String toString(){

        if(recipient != null) return "{" + type + "}" + sender + "to" + recipient + ": " + message; else return "{" + type + "}" + sender + ": " + message;
    }
}
