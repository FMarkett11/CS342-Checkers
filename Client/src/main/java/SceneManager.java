import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class SceneManager {

    private static Stage primaryStage;

    public static ClientController clientController;

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

            if (fxml.contains("client.fxml")) {
                clientController = loader.getController();
            }

            primaryStage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void handleMessage(Message msg) {

        if (clientController == null && !msg.type.equals("login_success")) {
            pendingMessages.add(msg);
            return;
        }

        switch (msg.type) {

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

            case "user_list":
                if (clientController != null) {
                    clientController.updateUsers(msg);
                }
                break;

            case "error":
                break;

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