package org.example.gogame.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.net.Socket;
import java.util.Optional;
/**
 * The main entry point for the Go Game Client application.
 * Initializes the JavaFX UI, establishes the connection to the server,
 * and sets up the Model-View-Controller components.
 *
 * @author Piotr Zieniewicz, Jan Langier
 *
 */
public class GoClient extends Application {

    /**
     * Starts the JavaFX application stage.
     * Connects to the server, initializes the View and Controller, and displays the main window.
     *
     * @param primaryStage The primary stage for this application, onto which
     * the application scene can be set.
     */
    @Override
    public void start(Stage primaryStage) {
        String serverAddress = askForServerAddress();
        if (serverAddress == null) {
            return;
        }

        try {
            String[] parts = serverAddress.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            Socket socket = new Socket(host, port);

            GuiView view = new GuiView(19);
            ClientGameController controller = new ClientGameController(socket, view);

            view.setController(controller);

            Scene scene = new Scene(view.getRoot(), 600, 700);
            primaryStage.setTitle("Go Game Client - JavaFX");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);

            primaryStage.setOnCloseRequest(e -> controller.handleUserInput("quit"));

            primaryStage.show();

            controller.startListener();

        } catch (Exception e) {
            showError("Connection Error", "Could not connect to server: " + e.getMessage());
        }
    }

    /**
     * Displays a dialog prompt asking the user for the server address.
     * Defaults to "localhost:1111".
     *
     * @return A string containing "host:port", or null if the user cancelled.
     */
    private String askForServerAddress() {
        TextInputDialog dialog = new TextInputDialog("localhost:1111");
        dialog.setTitle("Server Connection");
        dialog.setHeaderText("Connect to Go Server");
        dialog.setContentText("Please enter server address (host:port):");

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    /**
     * Helper method to display an error alert to the user.
     *
     * @param header  The header text of the alert.
     * @param content The main content text describing the error.
     */
    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * The main method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be launched
     * through deployment artifacts, e.g., in IDEs with limited FX support.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}