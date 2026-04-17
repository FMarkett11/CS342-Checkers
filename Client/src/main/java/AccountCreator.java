import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class AccountCreator {

    @FXML
    private TextField usernameField, passwordField, repassField;
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
    private void goBack(){
        SceneManager.loadScene("username.fxml");
    }

    @FXML
    private void handleAccountCreation(){
        String passcheck = repassField.getText();
        String uname = usernameField.getText();
        String password = passwordField.getText();

        if(checkLegality(uname, password, passcheck)){
            create_account(uname, password);
        }
    }

    private boolean checkLegality(String uname, String password, String passcheck){
        if(password.equals("") && uname.equals("") && passcheck.equals("")){
            errorLbl.setText("Can you atleast try?");
            errorLbl.setVisible(true);
            return false;
        }
        if(uname.equals("")){
            errorLbl.setText("Please enter a username.");
            errorLbl.setVisible(true);
            return false;
        }
        if(password.equals("")){
            errorLbl.setText("Please enter a password.");
            errorLbl.setVisible(true);
            return false;
        }
        if(passcheck.equals("")){
            errorLbl.setText("Please re-enter password.");
            errorLbl.setVisible(true);
            return false;
        }
        if(!password.equals(passcheck)){
            errorLbl.setText("Please make sure passwords match.");
            errorLbl.setVisible(true);
            return false;
        }
        if(uname.length() < 4){
            errorLbl.setText("Username must be 4 or more characters.");
            errorLbl.setVisible(true);
            return false;
        }
        if(uname.contains(" ")){
            errorLbl.setText("When have spaces ever been allowed in a username?");
            errorLbl.setVisible(true);
            return false;
        }
        if(uname.contains(",")){
            errorLbl.setText("NO COMMAS!!!!!");
            errorLbl.setVisible(true);
            return false;
        }
        if(uname.contains(password)){
            errorLbl.setText("Clearly security is not your strong suit.");
            errorLbl.setVisible(true);
            return false;
        }
        if(uname.length() > 20){
            errorLbl.setText("Username is way to long. Try less than 20 characters.");
            errorLbl.setVisible(true);
            return false;
        }
        return true;
    }

    private void create_account(String uname, String password){
        GuiClient.clientConnection.createAccount(uname, password);
    }

    public void successful_creation(String msg){
        SceneManager.loadScene("username.fxml");
    }

    public void errorOccured(String msg){
        errorLbl.setText(msg);
        errorLbl.setVisible(true);
    }
}
