package com.gameon.gameon.sudoku;

import com.gameon.gameon.sudoku.datatypes.SudokuBoard;
import com.gameon.gameon.datatypes.DifficultyType;
import com.gameon.gameon.sudoku.datatypes.SudokuMove;
import com.gameon.gameon.sudoku.datatypes.SudokuResultType;

import java.io.Serializable;
import java.util.Random;

/** Manages all the game data, game operations, game moves, and when game is finished.
 * Created by Alex on 4/16/2015.
 */
public class SudokuGameData implements Serializable{
    private static final long serialVersionUID = 1L;

    private final static int EASY_NUM_SQUARES = 50;
    private final static int MEDIUM_NUM_SQUARES = 35;
    private final static int HARD_NUM_SQUARES = 15;
    private final static boolean TAKEN_VALUE = false;
    private final static boolean AVAILABLE_VALUE = true;
    private final static int NUM_ITERATION = 5;

    private SudokuBoard _board;
    private SudokuBoard _originalBoard;

    // 3d array. (9x9x9)
    // each square has 9 value boolean array,
    // represents if that number can be fit in the sudoku board.
    private boolean[][][] _optionsBoard;

    private void updateOptionsBoardAfterSetMove(int row, int col, int value, boolean bool){
        // if updating empty value, no need to update.
        if(value == SudokuBoard.EMPTY_VAL)
            return;
        if(row>=0 && row<=8 && col>=0 && col<=8 && value>=0 && value<=9) {
            // updating options table base on rows and cols.
            for (int i = 0; i < 9; i++) {
                _optionsBoard[i][col][value] = bool;
                _optionsBoard[row][i][value] = bool;
            }

            // updating options table based on 3x3 square.
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    _optionsBoard[(row / 3) * 3 + i][(col / 3) * 3 + j][value] = bool;
                }
            }
        }
    }

    public SudokuGameData(SudokuGameData clone){
        _board = new SudokuBoard(clone.getBoard().getBoard());
        _originalBoard = new SudokuBoard(clone.getOriginalBoard().getBoard());
        _optionsBoard = new boolean[9][9][10];
        for(int i=0; i<9; i++) {
            for (int j = 0; j < 9; j++) {
                for(int k=0; k<=9; k++){
                    _optionsBoard[i][j][k] = clone._optionsBoard[i][j][k];
                }
            }
        }
    }

    public SudokuGameData(DifficultyType difficulty){
        _optionsBoard = new boolean[9][9][10];
        for(int i=0; i<9; i++){
            for(int j=0; j<9; j++)
                for(int k=0; k<=9; k++)
                    _optionsBoard[i][j][k] = AVAILABLE_VALUE;
        }

        _board = new SudokuBoard();
        _originalBoard = new SudokuBoard();



//        int[][] testBoard;
//        testBoard = new int[][]{{0, 0, 0, 0, 0, 0, 0, 0, 5},
//                                {0, 0, 0, 0, 0, 0, 0, 0, 0},
//                                {2, 1, 0, 0, 3, 0, 0, 0, 0},
//                                {0, 0, 0, 0, 0, 1, 0, 0, 0},
//                                {0, 0, 0, 6, 0, 0, 0, 0, 1},
//                                {0, 0, 1, 0, 0, 0, 2, 0, 0},
//                                {0, 2, 0, 5, 0, 9, 4, 0, 0},
//                                {5, 0, 0, 0, 0, 2, 0, 0, 6},
//                                {0, 0, 0, 0, 0, 0, 0, 0, 7}};
//
//        SudokuResultType result;
//        boolean totalResult = true;

//        result = setMove(new SudokuMove(0,8,5));
//        result = setMove(new SudokuMove(2,0,2));
//        result = setMove(new SudokuMove(2,1,1));
//        result = setMove(new SudokuMove(2,4,3));
//        result = setMove(new SudokuMove(3,5,1));
//        result = setMove(new SudokuMove(4,3,6));
//        result = setMove(new SudokuMove(4,8,1));
//        result = setMove(new SudokuMove(5,2,1));
//        result = setMove(new SudokuMove(5,6,2));
//        result = setMove(new SudokuMove(6,1,2));
//        result = setMove(new SudokuMove(6,3,5));
//        result = setMove(new SudokuMove(6,5,9));
//        result = setMove(new SudokuMove(6,6,4));
//        result = setMove(new SudokuMove(7,0,5));
//        result = setMove(new SudokuMove(7,5,2));
//        result = setMove(new SudokuMove(7,8,6));
//        result = setMove(new SudokuMove(8,8,7));
//
//        totalResult = solveBoard();

        // from this part we are creating the board.
        int numNumbers = 0;
        int numIteration;
        switch(difficulty){
            case EASY:
                numNumbers = EASY_NUM_SQUARES;
                break;
            case MEDIUM:
                numNumbers = MEDIUM_NUM_SQUARES;
                break;
            case HARD:
                numNumbers = HARD_NUM_SQUARES;
                break;
        }

        // const number of iteration ofr creating random board.
        numIteration = NUM_ITERATION;

        Random rand = new Random();
        int chosenRow;
        int chosenCol;
        int chosenValue;
        for(int i=0; i<numIteration;) {
            chosenRow = rand.nextInt()%9;
            if(chosenRow < 0)
                chosenRow *= (-1);
            chosenCol = rand.nextInt()%9;
            if(chosenCol < 0)
                chosenCol *= (-1);
            chosenValue = rand.nextInt()%9+1;
            if(chosenValue < 0)
                chosenValue *= (-1);
            if(_board.getValue(chosenRow,chosenCol) == SudokuBoard.EMPTY_VAL && isValidMove(new SudokuMove(chosenRow, chosenCol, chosenValue))){
                setMove(new SudokuMove(chosenRow, chosenCol, chosenValue));
                _originalBoard.setValue(chosenRow, chosenCol, chosenValue);
                boolean solveResult = solveBoard();
                clear();
                if(solveResult == true)
                    i++;
                else {
                    setMove(new SudokuMove(chosenRow, chosenCol, SudokuBoard.EMPTY_VAL));
                    _originalBoard.setValue(chosenRow, chosenCol, SudokuBoard.EMPTY_VAL);
                }
            }
        }
        solveBoard();

        // we have cleared random board now.
        // first we copy the relevant shown numbers.
        int[][] tempBoard = new int[9][9];

        // clearing board.
        for(int i=0; i<9; i++){
            for(int j=0; j<9; j++){
                tempBoard[i][j] = SudokuBoard.EMPTY_VAL;
            }
        }

        // taking random numbers.
        for(int i=0; i<numNumbers;){
            chosenRow = rand.nextInt()%9;
            if(chosenRow < 0)
                chosenRow *= (-1);
            chosenCol = rand.nextInt()%9;
            if(chosenCol < 0)
                chosenCol *= (-1);
            if(tempBoard[chosenRow][chosenCol] != SudokuBoard.EMPTY_VAL)
                continue;
            tempBoard[chosenRow][chosenCol] = _board.getValue(chosenRow, chosenCol);
            i++;
        }

        // clearing all board, and inserting values one by one.
        for(int i=0; i<9; i++){
            for(int j=0; j<9; j++)
                for(int k=0; k<=9; k++)
                    _optionsBoard[i][j][k] = AVAILABLE_VALUE;
        }
        _board = new SudokuBoard();
        _originalBoard = new SudokuBoard();

        for(int i=0; i<9; i++){
            for(int j=0; j<9; j++){
                if(tempBoard[i][j] != SudokuBoard.EMPTY_VAL){
                    setMove(new SudokuMove(i, j, tempBoard[i][j]));
                    _originalBoard.setValue(i, j, tempBoard[i][j]);
                }
            }
        }
    }

    /**
     * setMove- first checks for the validity of the move.
     * then checks if the board isn't already finished.
     * then sets the value, and updates the option table accordingly.
     * @param move the sudoku move to apply.
     * @return result of the move (success, invalid, already finished)
     */
    public SudokuResultType setMove(SudokuMove move){
        if(!isValidMove(move))
            return SudokuResultType.INVALID_MOVE;
        if(isFinished())
            return SudokuResultType.FINISH;
        if(move.row>=0 && move.row<=8 && move.col>=0 && move.col<=8 && move.value>=0 && move.value<=9) {
            // updating options table base on rows and cols.
            // only if we putting actual number. (on empty value no update needed)
            if(move.value != SudokuBoard.EMPTY_VAL)
                updateOptionsBoardAfterSetMove(move.row, move.col, move.value, TAKEN_VALUE);

            // if it is a change we should undo previous number options
            // and make them available again.
            int oldValue = _board.getValue(move.row, move.col);

            // finally, actually updating the value.
            _board.setValue(move.row, move.col, move.value);

            if(oldValue != SudokuBoard.EMPTY_VAL
                    && oldValue != move.value) {
                updateOptionsBoardAfterSetMove(move.row, move.col, oldValue, AVAILABLE_VALUE);
                // now we refresh the options by traversing over all 'oldValue'
                // and update the options table again.
                for(int i=0; i<9; i++){
                    for(int j=0; j<9; j++){
                        if(_board.getValue(i, j) == oldValue){
                            updateOptionsBoardAfterSetMove(i, j, oldValue, TAKEN_VALUE);
                        }
                    }
                }
            }
            return SudokuResultType.SUCCESS;
        }
        return SudokuResultType.INVALID_MOVE;
    }

    /**
     * isValidMove - checks validity of the parameters, and by the options table.
     * for checking change validity (if you can change it), should be passed with
     * EMPTY_VALUE parameter.
     * @param move the move to check.
     * @return true for valid move, false for some error (wrong parameters / invalid move)
     */
    public boolean isValidMove(SudokuMove move){
        if(!(move.row>=0 && move.row<=8 && move.col>=0 && move.col<=8 && move.value>=0 && move.value<=9))
            return false;
        if(_originalBoard.getValue(move.row, move.col) != SudokuBoard.EMPTY_VAL)
            return false;
        boolean status = _optionsBoard[move.row][move.col][move.value];
        if(status == TAKEN_VALUE)
            return false;
        return true;
    }

    public boolean isChangable(int row, int col){
        return _originalBoard.getValue(row, col) == SudokuBoard.EMPTY_VAL;
    }

    public boolean isFinished(){
        boolean finished = true;
        for(int i=0; i<9; i++){
            for(int j=0; j<9; j++){
                if(_board.getValue(i, j) == SudokuBoard.EMPTY_VAL)
                    finished = false;
            }
        }
        return finished;
    }

    public void clear(){
        // clearing the options board.
        for(int i=0; i<9; i++){
            for(int j=0; j<9; j++){
                for(int k=0; k<10; k++)
                    _optionsBoard[i][j][k] = AVAILABLE_VALUE;
            }
        }

        for(int i=0; i<9; i++){
            for(int j=0; j<9; j++){
                _board.setValue(i, j, SudokuBoard.EMPTY_VAL);
            }
        }

        for(int i=0; i<9; i++){
            for(int j=0; j<9; j++){
                // setting the value.
                _board.setValue(i, j, _originalBoard.getValue(i, j));

                // updating the options board.
                updateOptionsBoardAfterSetMove(i, j, _board.getValue(i, j), TAKEN_VALUE);
            }
        }
    }

    /**
     * tries to solve the sudoku board by inserting values.
     * returns whether succeed or failed.
     * @return true if the board is solved, false if there is no available solution.
     */
    private boolean solveBoard(){
        // searches for the first unassigned value.

        return solveBoardAux(0,0);

    }

    private boolean solveBoardAux(int row, int col){
        boolean breakFlag = false;
        int i=row,j;
        for(j=col; j<9; j++){
            if(_board.getValue(i, j) == SudokuBoard.EMPTY_VAL){
                breakFlag = true;
                break;
            }
        }
        if(breakFlag == false) {
            for (i = row + 1; i < 9; i++) {
                for (j = 0; j < 9; j++) {
                    if (_board.getValue(i, j) == SudokuBoard.EMPTY_VAL) {
                        breakFlag = true;
                        break;
                    }
                }
                if (breakFlag == true)
                    break;
            }
        }
        if(breakFlag == false) { //finished
            return true;
        }

        for(int k=1; k<=9; k++){
            SudokuResultType moveResult = setMove(new SudokuMove(i, j, k));
            if(moveResult == SudokuResultType.FINISH)
                return true;
            if(moveResult == SudokuResultType.INVALID_MOVE)
                continue;
            boolean solveResult = solveBoardAux(i, j + 1);
            if(solveResult == true)
                return true;
        }
        setMove(new SudokuMove(i,j,SudokuBoard.EMPTY_VAL));
        return false;
    }

//    private boolean solveBoard(){
//        counter++;
//        // searches for the first unassigned value.
//        boolean breakFlag = false;
//        int i=0,j=0;
//        for(i=0; i<9; i++){
//            for(j=0; j<9; j++){
//                if(_board.getValue(i, j) == SudokuBoard.EMPTY_VAL){
//                    breakFlag = true;
//                    break;
//                }
//            }
//            if(breakFlag == true)
//                break;
//        }
//        if(breakFlag == false) { //finished
//            return true;
//        }
//
//        for(int k=1; k<=9; k++){
//            SudokuResultType moveResult = setMove(new SudokuMove(i, j, k));
//            if(moveResult == SudokuResultType.FINISH)
//                return true;
//            if(moveResult == SudokuResultType.INVALID_MOVE)
//                continue;
//            boolean solveResult = solveBoard();
//            if(solveResult == true)
//                return true;
//        }
//        setMove(new SudokuMove(i,j,SudokuBoard.EMPTY_VAL));
//        return false;
//    }

    public SudokuBoard getBoard(){
        return _board;
    }

    public SudokuBoard getOriginalBoard(){
        return _originalBoard;
    }
}
