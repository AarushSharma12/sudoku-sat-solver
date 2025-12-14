// StarterSat4j.java
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ISolver;
import org.sat4j.core.VecInt;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

public class StarterSat4j {

    public static void main(String[] args) {
        // Create a SAT solver (default)
        ISolver solver = SolverFactory.newDefault();

        // Tell solver how many variables we plan to use (optional but recommended).
        // Variables are numbered 1..n (NOT 0-indexed).
        int numVars = 3;
        solver.newVar(numVars);

        // (Optional) hint about how many clauses we'll add
        solver.setExpectedNumberOfClauses(3);

        /*
         Example CNF:
           (x1 OR not x2)
           (not x1 OR x3)
           (x3)
         This should be satisfiable with x3 = true (and some consistent x1,x2).
        */

        try {
            // Clause: x1 OR not x2  -> represented as [ 1, -2 ]
            IVecInt c1 = new VecInt();
            c1.push(1).push(-2);
            solver.addClause(c1);

            // Clause: not x1 OR x3 -> [ -1, 3 ]
            solver.addClause(new VecInt(new int[] { -1, 3 }));

            // Clause: x3 -> [ 3 ]
            solver.addClause(new VecInt(new int[] { 3 }));

            // Set a timeout (seconds) to avoid long runs (optional)
            solver.setTimeout(5); // 5 seconds

            // Solve
            boolean isSat = solver.isSatisfiable();

            if (isSat) {
                System.out.println("SATISFIABLE");
                int[] model = solver.model(); // returns an array of signed ints (literals)
                // model contains literals such as 1 (x1=true), -2 (x2=false), ...
                // The array may list only the assigned literals; use absolute value to index variables.
                boolean[] value = new boolean[numVars + 1]; // 1-based
                for (int lit : model) {
                    int var = Math.abs(lit);
                    if (var <= numVars) value[var] = (lit > 0);
                }
                for (int i = 1; i <= numVars; i++) {
                    System.out.printf("x%d = %s%n", i, value[i]);
                }
            } else {
                System.out.println("UNSATISFIABLE");
            }

        } catch (ContradictionException ce) {
            // thrown if the formula becomes trivially unsatisfiable while adding clauses
            System.out.println("Formula is trivially unsatisfiable during construction: " + ce.getMessage());
        } catch (TimeoutException te) {
            System.out.println("Solving timed out: " + te.getMessage());
        }
    }
}
