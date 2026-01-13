package org.example.gogame.client;

import org.example.gogame.StoneColor;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientGameController {
    private Socket socket;
    private PrintWriter out;
    private ConsoleView view;
    private StoneColor myColor = StoneColor.EMPTY;
    private boolean isGameRunning = true;
    private boolean myTurn = false;

    public ClientGameController(Socket socket, ConsoleView view) throws Exception {
        this.socket = socket;
        this.view = view;
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    public void play() {
        ServerListener listener = null;
        try {
            listener = new ServerListener(socket.getInputStream(), this);
        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
            System.exit(1);
        }
        Thread listenerThread = new Thread(listener);
        listenerThread.start();

        while (isGameRunning) {
            String userInput = view.getUserInput();
            handleUserInput(userInput);
        }

        closeConnection();
    }

    private void handleUserInput(String input) {
        if (input == null) return;

        if (input.equalsIgnoreCase("quit")) {
            out.println("QUIT");
            isGameRunning = false;
        } else if (input.equalsIgnoreCase("pass")) {
            out.println("PASS");
        } else {

            out.println("MOVE " + input);
        }
    }

    public synchronized void handleServerMessage(String message) {
        if (message.startsWith("MESSAGE")) {
            view.setMessage(message.substring(8));
            view.displayBoard();
        }
        else if (message.startsWith("COLOR")) {
            String color = message.split(" ")[1];
            this.myColor = StoneColor.valueOf(color);
            view.setColor(color);
        }
        else if (message.startsWith("TURN")) {
            view.setTurn(StoneColor.valueOf(message.substring(5)) == myColor);
            view.setMessage(message);
            view.displayBoard();
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
            view.displayBoard();
        }
        else if (message.startsWith("GAME_OVER")) {
            isGameRunning = false;
            view.setMessage("!!! GAME OVER !!!\n" +
                    message.substring(9));
            view.displayBoard();
        }
        else if (message.startsWith("ERROR")) {
            view.setErr(message);
            view.displayBoard();
        }
    }

    public void handleConnectionError() {
        isGameRunning = false;
        view.setMessage("Disconnected from server.");
    }

    private void closeConnection() {
        try {
            socket.close();
        } catch (Exception e) {
            // ignore
        }
    }
}