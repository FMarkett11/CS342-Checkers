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
    public void initialize() {
        connectedClients.getItems().add("ALL");
        connectedClients.setValue("ALL");
    }


    public void addMessage(String msg) {
        chatList.getItems().add(msg);
    }

    public void addUser(String user) {
        connectedClients.getItems().add(user);
    }

    public void updateUsers(Message msg) {
        connectedClients.getItems().add("ALL");
        String prevSel = connectedClients.getValue();
        for (String s : msg.message.split(", ")) {
            connectedClients.getItems().add(s);
        }
        connectedClients.setValue(prevSel);
        connectedClients.getItems().remove(GuiClient.clientConnection.uname);
    }

    public void sendChat(){
        GuiClient.clientConnection.sendChat(messageField.getText(), connectedClients.getValue());
    }

}