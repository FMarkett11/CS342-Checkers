import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class SelectionController {
    @FXML
    private Label errorlbl;

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
    @FXML
    private void goBotSelection(){
        SceneManager.loadScene("BotSelection.fxml");
    }// goes to bot selection scene to choose easy or hard mode //todo update scenemanager

    public void changeLbl(String msg){
        errorlbl.setVisible(true);
        errorlbl.setText(msg);
    }

    public void noLabel(){
        errorlbl.setText("");
        errorlbl.setVisible(false);
    }
}
