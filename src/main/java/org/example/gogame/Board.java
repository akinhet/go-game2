package org.example.gogame;

/**
 * Represents the Go game board.
 * Manages the grid of stones and provides methods to access and modify the board state.
 *
 * @author Piotr Zieniewicz, Jan Langier
 */
public class Board {
    private int size;
    private StoneColor[][] grid;

    /**
     * Constructs a new Board with the specified size.
     * Initializes all intersections to {@link StoneColor#EMPTY}.
     *
     * @param size The dimension of the board (e.g., 9, 13, 19).
     */
    public Board(int size){
        this.size = size;
        this.grid = new StoneColor[size][size];
        for (int i = 0; i<size; i +=1){
            for (int j = 0; j<size; j+=1){
                grid[i][j] = StoneColor.EMPTY;
            }
        }
    }

    /**
     * Retrieves the stone color at the specified coordinates.
     *
     * @param x The x-coordinate (row).
     * @param y The y-coordinate (column).
     * @return The {@link StoneColor} at the specified position.
     */
    public StoneColor getStone(int x, int y){
        StoneColor color = grid[x][y];
        return color;
    }

    /**
     * Places a stone of the specified color at the given coordinates.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @param color The color of the stone to place.
     */
    public void setStone(int x, int y, StoneColor color){
        grid[x][y] = color;
    }

    /**
     * Removes a stone from the specified coordinates, setting it to EMPTY.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     */
    public void removeStone(int x, int y){
        grid[x][y] = StoneColor.EMPTY;
    }

    /**
     * Returns the size of the board.
     *
     * @return The dimension of the board.
     */
    public int getSize(){
        return size;
    }

    /**
     * Clears the board by setting all intersections to EMPTY.
     */
    public void clear(){
        for (int i = 0; i<size; i +=1){
            for (int j = 0; j<size; j+=1){
                grid[i][j] = StoneColor.EMPTY;
            }
        }
    }
}
