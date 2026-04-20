import java.lang.reflect.Array;
import java.util.ArrayList;

public class checkersBoard {
    String[][] board = new String[8][8];
    boolean Bsturn = false;//false means white turn. True means black turn.


    public void populateBoard() {
        int k = 0;
        for(int i = 0; i < 3; i++){//populate bottom half with (w)hite
            for(int j = 0 + k; j < 8; j+=2){
                board[i][j] = "w";
            }
            if(k == 1){
                k = 0;
            }else{
                k = 1;
            }
        }


        for(int i = 5; i < 7; i++){//populate top half with (b)lack
            for(int j = 0 + k; j < 8; j+=2){
                board[i][j] = "b";
            }
            if(k == 1){
                k = 0;
            }else{
                k = 1;
            }
        }
    }

    public ArrayList<int[]> Vmoves(int row, int col){//check for validmoves
        ArrayList<int[]> validMoves = new ArrayList<>();
        String curpiece = board[row][col];

        if(!Bsturn) {
            if (curpiece.equals( "w")) {//handle white pieces

                if ((row + 1 < 8 && col - 1 >= 0) && board[row + 1][col - 1] == null) {
                    validMoves.add(new int[]{row + 1, col - 1});
                }
                if ((row + 1 < 8 && col + 1 < 8) && board[row + 1][col + 1] == null) {
                    validMoves.add(new int[]{row + 1, col + 1});
                }
                if ((row + 2 < 8 && col - 2 >= 0) && "b".equals(board[row + 1][col - 1]) && board[row + 2][col - 2] == null) {
                    validMoves.add(new int[]{row + 2, col - 2});
                }
                if ((row + 2 < 8 && col + 2 < 8) && "b".equals(board[row + 1][col + 1]) && board[row + 2][col + 2] == null) {
                    validMoves.add(new int[]{row + 2, col + 2});
                }
            }
        }

        if(Bsturn) {
            if (curpiece.equals("b")) {// handle black pieces
                if ((row - 1 >= 0 && col - 1 >= 0) && board[row - 1][col - 1] == null) {
                    validMoves.add(new int[]{row - 1, col - 1});
                }
                if ((row - 1 >= 0 && col + 1 < 8) && board[row - 1][col + 1] == null) {
                    validMoves.add(new int[]{row - 1, col + 1});
                }
                if ((row - 2 >= 0 && col - 2 >= 0) && "w".equals(board[row - 1][col - 1]) && board[row - 2][col - 2] == null) {
                    validMoves.add(new int[]{row - 2, col - 2});
                }
                if ((row - 2 >= 0 && col + 2 < 8) && "w".equals(board[row - 1][col + 1]) && board[row - 2][col + 2] == null) {
                    validMoves.add(new int[]{row - 2, col + 2});
                }
            }
        }

        if(!Bsturn) {
            if (curpiece.equals("wk")) {//handle wk
                if ((row + 1 < 8 && col - 1 >= 0) && board[row + 1][col - 1] == null) {
                    validMoves.add(new int[]{row + 1, col - 1});
                }
                if ((row + 1 < 8 && col + 1 < 8) && board[row + 1][col + 1] == null) {
                    validMoves.add(new int[]{row + 1, col + 1});
                }
                if ((row + 2 < 8 && col - 2 >= 0) && "b".equals(board[row + 1][col - 1]) && board[row + 2][col - 2] == null) {
                    validMoves.add(new int[]{row + 2, col - 2});
                }
                if ((row + 2 < 8 && col + 2 < 8) && "b".equals(board[row + 1][col + 1]) && board[row + 2][col + 2] == null) {
                    validMoves.add(new int[]{row + 2, col + 2});
                }
                if ((row - 1 >= 0 && col - 1 >= 0) && board[row - 1][col - 1] == null) {
                    validMoves.add(new int[]{row - 1, col - 1});
                }
                if ((row - 1 >= 0 && col + 1 < 8) && board[row - 1][col + 1] == null) {
                    validMoves.add(new int[]{row - 1, col + 1});
                }
                if ((row - 2 >= 0 && col - 2 >= 0) && "b".equals(board[row - 1][col - 1]) && board[row - 2][col - 2] == null) {
                    validMoves.add(new int[]{row - 2, col - 2});
                }
                if ((row - 2 >= 0 && col + 2 < 8) && "b".equals(board[row - 1][col + 1]) && board[row - 2][col + 2] == null) {
                    validMoves.add(new int[]{row - 2, col + 2});
                }

            }
        }

        if(Bsturn) {
            if (curpiece.equals("bk")) {//handle bk
                if ((row + 1 < 8 && col - 1 >= 0) && board[row + 1][col - 1] == null) {
                    validMoves.add(new int[]{row + 1, col - 1});
                }
                if ((row + 1 < 8 && col + 1 < 8) && board[row + 1][col + 1] == null) {
                    validMoves.add(new int[]{row + 1, col + 1});
                }
                if ((row + 2 < 8 && col - 2 >= 0) && "w".equals(board[row + 1][col - 1]) && board[row + 2][col - 2] == null) {
                    validMoves.add(new int[]{row + 2, col - 2});
                }
                if ((row + 2 < 8 && col + 2 < 8) && "w".equals(board[row + 1][col + 1]) && board[row + 2][col + 2] == null) {
                    validMoves.add(new int[]{row + 2, col + 2});
                }
                if ((row - 1 >= 0 && col - 1 >= 0) && board[row - 1][col - 1] == null) {
                    validMoves.add(new int[]{row - 1, col - 1});
                }
                if ((row - 1 >= 0 && col + 1 < 8) && board[row - 1][col + 1] == null) {
                    validMoves.add(new int[]{row - 1, col + 1});
                }
                if ((row - 2 >= 0 && col - 2 >= 0) && "w".equals(board[row - 1][col - 1]) && board[row - 2][col - 2] == null) {
                    validMoves.add(new int[]{row - 2, col - 2});
                }
                if ((row - 2 >= 0 && col + 2 < 8) && "w".equals(board[row - 1][col + 1]) && board[row - 2][col + 2] == null) {
                    validMoves.add(new int[]{row - 2, col + 2});
                }
            }
        }
        return validMoves;
    }

    public void move(int row, int col, int startRow, int startCol, String piece){
        if ((row == 0 || row == 7) && (!piece.equals("bk") && !piece.equals("wk"))){
            piece = piece + "k";
        }
        board[row][col] = piece;
        board[startRow][startCol] = null;
        if(Math.abs(row - startRow) == 2){
            board[(row + startRow)/2][(col + startCol)/2] = null;
        }
        if(Bsturn){
            Bsturn = false;
        }
        else{
            Bsturn = true;
        }
    }

    public String checkWin(){
        boolean black = true;
        boolean white = true;
        for(int i = 0; i < 8;i++){
            for(int j = 0; j < 8; j++){
                if(("w".equals(board[i][j])) || ("wk".equals(board[i][j]))){
                    black = false;
                }
                if(("b".equals(board[i][j])) || ("bk".equals(board[i][j]))){
                    white = false;
                }
            }
        }
        if(black){
            return "b";
        }else if(white){
            return "w";
        }else{
            return "";
        }
    }

    public String[][] getBoard() {
        return board;
    }
}
