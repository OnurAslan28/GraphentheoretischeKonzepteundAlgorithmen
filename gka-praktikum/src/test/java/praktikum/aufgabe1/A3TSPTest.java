package test.java.praktikum.aufgabe1;

import org.graphstream.graph.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static main.java.praktikum.aufgabe1.TspAlgorithms.*;
import static org.junit.Assert.*;
import static test.java.praktikum.aufgabe1.A1TreeTest.*;

public class A3TSPTest {
    private static final int TESTRUNS = 10;
    private static final int MAXSIZE = 100;
    private static final int NUMBERKRUSKALFILES = 4;
    private static final int NUMBERTSPGRAPHS = 3;
    private final ArrayList<Graph> kruskalGraphs = new ArrayList<>();
    private final ArrayList<Graph> tspGraphs = new ArrayList<>();

    @Before
    public void readGraphs(){
        for (int i = 1; i<=NUMBERKRUSKALFILES;i++){
            kruskalGraphs.add(fromFile(String.format("src/data/Kruskal%d.dot",i)));
        }
        for (int i = 2;i<=NUMBERTSPGRAPHS;i++){
            tspGraphs.add(fromFile(String.format("src/data/TSP%d.dot",i)));
        }
    }

    @Test
    public void testKruskalWithExamples(){
        for (Graph graph:kruskalGraphs){
            Graph mstkruskal = kruskal(graph);
            double kruskalMSTlength = 0;
            for (Edge e:mstkruskal.edges().collect(Collectors.toList())){
                kruskalMSTlength += (double)e.getAttribute("weight");
            }
            assertEquals(graph.getAttribute("MST"),kruskalMSTlength);
            assertEquals(mstkruskal.getNodeCount()-1,mstkruskal.getEdgeCount());
            int nodesCounted = 0;
            BreadthFirstIterator bfi = new BreadthFirstIterator(mstkruskal.getNode(0));
            while(bfi.hasNext()) {
                Node node = bfi.next();
                nodesCounted++;
            }
            assertEquals(graph.getNodeCount(),nodesCounted);
        }
    }

    @Test
    public void testMinimalRouteAlgorithmsWithExamples(){
        for (Graph graph:tspGraphs){
            int generatedRouteEdgeWeightsMSTH = 0;
            Map<Integer,Node> minSpanningTreeHeuristikResult = minimumSpanningTreeHeuristic(graph);
            for (int j = 1; j < minSpanningTreeHeuristikResult.size();j++){
                generatedRouteEdgeWeightsMSTH += (double) minSpanningTreeHeuristikResult.get(j)
                        .getEdgeBetween(minSpanningTreeHeuristikResult.get(j+1)).getAttribute("weight");
            }

            int generatedRouteEdgeWeightsNI = 0;
            Map<Integer,Node> nearestInsertionResult = nearestInsertion(graph, graph.getNode(0));
            for (int k = 1; k < nearestInsertionResult.size();k++){
                generatedRouteEdgeWeightsNI += (double) nearestInsertionResult.get(k)
                        .getEdgeBetween(nearestInsertionResult.get(k+1)).getAttribute("weight");
            }
            assertTrue(generatedRouteEdgeWeightsMSTH <= (double)graph.getAttribute("optimalPath")*2);
            assertTrue(generatedRouteEdgeWeightsNI <= (double)graph.getAttribute("optimalPath")*2);
        }
    }

    @Test
    public void testGenerateCompleteGraphWithMetricTSP(){
        int[] values = {5,10,20,50};
        Edge currentEdge;
        for (int i = 0;i<4;i++){
            Graph randomCompleteGraph = generateCompleteGraphWithMetricTsp(values[i]);
            for (Node n1:randomCompleteGraph){
                for (Node n2:randomCompleteGraph){
                    if (!n1.equals(n2)){
                        currentEdge = n1.getEdgeBetween(n2);
                        assertNotNull(currentEdge);
                        Double[] xyNode0 = currentEdge.getNode0().getAttribute("xy",Double[].class);
                        Double[] xyNode1 = currentEdge.getNode1().getAttribute("xy",Double[].class);
                        double expectedWeight = Math.abs(xyNode0[0] - xyNode1[0]) + Math.abs(xyNode0[1] - xyNode1[1]);
                        assertEquals(expectedWeight,(double)currentEdge.getAttribute("weight"),1.0d);
                    }
                }
            }
        }
    }

    @Test
    public void testMinimalRouteAlgorithmsRandom(){
        Graph randonmCompleteGraph;
        Graph kruskalResult;

        for (int i = 0; i < TESTRUNS;i++){
            double minimumSpanningTreeEdgeWeights = 0;
            double generatedRouteEdgeWeightsMSTH = 0;
            double generatedRouteEdgeWeightsNI = 0;
            randonmCompleteGraph = generateCompleteGraphWithMetricTsp(new Random().nextInt(MAXSIZE)+3);
            kruskalResult = kruskal(randonmCompleteGraph);
            //System.out.println("Graph size: "+randonmCompleteGraph.getNodeCount());
            for (Edge e:kruskalResult.edges().collect(Collectors.toList())){
                minimumSpanningTreeEdgeWeights += (double)e.getAttribute("weight");
            }

            Map<Integer,Node> minSpanningTreeHeuristikResult = minimumSpanningTreeHeuristic(randonmCompleteGraph);
            for (int j = 1; j < minSpanningTreeHeuristikResult.size();j++){
                generatedRouteEdgeWeightsMSTH += (double) minSpanningTreeHeuristikResult.get(j)
                        .getEdgeBetween(minSpanningTreeHeuristikResult.get(j+1)).getAttribute("weight");
            }
            Map<Integer,Node> nearestInsertionResult = nearestInsertion(randonmCompleteGraph, randonmCompleteGraph.getNode(0));
            for (int k = 1; k < nearestInsertionResult.size();k++){
                generatedRouteEdgeWeightsNI += (double) nearestInsertionResult.get(k)
                        .getEdgeBetween(nearestInsertionResult.get(k+1)).getAttribute("weight");
            }
            //System.out.println(generatedRouteEdgeWeightsMSTH + "/" + minimumSpanningTreeEdgeWeights);
            //System.out.println(generatedRouteEdgeWeightsNI+"/"+minimumSpanningTreeEdgeWeights);
            assertTrue(generatedRouteEdgeWeightsMSTH <= minimumSpanningTreeEdgeWeights*2);
            assertTrue(generatedRouteEdgeWeightsNI <= minimumSpanningTreeEdgeWeights*2);
        }
    }

    @Test
    public void testKruskalRandom(){
        Graph kruskalResult;
        Graph randomGraph;
        for (int i = 0; i < TESTRUNS;i++){
            randomGraph = generateCompleteGraphWithMetricTsp(new Random().nextInt(MAXSIZE));
            kruskalResult = kruskal(randomGraph);
            assertEquals(kruskalResult.getNodeCount()-1,kruskalResult.getEdgeCount());
            BreadthFirstIterator bfi = new BreadthFirstIterator(kruskalResult.getNode(0));
            int nodesCounted = 0;
            while(bfi.hasNext()) {
                Node node = bfi.next();
                nodesCounted++;
            }
            assertEquals(randomGraph.getNodeCount(),nodesCounted);
        }
    }

    @Test
    public void testEulertourRandom(){
        Graph randomGraph;
        Graph kruskalOfRandomGraph;
        for (int i = 0; i<TESTRUNS;i++){
            randomGraph = generateCompleteGraphWithMetricTsp(new Random().nextInt(6)+4);
            kruskalOfRandomGraph = kruskal(randomGraph);
            Map<Integer,Node> eulertour = generateEulertourForTrees(kruskalOfRandomGraph);
            assertEquals(kruskalOfRandomGraph.getEdgeCount()* 2L+1,eulertour.size());
        }
    }
}
