import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;

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
    public checkersBoard board;

    private Button[][] boardButtons = new Button[8][8]; //stores all the button on the gridpane
    private int[] selectedPiece = null;  // stores [row, col] of clicked piece
    /*
        Mainly initializes the combo box to display all users properly.
        But even if we don't actually use the combo box, this is a good
        layout for how to handle pending messages.
    */
    public void initialize() {
        //todo should be equal to the board sent by the server
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

    //A method to initialize the checkers board.
    public void initBoard(){
        //Make the display text invisible
        dispMsg.setVisible(false);
        for (int row = 0; row < 8; row++) {//for every row
            for (int col = 0; col < 8; col++) { // for every column  make a new button and place it in the gridpane
                Button btn = new Button();
                btn.setPrefSize(47, 43); //make sure the button does not resize when image is loaded
                btn.setMinSize(47, 43);
                btn.setMaxSize(47, 43);
                final int r = row, c = col;
                btn.setOnAction(e -> handleSquareClick(r, c));
                btn.setStyle("-fx-background-color: transparent;");
                boardButtons[row][col] = btn;
                boardGrid.add(btn, col, row);
            }
        }
        refreshBoard();
    }

    private void handleSquareClick(int row, int col) {

        //A piece is already selected then try to move
        if (selectedPiece != null) {

            int oldRow = selectedPiece[0];
            int oldCol = selectedPiece[1];

            Message moveMsg = new Message("make_move", GuiClient.clientConnection.uname, "server", oldRow + "," + oldCol, row, col, null);

            GuiClient.clientConnection.send(moveMsg);

            //If the move is invalid reset selection
            selectedPiece = null;
            clearHighlights();
            return;
        }

        //If theres no piece selected then select one
        if (board.board[row][col] != null) {
            selectedPiece = new int[]{row, col};
            Message req = new Message("request_moves", GuiClient.clientConnection.uname, "server", "", row, col, null);

            GuiClient.clientConnection.send(req);
        }
    }

    public void setSelectedPiece(int row, int col){
        selectedPiece = new int[]{row, col};
    }

    public void highlightMoves(ArrayList<int[]> moves) {
        for (int[] move : moves) {
            boardButtons[move[0]][move[1]].setStyle("-fx-background-color: rgba(0,255,0,0.4);");
        }
    }

    private void clearHighlights() {
        for (Button[] row : boardButtons)
            for (Button btn : row)
                btn.setStyle("-fx-background-color: transparent;");
    }

    private void refreshBoard() {
        String[][] grid = board.getBoard();

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {

                Button btn = boardButtons[r][c];

                btn.setGraphic(null);
                btn.setText("");
                btn.setStyle("-fx-background-color: transparent;");

                if (grid[r][c] != null) {
                    Image img = new Image("Images/" + grid[r][c] + ".png");
                    ImageView imageView = new ImageView(img);
                    imageView.setFitWidth(30);
                    imageView.setFitHeight(30);

                    btn.setGraphic(null);
                    btn.setGraphic(imageView);
                }
            }
        }
        boardGrid.requestLayout();
        boardGrid.layout();
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

    public void setBoard(checkersBoard newBoard){
        this.board = newBoard;
    }

    public void makeBoard(checkersBoard board){
        setBoard(board);
        refreshBoard();
    }


}