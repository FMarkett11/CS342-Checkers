import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class ClientController {

    @FXML
    private TextField messageField;
    @FXML
    private ComboBox<String> connectedClients;
    @FXML
    private ComboBox<String> groups;
    @FXML
    private ListView<String> chatList;
    @FXML
    private Label dispMsg;
    @FXML
    private GridPane boardGrid;

    private Button[][] boardButtons = new Button[8][8];
    private int[] selectedPiece = null;  // stores [row, col] of clicked piece
    /*
        Mainly initializes the combo box to display all users properly.
        But even if we don't actually use the combo box, this is a good
        layout for how to handle pending messages.
    */
    public void initialize() {
        //Add the ALL option
        connectedClients.getItems().add("ALL");
        //Set the current selection to be ALL
        connectedClients.setValue("ALL");
        Platform.runLater(() -> {
            //Process all the pending messages
            for (Message m : SceneManager.getPendingMessages()) {
                SceneManager.processMessage(m);
            }
            //Clear the pending messages after processing
            SceneManager.clearPendingMessages();
        });
    }

    //A method to display a message in big text to the user, mostly used to display "Awaiting User..."
    public void displayText(String msg){
        //Make the text visible
        dispMsg.setVisible(true);
        //Set the text
        dispMsg.setText(msg);
    }

    //A method to initialize the checkers board. (TODO)
    public void initBoard(){
        //Make the display text invisible
        dispMsg.setVisible(false);
        dispMsg.setVisible(false);
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Button btn = new Button();
                //btn.setPrefSize(0, 0);
                final int r = row, c = col;
                //btn.setOnAction(e -> handleSquareClick(r, c));
                btn.setStyle("-fx-background-color: transparent;");
                boardButtons[row][col] = btn;
                boardGrid.add(btn, col, row);
            }
        }
    }

    //Hides the checkers board (TODO)
    public void hideBoard(){
        return;
    }

    //Adds a message to the chat
    public void addMessage(String msg) {
        chatList.getItems().add(msg);
    }

    //Adds a user to the combo box of users
    public void addUser(String user) {
        connectedClients.getItems().add(user);
    }

    //Updates the list of users in the combobox of users
    public void updateUsers(Message msg) {
        //Get the previous selection of the user
        String prevSel = connectedClients.getValue();
        //Clear the combobox
        connectedClients.getItems().clear();
        //Add back the all option
        connectedClients.getItems().add("ALL");
        //The server sends a string like user1, user2, user3, ... So we add each user split by the ,
        for (String s : msg.message.split(", ")) {
            //Add each user to the list of connected clients
            connectedClients.getItems().add(s);
        }
        //Remove the current user from the list
        connectedClients.getItems().remove(GuiClient.clientConnection.uname);
        //If the previous selection is not null set the value to be the previous selection
        if (prevSel != null && connectedClients.getItems().contains(prevSel)) {
            connectedClients.setValue(prevSel);
        } else {
            //Otherwise set it to all
            connectedClients.setValue("ALL");
        }
    }

    //Sends a message to the server requesting to send a chat
    public void sendChat(){
        GuiClient.clientConnection.sendChat(messageField.getText(), connectedClients.getValue());
    }

    //Goes back to the selection scene
    public void goBack(){
        //Send a leave lobby message
        GuiClient.clientConnection.leaveLobby();
        //Make the user no longer a host
        GuiClient.clientConnection.isHost = false;
        //Load the selection scene
        SceneManager.loadScene("selection.fxml");
    }

}