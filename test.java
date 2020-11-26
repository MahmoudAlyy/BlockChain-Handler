import java.util.ArrayList; // import the ArrayList class
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class test {



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
        Collections.reverse(order);
        System.out.println(order);
    }

    private static void blackMagic(Graph g, int v, Map<Integer, Boolean> visited, List<Integer> order) {
        // Mark the current node as visited
        visited.replace(v, true);
        Integer i;

        // We reuse the algorithm on all adjacent nodes to the current node
        for (Integer neighborId : g.getNode(v).getNeighbors()) {
            if (!visited.get(neighborId))
                blackMagic(g, neighborId, visited, order);
        }

        // Put the current node in the array
        order.add(v);
    }

    public void callme(){
        Graph g = new Graph();
        Node node1 = new Node(1);
        Node node2 = new Node(2);
        Node node3 = new Node(3);
        Node node4 = new Node(4);
        node1.addNeighbor(2);
        node2.addNeighbor(3);
        node4.addNeighbor(3);
        g.addNode(node1);
        g.addNode(node2);
        g.addNode(node3);
        g.addNode(node4);
        System.out.println(g);
        topoSort(g);



    }
}
