package org.example.gogame.server;

import org.example.gogame.Board;
import org.example.gogame.StoneColor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Contains the rules and mechanics of the game of Go.
 * Handles validation, liberty counting, capture logic, and Ko rule detection.
 *
 * @author Piotr Zieniewicz, Jan Langier
 */
public class GameLogic {
    private int regionSize = 0;
    /**
     * Checks if the given coordinates are within the board boundaries.
     *
     * @param board The game board.
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @return true if coordinates are valid, false otherwise.
     */
    private boolean inBounds(Board board, int x, int y){
        return x >= 0 && x < board.getSize() && y >= 0 && y < board.getSize();
    }

    /**
     * Validates if a move is structurally possible (in bounds and on an empty spot).
     * Does not check for suicide or Ko.
     *
     * @param board The game board.
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @return true if the move is valid based on board state, false otherwise.
     */
    public boolean validateMove(Board board, int x, int y){
        return inBounds(board,x,y) && board.getStone(x,y) == StoneColor.EMPTY;
    }

    /**
     * Counts the liberties of a connected chain of stones starting at (startX, startY).
     * Uses a Breadth-First Search (BFS) approach.
     *
     * @param board The game board.
     * @param startX The x-coordinate of a stone in the chain.
     * @param startY The y-coordinate of a stone in the chain.
     * @param color The color of the chain.
     * @return The number of unique liberties (empty adjacent points) for the chain.
     */
    public int countChainLiberties(Board board, int startX, int startY, StoneColor color) {
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

    /**
     * Retrieves all stones belonging to the chain connected to (startX, startY).
     *
     * @param board The game board.
     * @param startX The x-coordinate.
     * @param startY The y-coordinate.
     * @param color The color of the chain.
     * @return A list of coordinates representing the stones in the chain.
     */
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

    /**
     * Checks if placing a stone at (x, y) captures any enemy chains.
     *
     * @param board The game board.
     * @param x The x-coordinate of the placed stone.
     * @param y The y-coordinate of the placed stone.
     * @param color The color of the placed stone.
     * @return A list of coordinates of captured stones.
     */
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

    /**
     * Performs a final check after captures to ensure the move is not suicide.
     * A move is suicide if the placed stone has no liberties and captured nothing.
     *
     * @param board The game board.
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @param color The color of the stone.
     * @return The color if valid, or {@link StoneColor#EMPTY} if it's a suicide move.
     */
    public StoneColor finalCheck(Board board, int x, int y, StoneColor color) {
        int libs = countChainLiberties(board, x, y, color);
        if (libs == 0) {
            return StoneColor.EMPTY;
        }
        return color;
    }
    /**
     * Checks if the move violates the Ko rule (recreating the immediate previous board state).
     *
     * @param board The game board.
     * @param lastMove The coordinates of the last single stone captured (used for simple Ko check).
     * @param x The x-coordinate of the current move.
     * @param y The y-coordinate of the current move.
     * @return true if the move is Ko, false otherwise.
     */
    public boolean isKo(Board board, int[] lastMove, int x, int y){
        if (lastMove[0] == x && lastMove[1] == y){
            return true;
        }
        return false;

    }
    /**
     * Calculates the territory score for both Black and White players.
     * Iterates over the entire board to find empty regions and assigns them to a player
     * if the region is completely enclosed by that player's stones.
     *
     * @param board The current state of the game board.
     * @return An integer array where index 0 is Black's territory and index 1 is White's territory.
     */
    public int[] countTerritory(Board board) {
        int blackTerritory = 0;
        int whiteTerritory = 0;
        int size = board.getSize();
        boolean[][] visited = new boolean[size][size];

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (board.getStone(x, y) == StoneColor.EMPTY && !visited[x][y]) {
                    StoneColor owner = checkRegionOwner(board, x, y, visited);
                    if (owner == StoneColor.BLACK) blackTerritory += regionSize;
                    if (owner == StoneColor.WHITE) whiteTerritory += regionSize;
                }
            }
        }
        return new int[]{blackTerritory, whiteTerritory};
    }

    /**
     * Determines the owner of a contiguous region of empty intersections using BFS.
     * A region is owned by a color if it only touches stones of that color.
     * If it touches both colors, it is neutral (Dame).
     *
     * @param board   The game board.
     * @param startX  The starting x-coordinate of the empty region.
     * @param startY  The starting y-coordinate of the empty region.
     * @param visited A matrix to keep track of visited nodes during territory counting.
     * @return StoneColor.BLACK if owned by black, StoneColor.WHITE if owned by white, or StoneColor.EMPTY if neutral.
     */
    private StoneColor checkRegionOwner(Board board, int startX, int startY, boolean[][] visited) {
        ArrayList<int[]> queue = new ArrayList<>();
        queue.add(new int[]{startX, startY});
        visited[startX][startY] = true;
        regionSize = 0;

        boolean touchesBlack = false;
        boolean touchesWhite = false;

        while(!queue.isEmpty()) {
            int[] curr = queue.remove(0);
            regionSize++;

            int[][] dirs = {{1,0}, {-1,0}, {0,1}, {0,-1}};
            for(int[] d : dirs) {
                int nx = curr[0] + d[0];
                int ny = curr[1] + d[1];
                if(nx >= 0 && nx < board.getSize() && ny >= 0 && ny < board.getSize()) {
                    StoneColor stone = board.getStone(nx, ny);
                    if(stone == StoneColor.EMPTY && !visited[nx][ny]) {
                        visited[nx][ny] = true;
                        queue.add(new int[]{nx, ny});
                    } else if(stone == StoneColor.BLACK) touchesBlack = true;
                    else if(stone == StoneColor.WHITE) touchesWhite = true;
                }
            }
        }

        if(touchesBlack && !touchesWhite) return StoneColor.BLACK;
        if(!touchesBlack && touchesWhite) return StoneColor.WHITE;
        return StoneColor.EMPTY;
    }

}
