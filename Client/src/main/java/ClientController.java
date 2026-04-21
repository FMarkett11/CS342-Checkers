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
    checkersBoard board;

    private Button[][] boardButtons = new Button[8][8]; //stores all the button on the gridpane
    private int[] selectedPiece = null;  // stores [row, col] of clicked piece
    /*
        Mainly initializes the combo box to display all users properly.
        But even if we don't actually use the combo box, this is a good
        layout for how to handle pending messages.
    */
    public void initialize() {

        board = new checkersBoard();//todo should be equal to the board sent by the server
        board.populateBoard();
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

    //A method to initialize the checkers board.
    public void initBoard(){
        //Make the display text invisible
        dispMsg.setVisible(false);
        for (int row = 0; row < 8; row++) {//for every row
            for (int col = 0; col < 8; col++) { // for every column  make a new button and place it in the gridpane
                Button btn = new Button();
                btn.setPrefSize(47, 43); //makesure the button doesnt resize when image is loaded
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

    //handle clicking a square on grid/board base case is select square if you select and click a valid move it will send to server
    private void handleSquareClick(int row, int col) {

        if (selectedPiece != null) {//if you choose a piece get valid moves, make a move if valid and sent move to server.
            ArrayList<int[]> validMoves = board.Vmoves(selectedPiece[0], selectedPiece[1]);
            for (int[] move : validMoves) {
                if (move[0] == row && move[1] == col) {//check for valid move
                    String piece = board.board[selectedPiece[0]][selectedPiece[1]];
                    board.move(row, col, selectedPiece[0], selectedPiece[1], piece);
                    selectedPiece = null;
                    clearHighlights();
                    refreshBoard();
                    // TODO: send move to server
                    return;
                }
            }
            // no valid moves dont do anything
            selectedPiece = null;
            clearHighlights();
        }

        //if you havent chose a piece yet base case
        if (board.board[row][col] != null) {
            selectedPiece = new int[]{row, col};
            highlightMoves(board.Vmoves(row, col));
        }
    }

    private void highlightMoves(ArrayList<int[]> moves) {
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
                if (grid[r][c] != null) {
                    boardButtons[r][c].setText(grid[r][c]);
                    if(grid[r][c].equals("w") || grid[r][c].equals("b")) {
                        Image img = new Image("Images/" + grid[r][c] + ".png");
                        ImageView imageView = new ImageView(img);
                        imageView.setFitWidth(30);
                        imageView.setFitHeight(30);
                        boardButtons[r][c].setGraphic(imageView);
                    }
                } else {
                    boardButtons[r][c].setGraphic(null);
                    boardButtons[r][c].setText("");
                }
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