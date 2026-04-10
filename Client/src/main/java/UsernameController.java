import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class UsernameController {

    @FXML
    private TextField usernameField;

    @FXML
    private void handleLogin() {
        String uname = usernameField.getText();
        GuiClient.clientConnection.uname = uname;
        GuiClient.clientConnection.sendUsername(uname);
    }
}