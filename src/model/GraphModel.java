package model;

import java.util.*;

public class GraphModel {
    public int nodeNum;
    public int linkNum;
    public List<EdgeModel> edgeModelList = new ArrayList<>();
    public List<NodeModel> nodeModelList = new ArrayList<>();
    public int maxLevel;

    private LinkedList<Integer> adjacency[]; //Adjacency Lists

    // Function to add an edge into the graph
    public void prepareAdjacencyList() {
        adjacency = new LinkedList[nodeNum];
        for (int i = 0; i < nodeNum; ++i) {
            adjacency[i] = new LinkedList();
        }
    }

    public void addAdjacencyEdge(int source, int target) {
        adjacency[source].add(target);
        adjacency[target].add(source);
    }

    // prints getNhops traversal from a given source s
    public HashMap<Integer, String> getNhops(int s) {
        // Mark all the vertices as not visited(By default
        // set as false)
        boolean visited[] = new boolean[nodeNum];

        // Create a queue for getNhops
        LinkedList<Integer> queue = new LinkedList<Integer>();

        HashMap<Integer, String> levels = new HashMap<>();
        for (int level = 0; level < nodeNum; level++) {
            levels.put(level, "");
        }

        // Mark the current node as visited and enqueue it
        visited[s] = true;
        queue.add(s);
        int level = 0;
        int maxLevel = 1;
        ArrayList<Integer> childList = new ArrayList<Integer>();
        while (queue.size() != 0) {
            // Dequeue a vertex from queue and print it
            s = queue.poll();
            if (!childList.contains(level)) level++;
            if (!childList.isEmpty()) childList.remove(0);

            // Get all adjacent vertices of the dequeued vertex s
            // If a adjacent has not been visited, then mark it
            // visited and enqueue it
            Iterator<Integer> i = adjacency[s].listIterator();
            while (i.hasNext()) {
                int n = i.next();
                if (!visited[n]) {
                    visited[n] = true;
                    queue.add(n);
                    childList.add(level + 1);
                    String hop_neighbours = levels.get(level);
                    if (!hop_neighbours.equals(""))
                    levels.put(level, hop_neighbours + "," + n);
                    else levels.put(level, String.valueOf(n));
                    maxLevel = level;

                }
            }
        }
        this.maxLevel = maxLevel;
        return levels;
    }
}
