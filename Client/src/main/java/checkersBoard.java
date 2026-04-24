import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class checkersBoard implements Serializable {
    static final long serialVersionUID = 38L;
    String[][] board = new String[8][8];


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

        for(int i = 5; i < 8; i++){//populate top half with (b)lack
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

    public void clearBoard() {
        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                board[i][j] = null;
            }
        }
    }

    public ArrayList<int[]> Vmoves(int row, int col){

        //List to store all valid moves
        ArrayList<int[]> validMoves = new ArrayList<>();

        //Get the piece at the given position
        String piece = board[row][col];

        //If no piece exists return empty move list
        if (piece == null) return validMoves;

        //Determine piece type
        boolean isWhite = piece.startsWith("w");
        boolean isKing = piece.endsWith("k");

        //Stores all possible movement directions
        int[][] directions;

        //If the piece is a king it can move in all diagonal directions
        if (isKing) {
            directions = new int[][]{{1,1},{1,-1},{-1,1},{-1,-1}};

            //If the piece is white it moves "down" the board
        } else if (isWhite) {
            directions = new int[][]{{1,1},{1,-1}};

            //Otherwise the piece is black and moves "up" the board
        } else {
            directions = new int[][]{{-1,1},{-1,-1}};
        }

        //Check each possible direction
        for (int[] d : directions) {

            //Row and column for a normal move (1 step away)
            int r = row + d[0];
            int c = col + d[1];

            //If the square is within bounds and empty add as a valid normal move
            if (inBounds(r,c) && board[r][c] == null) {
                validMoves.add(new int[]{r,c});
            }

            //Row and column for a jump move (2 steps away)
            int jr = row + 2 * d[0];
            int jc = col + 2 * d[1];

            //Check if jump destination is within bounds and empty
            if (inBounds(jr,jc) && board[jr][jc] == null) {

                //Get the piece in between (the one being jumped over)
                String mid = board[r][c];

                //If there is a piece and it belongs to the opponent allow jump
                if (mid != null && !mid.substring(0,1).equals(piece.substring(0,1))) {
                    validMoves.add(new int[]{jr,jc});
                }
            }
        }

        //Return all valid moves found
        return validMoves;
    }

    private boolean inBounds(int r, int c){
        return r >= 0 && r < 8 && c >= 0 && c < 8;
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
    }

    public int checkWin(){
        boolean blackWin = true;
        boolean whiteWin = true;
        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                String piece = board[i][j];
                ArrayList<int[]> availableMoves;
                if(piece != null) availableMoves = Vmoves(i, j);
                else continue;
                if(piece.contains("w") && !availableMoves.isEmpty()) blackWin = false;
                else if (piece.contains("b") && !availableMoves.isEmpty()) whiteWin = false;
                if(!blackWin && !whiteWin) return 0;
            }
        }
        if(blackWin) return 1;
        else return 2;
    }

    public String[][] getBoard() {
        return board;
    }

    public checkersBoard copy() {
        checkersBoard newBoard = new checkersBoard();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                newBoard.board[i][j] = this.board[i][j];
            }
        }

        return newBoard;
    }

}
