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
        //Set all the controllers to be null
        clientController = null;
        accountController = null;
        joinController = null;
        usernameController = null;
        //Try to load the scene
        try {
            //Fetch the FXML file
            FXMLLoader loader = new FXMLLoader(
                    SceneManager.class.getResource("/FXML/" + fxml)
            );

            //Load the scene
            Parent root = loader.load();

            //Store the controller based on which scene is loaded
            switch (fxml) {
                //If the loaded scene is the client.fxml
                case "client.fxml":
                    //Set the clientController object to be the current controller
                    clientController = loader.getController();
                    break;
                //If the loaded scene is account creation.fxml
                case "accountcreation.fxml":
                    //Set the accountController object to be the current controller
                    accountController = loader.getController();
                    break;
                //If the loaded scene is username.fxml
                case "username.fxml":
                    //Set the usernameController to be the current controller
                    usernameController = loader.getController();
                    break;
                //If the loaded scene is joingame.fxml
                case "joingame.fxml":
                    //Set the joinGame controller to be the current controller
                    joinController = loader.getController();
                    //Get the current list of hosts from the server
                    GuiClient.clientConnection.send(new Message("request_host_list", GuiClient.clientConnection.uname, "server", "please..."));

                    //For every message currently being buffered
                    for (Message m : pendingMessages) {
                        //If the message type is a host_list, process it.
                        if (m.type.equals("host_list")) {
                            processMessage(m);
                        }
                    }
                    //Remove every message with type host_list
                    pendingMessages.removeIf(m -> m.type.equals("host_list"));
                    break;
            }

            //Set and display the new scene
            primaryStage.setScene(new Scene(root));

        } //If there is an exception
        catch (Exception e) {
            //Print the error
            e.printStackTrace();
        }
    }

    //Handle incoming messages from the server
    public static void handleMessage(Message msg) {
        //Depending on what the type of message is
        switch (msg.type) {
            //On login success
            case "login_success":
                //switch to selection scene
                loadScene("selection.fxml");

                //If client UI is ready, update user and process awaiting messages
                if (clientController != null) {
                    clientController.addUser(GuiClient.clientConnection.uname);

                    //For every buffered message
                    for (Message m : pendingMessages) {
                        //Process the message
                        processMessage(m);
                    }
                    //Clear out all of the pending messages
                    pendingMessages.clear();
                }
                break;
            //User list is always considered a pending message, so it isn't handled here.
            case "user_list":
            //Messages that require clientController
            case "message":
                //If the client controller is null
                if (clientController == null) {
                    //Add it to be a pending message
                    pendingMessages.add(msg);
                } else {
                    //Otherwise process it now
                    processMessage(msg);
                }
                break;

            //Host list needs to be updated
            case "host_list":
                //If the joinController is null, i.e. the join scene is not loaded
                if (joinController == null) {
                    //Add the host list to the pending messages list
                    pendingMessages.add(msg);
                } else {
                    //Otherwise process the message
                    processMessage(msg);
                }
                break;

            //Account successfully created
            case "successful_creation":
                //If the account controller is not null
                if (accountController != null) {
                    Platform.runLater(() ->
                            //Display that the account was successfully created
                            accountController.successful_creation(msg.message)
                    );
                }
                break;

            //Account creation error
            case "creation_error":
                //If the account creator is not null
                if (accountController != null) {
                    Platform.runLater(() ->
                            //State that an error occured
                            accountController.errorOccured(msg.message)
                    );
                }
                break;

            //Login error
            case "login_error":
                //If the username controller is not null
                if(usernameController != null){
                    Platform.runLater(() -> {
                        //Display an error in the username controller
                        usernameController.displayError(msg.message);
                    });
                }
                break;

            //If a match was successfully created
            case "match_created":
                //If the client controller is null
                if (clientController == null) {
                    //load the client scene
                    Platform.runLater(() -> {
                        loadScene("client.fxml");
                        //Initialize the checkers board
                        clientController.setBoard(msg.board);
                        clientController.initBoard();
                    });
                    //Otherwise
                } else {
                    //Just initialize the board
                    Platform.runLater(() -> {
                        clientController.setBoard(msg.board);
                        clientController.initBoard();
                    });
                }
                //If the client controller is not null (which it shouldn't be after the above but just in case).
                if (clientController != null) {
                    //add join message to the chat
                    clientController.addMessage(msg.toMessage());
                }
                break;

            //If the server notifies that you left the lobby
            case "leave_lobby":
                //If the client controller is not null
                if(clientController != null){
                    Platform.runLater(() -> {
                        //If the client that left is a host
                       if(!GuiClient.clientConnection.isHost){
                           //Load the selection scene
                           SceneManager.loadScene("selection.fxml");
                           //Clear all pending messages
                           clearPendingMessages();
                           //Make the client no longer a host
                           GuiClient.clientConnection.isHost = false;
                       } else{
                           //Otherwise hide the board
                           clientController.hideBoard();
                           //Display that you are now awaiting user
                           clientController.displayText("Awaiting User...");
                           //Add the notification message to the chat
                           clientController.addMessage(msg.toMessage());
                       }
                    });
                }
                break;

            //If a user has started hosting
            case "hosting_started":
                Platform.runLater(() -> {
                    //Load the client scene
                    loadScene("client.fxml");
                    //Display that you are now awaiting a user
                    clientController.displayText("Awaiting user...");
                });
                break;

            //Highlight the valid moves just requested
            case "valid_moves":
                Platform.runLater(() -> {
                    clientController.highlightMoves(msg.validMoves);
                    clientController.setSelectedPiece(msg.row, msg.col);
                });
                break;

            //Display the updated board to the client
            case "board_update":
                Platform.runLater(() -> {
                    clientController.makeBoard(msg.board);
                });
                break;
            //Fallback display message in chat if possible
            default:
                //if the client controller is not null add the message to the chat
                if (clientController != null) {
                    clientController.addMessage(msg.toMessage());
                }
                break;
        }
    }

    //Process messages that require specific UI to be loaded
    public static void processMessage(Message msg) {
        //If a message is a certain type
        switch (msg.type) {

            //Update user list in chat UI
            case "user_list":
                //Update the user list
                Platform.runLater(()->{
                    clientController.updateUsers(msg);
                });
                break;

            //Update available hosts in join game scene
            case "host_list":
                //If the join controller is not null
                if (joinController != null) {
                    //Update the host list
                    Platform.runLater(() -> {
                        if(joinController == null) {return;}
                        joinController.updateHosts(msg);
                    });
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