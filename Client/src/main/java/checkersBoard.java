import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class checkersBoard implements Serializable {
    static final long serialVersionUID = 38L;
    String[][] board = new String[8][8];
    public String[][] getBoard() {
        return board;
    }
}
