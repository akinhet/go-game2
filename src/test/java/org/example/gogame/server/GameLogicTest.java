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
}