package main.java.praktikum.aufgabe1;

import org.graphstream.graph.BreadthFirstIterator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.Graphs;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TspAlgorithms2 {
    public static Map<Integer, Node> generateEulertour(Graph graph){
        Graph clonedgraph = Graphs.clone(graph);
        Map<Integer,Node> eulertour = new HashMap<>();
        Node currentNode = clonedgraph.getNode(0);
        Edge currentEdge;
        int counter = 1;
        while(clonedgraph.edges().count() != 0){
            //for the current node find the first non bridge edge. If only bridges exist find first bridge
            Optional<Edge> edgeOptional = currentNode.edges().filter(e -> !isEdgeaBridge(clonedgraph,e)).findFirst();
            //If there is an Edge, which isnt a bridge, then it is the next edge to be cut. Else one of the bridges is.
            Node finalCurrentNode = currentNode;
            currentEdge = edgeOptional.orElseGet(() -> finalCurrentNode.edges().findFirst().get());
            //Put current Edge into eulertour
            eulertour.put(counter,currentNode);
            counter++;
            //update current location inside the graph during the eulertour
            currentNode = currentEdge.getOpposite(currentNode);
            //remove the traversed edge
            clonedgraph.removeEdge(currentEdge);
        }

        return eulertour;
    }

    public static boolean isEdgeaBridge(Graph graph, Edge edge){
        Graph clonegraph = Graphs.clone(graph);
        Edge edgeInClone = clonegraph.getEdge(edge.getId());
        int nodeCountWithedge = 0;
        int nodeCountWithoutedge = 0;
        //BreadthFirstIterator without the edge being removed
        BreadthFirstIterator bfiWithedge = new BreadthFirstIterator(edgeInClone.getNode0());
        while (bfiWithedge.hasNext()){
            bfiWithedge.next();
            nodeCountWithedge++;
        }
        clonegraph.removeEdge(edge.getId());
        //BreadthFirstIterator with the edge being removed
        BreadthFirstIterator bfiWithoutedge = new BreadthFirstIterator(edgeInClone.getNode0());
        while (bfiWithoutedge.hasNext()){
            bfiWithoutedge.next();
            nodeCountWithoutedge++;
        }
        //returning true if the counted nodes were different for the two Bfs runs else false
        return !(nodeCountWithedge == nodeCountWithoutedge);
    }
}
