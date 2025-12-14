public class Sudoku {
    private int[][] board;

    public Sudoku(int[][] board){
        if(board.length == 0){
            throw new IllegalArgumentException("Board must not be empty!");
        }
        if(board.length != board[0].length){
            throw new IllegalArgumentException("Board must be square!");
        }
        if(((int)Math.sqrt(board.length))*((int)Math.sqrt(board.length)) != board.length){
            throw new IllegalArgumentException("Board dimensions must be squares!");
        }
        for(int i = 0; i < board.length; i++){
            for(int j = 0; j < board.length; j++){
                if(board[i][j]>board.length || board[i][j]<0){
                    throw new IllegalArgumentException("Board must contain numbers 0-size");
                }
            }
        }
        this.board = board;
    }

    public int[][] getBoard(){
        return board;
    }

    public String toString(){
        String acc = "";
        for(int i = 0; i < board.length; i++){
            for(int j = 0; j < board.length; j++){
                acc += "" + board[i][j] + " ";
            }
            acc = acc + "\n";
        }
        return acc;
    }

    public boolean isSolved(){
        // check each cell is filled
        for(int i = 0; i < board.length; i++){
            for(int j = 0; j < board.length; j++){
                if(board[i][j] <= 0){
                    return false;
                }
            }
        }

        // check each row
        for(int i = 0; i < board.length; i++){
            int[] numSeen = new int[board.length];
            for(int j = 0; j < board.length; j++){
                numSeen[board[i][j]-1]++;
            }
            for(int x = 0; x < board.length; x++){
                if(numSeen[x] != 1){
                    return false;
                }
            }
        }

        // check each column
        for(int i = 0; i < board.length; i++){
            int[] numSeen = new int[board.length];
            for(int j = 0; j < board.length; j++){
                numSeen[board[j][i]-1]++;
            }
            for(int x = 0; x < board.length; x++){
                if(numSeen[x] != 1){
                    return false;
                }
            }
        }

        //check each box
        int n = (int)Math.sqrt(board.length);
        for(int boxRow = 0; boxRow < n; boxRow++){
            for(int boxCol = 0; boxCol < n; boxCol++){
                // for each box
                int[] numSeen = new int[board.length];
                for(int i = 0; i < n; i++){
                    int row = boxRow*n+i;
                    for(int j = 0; j < n; j++){
                        int col = boxCol*n+j;
                        numSeen[board[row][col]-1]++;
                    }
                }
                for(int x = 0; x < board.length; x++){
                    if(numSeen[x] != 1){
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
