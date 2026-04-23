import javafx.fxml.FXML;

public class SelectionController {
    public void initialize(){

    }

    @FXML
    private void goJoin(){
        SceneManager.loadScene("joingame.fxml");
    }
    @FXML
    private void goHost(){
        GuiClient.clientConnection.createLobby();
        GuiClient.clientConnection.isHost = true;
        GuiClient.clientConnection.isBlack = true;
    }
}
