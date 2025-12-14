/**
 * Represents an N×N Sudoku puzzle board.
 * 
 * <p>Supports any board size where N is a perfect square (4, 9, 16, etc.).
 * Empty cells are represented by 0, and filled cells contain values from 1 to N.</p>
 * 
 * <p>The board is validated on construction to ensure:</p>
 * <ul>
 *   <li>The board is non-empty and square</li>
 *   <li>The dimension is a perfect square (for valid block structure)</li>
 *   <li>All cell values are within the valid range [0, N]</li>
 * </ul>
 * 
 * @author Aarush Sharma
 */
public class Sudoku {

    private final int[][] board;

    /**
     * Constructs a Sudoku puzzle from a 2D array.
     * 
     * @param board the puzzle grid where 0 represents empty cells
     * @throws IllegalArgumentException if the board is invalid
     */
    public Sudoku(int[][] board) {
        validateBoard(board);
        this.board = board;
    }

    /**
     * Validates the board structure and cell values.
     */
    private void validateBoard(int[][] board) {
        if (board.length == 0) {
            throw new IllegalArgumentException("Board must not be empty");
        }
        
        if (board.length != board[0].length) {
            throw new IllegalArgumentException("Board must be square");
        }
        
        int size = board.length;
        int sqrt = (int) Math.sqrt(size);
        if (sqrt * sqrt != size) {
            throw new IllegalArgumentException(
                "Board dimension must be a perfect square (4, 9, 16, ...)");
        }
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] < 0 || board[i][j] > size) {
                    throw new IllegalArgumentException(
                        String.format("Cell value at (%d,%d) must be between 0 and %d", i, j, size));
                }
            }
        }
    }

    /**
     * Returns the puzzle board.
     * 
     * @return the 2D array representing the puzzle
     */
    public int[][] getBoard() {
        return board;
    }

    /**
     * Returns a formatted string representation of the puzzle.
     * 
     * @return the board as a space-separated grid
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int[] row : board) {
            for (int cell : row) {
                sb.append(cell).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Validates whether the puzzle is completely and correctly solved.
     * 
     * <p>Checks that:</p>
     * <ul>
     *   <li>All cells are filled (no zeros)</li>
     *   <li>Each row contains all values exactly once</li>
     *   <li>Each column contains all values exactly once</li>
     *   <li>Each block contains all values exactly once</li>
     * </ul>
     * 
     * @return {@code true} if the puzzle is solved correctly, {@code false} otherwise
     */
    public boolean isSolved() {
        int size = board.length;
        
        if (!allCellsFilled()) {
            return false;
        }
        
        if (!allRowsValid(size)) {
            return false;
        }
        
        if (!allColumnsValid(size)) {
            return false;
        }
        
        return allBlocksValid(size);
    }

    private boolean allCellsFilled() {
        for (int[] row : board) {
            for (int cell : row) {
                if (cell <= 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean allRowsValid(int size) {
        for (int i = 0; i < size; i++) {
            int[] seen = new int[size];
            for (int j = 0; j < size; j++) {
                seen[board[i][j] - 1]++;
            }
            if (!allOnes(seen)) {
                return false;
            }
        }
        return true;
    }

    private boolean allColumnsValid(int size) {
        for (int j = 0; j < size; j++) {
            int[] seen = new int[size];
            for (int i = 0; i < size; i++) {
                seen[board[i][j] - 1]++;
            }
            if (!allOnes(seen)) {
                return false;
            }
        }
        return true;
    }

    private boolean allBlocksValid(int size) {
        int blockSize = (int) Math.sqrt(size);
        
        for (int blockRow = 0; blockRow < blockSize; blockRow++) {
            for (int blockCol = 0; blockCol < blockSize; blockCol++) {
                int[] seen = new int[size];
                
                for (int i = 0; i < blockSize; i++) {
                    for (int j = 0; j < blockSize; j++) {
                        int row = blockRow * blockSize + i;
                        int col = blockCol * blockSize + j;
                        seen[board[row][col] - 1]++;
                    }
                }
                
                if (!allOnes(seen)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean allOnes(int[] array) {
        for (int count : array) {
            if (count != 1) {
                return false;
            }
        }
        return true;
    }
}
