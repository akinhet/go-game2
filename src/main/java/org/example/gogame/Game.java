package org.example.gogame;

import java.util.ArrayList;

public class Game {
    //todo - implement
    private PlayerHandler blackPlayer;
    private PlayerHandler whitePlayer;
    private PlayerHandler currentPlayer = blackPlayer;
    private int size;
    private Board board = new Board(size);
    private GameLogic gameLogic;
    private ArrayList<int[]> captures;
    private StoneColor finalform;
    //todo - implement
    public Game(PlayerHandler p1, PlayerHandler p2, int size) {}

    public synchronized void processMove(int x, int y, PlayerHandler player){
        String msg = "";
        if (gameLogic.validateMove(board,x,y,player.myColor)){
            captures = gameLogic.checkCaptures(board,x,y,player.myColor);
            board.setStone(x,y,player.myColor);
            for (int[] capture : captures){
                board.removeStone(capture[0],capture[1]);
            }
            finalform = gameLogic.finalCheck(board,x,y,player.myColor);
            board.setStone(x,y,finalform);
            msg += x + " ";
            msg += y + " ";
            msg += finalform.name() + ";";
            for (int[] point : captures){
                msg += point[0] + " ";
                msg += point[1] + " ";
                msg += "EMPTY";
            }
            BroadcastMessage(msg,blackPlayer);
            BroadcastMessage(msg,whitePlayer);
        }else {
            BroadcastMessage("Bad move - put valid move",player);
        }

    }
    private void switchTurn(){
        currentPlayer = (currentPlayer == blackPlayer ? whitePlayer : whitePlayer);
    }
    private void BroadcastMessage(String message, PlayerHandler player){
        player.sendMessage(message);
    }
}
