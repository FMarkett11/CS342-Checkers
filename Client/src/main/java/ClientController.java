import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;

public class ClientController {

    @FXML
    private StackPane overlay;
    @FXML
    private Label winlose;
    @FXML
    private TextField messageField, soloMessageField;
    @FXML
    private ComboBox<String> connectedClients;
    @FXML
    private ComboBox<String> groups;
    @FXML
    private ListView<String> chatList, soloChatList;
    @FXML
    private Label dispMsg, myuname, mystats, oppuname, oppstats;
    @FXML
    private GridPane boardGrid;
    @FXML
    private VBox Rematch;
    private boolean isMyTurn = true;
    public checkersBoard board;
    private ArrayList<int[]> currentValidMoves = new ArrayList<>();

    private Button[][] boardButtons = new Button[8][8]; //stores all the button on the gridpane
    private int[] selectedPiece = null;  // stores [row, col] of clicked piece
    /*
        Mainly initializes the combo box to display all users properly.
        But even if we don't actually use the combo box, this is a good
        layout for how to handle pending messages.
    */
    public void initialize() {
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
        myuname.setVisible(false);
        oppuname.setVisible(false);
        mystats.setVisible(false);
        oppstats.setVisible(false);
        //Make the text visible
        dispMsg.setVisible(true);
        //Set the text
        dispMsg.setText(msg);
    }

    //A method to initialize the checkers board.
    public void initBoard(){
        //Make the display text invisible
        dispMsg.setVisible(false);

        boardGrid.getChildren().clear();

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

    private boolean isMyPiece(int row, int col) {
        String piece = board.getBoard()[row][col];

        if (piece == null) return false;

        // black player controls "b" and "bk"
        if (GuiClient.clientConnection.isBlack) {
            return piece.startsWith("b");
        }

        // white player controls "w" and "wk"
        return piece.startsWith("w");
    }

    private void handleSquareClick(int row, int col) {

        //Do nothing if it is not this client's turn
        if (!isMyTurn) return;

        //Flip coordinates if the player is white (board is inverted visually)
        if (!GuiClient.clientConnection.isBlack) {
            row = 7 - row;
            col = 7 - col;
        }

        //If a piece is already selected attempt to move it
        if (selectedPiece != null) {

            int oldRow = selectedPiece[0];
            int oldCol = selectedPiece[1];

            //Check if the clicked square is a valid move
            boolean isValid = false;
            for (int[] move : currentValidMoves) {
                if (move[0] == row && move[1] == col) {
                    isValid = true;
                    break;
                }
            }

            //If the move is not valid reset selection and stop
            if (!isValid) {
                selectedPiece = null;
                currentValidMoves.clear();
                clearHighlights();
                return;
            }

            //Send the valid move to the server
            Message moveMsg = new Message("make_move", GuiClient.clientConnection.uname, "server", oldRow + "," + oldCol, row, col, null
            );

            GuiClient.clientConnection.send(moveMsg);

            //Reset selection after sending move
            selectedPiece = null;
            currentValidMoves.clear();
            clearHighlights();
            return;
        }

        //If no piece is selected yet, ensure the clicked piece belongs to the player
        if (!isMyPiece(row, col)) return;

        //If the square contains a piece, select it and request valid moves
        if (board.board[row][col] != null) {

            //Clear any previous highlights
            clearHighlights();

            //Store selected piece
            selectedPiece = new int[]{row, col};

            //Request valid moves from the server
            Message req = new Message("request_moves", GuiClient.clientConnection.uname, "server", "", row, col, null
            );

            GuiClient.clientConnection.send(req);
        }
    }

    public void setSelectedPiece(int row, int col){
        selectedPiece = new int[]{row, col};
    }

    /*
    Highlights all valid moves on the board.

    Also stores the valid moves locally so we can validate clicks client-side.
*/
    public void highlightMoves(ArrayList<int[]> moves) {

        //Store the moves so we can validate future clicks
        currentValidMoves = moves;
        for (int[] move : moves) {
            if (GuiClient.clientConnection.isBlack) {
                boardButtons[move[0]][move[1]].setStyle("-fx-background-color: rgba(0,255,0,0.4);");
            } else {
                boardButtons[7 - move[0]][7 - move[1]].setStyle("-fx-background-color: rgba(0,255,0,0.4);");
            }
        }
    }

    /*
    Clears all highlighted squares on the board.
    */
    private void clearHighlights() {

        for (Button[] row : boardButtons) {
            for (Button btn : row) {
                btn.setStyle("-fx-background-color: transparent;");
            }
        }

        //Also clear stored valid moves
        currentValidMoves.clear();
    }

    private void refreshBoard() {
        String[][] grid = board.getBoard();

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {

                int displayRow = r;
                int displayCol = c;

                if (!GuiClient.clientConnection.isBlack) {
                    displayRow = 7 - r;
                    displayCol = 7 - c;
                }

                Button btn = boardButtons[r][c];

                btn.setGraphic(null);
                btn.setText("");
                btn.setStyle("-fx-background-color: transparent;");

                if (grid[displayRow][displayCol] != null) {
                    Image img = new Image("Images/" + grid[displayRow][displayCol] + ".png");
                    ImageView imageView = new ImageView(img);
                    imageView.setFitWidth(30);
                    imageView.setFitHeight(30);

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

    //Adds a message to the chat
    public void addIndMessage(String msg) {
        soloChatList.getItems().add(msg);
    }

    //Adds a user to the combo box of users
    public void addUser(String user) {
        connectedClients.getItems().add(user);
    }

    public void setMyLabel(String myStats) {
        myuname.setText(myStats.substring(0, myStats.indexOf("|")));
        mystats.setText(myStats.substring(myStats.indexOf("|") + 1));
        myuname.setVisible(true);
        mystats.setVisible(true);
    }

    public void setOppLabel(String myStats) {
        oppuname.setText(myStats.substring(0, myStats.indexOf("|")));
        oppstats.setText(myStats.substring(myStats.indexOf("|") + 1));
        oppuname.setVisible(true);
        oppstats.setVisible(true);
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

    public void sendSoloChat(){
        GuiClient.clientConnection.sendSoloChat(soloMessageField.getText());
    }

    public void switchChat(){
        if(soloChatList.isVisible()){
            soloChatList.setVisible(false);
            soloMessageField.setVisible(false);
            connectedClients.setVisible(true);
            messageField.setVisible(true);
            chatList.setVisible(true);
        } else{
            soloChatList.setVisible(true);
            soloMessageField.setVisible(true);
            connectedClients.setVisible(false);
            messageField.setVisible(false);
            chatList.setVisible(false);
        }
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

    private void setBoardAble(boolean disabled) {
        for (Button[] row : boardButtons) {
            for (Button btn : row) {
                btn.setDisable(disabled);
            }
        }
    }

    public void setTurn(boolean myTurn) {
        this.isMyTurn = myTurn;
        setBoardAble(!myTurn);
    }

    public void handleGameCompletion(String winner){
        overlay.setMouseTransparent(false);
        if(winner.equals("black") && GuiClient.clientConnection.isBlack) winlose.setText("You Win!");
        else if(winner.equals("white") && !GuiClient.clientConnection.isBlack) winlose.setText("You Win!");
        else winlose.setText("You Lose");
        overlay.setVisible(true);
    }

    public void reset(){
        selectedPiece = null;
        currentValidMoves.clear();
        clearHighlights();
    }

    @FXML
    private void rematch(){
        overlay.setVisible(false);
        overlay.setMouseTransparent(true);
        dispMsg.setVisible(true);
        GuiClient.clientConnection.reqRematch();
    }

    public void makeBoard(checkersBoard board){
        setBoard(board);
        refreshBoard();
    }


}