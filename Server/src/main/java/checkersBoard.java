import java.lang.reflect.Array;
import java.util.ArrayList;

public class checkersBoard {
    String[][] board = new String[8][8];
    boolean turn = false;//false means white turn. True means black turn.


    private void populateBoard() {
        int k = 0;
        for(int i = 0; i < 3; i++){//populate bottom half with (w)hite
            for(int j = 0 + k; j <= 8; j+=2){
                board[i][j] = "w";
            }
            if(k == 1){
                k = 0;
            }else{
                k = 1;
            }
        }


        for(int i = 5; i < 7; i++){//populate top half with (b)lack
            for(int j = 0 + k; j <= 8; j+=2){
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

        if(!turn) {
            if (curpiece == "w") {//handle white pieces

                if ((row + 1 < 8 & col - 1 < 8) & board[row + 1][col - 1] == null) {
                    validMoves.add(new int[]{row + 1, col - 1});
                }
                if ((row + 1 < 8 & col + 1 < 8) & board[row + 1][col + 1] == null) {
                    validMoves.add(new int[]{row + 1, col + 1});
                }
                if ((row + 2 < 8 & col - 2 < 8) & board[row + 1][col - 1] == "b" & board[row + 2][col - 2] == null) {
                    validMoves.add(new int[]{row + 2, col - 2});
                }
                if ((row + 2 < 8 & col + 2 < 8) & board[row + 1][col + 1] == "b" & board[row + 2][col + 2] == null) {
                    validMoves.add(new int[]{row + 2, col + 2});
                }
            }
        }

        if(turn) {
            if (curpiece == "b") {// handle black pieces
                if ((row - 1 >= 0 & col - 1 >= 0) & board[row - 1][col - 1] == null) {
                    validMoves.add(new int[]{row - 1, col - 1});
                }
                if ((row - 1 >= 0 & col + 1 < 8) & board[row - 1][col + 1] == null) {
                    validMoves.add(new int[]{row - 1, col + 1});
                }
                if ((row - 2 >= 0 & col - 2 >= 0) & board[row - 1][col - 1] == "w" & board[row - 2][col - 2] == null) {
                    validMoves.add(new int[]{row - 2, col - 2});
                }
                if ((row - 2 >= 0 & col + 2 < 8) & board[row - 1][col + 1] == "w" & board[row - 2][col + 2] == null) {
                    validMoves.add(new int[]{row - 2, col + 2});
                }
            }
        }

        if(!turn) {
            if (curpiece == "wk") {//handle wk
                if ((row + 1 < 8 & col - 1 < 8) & board[row + 1][col - 1] == null) {
                    validMoves.add(new int[]{row + 1, col - 1});
                }
                if ((row + 1 < 8 & col + 1 < 8) & board[row + 1][col + 1] == null) {
                    validMoves.add(new int[]{row + 1, col + 1});
                }
                if ((row + 2 < 8 & col - 2 < 8) & board[row + 1][col - 1] == "b" & board[row + 2][col - 2] == null) {
                    validMoves.add(new int[]{row + 2, col - 2});
                }
                if ((row + 2 < 8 & col + 2 < 8) & board[row + 1][col + 1] == "b" & board[row + 2][col + 2] == null) {
                    validMoves.add(new int[]{row + 2, col + 2});
                }
                if ((row - 1 >= 0 & col - 1 >= 0) & board[row - 1][col - 1] == null) {
                    validMoves.add(new int[]{row - 1, col - 1});
                }
                if ((row - 1 >= 0 & col + 1 < 8) & board[row - 1][col + 1] == null) {
                    validMoves.add(new int[]{row - 1, col + 1});
                }
                if ((row - 2 >= 0 & col - 2 >= 0) & board[row - 1][col - 1] == "b" & board[row - 2][col - 2] == null) {
                    validMoves.add(new int[]{row - 2, col - 2});
                }
                if ((row - 2 >= 0 & col + 2 < 8) & board[row - 1][col + 1] == "b" & board[row - 2][col + 2] == null) {
                    validMoves.add(new int[]{row - 2, col + 2});
                }

            }
        }

        if(turn) {
            if (curpiece == "bk") {//handle bk
                if ((row + 1 < 8 & col - 1 < 8) & board[row + 1][col - 1] == null) {
                    validMoves.add(new int[]{row + 1, col - 1});
                }
                if ((row + 1 < 8 & col + 1 < 8) & board[row + 1][col + 1] == null) {
                    validMoves.add(new int[]{row + 1, col + 1});
                }
                if ((row + 2 < 8 & col - 2 < 8) & board[row + 1][col - 1] == "w" & board[row + 2][col - 2] == null) {
                    validMoves.add(new int[]{row + 2, col - 2});
                }
                if ((row + 2 < 8 & col + 2 < 8) & board[row + 1][col + 1] == "w" & board[row + 2][col + 2] == null) {
                    validMoves.add(new int[]{row + 2, col + 2});
                }
                if ((row - 1 >= 0 & col - 1 >= 0) & board[row - 1][col - 1] == null) {
                    validMoves.add(new int[]{row - 1, col - 1});
                }
                if ((row - 1 >= 0 & col + 1 < 8) & board[row - 1][col + 1] == null) {
                    validMoves.add(new int[]{row - 1, col + 1});
                }
                if ((row - 2 >= 0 & col - 2 >= 0) & board[row - 1][col - 1] == "w" & board[row - 2][col - 2] == null) {
                    validMoves.add(new int[]{row - 2, col - 2});
                }
                if ((row - 2 >= 0 & col + 2 < 8) & board[row - 1][col + 1] == "w" & board[row - 2][col + 2] == null) {
                    validMoves.add(new int[]{row - 2, col + 2});
                }
            }
        }
        return validMoves;
    }

    public void move(int row, int col, String piece){
        if ((row == 0 | row == 7) && (piece != "bk" || piece != "wk")){
            piece = piece + "k";
        }
        board[row][col] = piece;
        if(turn){
            turn = false;
        }
        else{
            turn = true;
        }
    }

    public String checkWin(){
        boolean black = true;
        boolean white = true;
        for(int i = 0; i < 8;i++){
            for(int j = 0; j < 8; j++){
                if((board[i][j] == "w") || (board[i][j] == "wk")){
                    black = false;
                }
                if((board[i][j] == "b") || (board[i][j] == "bk")){
                    white = false;
                }
            }
        }
        if(black){
            return "b";
        }else if(white){
            return "w";
        }else{
            return "n";
        }
    }

    public String[][] getBoard() {
        return board;
    }
}
