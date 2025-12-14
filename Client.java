import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Client {
    public static void main(String[] args){
        Sudoku sudo = buildSudoku("4x4.txt");
        System.out.println("given puzzle:");
        System.out.println(sudo);
        Sudoku solution = SudokuSolver.solveSudoku(sudo);
        if(solution != null){
            System.out.println(solution.isSolved());
            System.out.println("solved puzzle:");
            System.out.println(solution);
        } else{
            System.out.println("Puzzle not solved!");   
        }
    }

    public static Sudoku buildSudoku(String filename){
        File input = new File(filename);
         try(Scanner inputReader = new Scanner(input)){
            int size = Integer.parseInt(inputReader.nextLine());
            int[][] board = new int[size][size];
            int rowCount = 0;
            while(inputReader.hasNext()){
                String line = inputReader.nextLine();
                String[] splitLine = line.split(" ");
                for(int i = 0; i < size; i++){
                    board[rowCount][i] = Integer.parseInt(splitLine[i]);
                }
                rowCount++;
            }
            return new Sudoku(board);
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
            return null;
        }
    }
}
