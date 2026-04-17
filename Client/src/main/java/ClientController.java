import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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
    /*
        Mainly initializes the combo box to display all users properly.
        But even if we dont actually use the combo box, this is a good
        layout for how to handle pending messages.
    */
    public void initialize() {
        connectedClients.getItems().add("ALL");
        connectedClients.setValue("ALL");
        Platform.runLater(() -> {
            for (Message m : SceneManager.getPendingMessages()) {
                SceneManager.processMessage(m);
            }
            SceneManager.clearPendingMessages();
        });
    }

    public void displayText(String msg){
        dispMsg.setVisible(true);
        dispMsg.setText(msg);
    }

    public void initBoard(){
        dispMsg.setText("test");
        dispMsg.setVisible(false);
    }

    public void hideBoard(){
        return;
    }

    public void addMessage(String msg) {
        chatList.getItems().add(msg);
    }

    public void addUser(String user) {
        connectedClients.getItems().add(user);
    }

    public void updateUsers(Message msg) {
        String prevSel = connectedClients.getValue();
        connectedClients.getItems().clear();
        connectedClients.getItems().add("ALL");
        for (String s : msg.message.split(", ")) {
            connectedClients.getItems().add(s);
        }
        connectedClients.getItems().remove(GuiClient.clientConnection.uname);
        if (prevSel != null && connectedClients.getItems().contains(prevSel)) {
            connectedClients.setValue(prevSel);
        } else {
            connectedClients.setValue("ALL");
        }
    }

    public void sendChat(){
        GuiClient.clientConnection.sendChat(messageField.getText(), connectedClients.getValue());
    }

    public void goBack(){
        GuiClient.clientConnection.leaveLobby();
        GuiClient.clientConnection.isHost = false;
        SceneManager.loadScene("selection.fxml");
    }

}