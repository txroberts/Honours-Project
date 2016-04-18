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
    
    void shutDownDB(){
        graphDb.shutdown();
    }
    
    private void addBranchLengths(){
        // Sets an attribute in left branches linked to nodes that also right branched
        // How many assignments were made that were eventually backtracked
        String query = "MATCH (:Assignment)<-[left_branch:EQUALS]-(a:Assignment)-[:NOT_EQUALS]->(right:Assignment) ";
        query = query + "SET left_branch.length = abs(toInt(right.node_num) - toInt(a.node_num)) - 1";
        
        Transaction tx = graphDb.beginTx();
        
        try {
            graphDb.execute(query);
            
            tx.success();
        } finally {
            tx.close();
        }
    }
    
    long getBtLeftBranches(){
        // The number of assignments that have left branches that were eventually right branched to a valid assignment
        String query = "MATCH (:Assignment)<-[left_branch:EQUALS]-(a:Assignment)-[:NOT_EQUALS]->(right:Assignment) ";
        query = query + "RETURN count(a) AS bt_left_branches";
        
        Transaction tx = graphDb.beginTx();
        
        try {
            Result result = graphDb.execute(query);
            tx.success();
            
            return (long) result.next().get("bt_left_branches");
        } finally {
            tx.close();
        }
    }
    
    double getAvgLength(){
        // The average length of left branches where the assignment also eventually right branched
        String query = "MATCH (:Assignment)<-[left_branch:EQUALS]-(a:Assignment)-[:NOT_EQUALS]->(right:Assignment) ";
        query = query + "WITH left_branch.length AS left_branch_assignments ";
        query = query + "RETURN avg(left_branch_assignments)";
        
        Transaction tx = graphDb.beginTx();
        
        try {
            Result result = graphDb.execute(query);
            
            tx.success();            
            return (double) result.next().get("avg(left_branch_assignments)");
        } finally {
            tx.close();
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
        
        Transaction tx = graphDb.beginTx();
        
        try {
            Result result = graphDb.execute(query);
            
            tx.success();            
            return (long) result.next().get("count(left_branch_assignments)");
        } finally {
            tx.close();
        }
    }
    
    double getPropScore(){
        // The ratio of backtracked assignments to the total number of assignments made
        String query = "MATCH (:Assignment)<-[left_branch:EQUALS]-(a:Assignment)-[:NOT_EQUALS]->(:Assignment) ";
        query = query + "WITH count(a) AS bt_assignments_count ";
        query = query + "MATCH (all_assignments:Assignment) ";
        query = query + "RETURN toFloat(bt_assignments_count) / toFloat(count(all_assignments)) AS score";
        
        Transaction tx = graphDb.beginTx();
        
        try {
            Result result = graphDb.execute(query);
            
            tx.success();            
            return (double) result.next().get("score");
        } finally {
            tx.close();
        }
    }
    
    double getDEARatio(){
        // The ratio of dead ends to the total number of assignments made
        String query = "MATCH (de:DeadEnd) ";
        query = query + "WITH toFloat(count(de)) AS count_dead_ends ";
        query = query + "MATCH (a:Assignment) ";
        query = query + "RETURN count_dead_ends / toFloat(count(a)) AS ratio";
        
        Transaction tx = graphDb.beginTx();
        
        try {
            Result result = graphDb.execute(query);
            
            tx.success();            
            return (double) result.next().get("ratio");
        } finally {
            tx.close();
        }
    }
    
    double getLongestBranch(){
        // Get the longest left branch where the assignment also eventually right branched
        String query = "MATCH (:Assignment)<-[left_branch:EQUALS]-(a:Assignment)-[:NOT_EQUALS]->(right:Assignment) ";
        query = query + "RETURN left_branch.length AS left_branch_assignments ";
        query = query + "ORDER BY left_branch_assignments DESC ";
        query = query + "LIMIT 1";
        
        Transaction tx = graphDb.beginTx();
        
        try {
            Result result = graphDb.execute(query);
            
            tx.success();            
            return (double) result.next().get("left_branch_assignments");
        } finally {
            tx.close();
        }
    }
    
    int getLongestEqualsChain(){
        // Get the longest chain of successful assignments
        String query = "MATCH (start:Assignment)-[rout]->() ";
        query = query + "OPTIONAL MATCH ()-[rin]->(start) ";
        query = query + "WITH start, collect(DISTINCT type(rin)) AS incoming, collect(DISTINCT type(rout)) AS outgoing ";
        query = query + "WHERE none(rel in incoming WHERE rel = 'EQUALS') ";
        query = query + "AND all(rel in outgoing WHERE rel = 'EQUALS') ";
        query = query + "WITH start ";

        query = query + "MATCH ()-[rin]->(end:Assignment)-[rout]->() ";
        query = query + "WITH start, end, collect(DISTINCT type(rin)) AS incoming, collect(DISTINCT type(rout)) AS outgoing ";
        query = query + "WHERE all(rel in incoming WHERE rel = 'EQUALS') ";
        query = query + "AND all(rel in outgoing WHERE rel = 'EQUALS') ";
        query = query + "WITH start, end ";

        query = query + "MATCH path = (start)-[:EQUALS*]->(end) ";
        query = query + "WITH DISTINCT start, max(end.node_num) AS end_node ";
        query = query + "MATCH path = (start)-[:EQUALS*]->(end {node_num: end_node}) ";
        query = query + "RETURN length(path) AS path_length ";
        query = query + "ORDER BY path_length DESC ";
        query = query + "LIMIT 1";
        
        Transaction tx = graphDb.beginTx();
        
        try {
            Result result = graphDb.execute(query);
            
            if (result.hasNext()){
                tx.success();
                return (int) result.next().get("path_length");
            }
        } finally {
            tx.close();
        }
        
        return 0; // if the db doesn't return a path
    }
}