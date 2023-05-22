package main.java.praktikum.aufgabe1;

import org.graphstream.graph.*;


import java.util.*;
import java.util.stream.Collectors;

import static main.java.praktikum.aufgabe1.PrueferTreeAlgorithms.*;
import static test.java.praktikum.aufgabe1.A1TreeTest.*;

public class ChordalGraphAlgorithms {
    /**
     * Method creates a perfect elimination order out a given Graph starting from a given Node. For the PEO to be perfect
     * the graph needs to be chordal. This Method ist best used in conjunction with testElimination to check whether
     * or not the graph is chordal.
     * @param g The given Graph
     * @param v The start Node
     * @return Returns a Mapping between Integers and Nodes. The Integers represent the Order in which Nodes should be
     * removed
     */
    public static Map<Integer,Node> createPerfectEliminationOrdering(Graph g, Node v){
        //Checking for illegal arguments
        if (g == null || v == null){
            throw new IllegalArgumentException("Parameters can not be null");
        }
        if (!g.nodes().collect(Collectors.toList()).contains(v)){
            throw new IllegalArgumentException("The given node has to be included in g");
        }

        Map<Integer,Node> sigma = new HashMap<>();

        //Set empty label and number for each Node in g
        g.nodes().forEach(n -> n.setAttribute("label",""));
        g.nodes().forEach(n -> n.setAttribute("number", ""));

        List<Node> nodesWithoutNumber = g.nodes().collect(Collectors.toList());
        Node u = v;

        //Loop from n to 1 with n being the Nodecount
        for (int i = g.getNodeCount(); i > 0;i--){
            //Mapping u to i
            sigma.put(i,u);
            u.setAttribute("number","" + i);
            nodesWithoutNumber.remove(u);
            int finalI = i;
            //for all neighbors of u set label[v] to label[v] + i
            u.neighborNodes().forEach((Node n) -> {
                //exclude already numbered neighbors
                if (n.getAttribute("number") != ""){
                    return;
                }
                if (n.getAttribute("label") == ""){
                    n.setAttribute("label",""+finalI);
                    return;
                }
                n.setAttribute("label", n.getAttribute("label") + "," + finalI);
            });
            //DISCLAIMER: This sort is meant to be from highest to lowest so the return values are inverted!
            //Sort nodesWithoutNumber by their labels. By reversing the result, the highest labels are first in the List
            nodesWithoutNumber.sort((Node n1,Node n2) -> {
                //Necessary check to see whether the labels are set at all
                //case 1: Neither label is set. This would mean both have the same value
                if (n1.getAttribute("label") == "" && n2.getAttribute("label") == ""){
                    return 0;
                    //case 2: label 1 is set but label 2 is not. This would mean label 1 is larger
                } else if (n1.getAttribute("label") != "" && n2.getAttribute("label") == ""){
                    return -1;
                    //case 3: The opposite. This would mean label 2 is larger
                } else if (n1.getAttribute("label") == "" && n2.getAttribute("label") != ""){
                    return 1;
                }
                //Splitting the labels into an Array of Strings containing Strings for each entry in the label
                String[] label1 = ((String)n1.getAttribute("label")).split(",");
                String[] label2 = ((String)n2.getAttribute("label")).split(",");
                int counter = 0;
                //Instead of using the string compare the numbers contained in each split are compared as long as
                //both of the Arrays still have entries. If one we reach the end of at least one Array without one
                //of the numbers not being the same there are a few cases to cover
                while (counter < label1.length && counter < (label2).length){
                    if (!label1[counter].equals(label2[counter])){
                        return Integer.parseInt(label2[counter]) - Integer.parseInt(label1[counter]);
                    }
                    counter++;
                }
                //case 1: Reaching the end and both Arrays have no more entries. This means both labels are exactly the same
                if (counter == label1.length && counter == label2.length){
                    return 0;
                    //case 2: label 1 still has entries while label 2 doesnt. In this case label 1 is larger than label 2
                } else if (counter < (label1).length){
                    return -1;
                    //case 3: the opposite is the case. In this case label 2 is larger than label 1
                } else {
                    return 1;
                }
            });
            //update u
            if (nodesWithoutNumber.size() > 0){
                u = nodesWithoutNumber.get(0);
            }
        }

        g.nodes().forEach(n -> n.removeAttribute("label"));

        return sigma;
    }

    /**
     * Method to test a given Mapping for a given Graph.
     * @param g the given Graph
     * @param sigma the given Mapping
     * @return returns true if the Mapping is a PEO otherwise false
     */
    public static boolean testElimination(Graph g, Map<Integer,Node> sigma){
        //Checking for illegal arguments
        checkValidInputs(g, sigma);
        Map<Node,List<Node>> A = new HashMap<>();
        ArrayList<Node> visitedEntries = new ArrayList<>();

        //set A(v) to empty for each Node in g
        for (Node n: g){
            A.put(n,new ArrayList<>());
        }

        //Loop from 1 to n-1 with n being the NodeCount
        for (int i = 1;i<g.getNodeCount();i++){
            //u = sigma(i)
            Node u = sigma.get(i);
            visitedEntries.add(u);

            List<Node> X;
            //get all neighbors of u excluding already visited ones
            X = u.neighborNodes().filter(n -> !visitedEntries.contains(n)).collect(Collectors.toList());
            //if X isnt empty
            if (X.size() != 0){
                int finalI = i;
                //getting the minOfX by going through the Map and comparing the keys
                Node w = X.stream().min((Node n1, Node n2)->{
                    int x = 0;
                    int y = 0;
                    for (int j = finalI; j <= sigma.size(); j++){
                        if (n1 == sigma.get(j)){
                            x = j;
                        }
                        if (n2 == sigma.get(j)){
                            y = j;
                        }
                    }
                    return x-y;
                }).get();
                X.remove(w);
                //Creating the Union of A(w) and X/w
                X.forEach(n -> {
                    if (!A.get(w).contains(n)){
                        A.get(w).add(n);
                    }
                });
            }
            //if A(u)\ADJ[u] = empty then return false
            if (A.get(u).stream().filter(n -> !u.neighborNodes().collect(Collectors.toList()).contains(n)).count() != 0){
                return false;
            }
        }
        return true;
    }

    /**
     * Colors the given graph optimally based on the given perfect elimination odering
     * @param g the given graph
     * @param sigma the given PEO
     * @return the input graph with altered attributes
     */
    public static Graph colorChordalGraph(Graph g, Map<Integer,Node> sigma){
        //Checking for illegal arguments
        checkValidInputs(g, sigma);
        //The color for the start Node of the algorithm is the lowest color 1
        sigma.get(sigma.size()).setAttribute("color","1");
        //The algorithm runs from n to 1
        for (int i = sigma.size()-1;i > 0;i--){
            Node n = sigma.get(i);
            //Create a list of all the colors from neighbors of n
            List<Integer> colorOfNeighbors = n.neighborNodes()
                    .map(node -> {
                        if (node.getAttribute("color") != null){
                            return Integer.parseInt((String) node.getAttribute("color"));
                        }
                        return 0;
                    })
                    .collect(Collectors.toList());
            //Calculate new Color as the first value which is not included in the list
            int newColor = 0;
            for (int j = 1;newColor == 0;j++){
                if (!colorOfNeighbors.contains(j)){
                    newColor = j;
                }
            }
            //Set the color of n
            n.setAttribute("color",""+newColor);
                    //1 1 3 3 3 => 2
        }
        return g;
    }

    /**
     * Checks if the given graph and sigma are a valid input for the method this method is called in
     * @param g the graph g
     * @param sigma the PEO sigma
     */
    private static void checkValidInputs(Graph g, Map<Integer, Node> sigma) {
        if (g == null || sigma == null){
            throw new IllegalArgumentException("Parameters can not be null");
        }
        if (g.getNodeCount() != sigma.size()){
            throw new IllegalArgumentException("the given elimination ordering has a greater or lesser number of nodes than the graph");
        }
        List<Node> nodes = g.nodes().collect(Collectors.toList());
        nodes.forEach(n -> {
            if (!sigma.containsValue(n)){
                throw new IllegalArgumentException("Not all nodes of g have an entry in sigma");
            }
        });
    }

    /**
     * Method to calculate the chromatic number of a give graph based on its PEO
     * @param g the given graph
     * @param sigma the given PEO
     * @return the chromatic number of the Input graph
     */
    public static int calculateChromaticNumber(Graph g, Map<Integer,Node> sigma){
        //Checking for illegal arguments
        checkValidInputs(g, sigma);
        //Checks whether the given graph is chordal. It is also colored
        if (testElimination(g, sigma)){
            colorChordalGraph(g, sigma);
        } else {
            throw new IllegalArgumentException("Input graph is not chordal");
        }
        //Filters for the Max
        Node n = g.nodes().max(Comparator.comparingInt((Node n2) -> Integer.parseInt((String) n2.getAttribute("color")))).get();
        return Integer.parseInt((String)n.getAttribute("color"));
    }

    /**
     * Method to generate a n sized chordal graph
     * @param n the size of the graph
     * @return the generated graph
     */
    public static Graph generateChordalGraph(int n){
        //Checking for invalid inputs
        if (n <= 0){
            throw new IllegalArgumentException("n has to be larger than 0");
        }
        //Save the initial Edge Count so the result isnt a complete graph each time
        Graph g = generateTree(n);
        int initialEdgeCount = g.getEdgeCount();
        Edge e;
        Edge e2;
        //Check whether a pair of two edges is connected by a node
        for (int i = 0; i< initialEdgeCount;i++){
            for (int j = 0; j < initialEdgeCount;j++){
                //set the pair of edges to compare, with i and j
                e = g.getEdge(i);
                e2 = g.getEdge(j);
                //if the edges are not the same edge e != e2
                if (!e.equals(e2)) {
                    Node u = null;
                    Node w = null;
                    //Multiple cases to find the node which connects the edges and figure out u w u--v--w
                    if (e.getNode0() == e2.getNode0()) {
                        u = e.getNode1();
                        w = e2.getNode1();
                    } else if (e.getNode1() == e2.getNode0()) {
                        u = e.getNode0();
                        w = e2.getNode1();
                    } else if (e.getNode0() == e2.getNode1()) {
                        u = e.getNode1();
                        w = e2.getNode0();
                    } else if (e.getNode1() == e2.getNode1()) {
                        u = e.getNode0();
                        w = e2.getNode0();
                    }
                    //Check if u and w were set and whether the edge between them doesnt already exists
                    if (u != null && w != null
                            && g.getEdge(u.getId() + "to" + w.getId()) == null
                            && g.getEdge(w.getId() + "to" + u.getId()) == null
                    ) {
                        //If both are true, then the edge can be added
                        g.addEdge(u.getId() + "to" + w.getId(), u, w);
                    }
                }
            }
        }
        return g;
    }

    /**
     * Almost exact copy of the method above with a slight change to creat complete graphs. This was just used to create
     * and example for tests
     * @param n the size of the complete graph
     * @return the complete graph of size n
     */
    public static Graph generateCompleteGraph(int n){
        //Checking for invalid inputs
        if (n <= 0){
            throw new IllegalArgumentException("n has to be larger than 0");
        }
        //Save the initial Edge Count so the result isnt a complete graph each time
        Graph g = generateTree(n);
        Edge e;
        Edge e2;
        //Check whether a pair of two edges is connected by a node
        for (int i = 0; i< g.getEdgeCount();i++){
            for (int j = 0; j < g.getEdgeCount();j++){
                //set the pair of edges to compare, with i and j
                e = g.getEdge(i);
                e2 = g.getEdge(j);
                //if the edges are not the same edge e != e2
                if (!e.equals(e2)) {
                    Node u = null;
                    Node w = null;
                    //Multiple cases to find the node which connects the edges and figure out u w
                    if (e.getNode0() == e2.getNode0()) {
                        u = e.getNode1();
                        w = e2.getNode1();
                    } else if (e.getNode1() == e2.getNode0()) {
                        u = e.getNode0();
                        w = e2.getNode1();
                    } else if (e.getNode0() == e2.getNode1()) {
                        u = e.getNode1();
                        w = e2.getNode0();
                    } else if (e.getNode1() == e2.getNode1()) {
                        u = e.getNode0();
                        w = e2.getNode0();
                    }
                    //Check if u and w were set and whether the edge between them doesnt already exists
                    if (u != null && w != null
                            && g.getEdge(u.getId() + "to" + w.getId()) == null
                            && g.getEdge(w.getId() + "to" + u.getId()) == null
                    ) {
                        //If both are true, then the edge can be added
                        g.addEdge(u.getId() + "to" + w.getId(), u, w);
                    }
                }
            }
        }
        return g;
    }

    public static void main(String[] args) {
//        Graph g = A1TreeTest.fromFile("src/data/CG1.dot");
//        Map<Integer,Node> test = createPerfectEliminationOrdering(g, g.getNode(0));
//        test.forEach((Integer i,Node n)-> {
//            System.out.println("step " + i + " node " + n.getId());
//        });

        //Graph g1 = generateChordalGraph(7);
        //Graph g1 = generateCompleteGraph(10);
        Graph g1 = fromFile("src/data/CG10.dot");

        Map<Integer,Node> sigma = createPerfectEliminationOrdering(g1, g1.getNode(0));
        for (int i = 1; i<=sigma.size();i++){
            System.out.println("number: " + i + " Node: " + sigma.get(i));
        }
        System.out.println(testElimination(g1,sigma));
        testElimination(g1,sigma);
        System.out.println(testElimination(g1,sigma));

        if (testElimination(g1,sigma)){
            colorChordalGraph(g1,sigma);
        }
        toFile(g1,"src/data/CGTest.dot");
        System.out.println(calculateChromaticNumber(g1,sigma));
    }
}
