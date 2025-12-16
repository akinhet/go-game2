package org.example.gogame.server;

import org.example.gogame.StoneColor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class PlayerHandler implements Runnable {

    private final Socket socket;
    private PrintWriter output;
    private BufferedReader input;

    private Game game = null;

    private StoneColor color;

    public PlayerHandler(Socket socket, StoneColor color) {
        this.socket = socket;
        this.color = color;
    }

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

    private void setupStreams() throws IOException {
        output = new PrintWriter(socket.getOutputStream(), true);
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    private void handleCommand(String command) {
        if (game == null) {
            sendMessage("MESSAGE Waiting for opponent...");
            return;
        }

        System.out.println("Received from " + color + ": " + command);

        if (command.startsWith("MOVE")) {
            String[] parts = command.split(" ");
            if (parts.length == 3) {
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);

                game.processMove(x, y, this);
            }
        }  else if (command.equals("PASS")) {
            game.processPass(this);
        } else if (command.equals("QUIT")) {
            game.processQuit(this);
        } else {
            sendMessage("ERROR Unknown command");
    }
    }

    public void sendMessage(String message) {
        if (output != null) {
            output.println(message);
        }
    }

    private void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void setColor(StoneColor color) {
        this.color = color;
    }

    public StoneColor getColor() {
        return color;
    }
}