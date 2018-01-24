package com.gameon.shared.sudoku.datatypes;

import java.io.Serializable;

/**
 * Created by Alex on 4/11/2015.
 * Abstraction class of a Sudoku Move.
 */
public class SudokuMove implements Serializable {
    private static final long serialVersionUID = 1L;

    public int row;
    public int col;
    public int value;

    /**
     * Sudoku Move
     * @param row - Sudoku row square to fill (0-8)
     * @param col - Sudoku col square to fill (0-8)
     * @param value - Sudoku value (0-9), zero as empty value.
     */
    public SudokuMove(int row, int col, int value){
        this.row = row;
        this.col = col;
        this.value = value;
    }
}
