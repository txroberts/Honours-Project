
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
     */
    public static void main(String[] args) {
        if (args.length == 2) {
            String db_directory_path = args[0];
            String output_data_csv = args[1];

            File[] files = new File(db_directory_path).listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    String db_path = file.getAbsolutePath();

                    String[] db_path_parts = db_path.split("\\\\");
                    String db_name = db_path_parts[db_path_parts.length - 1];
                    String[] db_name_parts = db_name.split("_");

                    String problem = db_name_parts[0].split("\\.")[0];
                    String varOrdering = db_name_parts[1];
                    String preProcessing = db_name_parts[2];

                    System.out.println("Accessing " + db_name);
                    SearchTree tree = new SearchTree(db_path);

                    System.out.println("Measuring " + db_name);
                    long bt_left_branches = tree.getBtLeftBranches();
                    double avg_length = tree.getAvgLength();
                    long num_outlier_branches = tree.getNumOutlierBranches();
                    double prop_score = tree.getPropScore();
                    double de_ratio = tree.getDEARatio();
                    double longest_branch = tree.getLongestBranch();

                    // Create the output file and add column headers (if it doesn't exist)
                    if (!new File(output_data_csv).exists()) {
                        System.out.println("Creating data file");

                        FileWriter fileWriter;
                        try {
                            fileWriter = new FileWriter(output_data_csv, true);

                            PrintWriter printWriter = new PrintWriter(fileWriter);
                            printWriter.println("Problem,Variable Ordering,Pre-processing,Measure,Data");

                            fileWriter.close();
                        } catch (IOException ex) {
                            Logger.getLogger(MeasureNeo4j.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                    // Append the data to the file
                    System.out.println("Appending data");
                    try (FileWriter fileWriter = new FileWriter(output_data_csv, true)) {
                        try (PrintWriter printWriter = new PrintWriter(fileWriter)) {
                            printWriter.printf("%s,%s,%s,%s,%d%n", problem, varOrdering, preProcessing, "Number of Backtracked Left Branches", bt_left_branches);
                            printWriter.printf("%s,%s,%s,%s,%f%n", problem, varOrdering, preProcessing, "Average Left Branch Length", avg_length);
                            printWriter.printf("%s,%s,%s,%s,%d%n", problem, varOrdering, preProcessing, "Number of Outlier Left Branches", num_outlier_branches);
                            printWriter.printf("%s,%s,%s,%s,%f%n", problem, varOrdering, preProcessing, "Normalised Constraint Propagation Score", prop_score);
                            printWriter.printf("%s,%s,%s,%s,%f%n", problem, varOrdering, preProcessing, "Dead Ends/Assignments Ratio", de_ratio);
                            printWriter.printf("%s,%s,%s,%s,%f%n", problem, varOrdering, preProcessing, "Longest backtracked left branch", longest_branch);

                            System.out.println(db_name + " finished\n");
                        }
                    } catch (IOException e) {
                        e.printStackTrace(System.out);
                    }
                }
            }
        } else {
            System.out.println("Error - Expects:");
            System.out.println("(1) Directory of Neo4j databases");
            System.out.println("(2) Output CSV file");
        }
    }
}
