import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

/**
 *
 * @author Tom Roberts
 */
public class SearchTree {
    GraphDatabaseService graphDb;
    
    SearchTree(String dbPath){
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbPath);
        registerShutdownHook(graphDb);
        
        addBranchLengths();
    }
    
    private static void registerShutdownHook(final GraphDatabaseService graphDb) {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook( new Thread() {
            @Override
            public void run() {
                graphDb.shutdown();
            }
        } );
    }
    
    private void addBranchLengths(){
        // Sets an attribute in left branches linked to nodes that also right branched
        // How many assignments were made that were eventually backtracked
        String query = "MATCH (:Assignment)<-[left_branch:EQUALS]-(a:Assignment)-[:NOT_EQUALS]->(right:Assignment) ";
        query = query + "SET left_branch.length = abs(toInt(right.node_num) - toInt(a.node_num)) - 1";
        
        try (Transaction tx = graphDb.beginTx()){
            graphDb.execute(query);
            
            tx.success();
        }
    }
    
    long getBtLeftBranches(){
        // The number of assignments that have left branches that were eventually right branched
        String query = "MATCH (:Assignment)<-[left_branch:EQUALS]-(a:Assignment)-[:NOT_EQUALS]->(right:Assignment) ";
        query = query + "RETURN count(a) AS bt_left_branches";
        
        try (Transaction tx = graphDb.beginTx()){
            Result result = graphDb.execute(query);
            tx.success();
            
            return (long) result.next().get("bt_left_branches");
        }
    }
    
    double getAvgLength(){
        // The average length of left branches where the assignment also eventually right branched
        String query = "MATCH (:Assignment)<-[left_branch:EQUALS]-(a:Assignment)-[:NOT_EQUALS]->(right:Assignment) ";
        query = query + "WITH left_branch.length AS left_branch_assignments ";
        query = query + "RETURN avg(left_branch_assignments)";
        
        try (Transaction tx = graphDb.beginTx()){
            Result result = graphDb.execute(query);
            
            tx.success();            
            return (double) result.next().get("avg(left_branch_assignments)");
        }
    }
    
    long getNumOutlierBranches(){
        // The number of left branches (where the assignment also eventually right branched) whose length was outside 1 std dev of the average
        String query = "MATCH (:Assignment)<-[left_branch:EQUALS]-(a:Assignment)-[:NOT_EQUALS]->(right:Assignment) ";
        query = query + "WITH left_branch.length AS left_branch_assignments ";
        query = query + "WITH avg(left_branch_assignments) AS average_left_branch_assignments, stdevp(left_branch_assignments) AS stdev ";
        query = query + "WITH average_left_branch_assignments, average_left_branch_assignments - stdev AS lower_bound, average_left_branch_assignments + stdev AS upper_bound ";
        query = query + "MATCH (:Assignment)<-[left_branch:EQUALS]-(a:Assignment)-[:NOT_EQUALS]->(right:Assignment) ";
        query = query + "WITH left_branch.length AS left_branch_assignments, lower_bound, upper_bound ";
        query = query + "WHERE left_branch_assignments < lower_bound or left_branch_assignments > upper_bound ";
        query = query + "RETURN count(left_branch_assignments)";
        
        try (Transaction tx = graphDb.beginTx()){
            Result result = graphDb.execute(query);
            
            tx.success();            
            return (long) result.next().get("count(left_branch_assignments)");
        }
    }
    
    double getPropScore(){
        // The ratio of backtracked assignments to the total number of assignments made
        String query = "MATCH (:Assignment)<-[left_branch:EQUALS]-(a:Assignment)-[:NOT_EQUALS]->(:Assignment) ";
        query = query + "WITH count(a) AS bt_assignments_count ";
        query = query + "MATCH (all_assignments:Assignment) ";
        query = query + "RETURN toFloat(bt_assignments_count) / toFloat(count(all_assignments)) AS score";
        
        try (Transaction tx = graphDb.beginTx()){
            Result result = graphDb.execute(query);
            
            tx.success();            
            return (double) result.next().get("score");
        }
    }
    
    double getDEARatio(){
        // The ratio of dead ends to the total number of assignments made
        String query = "MATCH (de:DeadEnd) ";
        query = query + "WITH toFloat(count(de)) AS count_dead_ends ";
        query = query + "MATCH (a:Assignment) ";
        query = query + "RETURN count_dead_ends / toFloat(count(a)) AS ratio";
        
        try (Transaction tx = graphDb.beginTx()){
            Result result = graphDb.execute(query);
            
            tx.success();            
            return (double) result.next().get("ratio");
        }
    }
    
    double getLongestBranch(){
        // Get the longest left branch where the assignment also eventually right branched
        String query = "MATCH (:Assignment)<-[left_branch:EQUALS]-(a:Assignment)-[:NOT_EQUALS]->(right:Assignment) ";
        query = query + "RETURN left_branch.length AS left_branch_assignments ";
        query = query + "ORDER BY left_branch_assignments DESC ";
        query = query + "LIMIT 1";
        
        try (Transaction tx = graphDb.beginTx()){
            Result result = graphDb.execute(query);
            
            tx.success();            
            return (double) result.next().get("left_branch_assignments");
        }
    }
}