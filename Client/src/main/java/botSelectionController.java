import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class botSelectionController {
    @FXML
    private Label errorlbl;

    public void initialize(){

    }

    @FXML
    private void goEasy(){//func activated by easy button go to client and start easy game
        GuiClient.clientConnection.isHost = true;
        SceneManager.loadScene("client.fxml");
        SceneManager.clientController.startBotGame(false);
    }

    @FXML
    private void goHard(){//func activated by hard button go to client and start hard game
        GuiClient.clientConnection.isHost = true;
        SceneManager.loadScene("client.fxml");
        SceneManager.clientController.startBotGame(true);
    }

    public void changeLbl(String msg){
        errorlbl.setVisible(true);
        errorlbl.setText(msg);
    }

    public void noLabel(){
        errorlbl.setText("");
        errorlbl.setVisible(false);
    }
}
