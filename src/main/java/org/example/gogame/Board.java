package org.example.gogame;

public class Board {
    private int size;
    private StoneColor[][] grid;

    public Board(int size){
        this.size = size;
        this.grid = new StoneColor[size][size];
        for (int i = 0; i<size; i +=1){
            for (int j = 0; j<size; j+=1){
                grid[i][j] = StoneColor.EMPTY;
            }
        }
    }

    public StoneColor getStone(int x, int y){
        StoneColor color = grid[x][y];
        return color;
    }

    public void setStone(int x, int y, StoneColor color){
        grid[x][y] = color;
    }

    public void removeStone(int x, int y){
        grid[x][y] = StoneColor.EMPTY;
    }

    public int getSize(){
        return size;
    }
    public void clear(){
        for (int i = 0; i<size; i +=1){
            for (int j = 0; j<size; j+=1){
                grid[i][j] = StoneColor.EMPTY;
            }
        }
    }
}
