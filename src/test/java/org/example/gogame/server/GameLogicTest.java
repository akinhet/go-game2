package org.example.gogame.server;

import org.example.gogame.Board;
import org.example.gogame.StoneColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class GameLogicTest {

    private GameLogic gameLogic;
    private Board board;

    @BeforeEach
    void setUp() {
        gameLogic = new GameLogic();
        board = new Board(9);
    }

    @Test
    void testValidateMoveOnOccupiedSpace() {
        board.setStone(2, 2, StoneColor.BLACK);

        boolean isValid = gameLogic.validateMove(board, 2, 2);

        assertFalse(isValid, "Ruch na zajęte pole powinien być zabroniony");
    }

    @Test
    void testCaptureSingleStone() {
        board.setStone(1, 1, StoneColor.BLACK);
        board.setStone(0, 1, StoneColor.WHITE);
        board.setStone(2, 1, StoneColor.WHITE);
        board.setStone(1, 0, StoneColor.WHITE);

        board.setStone(1, 2, StoneColor.WHITE);

        ArrayList<int[]> captures = gameLogic.checkCaptures(board, 1, 2, StoneColor.WHITE);

        assertFalse(captures.isEmpty(), "Powinno nastąpić zbicie");
        assertEquals(1, captures.size(), "Powinien zostać zbity jeden kamień");

        assertEquals(1, captures.get(0)[0]);
        assertEquals(1, captures.get(0)[1]);
    }

    @Test
    void testNoCaptureIfLibertyExists() {
        board.setStone(1, 1, StoneColor.BLACK);
        board.setStone(0, 1, StoneColor.WHITE);
        board.setStone(1, 2, StoneColor.WHITE);

        ArrayList<int[]> captures = gameLogic.checkCaptures(board, 1, 2, StoneColor.WHITE);

        assertTrue(captures.isEmpty(), "Nie powinno być zbicia, kamień ma oddech");
    }

    @Test
    void testKoRule(){
        board.setStone(1,0, StoneColor.BLACK);
        board.setStone(0,1, StoneColor.BLACK);
        board.setStone(2,1, StoneColor.BLACK);
        board.setStone(1,2, StoneColor.BLACK);
        board.setStone(1,3, StoneColor.WHITE);
        board.setStone(0,2, StoneColor.WHITE);
        board.setStone(2,2, StoneColor.WHITE);
        board.setStone(1,1, StoneColor.WHITE);
        ArrayList<int[]> captures = gameLogic.checkCaptures(board,1,1,StoneColor.WHITE);
        board.setStone(captures.getFirst()[0],captures.getFirst()[1],StoneColor.EMPTY);
        int[] lastMove = new int[]{captures.getFirst()[0],captures.getFirst()[1]};
        assertTrue(gameLogic.isKo(board,lastMove,captures.getFirst()[0],captures.getFirst()[1]));
    }

    @Test
    void testSuicide(){
        board.setStone(1,0, StoneColor.BLACK);
        board.setStone(0,1, StoneColor.BLACK);
        board.setStone(2,1, StoneColor.BLACK);
        board.setStone(1,2, StoneColor.BLACK);
        board.setStone(1,1, StoneColor.WHITE);
        StoneColor suicide = gameLogic.finalCheck(board,1,1,StoneColor.WHITE);
        assertEquals(StoneColor.EMPTY,suicide);
    }

    @Test
    void testTerritoryCounting() {
        //Otoczone czarnymi
        board.setStone(1, 0, StoneColor.BLACK);
        board.setStone(0, 1, StoneColor.BLACK);

        //Otoczone białymi
        board.setStone(4, 5, StoneColor.WHITE);
        board.setStone(6, 5, StoneColor.WHITE);
        board.setStone(5, 4, StoneColor.WHITE);
        board.setStone(5, 6, StoneColor.WHITE);

        //Otoczone różnymi
        board.setStone(7, 8, StoneColor.BLACK);
        board.setStone(8, 7, StoneColor.WHITE);

        int[] scores = gameLogic.countTerritory(board);

        assertEquals(1, scores[0], "Czarne terytorium powinno wynosić 1");
        assertEquals(1, scores[1], "Białe terytorium powinno wynosić 1");
    }
}