package org.example.gogame.server;

import org.example.gogame.Board;
import org.example.gogame.StoneColor;

import java.util.ArrayList;

public class GameLogic {
    private boolean inBounds(Board board, int x, int y){
        return x >= 0 && x < board.getSize() && y >= 0 && y < board.getSize();
    }
    private int countLiberties(Board board, int x, int y){
        int liberties = 0;
        int[][] directions = {{1,0}, {-1,0}, {0,1}, {0,-1}};

        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            if (inBounds(board, nx, ny) &&
                    board.getStone(nx, ny) == StoneColor.EMPTY) {
                liberties++;
            }
        }
        return liberties;
    }
    public boolean validateMove(Board board, int x, int y){
        return inBounds(board,x,y) && board.getStone(x,y) == StoneColor.EMPTY;
    }
    public ArrayList<int[]> checkCaptures(Board board, int x, int y, StoneColor color){
        ArrayList<int[]> captures = new ArrayList<>();
        int[][] directions = {{1,0}, {-1,0}, {0,1}, {0,-1}};

        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];

            if (inBounds(board, nx, ny)) {
                if (countLiberties(board, nx, ny) == 0) {
                    captures.add(new int[]{nx, ny});
                }
            }
        }
        return captures;
    }
    public StoneColor finalCheck(Board board, int x, int y, StoneColor color){
        if (countLiberties(board,x,y)==0){
            return StoneColor.EMPTY;
        }
        return color;
    }
}
