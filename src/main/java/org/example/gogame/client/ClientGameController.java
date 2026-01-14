package org.example.gogame.client;

import org.example.gogame.StoneColor;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Controls the client-side game flow.
 * Bridges communication between the network (ServerListener) and the UI (GuiView).
 *
 * @author Piotr Zieniewicz, Jan Langier
 */
public class ClientGameController {
    private Socket socket;
    private PrintWriter out;
    private GuiView view;
    private StoneColor myColor = StoneColor.EMPTY;
    private boolean isGameRunning = true;

    /**
     * Constructs the controller.
     *
     * @param socket The socket connected to the server.
     * @param view The UI view to update.
     * @throws Exception If socket stream creation fails.
     */
    public ClientGameController(Socket socket, GuiView view) throws Exception {
        this.socket = socket;
        this.view = view;
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    /**
     * Initializes the server listener thread.
     */
    public void startListener() {
        ServerListener listener = null;
        try {
            listener = new ServerListener(socket.getInputStream(), this);
        } catch (IOException e) {
            view.setErr("Client error: " + e.getMessage());
            return;
        }
        Thread listenerThread = new Thread(listener);
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    /**
     * Handles input received from the user via the view.
     * Sends appropriate commands (QUIT, PASS, MOVE) to the server.
     *
     * @param input The user input string.
     */
    public void handleUserInput(String input) {
        if (!isGameRunning || input == null) return;

        if (input.equalsIgnoreCase("quit")) {
            out.println("QUIT");
            isGameRunning = false;
            closeConnection();
            System.exit(0);
        } else if (input.equalsIgnoreCase("pass")) {
            out.println("PASS");
        } else if (input.equalsIgnoreCase("resume")) {
            out.println("RESUME");
        } else if (input.equalsIgnoreCase("agree")) {
            out.println("AGREE");
        } else {
            // Zakładamy format "x y"
            out.println("MOVE " + input);
        }
    }

    /**
     * Processes messages received from the server.
     * Updates the view state accordingly.
     *
     * @param message The raw message string from the server.
     */
    public synchronized void handleServerMessage(String message) {
        System.out.println("Server: " + message);

        if (message.startsWith("MESSAGE")) {
            view.setMessage(message.substring(8));
        }
        else if (message.startsWith("COLOR")) {
            String color = message.split(" ")[1];
            this.myColor = StoneColor.valueOf(color);
            view.setColor(color);
        }
        else if (message.startsWith("TURN")) {
            boolean turn = StoneColor.valueOf(message.substring(5)) == myColor;
            view.setTurn(turn);
            view.setMessage(turn ? "Your Turn!" : "Opponent's Turn...");
        }
        else if (message.startsWith("MOVE")) {
            String[] parts = message.split(" ");
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            String color = parts[3];
            view.updateBoard(x, y, StoneColor.valueOf(color));
        }
        else if (message.startsWith("CAPTURES")) {
            String[] parts = message.split(" ");
            for (int i = 1; i + 1 < parts.length; i += 2) {
                int x = Integer.parseInt(parts[i]);
                int y = Integer.parseInt(parts[i+1]);
                view.updateBoard(x, y, StoneColor.EMPTY);
            }
        }
        else if (message.startsWith("GAME_OVER")) {
            isGameRunning = false;
            view.setMessage("GAME OVER: " + message.substring(9));
        }
        else if (message.startsWith("ERROR")) {
            view.setErr(message);
        } else if (message.startsWith("NEGOTIATION")) {
            view.negotiate(message.substring(11));
        }
    }

    /**
     * Handles connection errors by stopping the game and notifying the user.
     */
    public void handleConnectionError() {
        isGameRunning = false;
        view.setMessage("Disconnected from server.");
        view.setErr("Disconnected from server.");
    }

    /**
     * Closes the socket connection.
     */
    private void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception e) {
            // ignore
        }
    }
}