package org.example.gogame.server;

import org.example.gogame.StoneColor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Main entry point for the Go Game Server.
 * Listens for client connections and starts a new Game instance when two players connect.
 *
 * @author Piotr Zieniewicz, Jan Langier
 */
public class GoServer {

    private static int port = 1111;

    /**
     * Starts the server.
     *
     * @param args Command line arguments (optional port number).
     */
    public static void main(String[] args) {
        if (args.length == 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Not a port number: " + args[1]);
            }
        } else if (args.length == 0) {
            new GoServer().start();
        } else {
            System.err.println("Malformed arguments. Exiting...");
        }
    }

    /**
     * Runs the server loop, accepting connections and pairing players.
     */
    public void start() {
        System.out.println("Go Server is running on port " + port);

        try (ServerSocket listener = new ServerSocket(port)) {
            while (true) {
                System.out.println("Waiting for Player 1 (BLACK)...");
                Socket socket1 = listener.accept();
                PlayerHandler player1 = new PlayerHandler(socket1, StoneColor.BLACK);
                player1.sendMessage("MESSAGE Connected as BLACK. Waiting for opponent...");

                System.out.println("Player 1 connected. Waiting for Player 2 (WHITE)...");
                Socket socket2 = listener.accept();
                PlayerHandler player2 = new PlayerHandler(socket2, StoneColor.WHITE);
                player2.sendMessage("MESSAGE Connected as WHITE. Game starting...");

                System.out.println("Both players connected. Starting game.");
                Game game = new Game(player1, player2, 19);
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}