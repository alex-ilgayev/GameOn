package com.gameon.shared.sudoku.datatypes;

import java.io.Serializable;

/**
 * Created by Alex on 4/11/2015.
 * Abstraction class of the Sudoku board.
 */
public class SudokuBoard implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int EMPTY_VAL = 0;

    private int[][] _board;

    public SudokuBoard(int[][] board){
        _board = new int[9][9];
        for(int i=0; i<9; i++)
            for(int j=0; j<9; j++)
                _board[i][j] = board[i][j];
    }

    public SudokuBoard(){
        _board = new int[9][9];
        for(int i=0; i<9; i++){
            for(int j=0; j<9; j++){
                _board[i][j] = EMPTY_VAL;
            }
        }
    }

    public int[][] getBoard(){
        return _board;
    }

    /**
     * gets index value from board
     * @param row row index (0-8)
     * @param col col index (0-8)
     * @return value (0-9)
     */
    public int getValue(int row, int col){
        if(row >=0 && row <=8 && col >=0 && col <=8)
            return _board[row][col];
        return EMPTY_VAL;
    }

    /**
     * sets value in board
     * @param row row index (0-8)
     * @param col col index (0-8)
     * @param value value (1-9)
     */
    public void setValue(int row, int col, int value){
        if(row >=0 && row <=8 && col >=0 && col <=8 && value >=0 && value <=9)
            _board[row][col] = value;
    }
}
