package org.example.gogame.server;

import org.example.gogame.Board;
import org.example.gogame.StoneColor;

import java.util.ArrayList;

public class Game {
    private PlayerHandler blackPlayer;
    private PlayerHandler whitePlayer;
    private PlayerHandler currentPlayer;
    private Board board;
    private GameLogic gameLogic;
    private boolean gameOver = false;
    private int consecutivePasses = 0;
    private int[] lastMove = {-2,0};
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

    public synchronized void processMove(int x, int y, PlayerHandler player) {
        if (gameOver) {
            player.sendMessage("ERROR Game is over");
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
    public synchronized void processPass(PlayerHandler player) {
        if (currentPlayer != player) {
            player.sendMessage("ERROR Not your turn");
            return;
        }

        BroadcastMessage("PASS " + player.getColor().name());
        consecutivePasses++;

        if (consecutivePasses >= 2) {
            endGame();
            return;
        }

        switchTurn();
        BroadcastMessage("TURN " + currentPlayer.getColor().name());
    }

    public synchronized void processQuit(PlayerHandler player) {
        BroadcastMessage("PLAYER_QUIT " + player.getColor().name());
        BroadcastMessage("GAME_OVER " +
                (player.getColor() == StoneColor.BLACK ? "WHITE" : "BLACK") +
                "_WINS");
    }

    private void switchTurn(){
        currentPlayer = (currentPlayer == blackPlayer ? whitePlayer : blackPlayer);
    }
    private void BroadcastMessage(String message){
        whitePlayer.sendMessage(message);
        blackPlayer.sendMessage(message);
    }

    private void endGame() {
        gameOver = true;

        int blackScore = calculateScore(StoneColor.BLACK);
        int whiteScore = calculateScore(StoneColor.WHITE);

        String resultMessage = "GAME_OVER SCORE BLACK:" + blackScore +
                " WHITE:" + whiteScore + " ";

        if (blackScore > whiteScore) {
            resultMessage += "BLACK_WINS";
        } else if (whiteScore > blackScore) {
            resultMessage += "WHITE_WINS";
        } else {
            resultMessage += "DRAW";
        }

        BroadcastMessage(resultMessage);
    }

    private int calculateScore(StoneColor color) {
        int score = 0;
        for (int i = 0; i < board.getSize(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                if (board.getStone(i, j) == color) {
                    score++;
                }
            }
        }
        return score;
    }
}
