// StarterSat4j.java
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ISolver;
import org.sat4j.core.VecInt;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

public class SudokuSolver {

    // creates a sat instance using the given sudoku puzzle, solves that instance, 
    // then uses the solution to construct and return a solved puzzle
    // returns null if the puzzle cannot be solved
    public static Sudoku solveSudoku(Sudoku sudo){
        ISolver solver = buildSolver(sudo); //invokes your solver code
        int boardSize = sudo.getBoard().length; // gets the board size
        try{
            boolean isSat = solver.isSatisfiable(); // check that the formula is satisfiabl
            if(isSat){ // if so
                int[] model = solver.model(); // get the true variables
                return buildSolution(boardSize, model); // call your code to construct the solved puzzle, returning the result
            }
        } catch (TimeoutException te) { // solving the SAT instance may time out, if so then it throws an exception
                                            // If your code times out, you can try changing the timeout length
                                            // but more likely your formula is unneccessarily long.
            System.out.println("Solving timed out: " + te.getMessage());
        }
        return null; // return null if the puzzle is not solvable (we do not test your code on any such puzzles)
    }

    private static ISolver buildSolver(Sudoku sudo){
        ISolver solver = SolverFactory.newDefault(); // create an ISolver
        solver.setTimeout(5); // set the timeout to 5 seconds, you may change this but likely won't need to
        int[][] board = sudo.getBoard(); // get the contents of the puzzle, you'll use this to build your SAT formula
        int boardSize = board.length; // the size of the board
        int n = (int) Math.sqrt(boardSize);  // the value of n in the problem definition, i.e. the square root of the board size

        // number of Boolean variables x_{r,c,v}
        int numVars = boardSize * boardSize * boardSize;
        solver.newVar(numVars);
        // optional hint; not required
        solver.setExpectedNumberOfClauses(100000);

        try {
            // 1. Each cell has exactly one value
            addCellConstraints(solver, boardSize);

            // 2. Each row has each value exactly once
            addRowConstraints(solver, boardSize);

            // 3. Each column has each value exactly once
            addColumnConstraints(solver, boardSize);

            // 4. Each n x n block has each value exactly once
            addBlockConstraints(solver, boardSize, n);

            // 5. Clues from the given puzzle
            addClueConstraints(solver, board);

        } catch (ContradictionException e) {
            // If we get a contradiction while building, the encoding is inconsistent.
            // For this assignment, puzzles are solvable, so this likely indicates a bug.
            throw new RuntimeException("Contradiction while building Sudoku SAT instance", e);
        }

        return solver; // return the formula when done.
    }


    // use the assignments of true/false to each variable to construct
    // a solved sudoku puzzle by filling in a 2d array of numbers.
    public static Sudoku buildSolution(int boardSize, int[] model){
        int[][] solved = new int[boardSize][boardSize];

        // model contains all literals that are true in some order
        for (int lit : model) {
            if (lit <= 0) {
                // ignore negative literals
                continue;
            }
            int var = lit - 1; // back to 0-based

            int v = var % boardSize;
            int tmp = var / boardSize;
            int c = tmp % boardSize;
            int r = tmp / boardSize;

            // if x_{r,c,v} is true, then cell (r,c) has value v+1
            if (r >= 0 && r < boardSize && c >= 0 && c < boardSize) {
                solved[r][c] = v + 1;
            }
        }

        // Sudoku constructor will validate the completed board
        return new Sudoku(solved);
    }

    // ================== Helper methods ==================

    // Map (row r, col c, value v) to a Sat4J variable id in 1..boardSize^3.
    // r, c, v are 0-based.
    private static int var(int r, int c, int v, int boardSize) {
        return r * boardSize * boardSize + c * boardSize + v + 1;
    }

    // 1. Cell constraints: each cell has exactly one value
    private static void addCellConstraints(ISolver solver, int boardSize)
            throws ContradictionException {

        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {

                // At least one value in each cell
                int[] atLeastOne = new int[boardSize];
                for (int v = 0; v < boardSize; v++) {
                    atLeastOne[v] = var(r, c, v, boardSize);
                }
                solver.addClause(new VecInt(atLeastOne));

                // At most one value: pairwise negative clauses
                for (int v1 = 0; v1 < boardSize; v1++) {
                    for (int v2 = v1 + 1; v2 < boardSize; v2++) {
                        int[] clause = new int[] {
                            -var(r, c, v1, boardSize),
                            -var(r, c, v2, boardSize)
                        };
                        solver.addClause(new VecInt(clause));
                    }
                }
            }
        }
    }

    // 2. Row constraints: each value appears exactly once per row
    private static void addRowConstraints(ISolver solver, int boardSize)
            throws ContradictionException {

        for (int r = 0; r < boardSize; r++) {
            for (int v = 0; v < boardSize; v++) {

                // At least one column in this row has value v
                int[] atLeastOne = new int[boardSize];
                for (int c = 0; c < boardSize; c++) {
                    atLeastOne[c] = var(r, c, v, boardSize);
                }
                solver.addClause(new VecInt(atLeastOne));

                // At most one column in this row has value v
                for (int c1 = 0; c1 < boardSize; c1++) {
                    for (int c2 = c1 + 1; c2 < boardSize; c2++) {
                        int[] clause = new int[] {
                            -var(r, c1, v, boardSize),
                            -var(r, c2, v, boardSize)
                        };
                        solver.addClause(new VecInt(clause));
                    }
                }
            }
        }
    }

    // 3. Column constraints: each value appears exactly once per column
    private static void addColumnConstraints(ISolver solver, int boardSize)
            throws ContradictionException {

        for (int c = 0; c < boardSize; c++) {
            for (int v = 0; v < boardSize; v++) {

                // At least one row in this column has value v
                int[] atLeastOne = new int[boardSize];
                for (int r = 0; r < boardSize; r++) {
                    atLeastOne[r] = var(r, c, v, boardSize);
                }
                solver.addClause(new VecInt(atLeastOne));

                // At most one row in this column has value v
                for (int r1 = 0; r1 < boardSize; r1++) {
                    for (int r2 = r1 + 1; r2 < boardSize; r2++) {
                        int[] clause = new int[] {
                            -var(r1, c, v, boardSize),
                            -var(r2, c, v, boardSize)
                        };
                        solver.addClause(new VecInt(clause));
                    }
                }
            }
        }
    }

    // 4. Block constraints: each value appears exactly once per n x n block
    private static void addBlockConstraints(ISolver solver, int boardSize, int n)
            throws ContradictionException {

        // There are n x n blocks, each of size n x n
        for (int br = 0; br < n; br++) {
            for (int bc = 0; bc < n; bc++) {

                int rowStart = br * n;
                int colStart = bc * n;

                for (int v = 0; v < boardSize; v++) {

                    // At least one cell in this block has value v
                    int[] atLeastOne = new int[boardSize]; // n^2 = boardSize cells in a block
                    int idx = 0;
                    for (int dr = 0; dr < n; dr++) {
                        for (int dc = 0; dc < n; dc++) {
                            int r = rowStart + dr;
                            int c = colStart + dc;
                            atLeastOne[idx++] = var(r, c, v, boardSize);
                        }
                    }
                    solver.addClause(new VecInt(atLeastOne));

                    // At most one cell in this block has value v: pairwise negatives
                    for (int i = 0; i < boardSize; i++) {
                        for (int j = i + 1; j < boardSize; j++) {
                            int dr1 = i / n;
                            int dc1 = i % n;
                            int r1 = rowStart + dr1;
                            int c1 = colStart + dc1;

                            int dr2 = j / n;
                            int dc2 = j % n;
                            int r2 = rowStart + dr2;
                            int c2 = colStart + dc2;

                            int[] clause = new int[] {
                                -var(r1, c1, v, boardSize),
                                -var(r2, c2, v, boardSize)
                            };
                            solver.addClause(new VecInt(clause));
                        }
                    }
                }
            }
        }
    }

    // 5. Clues: pre-filled cells in the given puzzle
    private static void addClueConstraints(ISolver solver, int[][] board)
            throws ContradictionException {

        int boardSize = board.length;

        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                int value = board[r][c];
                if (value != 0) {
                    // value is in 1..boardSize; convert to v in 0..boardSize-1
                    int v = value - 1;
                    int lit = var(r, c, v, boardSize);
                    IVecInt clause = new VecInt(new int[] { lit });
                    solver.addClause(clause);
                }
            }
        }
    }
}
