package org.example.gogame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board(19);
    }

    @Test
    void testBoardInitialization() {
        assertEquals(StoneColor.EMPTY, board.getStone(0, 0));
        assertEquals(StoneColor.EMPTY, board.getStone(10, 10));
    }

    @Test
    void testSetAndGetStone() {
        board.setStone(5, 5, StoneColor.BLACK);

        assertEquals(StoneColor.BLACK, board.getStone(5, 5));

        assertEquals(StoneColor.EMPTY, board.getStone(5, 6));
    }

    @Test
    void testRemoveStone() {
        board.setStone(3, 3, StoneColor.WHITE);
        board.removeStone(3, 3);

        assertEquals(StoneColor.EMPTY, board.getStone(3, 3));
    }

    @Test
    void testBoundaries() {
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            board.getStone(-1, 0);
        });
    }
}