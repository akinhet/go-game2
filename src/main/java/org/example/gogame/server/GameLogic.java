package org.example.gogame.server;

import org.example.gogame.Board;
import org.example.gogame.StoneColor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GameLogic {
    private boolean inBounds(Board board, int x, int y){
        return x >= 0 && x < board.getSize() && y >= 0 && y < board.getSize();
    }
    public boolean validateMove(Board board, int x, int y){
        return inBounds(board,x,y) && board.getStone(x,y) == StoneColor.EMPTY;
    }
    private int countChainLiberties(Board board, int startX, int startY, StoneColor color) {
        Set<String> visited = new HashSet<>();
        Set<String> liberties = new HashSet<>();
        ArrayList<int[]> stack = new ArrayList<>();

        stack.add(new int[]{startX, startY});
        visited.add(startX + "," + startY);

        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        while (!stack.isEmpty()) {
            int[] current = stack.remove(stack.size() - 1);
            int cx = current[0];
            int cy = current[1];

            for (int[] dir : directions) {
                int nx = cx + dir[0];
                int ny = cy + dir[1];

                if (inBounds(board, nx, ny)) {
                    StoneColor neighborColor = board.getStone(nx, ny);
                    String key = nx + "," + ny;

                    if (neighborColor == StoneColor.EMPTY) {
                        liberties.add(key);
                    } else if (neighborColor == color && !visited.contains(key)) {
                        visited.add(key);
                        stack.add(new int[]{nx, ny});
                    }
                }
            }
        }
        return liberties.size();
    }
    private ArrayList<int[]> getChain(Board board, int startX, int startY, StoneColor color) {
        Set<String> visited = new HashSet<>();
        ArrayList<int[]> chain = new ArrayList<>();
        ArrayList<int[]> stack = new ArrayList<>();

        stack.add(new int[]{startX, startY});
        visited.add(startX + "," + startY);
        chain.add(new int[]{startX, startY});

        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        while (!stack.isEmpty()) {
            int[] current = stack.remove(stack.size() - 1);
            int cx = current[0];
            int cy = current[1];

            for (int[] dir : directions) {
                int nx = cx + dir[0];
                int ny = cy + dir[1];
                String key = nx + "," + ny;

                if (inBounds(board, nx, ny) &&
                        board.getStone(nx, ny) == color &&
                        !visited.contains(key)) {

                    visited.add(key);
                    int[] stone = new int[]{nx, ny};
                    stack.add(stone);
                    chain.add(stone);
                }
            }
        }
        return chain;
    }

    public ArrayList<int[]> checkCaptures(Board board, int x, int y, StoneColor color) {
        ArrayList<int[]> capturedStones = new ArrayList<>();
        StoneColor enemyColor = (color == StoneColor.BLACK ? StoneColor.WHITE : StoneColor.BLACK);
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        Set<String> visitedEnemies = new HashSet<>();

        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            String key = nx + "," + ny;

            if (inBounds(board, nx, ny) && board.getStone(nx, ny) == enemyColor) {
                if (!visitedEnemies.contains(key)) {

                    int libs = countChainLiberties(board, nx, ny, enemyColor);

                    ArrayList<int[]> enemyChain = getChain(board, nx, ny, enemyColor);
                    for(int[] stone : enemyChain) {
                        visitedEnemies.add(stone[0] + "," + stone[1]);
                    }

                    if (libs == 0) {
                        capturedStones.addAll(enemyChain);
                    }
                }
            }
        }
        return capturedStones;
    }
    public StoneColor finalCheck(Board board, int x, int y, StoneColor color) {
        int libs = countChainLiberties(board, x, y, color);
        if (libs == 0) {
            return StoneColor.EMPTY;
        }
        return color;
    }
}
