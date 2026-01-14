package org.example.gogame.client;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import org.example.gogame.StoneColor;

import java.util.Optional;

/**
 * Represents the Graphical User Interface (GUI) for the Go game.
 * Handles the display of the board, stones, and status messages using JavaFX.
 *
 * @author Piotr Zieniewicz, Jan Langier
 */
public class GuiView {
    private final int size;
    private final BorderPane root;
    private final Pane boardPane;
    private final Label statusLabel,
                        colorLabel;
    private ClientGameController controller;

    private Circle[][] stones;

    private static final int CELL_SIZE = 30;
    private static final int PADDING = 20;

    /**
     * Constructs the GUI View.
     * Initializes the board array and sets up the initial UI layout.
     *
     * @param size The size of the game board (e.g., 19 for a 19x19 board).
     */
    public GuiView(int size) {
        this.size = size;
        this.root = new BorderPane();
        this.boardPane = new Pane();
        this.statusLabel = new Label("Connecting...");
        this.colorLabel = new Label("");
        this.stones = new Circle[size][size];

        setupUI();
    }

    /**
     * Sets the controller responsible for handling user actions from this view.
     *
     * @param controller The game controller instance.
     */
    public void setController(ClientGameController controller) {
        this.controller = controller;
    }

    /**
     * Retrieves the root layout node of the scene graph.
     *
     * @return The parent root node containing the game UI.
     */
    public Parent getRoot() {
        return root;
    }

    /**
     * Configures the visual components of the user interface.
     * Draws the grid lines, background, and initializes invisible stone shapes
     * for interaction.
     */
    private void setupUI() {
        int boardPixelSize = (size - 1) * CELL_SIZE + 2 * PADDING;
        boardPane.setPrefSize(boardPixelSize, boardPixelSize);

        Rectangle background = new Rectangle(boardPixelSize, boardPixelSize);
        background.setFill(Color.web("#DCB35C"));
        boardPane.getChildren().add(background);

        for (int i = 0; i < size; i++) {
            Line vLine = new Line(PADDING + i * CELL_SIZE, PADDING, PADDING + i * CELL_SIZE, PADDING + (size - 1) * CELL_SIZE);
            Line hLine = new Line(PADDING, PADDING + i * CELL_SIZE, PADDING + (size - 1) * CELL_SIZE, PADDING + i * CELL_SIZE);
            boardPane.getChildren().addAll(vLine, hLine);
        }

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                int finalX = x;
                int finalY = y;

                Circle stone = new Circle(CELL_SIZE / 2.0 - 2);
                stone.setCenterX(PADDING + x * CELL_SIZE);
                stone.setCenterY(PADDING + y * CELL_SIZE);
                stone.setVisible(false); // Na start puste
                stones[x][y] = stone;

                Circle clickArea = new Circle(CELL_SIZE / 2.0);
                clickArea.setCenterX(PADDING + x * CELL_SIZE);
                clickArea.setCenterY(PADDING + y * CELL_SIZE);
                clickArea.setFill(Color.TRANSPARENT);

                clickArea.setOnMouseClicked(e -> handleBoardClick(finalX, finalY));

                boardPane.getChildren().addAll(stone, clickArea);
            }
        }

        root.setCenter(boardPane);

        HBox bottomPanel = new HBox(10);
        bottomPanel.setAlignment(Pos.CENTER);
        bottomPanel.setStyle("-fx-padding: 10; -fx-background-color: #EEE;");
        statusLabel.setStyle("-fx-font-weight: bold;");
        bottomPanel.getChildren().add(statusLabel);
        root.setBottom(bottomPanel);

        HBox topPanel = new HBox(10);
        topPanel.setStyle("-fx-padding: 10;");
        colorLabel.setStyle("-fx-font-weight: bold;");

        Button passBtn = new Button("PASS");
        passBtn.setOnAction(e -> {
            if (controller != null) controller.handleUserInput("pass");
        });

        topPanel.getChildren().addAll(colorLabel, passBtn);
        root.setTop(topPanel);
    }

    /**
     * Handles the user interaction when a specific intersection on the board is clicked.
     * Converts the click into coordinates and forwards the move command to the controller.
     *
     * @param x The x-coordinate of the clicked intersection.
     * @param y The y-coordinate of the clicked intersection.
     */
    private void handleBoardClick(int x, int y) {
        if (controller != null) {
            controller.handleUserInput(x + " " + y);
        }
    }

    /**
     * Updates the visual state of a specific intersection on the board.
     * This method ensures the update is run on the JavaFX Application Thread.
     *
     * @param x     The x-coordinate of the stone.
     * @param y     The y-coordinate of the stone.
     * @param color The color to display (BLACK, WHITE, or EMPTY to clear).
     */
    public void updateBoard(int x, int y, StoneColor color) {
        Platform.runLater(() -> {
            if (x >= 0 && x < size && y >= 0 && y < size) {
                Circle stone = stones[x][y];
                if (color == StoneColor.EMPTY) {
                    stone.setVisible(false);
                } else {
                    stone.setVisible(true);
                    stone.setFill(color == StoneColor.BLACK ? Color.BLACK : Color.WHITE);
                    stone.setStroke(color == StoneColor.BLACK ? Color.BLACK : Color.WHITE);
                }
            }
        });
    }

    /**
     * Updates the status message displayed at the top of the window.
     *
     * @param msg The message string to display.
     */
    public void setMessage(String msg) {
        Platform.runLater(() -> statusLabel.setText(msg));
    }

    /**
     * Sets the label indicating the player's assigned color.
     *
     * @param color The name of the color (e.g., "BLACK" or "WHITE").
     */
    public void setColor(String color) {
        Platform.runLater(() -> {
            statusLabel.setText("You are playing as: " + color);
            colorLabel.setText(color);
        });
    }

    /**
     * Visually indicates whether it is currently this player's turn.
     * Changes the border color of the board to green if true.
     *
     * @param myTurn true if it is the player's turn, false otherwise.
     */
    public void setTurn(boolean myTurn) {
        Platform.runLater(() -> {
            if (myTurn) {
                root.setStyle("-fx-border-color: green; -fx-border-width: 5;");
            } else {
                root.setStyle("-fx-border-color: transparent; -fx-border-width: 5;");
            }
        });
    }

    /**
     * Displays an error popup to the user containing a message from the server.
     *
     * @param err The error message text.
     */
    public void setErr(String err) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Message from the server");
            alert.setHeaderText("Message from the server");
            alert.setContentText(err);
            alert.showAndWait();
        });
    }


    /**
     * Shows the user a dialog regarding the negotiations to end the game.
     *
     * @param msg The message text.
     */
    public void negotiate(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Negotiation");
            alert.setHeaderText("Opponent wants to negotiate");
            alert.setContentText(msg);
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES)
                controller.handleUserInput("AGREE");
            else
                controller.handleUserInput("RESUME");
        });
    }
}