import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class SceneManager {

    //Primary stage for the application (main window)
    private static Stage primaryStage;

    //Controllers for different scenes
    public static ClientController clientController;
    public static AccountCreator accountController;
    public static JoinController joinController;
    public static UsernameController usernameController;

    //Buffer to hold messages that arrive before the correct scene is loaded
    private static final List<Message> pendingMessages = new ArrayList<>();

    //Set the main stage (called at app startup)
    public static void setPrimaryStage(Stage s) {
        primaryStage = s;
    }

    //Load a new scene from an FXML file
    public static void loadScene(String fxml) {
        try {
            //Fetch the FXML file
            FXMLLoader loader = new FXMLLoader(
                    SceneManager.class.getResource("/FXML/" + fxml)
            );

            //Load the scene
            Parent root = loader.load();

            //Store the controller based on which scene is loaded
            switch (fxml) {
                case "client.fxml":
                    clientController = loader.getController();
                    break;
                case "accountcreation.fxml":
                    accountController = loader.getController();
                    break;
                case "username.fxml":
                    usernameController = loader.getController();
                    break;
                case "joingame.fxml":
                    joinController = loader.getController();

                    for (Message m : pendingMessages) {
                        if (m.type.equals("host_list")) {
                            processMessage(m);
                        }
                    }
                    pendingMessages.removeIf(m -> m.type.equals("host_list"));
                    break;
            }

            //Set and display the new scene
            primaryStage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Handle incoming messages from the server
    public static void handleMessage(Message msg) {
        switch (msg.type) {
            //Login success
            case "login_success":
                //switch to selection scene
                loadScene("selection.fxml");

                //If client UI is ready, update user and process awaiting messages
                if (clientController != null) {
                    clientController.addUser(GuiClient.clientConnection.uname);

                    for (Message m : pendingMessages) {
                        processMessage(m);
                    }
                    pendingMessages.clear();
                }
                break;
            case "user_list":
            //Messages that require clientController
            case "message":
                if (clientController == null) {
                    pendingMessages.add(msg);
                } else {
                    processMessage(msg);
                }
                break;

            //Host list needs to be updated
            case "host_list":
                if (joinController == null) {
                    pendingMessages.add(msg);
                } else {
                    processMessage(msg);
                }
                break;

            //Account successfully created
            case "successful_creation":
                if (accountController != null) {
                    Platform.runLater(() ->
                            accountController.successful_creation(msg.message)
                    );
                }
                break;

            //Account creation error
            case "creation_error":
                if (accountController != null) {
                    Platform.runLater(() ->
                            accountController.errorOccured(msg.message)
                    );
                }
                break;

            //Login error
            case "login_error":
                if(usernameController != null){
                    Platform.runLater(() -> {
                        usernameController.displayError(msg.message);
                    });
                }
                break;

            case "match_created":
                if (clientController == null) {
                    loadScene("client.fxml");

                    Platform.runLater(() -> {
                        clientController.initBoard();
                    });
                } else {
                    Platform.runLater(() -> {
                        clientController.initBoard();
                    });
                }
                if (clientController != null) {
                    clientController.addMessage(msg.toMessage());
                }
                break;

            case "leave_lobby":
                if(clientController != null){
                    Platform.runLater(() -> {
                       clientController.hideBoard();
                       clientController.displayText("Awaiting User...");
                    });
                }
                break;

            case "hosting_started":
                loadScene("client.fxml");

                Platform.runLater(() -> {
                    clientController.displayText("Awaiting user...");
                });
                break;
            //Fallback display message in chat if possible
            default:
                if (clientController != null) {
                    clientController.addMessage(msg.toMessage());
                }
                break;
        }
    }

    //Process messages that require specific UI to be loaded
    public static void processMessage(Message msg) {

        switch (msg.type) {

            //Update user list in chat UI
            case "user_list":
                clientController.updateUsers(msg);
                break;

            //Update available hosts in join game scene
            case "host_list":
                if (joinController != null) {
                    joinController.updateHosts(msg);
                }
                break;

            //Default add message to chat
            default:
                clientController.addMessage(msg.toMessage());
                break;
        }
    }

    //Return awaiting messages
    public static List<Message> getPendingMessages() {
        return pendingMessages;
    }

    //Clear awaiting messages
    public static void clearPendingMessages() {
        pendingMessages.clear();
    }
}