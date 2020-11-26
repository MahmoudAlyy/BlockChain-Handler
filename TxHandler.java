import java.util.HashMap; // import the HashMap class
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;

public class TxHandler {

    private UTXOPool utxoPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. 
     */
    public TxHandler(UTXOPool utxoPool_NEW) {
        utxoPool = new UTXOPool(utxoPool_NEW);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {

        //1
        for (int i = 0; i < tx.getInputs().size(); i++) {
            if (tx.getInput(i).prevTxHash == null)
                return false;
            UTXO utxo_new = new UTXO(tx.getInput(i).prevTxHash,tx.getInput(i).outputIndex);
            if (utxoPool.contains(utxo_new) == false) 
                return false;               
        }

        //2
        for(int i = 0;i<tx.getInputs().size();i++){

            UTXO utxo_new = new UTXO(tx.getInput(i).prevTxHash,tx.getInput(i).outputIndex);
            boolean check_crypto = Crypto.verifySignature(utxoPool.getTxOutput(utxo_new).address, tx.getRawDataToSign(i), tx.getInput(i).signature);          
        
            if (check_crypto == false)
                return false;
            
        }

        //3
        HashMap<Long, Integer> keyMap = new HashMap<Long, Integer>();
        Long key;

        for(int i = 0;i<tx.getInputs().size();i++){

            key = concat(UniqueID(tx.getInput(i).prevTxHash), tx.getInput(i).outputIndex);
            if (keyMap.containsKey(key)) 
                return false;
            else
                keyMap.put(key, 1);
            
        }

        //5 & 4
        Double inputSum = 0.0;
        Double outputSum = 0.0;

        for(int i = 0;i<tx.getInputs().size();i++){
            UTXO utxo_new = new UTXO(tx.getInput(i).prevTxHash,tx.getInput(i).outputIndex);
            inputSum = inputSum + utxoPool.getTxOutput(utxo_new).value;
        }

        for(int i = 0;i<tx.getOutputs().size();i++){

            //4
            if (tx.getOutput(i).value < 0.0)
                return false;
            
            outputSum = outputSum + tx.getOutput(i).value;
        }

        if (inputSum < outputSum)
            return false;

        // finally       
        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {

        // hash map to return transcation when given trasnaction hash
        HashMap<Integer, Transaction> hashToTxMap = new HashMap<Integer, Transaction>();

        // output
        List<Transaction> validTxs = new ArrayList<Transaction>();

        /// for toplogical sort 
        Graph g = new Graph();
        
        for(int i = 0;i<possibleTxs.length;i++){

            /// put transaction in hashmap for quick retrieval 
            hashToTxMap.put(UniqueID(possibleTxs[i].getHash()), possibleTxs[i]);

            /// if its a coinbase transcation its valid with no checks
            if (possibleTxs[i].isCoinbase() == true ){
                validTxs.add(possibleTxs[i]);
                UTXO utxo_new = new UTXO(possibleTxs[i].getHash(),0);
                utxoPool.addUTXO(utxo_new, possibleTxs[i].getOutput(0));
                continue;
            }
           
            Node n1 = new Node(UniqueID(possibleTxs[i].getHash()));
            
            for (int j = 0; j < possibleTxs[i].getInputs().size(); j++) {
                if (possibleTxs[i].getInput(j).prevTxHash != null) {
                    n1.addNeighbor(UniqueID(possibleTxs[i].getInput(j).prevTxHash));
                }
            }
            g.addNode(n1);
                       
        }

        List<Integer> transactionsOrder = topoSort(g);
        
        for(int i = 0;i<transactionsOrder.size();i++){

            Transaction tx = hashToTxMap.get(transactionsOrder.get(i));

            if (isValidTx(tx) == true){

                // remove outputs that pointed to by the input from utxoPool
                for (int j = 0; j < tx.getInputs().size(); j++) {
                    UTXO utxo_new = new UTXO(tx.getInput(j).prevTxHash, tx.getInput(j).outputIndex);
                    utxoPool.removeUTXO(utxo_new);
                }
                // add all tx outputs to utxopool
                for(int j = 0;j<tx.getOutputs().size();j++){
                    UTXO utxo_new = new UTXO(tx.getHash(), j);
                    utxoPool.addUTXO(utxo_new, tx.getOutput(j));
                }
                
                validTxs.add(tx);

            }
        }

        // from arraylist to array
        Transaction[] out = new Transaction[validTxs.size()];
        out = validTxs.toArray(out);

     
        return out;
    }

    /** returns a unique integer ID for the givenhash (so that debugging is easier) */
    public Integer UniqueID(byte[] hash) {
        return Arrays.toString(hash).hashCode();
    }


    public long concat(int a, int b) {

        // Convert both the integers to string
        String s1 = Integer.toString(a);
        String s2 = Integer.toString(b);

        // Concatenate both strings
        String s = s1 + s2;

        // Convert the concatenated string
        // to integer
        // int c = Integer.valueOf(s);
        long c = Long.parseLong(s);

        // return the formed integer
        return c;
    }


    //////////////////////////////////////
    // below is the implementaion of graph, node & toplogical sort used in sorting the transcations (non blockchain related stuff)
    public class Graph {
        private List<Node> nodes;

        public Graph() {
            this.nodes = new ArrayList<>();
        }

        public Graph(List<Node> nodes) {
            this.nodes = nodes;
        }

        public void addNode(Node e) {
            this.nodes.add(e);
        }

        public List<Node> getNodes() {
            return nodes;
        }

        public Node getNode(int searchId) {
            for (Node node : this.getNodes()) {
                if (node.getId() == searchId) {
                    return node;
                }
            }
            return null;
        }

        public int getSize() {
            return this.nodes.size();
        }

        @Override
        public String toString() {
            return "Graph{" + "nodes=" + nodes + "}";
        }
    }

    public class Node {
        private int id;
        private List<Integer> neighbors;

        public Node(int id) {
            this.id = id;
            this.neighbors = new ArrayList<>();
        }

        public void addNeighbor(int e) {
            this.neighbors.add(e);
        }

        public int getId() {
            return id;
        }

        public List<Integer> getNeighbors() {
            return neighbors;
        }

        @Override
        public String toString() {
            return "Node{" + "id=" + id + ", neighbors=" + neighbors + "}" + "\n";
        }
    }

    private static List<Integer> topoSort(Graph g) {

        // Fetching the number of nodes in the graph

        // List where we'll be storing the topological order
        List<Integer> order = new ArrayList<>();

        // Map which indicates if a node is visited (has been processed by the
        // algorithm)
        Map<Integer, Boolean> visited = new HashMap<>();
        for (Node tmp : g.getNodes())
            visited.put(tmp.getId(), false);

        // We go through the nodes using black magic
        for (Node tmp : g.getNodes()) {
            if (!visited.get(tmp.getId()))
                blackMagic(g, tmp.getId(), visited, order);
        }

        return order;
    }

    private static void blackMagic(Graph g, int v, Map<Integer, Boolean> visited, List<Integer> order) {
        // Mark the current node as visited
        visited.replace(v, true);

        // We reuse the algorithm on all adjacent nodes to the current node
        for (Integer neighborId : g.getNode(v).getNeighbors()) {
            if (visited.get(neighborId) != null) {
                if (!visited.get(neighborId))
                    blackMagic(g, neighborId, visited, order);
            }
        }

        // Put the current node in the array
        order.add(v);
    }

  

}
