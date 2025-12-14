import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

/**
 * Command-line application for solving Sudoku puzzles.
 * 
 * <p>Reads puzzle files in a simple text format and displays both
 * the input puzzle and its solution.</p>
 * 
 * <h2>File Format</h2>
 * <pre>
 * N
 * v11 v12 ... v1N
 * v21 v22 ... v2N
 * ...
 * vN1 vN2 ... vNN
 * </pre>
 * <p>Where N is the board size and 0 represents empty cells.</p>
 * 
 * <h2>Usage</h2>
 * <pre>
 * java SudokuApp [puzzle-file]
 * </pre>
 * 
 * @author Aarush Sharma
 */
public class SudokuApp {

    private static final String DEFAULT_PUZZLE = "easy.txt";

    /**
     * Application entry point.
     * 
     * @param args command-line arguments; first argument is the puzzle file path
     */
    public static void main(String[] args) {
        String filename = args.length > 0 ? args[0] : DEFAULT_PUZZLE;
        
        try {
            Sudoku puzzle = loadPuzzle(filename);
            System.out.println("Input puzzle:");
            System.out.println(puzzle);
            
            Sudoku solution = SudokuSolver.solveSudoku(puzzle);
            
            if (solution != null && solution.isSolved()) {
                System.out.println("Solution found:");
                System.out.println(solution);
            } else {
                System.err.println("No solution found for the given puzzle.");
                System.exit(1);
            }
        } catch (IOException e) {
            System.err.println("Error loading puzzle: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Loads a Sudoku puzzle from a text file.
     * 
     * @param filename path to the puzzle file
     * @return the parsed Sudoku puzzle
     * @throws IOException if the file cannot be read or parsed
     */
    public static Sudoku loadPuzzle(String filename) throws IOException {
        File file = new File(filename);
        
        try (Scanner scanner = new Scanner(file)) {
            int size = Integer.parseInt(scanner.nextLine().trim());
            int[][] board = new int[size][size];
            
            for (int row = 0; row < size && scanner.hasNextLine(); row++) {
                String line = scanner.nextLine().trim();
                String[] values = line.split("\\s+");
                
                for (int col = 0; col < size && col < values.length; col++) {
                    board[row][col] = Integer.parseInt(values[col]);
                }
            }
            
            return new Sudoku(board);
        } catch (FileNotFoundException e) {
            throw new IOException("Puzzle file not found: " + filename, e);
        } catch (NumberFormatException e) {
            throw new IOException("Invalid number format in puzzle file", e);
        }
    }
}
