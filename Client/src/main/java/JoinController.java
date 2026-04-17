import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

public class JoinController {
    @FXML
    private ComboBox<String> hosts;
    @FXML
    private Label errLbl;

    public void initialize(){

    }

    public void updateHosts(Message msg) {
        String prevSel = hosts.getValue();
        hosts.getItems().clear();
        for (String s : msg.message.split(", ")) {
            hosts.getItems().add(s);
        }
        if (prevSel != null && hosts.getItems().contains(prevSel)) {
            hosts.setValue(prevSel);
        }
    }

    public void joinGame(){
        String host = hosts.getValue();
        if(host == null){
            errLbl.setText("Please select a user to join");
        }
        GuiClient.clientConnection.joinLobby(host);
    }

    public void goBack(){
        SceneManager.loadScene("selection.fxml");
    }
}