import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GuiClient extends Application {

	public static Client clientConnection;

	@Override
	public void start(Stage primaryStage) throws Exception {

		clientConnection = new Client(data -> {
			Platform.runLater(() -> {
				Message msg = (Message) data;

				SceneManager.handleMessage(msg);
			});
		});

		clientConnection.start();

		SceneManager.setPrimaryStage(primaryStage);

		SceneManager.loadScene("username.fxml");

		primaryStage.setTitle("Checkers");
		primaryStage.setOnCloseRequest((WindowEvent t) -> {
			Platform.exit();
			System.exit(0);
		});

		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}