package org.example.gogame.server;

import org.example.gogame.StoneColor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Handles the network communication for a single player on the server side.
 * Runs in a separate thread to listen for incoming commands.
 *
 * @author Piotr Zieniewicz, Jan Langier
 */
public class PlayerHandler implements Runnable {

    private final Socket socket;
    private PrintWriter output;
    private BufferedReader input;

    private Game game = null;

    private StoneColor color;

    /**
     * Constructs a PlayerHandler.
     *
     * @param socket The client socket.
     * @param color The assigned color for this player.
     */
    public PlayerHandler(Socket socket, StoneColor color) {
        this.socket = socket;
        this.color = color;
    }

    /**
     * The main run loop. Listens for commands from the client and delegates to the Game instance.
     */
    @Override
    public void run() {
        try {
            setupStreams();

            String command;
            while ((command = input.readLine()) != null) {
                handleCommand(command);
            }
        } catch (IOException e) {
            System.err.println("Player disconnected: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    /**
     * Initializes input and output streams for the socket.
     *
     * @throws IOException If stream creation fails.
     */
    private void setupStreams() throws IOException {
        output = new PrintWriter(socket.getOutputStream(), true);
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    /**
     * Parses and handles a command string received from the client.
     *
     * @param command The command string.
     */
    private void handleCommand(String command) {
        if (game == null) {
            sendMessage("MESSAGE Waiting for opponent...");
            return;
        }

        System.out.println("Received from " + color + ": " + command);

        if (command.startsWith("MOVE")) {
            String[] parts = command.split(" ");
            try {
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);

                game.processMove(x, y, this);
            } catch (Exception e) {
                sendMessage("ERROR Wrong move");
            }

        }  else if (command.equals("PASS")) {
            game.processPass(this);
        } else if (command.equals("QUIT")) {
            game.processQuit(this);
        } else if (command.equals("RESUME")) {
            game.processResume(this);
        } else if (command.equals("AGREE")) {
            game.processAgree(this);
        } else {
            sendMessage("ERROR Unknown command");
    }
    }

    /**
     * Sends a message to the client.
     *
     * @param message The message string to send.
     */
    public void sendMessage(String message) {
        if (output != null) {
            output.println(message);
        }
    }

    /**
     * Closes the socket connection.
     */
    private void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Associates this handler with a game instance.
     *
     * @param game The game instance.
     */
    public void setGame(Game game) {
        this.game = game;
    }

    /**
     * Sets the player's stone color.
     *
     * @param color The stone color.
     */
    public void setColor(StoneColor color) {
        this.color = color;
    }

    /**
     * Gets the player's stone color.
     *
     * @return The stone color.
     */
    public StoneColor getColor() {
        return color;
    }
}