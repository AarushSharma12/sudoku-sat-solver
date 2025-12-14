import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ISolver;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

/**
 * Solves Sudoku puzzles by reducing them to Boolean Satisfiability (SAT) problems.
 * 
 * <p>This solver encodes Sudoku constraints as Conjunctive Normal Form (CNF) clauses
 * and delegates solving to the Sat4j library. It supports any N×N board where N is
 * a perfect square (4×4, 9×9, 16×16, etc.).</p>
 * 
 * <h2>Encoding Strategy</h2>
 * <p>Each possible cell assignment is represented as a Boolean variable {@code x(r,c,v)}
 * meaning "cell (r,c) contains value v". The variable mapping function:</p>
 * <pre>
 *   var(r, c, v) = r × N² + c × N + v + 1
 * </pre>
 * 
 * <h2>Constraint Types</h2>
 * <ul>
 *   <li><b>Cell constraints:</b> Each cell contains exactly one value</li>
 *   <li><b>Row constraints:</b> Each value appears exactly once per row</li>
 *   <li><b>Column constraints:</b> Each value appears exactly once per column</li>
 *   <li><b>Block constraints:</b> Each value appears exactly once per √N×√N block</li>
 *   <li><b>Clue constraints:</b> Pre-filled cells are fixed to their given values</li>
 * </ul>
 * 
 * @author Aarush Sharma
 * @see <a href="http://www.sat4j.org/">Sat4j SAT Solver</a>
 */
public class SudokuSolver {

    private static final int DEFAULT_TIMEOUT_SECONDS = 5;
    private static final int EXPECTED_CLAUSE_COUNT = 100000;

    /**
     * Solves a Sudoku puzzle using SAT reduction.
     * 
     * <p>Constructs a SAT instance encoding all Sudoku constraints, solves it,
     * and reconstructs the solution from the satisfying assignment.</p>
     * 
     * @param puzzle the Sudoku puzzle to solve (0 represents empty cells)
     * @return the solved puzzle, or {@code null} if unsolvable or timeout occurs
     */
    public static Sudoku solveSudoku(Sudoku puzzle) {
        ISolver solver = buildSolver(puzzle);
        int boardSize = puzzle.getBoard().length;
        
        try {
            if (solver.isSatisfiable()) {
                int[] model = solver.model();
                return buildSolution(boardSize, model);
            }
        } catch (TimeoutException e) {
            System.err.println("SAT solver timeout: " + e.getMessage());
        }
        
        return null;
    }

    /**
     * Constructs a SAT solver instance with all Sudoku constraints encoded as CNF clauses.
     * 
     * @param puzzle the input puzzle containing clues
     * @return configured ISolver instance ready for satisfiability checking
     * @throws RuntimeException if a contradiction is detected during clause construction
     */
    private static ISolver buildSolver(Sudoku puzzle) {
        ISolver solver = SolverFactory.newDefault();
        solver.setTimeout(DEFAULT_TIMEOUT_SECONDS);
        
        int[][] board = puzzle.getBoard();
        int boardSize = board.length;
        int blockSize = (int) Math.sqrt(boardSize);
        int numVars = boardSize * boardSize * boardSize;
        
        solver.newVar(numVars);
        solver.setExpectedNumberOfClauses(EXPECTED_CLAUSE_COUNT);

        try {
            addCellConstraints(solver, boardSize);
            addRowConstraints(solver, boardSize);
            addColumnConstraints(solver, boardSize);
            addBlockConstraints(solver, boardSize, blockSize);
            addClueConstraints(solver, board);
        } catch (ContradictionException e) {
            throw new RuntimeException("Contradiction detected during CNF construction", e);
        }

        return solver;
    }

    /**
     * Reconstructs a solved Sudoku board from a SAT model.
     * 
     * <p>Iterates through positive literals in the model and maps each
     * variable back to its (row, column, value) coordinates.</p>
     * 
     * @param boardSize the dimension of the puzzle (N for an N×N board)
     * @param model array of literals representing the satisfying assignment
     * @return the completed Sudoku puzzle
     */
    private static Sudoku buildSolution(int boardSize, int[] model) {
        int[][] solved = new int[boardSize][boardSize];

        for (int literal : model) {
            if (literal <= 0) {
                continue;
            }
            
            int var = literal - 1;
            int value = var % boardSize;
            int temp = var / boardSize;
            int col = temp % boardSize;
            int row = temp / boardSize;

            if (row >= 0 && row < boardSize && col >= 0 && col < boardSize) {
                solved[row][col] = value + 1;
            }
        }

        return new Sudoku(solved);
    }

    /**
     * Maps a (row, column, value) triple to a unique SAT variable ID.
     * 
     * @param row zero-indexed row
     * @param col zero-indexed column
     * @param value zero-indexed value (actual value minus 1)
     * @param boardSize dimension of the board
     * @return 1-indexed variable ID for the SAT solver
     */
    private static int var(int row, int col, int value, int boardSize) {
        return row * boardSize * boardSize + col * boardSize + value + 1;
    }

    /**
     * Adds cell constraints: each cell contains exactly one value.
     * 
     * <p>For each cell, adds an "at least one" clause and pairwise
     * "at most one" exclusion clauses.</p>
     */
    private static void addCellConstraints(ISolver solver, int boardSize)
            throws ContradictionException {

        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                int[] atLeastOne = new int[boardSize];
                for (int v = 0; v < boardSize; v++) {
                    atLeastOne[v] = var(row, col, v, boardSize);
                }
                solver.addClause(new VecInt(atLeastOne));

                for (int v1 = 0; v1 < boardSize; v1++) {
                    for (int v2 = v1 + 1; v2 < boardSize; v2++) {
                        solver.addClause(new VecInt(new int[] {
                            -var(row, col, v1, boardSize),
                            -var(row, col, v2, boardSize)
                        }));
                    }
                }
            }
        }
    }

    /**
     * Adds row constraints: each value appears exactly once per row.
     */
    private static void addRowConstraints(ISolver solver, int boardSize)
            throws ContradictionException {

        for (int row = 0; row < boardSize; row++) {
            for (int v = 0; v < boardSize; v++) {
                int[] atLeastOne = new int[boardSize];
                for (int col = 0; col < boardSize; col++) {
                    atLeastOne[col] = var(row, col, v, boardSize);
                }
                solver.addClause(new VecInt(atLeastOne));

                for (int c1 = 0; c1 < boardSize; c1++) {
                    for (int c2 = c1 + 1; c2 < boardSize; c2++) {
                        solver.addClause(new VecInt(new int[] {
                            -var(row, c1, v, boardSize),
                            -var(row, c2, v, boardSize)
                        }));
                    }
                }
            }
        }
    }

    /**
     * Adds column constraints: each value appears exactly once per column.
     */
    private static void addColumnConstraints(ISolver solver, int boardSize)
            throws ContradictionException {

        for (int col = 0; col < boardSize; col++) {
            for (int v = 0; v < boardSize; v++) {
                int[] atLeastOne = new int[boardSize];
                for (int row = 0; row < boardSize; row++) {
                    atLeastOne[row] = var(row, col, v, boardSize);
                }
                solver.addClause(new VecInt(atLeastOne));

                for (int r1 = 0; r1 < boardSize; r1++) {
                    for (int r2 = r1 + 1; r2 < boardSize; r2++) {
                        solver.addClause(new VecInt(new int[] {
                            -var(r1, col, v, boardSize),
                            -var(r2, col, v, boardSize)
                        }));
                    }
                }
            }
        }
    }

    /**
     * Adds block constraints: each value appears exactly once per √N×√N block.
     */
    private static void addBlockConstraints(ISolver solver, int boardSize, int blockSize)
            throws ContradictionException {

        for (int blockRow = 0; blockRow < blockSize; blockRow++) {
            for (int blockCol = 0; blockCol < blockSize; blockCol++) {
                int rowStart = blockRow * blockSize;
                int colStart = blockCol * blockSize;

                for (int v = 0; v < boardSize; v++) {
                    int[] atLeastOne = new int[boardSize];
                    int idx = 0;
                    
                    for (int dr = 0; dr < blockSize; dr++) {
                        for (int dc = 0; dc < blockSize; dc++) {
                            atLeastOne[idx++] = var(rowStart + dr, colStart + dc, v, boardSize);
                        }
                    }
                    solver.addClause(new VecInt(atLeastOne));

                    for (int i = 0; i < boardSize; i++) {
                        for (int j = i + 1; j < boardSize; j++) {
                            int r1 = rowStart + (i / blockSize);
                            int c1 = colStart + (i % blockSize);
                            int r2 = rowStart + (j / blockSize);
                            int c2 = colStart + (j % blockSize);

                            solver.addClause(new VecInt(new int[] {
                                -var(r1, c1, v, boardSize),
                                -var(r2, c2, v, boardSize)
                            }));
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds clue constraints: pre-filled cells are fixed to their given values.
     * 
     * <p>Each non-zero cell in the input board generates a unit clause
     * asserting that specific variable assignment.</p>
     */
    private static void addClueConstraints(ISolver solver, int[][] board)
            throws ContradictionException {

        int boardSize = board.length;

        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                int value = board[row][col];
                if (value != 0) {
                    int v = value - 1;
                    solver.addClause(new VecInt(new int[] { var(row, col, v, boardSize) }));
                }
            }
        }
    }
}
