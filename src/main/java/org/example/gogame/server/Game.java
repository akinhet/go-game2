package org.example.gogame.server;

import org.example.gogame.Board;
import org.example.gogame.StoneColor;

import java.util.ArrayList;

/**
 * Manages the state and flow of a single Go game session.
 * Handles turns, move processing, scoring, and communication between two players.
 *
 * @author Piotr Zieniewicz, Jan Langier
 */
public class Game {
    private PlayerHandler blackPlayer;
    private PlayerHandler whitePlayer;
    private PlayerHandler currentPlayer;
    private Board board;
    private GameLogic gameLogic;
    private boolean gameOver = false;
    private boolean isUnderNegotiation = false;
    private boolean[] playerAgreed = {false, false};
    private int consecutivePasses = 0;
    private int[] lastMove = {-2,0};
    private int blackPrisoners = 0;
    private int whitePrisoners = 0;

    /**
     * Initializes a new game with two players and a board size.
     *
     * @param p1 The handler for the black player.
     * @param p2 The handler for the white player.
     * @param size The size of the board.
     */
    public Game(PlayerHandler p1, PlayerHandler p2, int size) {
        this.blackPlayer = p1;
        this.whitePlayer = p2;
        this.currentPlayer = blackPlayer;
        this.board = new Board(size);
        this.gameLogic = new GameLogic();

        blackPlayer.setGame(this);
        whitePlayer.setGame(this);

        new Thread(blackPlayer).start();
        new Thread(whitePlayer).start();

        try {
            Thread.sleep(100);
        } catch (Exception e) {}

        blackPlayer.sendMessage("COLOR BLACK");
        whitePlayer.sendMessage("COLOR WHITE");

        BroadcastMessage("GAME_START " + size);
        BroadcastMessage("TURN BLACK");
    }

    /**
     * Processes a move attempt by a player.
     * Validates the move, updates board state, handles captures, checks for Ko/Suicide,
     * and broadcasts the result or error.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @param player The player attempting the move.
     */
    public synchronized void processMove(int x, int y, PlayerHandler player) {
        if (gameOver) {
            player.sendMessage("ERROR Game is over");
            return;
        }
        if (isUnderNegotiation) {
            player.sendMessage("ERROR Game stopped. Type AGREE or RESUME.");
            return;
        }
        consecutivePasses = 0;
        ArrayList<int[]> captures;
        StoneColor finalform;
        if (currentPlayer == player) {
            if (gameLogic.validateMove(board, x, y)) {
                if (!gameLogic.isKo(board,lastMove,x,y)){
                    board.setStone(x, y, player.getColor());

                    captures = gameLogic.checkCaptures(board, x, y, player.getColor());
                    for (int[] capture : captures) {
                        board.removeStone(capture[0], capture[1]);
                    }
                    if (!captures.isEmpty()) {
                        if (player.getColor() == StoneColor.BLACK) {
                            blackPrisoners += captures.size();
                        } else {
                            whitePrisoners += captures.size();
                        }
                    }
                    if (captures.size()==1 && gameLogic.countChainLiberties(board,x,y,player.getColor()) == 1){
                        lastMove = new int[]{captures.get(0)[0],captures.get(0)[1]};
                    } else {
                        lastMove = new int[]{-2,0};
                    }

                    finalform = gameLogic.finalCheck(board, x, y, player.getColor());
                    if (finalform == StoneColor.EMPTY){
                        board.removeStone(x,y);
                        StoneColor enemy = (player.getColor() == StoneColor.BLACK ? StoneColor.WHITE : StoneColor.BLACK);
                        for (int[] capture : captures) {
                            board.setStone(capture[0], capture[1], enemy);
                        }
                        player.sendMessage("ERROR Suicide move - put valid move");
                    } else {
                        board.setStone(x, y, finalform);
                        StringBuilder moveMessage = new StringBuilder();
                        moveMessage.append("MOVE ")
                                .append(x).append(" ")
                                .append(y).append(" ")
                                .append(player.getColor().name());

                        BroadcastMessage(moveMessage.toString());

                        moveMessage = new StringBuilder();
                        moveMessage.append("CAPTURES");
                        if (!captures.isEmpty()) {
                            for (int[] point : captures) {
                                moveMessage.append(" ").append(point[0])
                                        .append(" ").append(point[1]);
                            }
                        }
                        BroadcastMessage(moveMessage.toString());
                        switchTurn();
                        BroadcastMessage("TURN " + currentPlayer.getColor().name());
                    }
                } else {
                    player.sendMessage("ERROR This move leads to Ko - put valid move");
                }
            } else {
                player.sendMessage("ERROR Invalid move - put valid move");
            }

        } else {
            player.sendMessage("ERROR Wait for your turn");
        }
    }

    /**
     * Processes a pass action by a player.
     * Two consecutive passes end the game.
     *
     * @param player The player passing.
     */
    public synchronized void processPass(PlayerHandler player) {
        if (currentPlayer != player) {
            player.sendMessage("ERROR Not your turn");
            return;
        }
        if (isUnderNegotiation) {
            player.sendMessage("ERROR Game stopped. Type AGREE or RESUME.");
        }

        BroadcastMessage("PASS " + player.getColor().name());
        consecutivePasses++;
        if (consecutivePasses >= 2) {
            startNegotiationPhase();
        }else {
            switchTurn();
            BroadcastMessage("TURN " + currentPlayer.getColor().name());
        }
    }

    /**
     * Initiates the negotiation phase after two consecutive passes.
     * Calculates the current territory (including prisoners) and suggests a score to both players.
     * Players are prompted to either AGREE to the score or RESUME play.
     */
    private void startNegotiationPhase() {
        isUnderNegotiation = true;
        playerAgreed[0] = false; // Black
        playerAgreed[1] = false; // White

        int[] territory = gameLogic.countTerritory(board);
        int currentBlack = territory[0] + blackPrisoners;
        int currentWhite = territory[1] + whitePrisoners;

        BroadcastMessage("NEGOTIATION Suggested Score -> BLACK: " + currentBlack + ", WHITE: " + currentWhite);
    }

    /**
     * Process a player's request to resume the game during the negotiation phase.
     * If one player disagrees with the calculated score/dead stones, the game continues.
     *
     * @param player The player requesting to resume.
     */
    public synchronized void processResume(PlayerHandler player) {
        if (!isUnderNegotiation) {
            player.sendMessage("ERROR Game is not paused.");
            return;
        }
        isUnderNegotiation = false;
        consecutivePasses = 0;
        playerAgreed[0] = false;
        playerAgreed[1] = false;

        BroadcastMessage("MESSAGE Game Resumed by " + player.getColor());

        if (player == blackPlayer) {
            currentPlayer = whitePlayer;
        } else {
            currentPlayer = blackPlayer;
        }

        BroadcastMessage("TURN " + currentPlayer.getColor().name());
    }

    /**
     * Handles a player's agreement to the proposed game result during negotiation.
     * If both players agree, the game ends and the final result is broadcast.
     *
     * @param player The player sending the agreement.
     */
    public synchronized void processAgree(PlayerHandler player) {
        if (!isUnderNegotiation) {
            player.sendMessage("ERROR Game is still running. Pass to stop.");
            return;
        }

        int index = (player.getColor() == StoneColor.BLACK) ? 0 : 1;
        if (!playerAgreed[index]) {
            playerAgreed[index] = true;
            BroadcastMessage("MESSAGE " + player.getColor() + " agreed to end.");
        }

        if (playerAgreed[0] && playerAgreed[1]) {
            endGame();
        }
    }

    /**
     * Handles a player quitting the game.
     *
     * @param player The player quitting.
     */
    public synchronized void processQuit(PlayerHandler player) {
        BroadcastMessage("PLAYER_QUIT " + player.getColor().name());
        BroadcastMessage("GAME_OVER " +
                (player.getColor() == StoneColor.BLACK ? "WHITE" : "BLACK") +
                "_WINS");
    }

    /**
     * Switches the current turn to the other player.
     */
    private void switchTurn(){
        currentPlayer = (currentPlayer == blackPlayer ? whitePlayer : blackPlayer);
    }

    /**
     * Sends a message to both players.
     *
     * @param message The message to send.
     */
    private void BroadcastMessage(String message){
        whitePlayer.sendMessage(message);
        blackPlayer.sendMessage(message);
    }

    /**
     * Ends the game, calculates scores, and broadcasts the result.
     */
    private void endGame() {
        gameOver = true;

        int[] territory = gameLogic.countTerritory(board);
        int blackTotal = territory[0] + blackPrisoners;
        int whiteTotal = territory[1] + whitePrisoners;

        String resultMessage = "GAME_OVER SCORE BLACK:" + blackTotal +
                " WHITE:" + whiteTotal + " ";

        if (blackTotal > whiteTotal) {
            resultMessage += "BLACK_WINS";
        } else if (whiteTotal > blackTotal) {
            resultMessage += "WHITE_WINS";
        } else {
            resultMessage += "DRAW";
        }

        BroadcastMessage(resultMessage);
    }

}
