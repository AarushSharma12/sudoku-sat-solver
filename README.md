# 🧩 Sudoku-SAT-Solver

A high-performance Sudoku solver that reduces the classic puzzle to a **Boolean Satisfiability (SAT)** problem. By encoding Sudoku rules as logical constraints in Conjunctive Normal Form (CNF), this solver leverages the power of modern SAT solvers to crack puzzles from $4 \times 4$ grids all the way up to $16 \times 16$ Hexadoku.

## ✨ Features

- **Variable Grid Sizes** — Supports any $N \times N$ board where $N$ is a perfect square ($4 \times 4$, $9 \times 9$, $16 \times 16$, etc.)
- **Efficient Encoding** — Uses $O(N^3)$ Boolean variables and polynomial clauses
- **Guaranteed Correctness** — Solutions are validated against all Sudoku constraints
- **Fast Solving** — Delegates to [Sat4j](http://www.sat4j.org/), a battle-tested Java SAT solver
- **Simple Input Format** — Plain text files with space-separated values

## 🔬 How It Works

The solver transforms a Sudoku puzzle into a SAT instance using the following **5 constraint types**:

| Constraint | Description                                                          | Encoding Strategy                        |
| ---------- | -------------------------------------------------------------------- | ---------------------------------------- |
| **Cell**   | Each cell contains exactly one value                                 | At-least-one clause + pairwise negations |
| **Row**    | Each value appears exactly once per row                              | At-least-one clause + pairwise negations |
| **Column** | Each value appears exactly once per column                           | At-least-one clause + pairwise negations |
| **Block**  | Each value appears exactly once per $\sqrt{N} \times \sqrt{N}$ block | At-least-one clause + pairwise negations |
| **Clues**  | Pre-filled cells are fixed                                           | Unit clauses for given values            |

### Variable Mapping

Each possible assignment is encoded as a Boolean variable $x_{r,c,v}$ representing "cell $(r, c)$ contains value $v$". The mapping function:

$$\text{var}(r, c, v) = r \cdot N^2 + c \cdot N + v + 1$$

converts 3D coordinates into a unique 1-indexed variable ID for Sat4j.

### "At Most One" Encoding

To enforce that each cell/row/column/block contains a value **at most once**, the solver adds pairwise negative clauses:

$$(\neg x_{r,c,v_1} \lor \neg x_{r,c,v_2}) \quad \text{for all } v_1 < v_2$$

## 📦 Installation & Usage

### Prerequisites

- Java JDK 21+ (LTS recommended)
- [Sat4j](http://www.sat4j.org/) library (`sat4j.jar`)

### Compile

**Linux/macOS:**

```bash
javac -cp .:sat4j.jar *.java
```

**Windows:**

```cmd
javac -cp .;sat4j.jar *.java
```

### Run

**Linux/macOS:**

```bash
java -cp .:sat4j.jar SudokuApp [puzzle-file]
```

**Windows:**

```cmd
java -cp .;sat4j.jar SudokuApp [puzzle-file]
```

**Examples:**

```bash
java -cp .;sat4j.jar SudokuApp easy.txt
java -cp .;sat4j.jar SudokuApp giant.txt
```

### Input File Format

```
N
v₁₁ v₁₂ ... v₁ₙ
v₂₁ v₂₂ ... v₂ₙ
...
vₙ₁ vₙ₂ ... vₙₙ
```

- First line: board size $N$
- Subsequent lines: space-separated values (use `0` for empty cells)

**Example (`4x4.txt`):**

```
4
0 0 0 4
0 0 0 0
2 0 0 3
4 0 1 2
```

## 📁 Project Structure

```
Sudoku-SAT-Solver/
├── SudokuApp.java       # CLI entry point for solving puzzles
├── SudokuSolver.java    # Core SAT encoding logic and Sat4j interface
├── Sudoku.java          # Board representation and validation
├── 4x4.txt              # Sample 4×4 puzzle
├── easy.txt             # Sample 9×9 puzzle (easy)
├── hard.txt             # Sample 9×9 puzzle (hard)
├── evil.txt             # Sample 9×9 puzzle (evil)
├── hardest.txt          # Sample 9×9 puzzle (hardest)
└── giant.txt            # Sample 16×16 Hexadoku puzzle
```

## 📄 License

MIT License - Feel free to use, modify, and distribute.
