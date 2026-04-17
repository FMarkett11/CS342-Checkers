import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class UsernameController {

    @FXML
    private TextField usernameField, passwordField;
    @FXML
    private Label errorLbl;
    @FXML
    private AnchorPane AnchorPane;

    public void initialize() {
        errorLbl.setMaxWidth(Double.MAX_VALUE);
        AnchorPane.setLeftAnchor(errorLbl, 0.0);
        AnchorPane.setRightAnchor(errorLbl, 0.0);
        errorLbl.setAlignment(Pos.CENTER);
    }

    @FXML
    private void handleLogin() {
        String uname = usernameField.getText();
        String password = passwordField.getText();
        GuiClient.clientConnection.uname = uname;
        GuiClient.clientConnection.sendLogin(uname, password);
    }

    @FXML
    private void createAccount() {
        SceneManager.loadScene("accountcreation.fxml");
    }

    @FXML
    private void goToPass(){
        passwordField.requestFocus();
    }

    public void displayError(String msg){
        errorLbl.setText(msg);
        errorLbl.setVisible(true);
    }
}