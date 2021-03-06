import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tom Roberts
 */
public class MeasureNeo4j {

    /**
     * @param args the command line arguments
     * 
     * Invoke with:
     * MeasureNeo4j {directory_of_neo4j_databases} {data_output_directory)
     */
    public static void main(String[] args) {
        if (args.length == 2) {
            String db_directory_path = args[0];
            String output_data_csv = args[1] + "\\auto_data.csv";
            
            // Create the output file and add column headers (if it doesn't exist)
            if (!new File(output_data_csv).exists()) {
                try {
                    FileWriter fileWriter = new FileWriter(output_data_csv, true);

                    PrintWriter printWriter = new PrintWriter(fileWriter);
                    printWriter.println("Problem,Variable Ordering,Pre-processing,Measure,Data");

                    fileWriter.close();
                    printWriter.close();
                    System.out.println("Auto data CSV file created...");
                } catch (IOException ex) {
                    System.out.println("Error creating data CSV file");
                    Logger.getLogger(MeasureNeo4j.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            File[] files = new File(db_directory_path).listFiles();
            
            for (int i = 0; i < files.length; i++) {
                System.out.println((i+1) + "/" + files.length);
                
                if (files[i].isDirectory()) {
                    String db_path = files[i].getAbsolutePath();

                    String[] db_path_parts = db_path.split("\\\\");
                    String db_name = db_path_parts[db_path_parts.length - 1];
                    String[] db_name_parts = db_name.split("_");

                    String problem = db_name_parts[0].split("\\.")[0];
                    String varOrdering = db_name_parts[1];
                    String preProcessing = db_name_parts[2];

                    SearchTree tree = new SearchTree(db_path);

                    long num_assignments = tree.getNumOfAssignments();
                    long num_solutions = tree.getNumOfSolutions();
                    long num_dead_ends = tree.getNumOfDeadEnds();
                    long num_backtracks = tree.getNumOfBacktracks();
                    
                    long bt_left_branches = tree.getBtLeftBranches();
                    double avg_length = tree.getAvgLength();
                    double longest_branch = tree.getLongestBranch();
                    int longest_good_chain = tree.getLongestEqualsChain();
                    
                    long bt_first_assignments = tree.getBTFirstAssignments();
                    
                    tree.shutDownDB();
                    
                    String problemDetails = problem + "," + varOrdering + "," + preProcessing;

                    // Append the data to the file
                    try {
                        FileWriter fileWriter = new FileWriter(output_data_csv, true);
                        
                        PrintWriter printWriter = new PrintWriter(fileWriter);
                        printWriter.printf("%s,%s,%d%n", problemDetails, "Assignments", num_assignments);
                        printWriter.printf("%s,%s,%d%n", problemDetails, "Solutions", num_solutions);
                        printWriter.printf("%s,%s,%d%n", problemDetails, "Dead Ends", num_dead_ends);
                        printWriter.printf("%s,%s,%d%n", problemDetails, "Backtracks", num_backtracks);
                        
                        printWriter.printf("%s,%s,%d%n", problemDetails, "Backtracked left branches", bt_left_branches);
                        printWriter.printf("%s,%s,%f%n", problemDetails, "Average left branch length", avg_length);
                        printWriter.printf("%s,%s,%f%n", problemDetails, "Longest backtracked left branch", longest_branch);
                        printWriter.printf("%s,%s,%d%n", problemDetails, "Longest chain of successful assignments", longest_good_chain);
                        
                        printWriter.printf("%s,%s,%d%n", problemDetails, "First ten assignments backtracked", bt_first_assignments);

                        printWriter.close();
                        fileWriter.close();
                    } catch (IOException ex) {
                        Logger.getLogger(MeasureNeo4j.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } else {
            System.out.println("Error - Expects:");
            System.out.println("(1) Directory of Neo4j databases");
            System.out.println("(2) Directory to output data CSV file to");
        }
    }
}