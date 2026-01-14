package org.example.gogame.client;

import org.example.gogame.Board;
import org.example.gogame.StoneColor;
import java.util.Scanner;

/**
 * A console-based implementation of the Game View.
 * Displays the board using text characters and reads input from standard in.
 *
 * @author Piotr Zieniewicz, Jan Langier
 */
public class ConsoleView {
    private Scanner scanner;
    private Board board;
    private String color;
    private String msg = "";
    private String err = "";
    private boolean myTurn = false;

    /**
     * Constructs the console view.
     *
     * @param size The size of the game board.
     */
    public ConsoleView(int size) {
        this.board = new Board(size);
        this.scanner = new Scanner(System.in);

        board.clear();
    }

    /**
     * Updates a specific point on the local board representation.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @param color The new color of the stone.
     */
    public void updateBoard(int x, int y, StoneColor color) {
        board.setStone(x, y, color);
    }

    /**
     * Clears the console and redraws the current state of the board and messages.
     */
    public void displayBoard() {
        System.out.print("\033[2J\033[H");
        System.out.println(">> " + color + " <<");
        for (int x = 0; x < board.getSize(); x++) {
            for (int y = 0; y < board.getSize(); y++) {
                if (board.getStone(x, y) == StoneColor.BLACK)
                    System.out.print("X");
                else if (board.getStone(x, y) == StoneColor.WHITE)
                    System.out.print("O");
                else
                    System.out.print(".");
            }
            System.out.println();
        }

        System.out.println(">> " + msg + " <<");
        if (!err.isEmpty()) {
            System.out.println(err);
            err = "";
        }
        if (myTurn)
            System.out.print("> ");
    }

    /**
     * Sets the general status message to display.
     *
     * @param msg The message string.
     */
    public void setMessage(String msg) {
        this.msg = msg;
    }

    /**
     * Reads a line of input from the user.
     *
     * @return The input string, or "quit" if stream is closed.
     */
    public String getUserInput() {
        if (scanner.hasNextLine()) {
            return scanner.nextLine();
        }
        return "quit";
    }

    /**
     * Sets the player's color string for display.
     *
     * @param color The color name.
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Updates the turn indicator.
     *
     * @param turn true if it is this client's turn, false otherwise.
     */
    public void setTurn(boolean turn) {
        myTurn = turn;
    }

    /**
     * Sets an error message to be displayed once.
     *
     * @param err The error message.
     */
    public void setErr(String err) {
        this.err = err;
    }
}