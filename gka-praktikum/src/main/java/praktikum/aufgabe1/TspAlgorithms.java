package main.java.praktikum.aufgabe1;

import org.graphstream.algorithm.generator.GridGenerator;
import org.graphstream.algorithm.util.DisjointSets;
import org.graphstream.graph.BreadthFirstIterator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.Graphs;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.algorithm.generator.FullGenerator;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.graphstream.algorithm.Toolkit.randomNode;


public class TspAlgorithms {
    /**
     * MinumumSpanningTreeHeuristic algorithm used to solve TSP problems for complete Graphs with the solution
     * having not more than twice the length of the optimal solution
     * @param graph a given Graph to solve the TSP problem for
     * @return A Mapping between Integers and nodes symbolizing an Order in which nodes are to be traversed
     */
    public static Map<Integer,Node> minimumSpanningTreeHeuristic(Graph graph){
        Map<Integer,Node> resultmapping = new HashMap<>();
        Graph kruskalresult = kruskal(graph);
        //Double every edge inside the minimal spanning tree
        kruskalresult.edges().collect(Collectors.toList()).forEach(e -> kruskalresult.addEdge("reverse: " + e.getId(),e.getNode0(), e.getNode1()));

        Map<Integer,Node> eulertour = generateEulertourForTrees(kruskalresult);

        int eulertoursize = eulertour.size();
        int counter = 1;
        for (int i = 1;i < eulertoursize;i++){
            if (!resultmapping.containsValue(graph.getNode(eulertour.get(i).getId()))){
                resultmapping.put(counter, graph.getNode(eulertour.get(i).getId()));
                counter++;
            }
        }
        resultmapping.put(counter, graph.getNode(eulertour.get(1).getId()));
        //resultmapping.forEach((k,n) -> System.out.println("Key: "+k+ " Node: "+n));
        return resultmapping;
    }

    /**
     * Kruskal Algorithm to create and return the minimal Spanning tree for a given Graph
     * @param graph the griven Graph
     * @return The minimal spanning tree
     */
    public static Graph kruskal(Graph graph){
        Graph minimalSpanningTree = new MultiGraph("Minimal-Spanning-Tree Kruskal");
        //every edge contained in the given graph sorted by their weight
        List<Edge> sortededges = graph.edges().sorted(Comparator.comparingDouble(e -> (Double) e.getAttribute("weight"))).collect(Collectors.toList());
        //DisjointSet to keep track of unlinked components during the creation of the mst
        DisjointSets<Node> components = new DisjointSets<>();
        //Adding all the nodes into the components as disjoint sets since there are no edges in the beginning
        graph.nodes().forEach(components::add);
        //Adding all the nodes into the minimalSpanningTree and adding edges in the next step
        graph.nodes().forEach(n -> minimalSpanningTree.addNode(n.getId()));
        //Looking through each edge. If an edge creates a connection between two components it can be added otherwise it cant be added
        sortededges.forEach(e -> {
            //union returns true if sets containing the first and second parameter are disjoint
            if (components.union(e.getNode0(),e.getNode1())){
                minimalSpanningTree.addEdge(e.getId(),e.getNode0().getId(),e.getNode1().getId()).setAttribute("weight",e.getAttribute("weight"));
            }
        });
        return minimalSpanningTree;
    }

    /**
     * Method to generate an Eulertour for a given Tree
     * @param graph the given tree
     * @return a Mapping symbolizing an order in which nodes are to be traversed
     */
    public static Map<Integer,Node> generateEulertourForTrees(Graph graph){
        Map<Integer,Node> eulertour = new HashMap<>();
        List<Node> eulertourList = new ArrayList<>();
        Set<Node> visitedNodes = new HashSet<>();
        generateEulertourForTrees(graph.getNode(0),eulertourList,visitedNodes);
        for (int i = 1; i<=eulertourList.size();i++){
            eulertour.put(i, eulertourList.get(i-1));
        }
        return eulertour;
    }

    /**
     * Overloaded version of the Method with the same name containing the recursion
     * @param node the current Node
     * @param eulertour the eulertour
     * @param visitedNodes already visited nodes
     */
    public static void generateEulertourForTrees(Node node, List<Node> eulertour, Set<Node> visitedNodes){
        visitedNodes.add(node);
        eulertour.add(node);

        for (Node n:node.neighborNodes().collect(Collectors.toList())){
            if (!visitedNodes.contains(n)){
                generateEulertourForTrees(n,eulertour,visitedNodes);
                eulertour.add(node);
            }
        }
    }

    /**
     * Nearest Insertion Algorithm to solve TSP problems for a given complete Graph
     * @param graph the given Graph
     * @param n the starting node for the algorithm
     * @return a Mapping for the order of Nodes
     */
    public static Map<Integer,Node> nearestInsertion(Graph graph, Node n){
        //Circle W
        List<Node> w = new LinkedList<>();
        //PriorityQueue to keep track of edges connected from inside W to nodes yet to be added into W
        //Given the comparator the PriorityQueue sorts its entries by the weight of the Edges
        PriorityQueue<Edge> connectingEdges = new PriorityQueue<>(Comparator.comparingDouble(e -> (Double) e.getAttribute("weight")));
        List<Node> visitedNodes = new ArrayList<>();
        List<Node> nonVisitedNodes = graph.nodes().collect(Collectors.toList());

        Node currentNode = n;
        //n is the first node to enter W and functions as starting and endpoint of the circle
        nonVisitedNodes.remove(currentNode);
        visitedNodes.add(currentNode);
        w.add(0,n);
        w.add(1,n);
        connectingEdges.addAll(currentNode.edges().collect(Collectors.toList())); //filter(e -> !connectingEdges.contains(e)).?
        Edge edgetoClosestNode = connectingEdges.poll();
        while(!nonVisitedNodes.isEmpty()){
            //Filter for edges which wouldnt lead to new connections
            while (edgetoClosestNode != null && visitedNodes.contains(edgetoClosestNode.getNode0()) && visitedNodes.contains(edgetoClosestNode.getNode1())){
                edgetoClosestNode = connectingEdges.poll();
            }
            if (edgetoClosestNode != null) {
                //updating currentNode to the Node which is to be added into the circle
                currentNode = visitedNodes.contains(edgetoClosestNode.getNode0()) ? edgetoClosestNode.getNode1() : edgetoClosestNode.getNode0();
                connectingEdges.addAll(currentNode.edges().collect(Collectors.toList()));
                visitedNodes.add(currentNode);
                nonVisitedNodes.remove(currentNode);

                //First insert is always between start and endpoint
                if (w.size() == 2) {
                    w.add(1, currentNode);
                } else {
                    //findshortestcirclewithnewnode inserts the node to be added at the index of the cirlce, which
                    //results in the lowest increase in cost
                    findShortestCircleWithNewNode(w, currentNode);
                }
            }
        }

        Map<Integer,Node> returnMapping = new HashMap<>();
        for (int i = 0; i<w.size();i++){
            returnMapping.put(i+1,w.get(i));
        }

        return returnMapping;
    }

    /**
     * Sub Method used to find the best point of insertion for a given circle
     * @param circle the given circle
     * @param nodeToBeAdded node to be added
     */
    private static void findShortestCircleWithNewNode(List<Node> circle,Node nodeToBeAdded){
        //Variables to save the optimal length and the index used to insert the node
        double circlelengthIncreaseWithNewNode = Double.MAX_VALUE;
        int indexForShortestCircle = 1;
        //used to cycle through the possible indices and compare the results of each node insertion

        int currentCirclesize = circle.size();
        //for circles of length n the node can be inserted at index 1 to n-1
        //insertions at a specific index shift existing elements to the right a b c insert x at index 1 => a x b c
        //loop through all possible points for an insertion and compare their increase in the total circle length
        for(int i = 0;i < currentCirclesize-1;i++){
            double newCirclelengthincrease = (double) circle.get(i).getEdgeBetween(nodeToBeAdded).getAttribute("weight")
                    + (double) nodeToBeAdded.getEdgeBetween(circle.get(i+1)).getAttribute("weight")
                    - (double) circle.get(i).getEdgeBetween(circle.get(i+1)).getAttribute("weight");
            //if the calculated weight for index is lower than the previous one update the optimal values
            if (newCirclelengthincrease < circlelengthIncreaseWithNewNode){
                circlelengthIncreaseWithNewNode = newCirclelengthincrease;
                indexForShortestCircle = i;
            }
            //remove the node at the specified index again and increase index by 1
            //circle.remove(index);
        }
        //add node at the optimal index to create the optimal circle
        circle.add(indexForShortestCircle+1,nodeToBeAdded);
    }

    /**
     * Method to create a complete Graph with size n and set their edgeweight according to the ManhattenMetric
     * @param n the size of the graph to be created
     * @return the created complete graph of size n
     */
    public static Graph generateCompleteGraphWithMetricTsp(int n){
        Graph completeGraph = new SingleGraph("Complete Graph");
        FullGenerator completeGraphgenerator = new FullGenerator();
        completeGraphgenerator.addSink(completeGraph);
        Graph gridGraph = new SingleGraph("Grid Graph");
        GridGenerator gridGraphgenerator = new GridGenerator();
        gridGraphgenerator.addSink(gridGraph);
        completeGraphgenerator.begin();
        gridGraphgenerator.begin();
        for (int i = 0; i < n-1; i++){
            completeGraphgenerator.nextEvents();
            gridGraphgenerator.nextEvents();
        }
        completeGraphgenerator.end();
        gridGraphgenerator.end();
        for (int i = 0;i<n;i++){
            completeGraph.getNode(i).setAttribute("xy",gridGraph.removeNode(randomNode(gridGraph)).getAttribute("xy"));
        }

        completeGraph.edges().forEach(e -> e.setAttribute("weight",getWeight(e)));
        return completeGraph;
    }

    /**
     * Sub-Method to calculate the weight for a given edge according to the Manhatten-metric
     * @param edge the given edge
     * @return the weight value calculated for the edge
     */
    public static double getWeight(Edge edge){
        Double[] xyNode0 = edge.getNode0().getAttribute("xy",Double[].class);
        Double[] xyNode1 = edge.getNode1().getAttribute("xy",Double[].class);

        return Math.abs(xyNode0[0] - xyNode1[0]) + Math.abs(xyNode0[1] - xyNode1[1]);
    }

    /**
     * Method to log Graphsize Routelength Runtime and Size of the spanningtree for a specific Graphsizes
     */
    public static void generateComparissonData(){
        int[] values = {100,200,500,1000};
        int currentGraphsize;
        int lengthOfRouteMSTH;
        int lengthOfRouteNI;
        long runtimeinMsMSTH;
        long runtimeinMsNI;
        int doubleLengthOfMST;
        for (int i = 0;i < values.length;i++){
            lengthOfRouteMSTH = 0;
            lengthOfRouteNI = 0;
            doubleLengthOfMST = 0;
            currentGraphsize = values[i];
            Graph randomCompleteGraph = generateCompleteGraphWithMetricTsp(values[i]);

            Graph mstkruskal = kruskal(randomCompleteGraph);
            for (Edge e:mstkruskal.edges().collect(Collectors.toList())){
                doubleLengthOfMST += (double)e.getAttribute("weight");
            }

            long starttimeMSTH = System.currentTimeMillis();
            Map<Integer,Node> resultMSTH = minimumSpanningTreeHeuristic(randomCompleteGraph);
            long endtimeMSTH = System.currentTimeMillis();
            runtimeinMsMSTH = endtimeMSTH - starttimeMSTH;
            for (int j = 1; j < resultMSTH.size();j++){
                lengthOfRouteMSTH += (double) resultMSTH.get(j)
                        .getEdgeBetween(resultMSTH.get(j+1)).getAttribute("weight");
            }

            long starttimeNI = System.currentTimeMillis();
            Map<Integer,Node> resultNI = nearestInsertion(randomCompleteGraph,randomCompleteGraph.getNode(0));
            long endtimeNI = System.currentTimeMillis();
            runtimeinMsNI = endtimeNI - starttimeNI;
            for (int j = 1; j < resultNI.size();j++){
                lengthOfRouteNI += (double) resultNI.get(j)
                        .getEdgeBetween(resultNI.get(j+1)).getAttribute("weight");
            }

            LoggingUtil log;
            try {
                log = new LoggingUtil("src/test/java/praktikum/aufgabe1","logfileAlgoCompare.txt");
                log.append("\n" + currentGraphsize + "\t" + lengthOfRouteMSTH + "\t" + lengthOfRouteNI + "\t" + runtimeinMsMSTH + "\t" + runtimeinMsNI + "\t" + doubleLengthOfMST + "\t");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {
        for (int i = 0;i<20;i++){
            generateComparissonData();
        }
    }
}
