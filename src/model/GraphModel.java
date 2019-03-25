package model;

import java.util.*;

public class GraphModel {
    public int nodeNum;
    public int linkNum;
    public String graphName;
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
    public LinkedList<Integer>[] getAdjacency(){

        return adjacency;
    }

    public long[][] makeGraphMatrix() {
        long graph[][] = new long[nodeNum][nodeNum];
        for (int xIndex = 0; xIndex < nodeNum; xIndex++) {
            for (int yIndex = 0; yIndex < nodeNum; yIndex++) {
                for (int edge = 0; edge < edgeModelList.size(); edge++) {
                    if (xIndex == (int) edgeModelList.get(edge).source && yIndex == (int) edgeModelList.get(edge).target ||
                            xIndex == (int) edgeModelList.get(edge).target && yIndex == (int) edgeModelList.get(edge).source) {
                        graph[xIndex][yIndex] = edgeModelList.get(edge).distance;
                        break;
                    } else {
                        graph[xIndex][yIndex] = 0;
                    }
                }

            }
        }
        return graph;
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


    // Function that implements Dijkstra's
    // single source shortest path
    // algorithm for a graph represented
    // using adjacency matrix
    // representation
    public ShortestPath dijkstra(long[][] adjacencyMatrix,
                                 int startVertex) {
        int nVertices = adjacencyMatrix[0].length;

        // shortestDistances[i] will hold the
        // shortest distance from src to i
        long[] shortestDistances = new long[nVertices];

        // added[i] will true if vertex i is
        // included / in shortest path tree
        // or shortest distance from src to
        // i is finalized
        boolean[] added = new boolean[nVertices];

        // Initialize all distances as
        // INFINITE and added[] as false
        for (int vertexIndex = 0; vertexIndex < nVertices;
             vertexIndex++) {
            shortestDistances[vertexIndex] = Integer.MAX_VALUE;
            added[vertexIndex] = false;
        }

        // Distance of source vertex from
        // itself is always 0
        shortestDistances[startVertex] = 0;

        // Parent array to store shortest
        // path tree
        long[] parents = new long[nVertices];

        // The starting vertex does not
        // have a parent
        parents[startVertex] = -1;

        // Find shortest path for all
        // vertices
        for (int i = 1; i < nVertices; i++) {

            // Pick the minimum distance vertex
            // from the set of vertices not yet
            // processed. nearestVertex is
            // always equal to startNode in
            // first iteration.
            int nearestVertex = -1;
            long shortestDistance = Integer.MAX_VALUE;
            for (int vertexIndex = 0;
                 vertexIndex < nVertices;
                 vertexIndex++) {
                if (!added[vertexIndex] &&
                        shortestDistances[vertexIndex] <
                                shortestDistance) {
                    nearestVertex = vertexIndex;
                    shortestDistance = shortestDistances[vertexIndex];
                }
            }

            // Mark the picked vertex as
            // processed
            added[nearestVertex] = true;

            // Update dist value of the
            // adjacent vertices of the
            // picked vertex.
            for (int vertexIndex = 0;
                 vertexIndex < nVertices;
                 vertexIndex++) {
                long edgeDistance = adjacencyMatrix[nearestVertex][vertexIndex];

                if (edgeDistance > 0
                        && ((shortestDistance + edgeDistance) <
                        shortestDistances[vertexIndex])) {
                    parents[vertexIndex] = nearestVertex;
                    shortestDistances[vertexIndex] = shortestDistance +
                            edgeDistance;
                }
            }
        }

        ShortestPath shortestPath = new ShortestPath();
        shortestPath.shorestDist = shortestDistances;
        shortestPath.parent = parents;
        return shortestPath;
        // printSolution(startVertex, shortestDistances, parents);
    }

    // A utility function to print
    // the constructed distances
    // array and shortest paths
    private void printSolution(int startVertex,
                               long[] distances,
                               long[] parents) {
        int nVertices = distances.length;
        System.out.print("Vertex\t Distance\tPath");

        for (int vertexIndex = 0;
             vertexIndex < nVertices;
             vertexIndex++) {
            if (vertexIndex != startVertex) {
                System.out.print("\n" + startVertex + " -> ");
                System.out.print(vertexIndex + " \t\t ");
                System.out.print(distances[vertexIndex] + "\t\t");
                printPath(vertexIndex, parents);
            }
        }
    }

    // Function to print shortest path
    // from source to currentVertex
    // using parents array
    private void printPath(long currentVertex,
                           long[] parents) {

        // Base case : Source node has
        // been processed
        if (currentVertex == -1) {
            return;
        }
        printPath(parents[(int) currentVertex], parents);
        System.out.print(currentVertex + " ");
    }
}
