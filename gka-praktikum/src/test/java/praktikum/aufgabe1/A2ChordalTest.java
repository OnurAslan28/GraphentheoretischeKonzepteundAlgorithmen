package test.java.praktikum.aufgabe1;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static main.java.praktikum.aufgabe1.ChordalGraphAlgorithms.*;
import static main.java.praktikum.aufgabe1.PrueferTreeAlgorithms.generateTree;
import static org.junit.Assert.*;
import static test.java.praktikum.aufgabe1.A1TreeTest.*;

public class A2ChordalTest {
    public static final int NUMFILES = 10;
    public static final int NUMFILESNC = 3;
    public static final int TESTRUNS = 10;
    public static final int MAXCODELENGTH = 1000;

    private List<Graph> chordalGraphs = new ArrayList<>();
    private List<Graph> nonchordalGraphs = new ArrayList<>();

    //Reads both chordal and nonchordalGraphs into the respective list Before each test
    @Before
    public void readChordalGraphs() {
        for (int i = 1; i <= NUMFILES; i++) {
            //die .dot-files liegen bei mir im Projekt unter data
            //ggf. anpassen
            String fileName = String.format("src/data/CG%s.dot", i);
            chordalGraphs.add(fromFile(fileName));
        }

        for (int i = 1; i <= NUMFILESNC; i++) {
            //die .dot-files liegen bei mir im Projekt unter data
            //ggf. anpassen
            String fileName = String.format("src/data/NCG%s.dot", i);
            nonchordalGraphs.add(fromFile(fileName));
        }
    }

    //Tests both the chordal graph examples aswell as the nonchordalgraph examples for their respective results
    @Test
    public void testChordalRecognitionWithExamples(){
        for (Graph g: chordalGraphs){
            assert(testElimination(g,createPerfectEliminationOrdering(g,g.getNode(0))));
        }
        for (Graph g: nonchordalGraphs){
            assertFalse(testElimination(g,createPerfectEliminationOrdering(g,g.getNode(0))));
        }
    }

    //Tests random chordalgraphs with given sizes
    @Test
    public void testChordalRecognitionWithRandomGivenSize(){
        for (int i = 0; i< TESTRUNS; i++){
            int[] nodeCounts = {1,2,4,10,25,100,1000,200,500,800};
            Graph g = generateChordalGraph(nodeCounts[i]);
            assert(testElimination(g,createPerfectEliminationOrdering(g,g.getNode(0))));
        }
    }

    //Tests random chordalgraphs with random sizes
    @Test
    public void testChordalRecognitionWithRandomSize(){
        for (int i = 0; i<TESTRUNS;i++){
            Graph g = generateChordalGraph(MAXCODELENGTH);
            assert(testElimination(g,createPerfectEliminationOrdering(g, g.getNode(0))));
        }
    }

    //Tests the calculation of the chromatic number with 3 given examples
    @Test
    public void testChromaticNumbergenerationwithExamples(){
        for (int i = 0; i < 3; i++){
            Graph g = chordalGraphs.get(i);

            assert(Integer.parseInt((String)(g.getAttribute("chromaticNumber")))
                    == calculateChromaticNumber(g,createPerfectEliminationOrdering(g,g.getNode(0))));
        }
    }

    //Tests the calculation of the chromatic number with random trees
    @Test
    public void testChromaticNumberOnRandomTrees(){
        for (int i = 0;i<TESTRUNS;i++){
            Graph g = generateTree(MAXCODELENGTH);
            assertEquals(2,calculateChromaticNumber(g,createPerfectEliminationOrdering(g,g.getNode(0))));
        }
    }

    //Tests each method for their invalid agrument check
    @Test
    public void testIllegalArgumentRecognition(){
        Graph g = fromFile("src/data/CG1.dot");
        Map<Integer, Node> sigma = new HashMap<>();
        //Null values or invald n values
        assertThrows(IllegalArgumentException.class, ()-> {
            createPerfectEliminationOrdering(null,null);
        });
        assertThrows(IllegalArgumentException.class, ()-> {
            testElimination(null,null);
        });
        assertThrows(IllegalArgumentException.class, ()-> {
            generateChordalGraph(0);
        });
        assertThrows(IllegalArgumentException.class, ()-> {
            generateChordalGraph(-5);
        });
        //Sigma having invalid size
        assertThrows(IllegalArgumentException.class, ()-> {
            testElimination(g,sigma);
        });
        //Sigma having valid size, but non matching nodes
        for (int i = 1; i <= 5;i++){
            g.addNode("node: "+i);
        }
        assertThrows(IllegalArgumentException.class, ()-> {
            testElimination(g,sigma);
        });
    }
}
