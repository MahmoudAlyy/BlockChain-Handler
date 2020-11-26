import java.util.ArrayList; // import the ArrayList class
import java.util.Arrays;
import java.util.List;
import java.util.HashMap; 
import java.util.Map;

public class test2 {

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

    private static void topoSort(Graph g) {

        // Fetching the number of nodes in the graph
        int V = g.getSize();

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

        // We reverse the order we constructed to get the
        // proper toposorting
        //Collections.reverse(order);
        System.out.println(order);
    }

    private static void blackMagic(Graph g, int v, Map<Integer, Boolean> visited, List<Integer> order) {
        // Mark the current node as visited
        visited.replace(v, true);

        // We reuse the algorithm on all adjacent nodes to the current node
        for (Integer neighborId : g.getNode(v).getNeighbors()) {
            if (visited.get(neighborId) != null ){
            if (!visited.get(neighborId))
                blackMagic(g, neighborId, visited, order);
        }
        }

        // Put the current node in the array
        order.add(v);
    }




    public Integer UniqueID(byte [] hash){
        return Arrays.toString(hash).hashCode();
    }

    public void callme(Transaction[] possibleTxs) {
        
        Graph g = new Graph();
        //Node n1 = new Node();

        for(int i = 0;i<possibleTxs.length;i++){
            Node n1 = new Node(UniqueID(possibleTxs[i].getHash()));

            for(int j = 0; j<possibleTxs[i].getInputs().size(); j++){
                if (possibleTxs[i].getInput(j).prevTxHash != null){
                n1.addNeighbor(UniqueID(possibleTxs[i].getInput(j).prevTxHash));
                }
            }

            g.addNode(n1);

        }
        
        
        System.out.println(g);
        topoSort(g);
    }

    
}


