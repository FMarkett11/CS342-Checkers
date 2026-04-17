import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class SceneManager {

    private static Stage primaryStage;

    public static ClientController clientController;

    public static AccountCreator accountController;

    public static UsernameController usernameController;

    private static final List<Message> pendingMessages = new ArrayList<>();

    public static void setPrimaryStage(Stage s) {
        primaryStage = s;
    }

    public static void loadScene(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneManager.class.getResource("/FXML/" + fxml)
            );

            Parent root = loader.load();

            if (fxml.equals("client.fxml")) {
                clientController = loader.getController();
            }
            if(fxml.equals("accountcreation.fxml")){
                accountController = loader.getController();
            }
            if(fxml.equals("username.fxml")){
                usernameController = loader.getController();
            }

            primaryStage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void handleMessage(Message msg) {
        switch (msg.type) {

            //login
            case "login_success":
                loadScene("client.fxml");

                if (clientController != null) {
                    clientController.addUser(GuiClient.clientConnection.uname);

                    for (Message m : pendingMessages) {
                        processMessage(m);
                    }
                    pendingMessages.clear();
                }
                break;

            //update the user list
            case "user_list":
            //Add to chat
            case "message":
                if (clientController == null) {
                    pendingMessages.add(msg);   // buffer only what needs client UI
                } else {
                    processMessage(msg);
                }
                break;

            //create account
            case "successful_creation":
                if (accountController != null) {
                    Platform.runLater(() ->
                            accountController.successful_creation(msg.message)
                    );
                }
                break;
            //Display error
            case "creation_error":
                if (accountController != null) {
                    Platform.runLater(() ->
                            accountController.errorOccured(msg.message)
                    );
                }
                break;
            case "login_error":
                if(usernameController != null){
                    Platform.runLater(() -> {
                        usernameController.displayError(msg.message);
                    });
                }
            //Fallback
            default:
                if (clientController != null) {
                    clientController.addMessage(msg.toMessage());
                }
                break;
        }
    }

    private static void processMessage(Message msg) {

        switch (msg.type) {
            case "user_list":
                clientController.updateUsers(msg);
                break;

            default:
                clientController.addMessage(msg.toMessage());
                break;
        }
    }
}