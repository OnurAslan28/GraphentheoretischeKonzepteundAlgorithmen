package main.java.praktikum.aufgabe1;

import org.graphstream.graph.BreadthFirstIterator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.Graphs;
import org.graphstream.graph.implementations.SingleGraph;


import java.util.ArrayList;

import java.util.Optional;

import static test.java.praktikum.aufgabe1.A1TreeTest.treeEquals;


public class PrueferTreeAlgorithms {
    /**
     * Generiert einen zufällig erstellten Baum der Größe n
     * @param n Knotenanzahl des zu generierenden Baums
     * @return Gibt den generierten Baum Zurück
     */
    public static Graph generateTree(int n){
        Graph graph = new SingleGraph("Random Graph of size: " + n);
        ArrayList<Integer> nodestoAdd = new ArrayList<>();
        //nodestoAdd wird mit den Knoten Bezeichnern befüllt
        for (int i = 0;i<n;i++){
            nodestoAdd.add(i,i+1);
        }
        //for Loop um die Knoten nacheinander in den Graphen hinzuzufügen
        for (int i = 0;i < n;i++){
            //ein zufälliger Eintrag aus nodestoAdd wird der nächste Knoten
            int nodetoAdd = nodestoAdd.get((int)(Math.random()*nodestoAdd.size()));
            //Falls es keinen Knoten im Graphen gibt wird der erste Knoten ohne Kante hinzugefügt
            if (graph.getNodeCount() == 0){
                graph.addNode("v"+nodetoAdd).setAttribute("label",  Integer.toString(nodetoAdd));
            } else {
                //Falls es schon mindestens einen Knoten gibt wird ein Knoten und eine Kante von diesem zu einem bereits
                //bestehenden Knoten hinzugefügt
                int otherEdgeNode = (int) (Math.random()*graph.getNodeCount());
                graph.addNode("v"+nodetoAdd).setAttribute("label", Integer.toString(nodetoAdd));
                graph.addEdge(graph.getNode(otherEdgeNode).getId() + "to" + "v" + nodetoAdd,"v" + nodetoAdd,"v" + graph.getNode(otherEdgeNode).getAttribute("label"));
            }
            //Nach dem hinzufügen wird der Knoten aus den nodestoAdd entfernt und ein neuer Schleifendurchlauf startet
            Integer removeNode = nodetoAdd;
            nodestoAdd.remove(removeNode);
        }

        return graph;
    }

    /**
     * Erstellt aus dem übergebenen Pruefercode in Form eines Arrays den passenden Baum
     * @param code Pruefercode als Array. Randfälle sind die übergabe eines leeren Arrays und eines Arrays der
     *             Größe eins mit dem Inhalt 0 an Index 0
     * @return Gibt den erstellten Baum zurück. Bei leerem Array einen Baum der Größe 1 und bei einem Array
     * der Größe eins mit dem Inhalt 0 an Index 0 einen Baum mit zwei Knoten und einer Kante zwischen diesen
     */
    public static Graph fromPrueferCodeToTree(long[] code){
        Graph graph = new SingleGraph("Graph from Code");
        //Edge case für leeres Tupel
        if (code.length == 0){
            graph.addNode("v1").setAttribute("label",1);
            return graph;
        }
        //Edge case für Tupel mit Wert 0 (Unterscheidung zwischen 1/2 Knoten)
        if (code.length == 1 && code[0] == 0){
            graph.addNode("v1").setAttribute("label",Integer.toString(1));
            graph.addNode("v2").setAttribute("label",Integer.toString(2));
            graph.addEdge("v1tov2","v1","v2");
            return graph;
        }
        //Edge case ungültiges Tupel
        for (long l: code){
            if (l <= 0 || l > code.length + 2)throw new IllegalArgumentException("ungültiges Tupel");
        }

        //Arraylists für die Knoten und das Tupel erstellt und befüllen
        int nodeCount = code.length + 2;
        ArrayList<Long> nodes = new ArrayList<>();
        ArrayList<Long> tupel = new ArrayList<>();
        for(int i = 0; i < nodeCount;i++){
            nodes.add(i, (long) (i+1));
        }
        for(int i = 0; i < code.length ; i++){
            tupel.add(i, code[i]);
        }
        //Hinzufügen von Knoten und das setzen der Label
        for (long l: nodes){
            graph.addNode("v"+l);
            graph.getNode("v"+l).setAttribute("label",Long.toString(l));
        }
        //While loop bis das Tuple leer ist
        while (tupel.size() > 0){
            long edgestart = minXwithoutT(nodes, tupel); //Hilfsmethode um das Min aus X/T zu berechnen
            long edgeend = tupel.get(0); //erster Eintrag vom Tupel
            graph.addEdge("v"+edgestart+"to"+"v"+edgeend,"v"+Long.toString(edgestart),"v"+Long.toString(edgeend)); //Kante mit den ermittelten Werten einfügen
            nodes.remove(edgestart); //Elemente aus Tupel/Knoten streichen
            tupel.remove(0);
        }
        graph.addEdge("v"+nodes.get(0)+"to"+"v"+nodes.get(1),"v"+Long.toString(nodes.get(0)),"v"+Long.toString(nodes.get(1))); //Sobald das Tupel leer ist, letzte Kante aus den verbleibenden Nodes erstellen
        return graph;
    }

    /**
     * Hilfsmethode zum errechnen des Minimums von X ohne T zweier ArrayLists
     * @param x Menge an noch nicht verknüpften Knoten X
     * @param t Pruefertupel T
     * @return Gibt das Minimum aus X ohne T zurück
     */
    public static long minXwithoutT(ArrayList<Long> x, ArrayList<Long> t){
        ArrayList<Long> XwithoutT = new ArrayList<>();
        //Durchlauf aller Elemente in X und Überprüfung ob diese in T sind
        for (Long aLong : x) {
            if (!t.contains(aLong)) XwithoutT.add(aLong);
        }
        //Sortieren der Elemente nach Größe
        return XwithoutT.stream().min(Long::compareTo).get(); //Der niedrigste Wert wird zurück gegeben
    }

    /**
     * Erstellt aus einem übergebenen Baum den passenden Pruefercode
     * @param graph der übergebene Baum
     * @return Gibt den berechneten Pruefercode zurück
     */
    public static long[] fromTreeToPrueferCode(Graph graph){
        //Check ob es sich bei dem übergebenen Graphen um einen Baum handelt
        if (!treeCheck(graph)){
            throw new IllegalArgumentException("Der übergebene Graph ist kein Baum!");
        }
        //Erstellung einer Kopie des Eingabe Graphen um den Eingabe Graph unverändert zu lassen
        Graph g = Graphs.clone(graph);
        //Edge case. Graph hat NodeCount < 2
        if(g.getNodeCount() < 2){
            return new long[0];
        }
        //Edge case. Graph hat genau zwei Nodes
        if (g.getNodeCount() == 2){
            long[] result = {0};
            return result;
        }
        //Tupel mit passender Größe wird erstellt
        long[] result = new long[g.getNodeCount()-2];
        int counter = 0;

        //Liste aller Knoten im Graph mit Grad 1
        ArrayList<Node> nodesWithD1 = new ArrayList<>();
        //Loop über die Knoten im Graph wobei Knoten mit Grad 1 in nodesWithD1 hinzugefügt werden
        for (Node node: g){
            if (node.getDegree() == 1)nodesWithD1.add(node);
        }


        //Loop um die Knoten nacheinander in das Tupel einzufügen bis nur noch zwei Knoten über sind
        while (g.getNodeCount() > 2){
            //Das Minimum der Knoten mit Grad 1 wird gesucht und gespeichert
            Optional<Node> smallestNodeOptional = nodesWithD1.stream().min((Node n1, Node n2) -> (int) (Long.parseLong(n1.getId().replaceAll("[^0-9]","")) - Long.parseLong(n2.getId().replaceAll("[^0-9]",""))));
            if (smallestNodeOptional.isPresent()){
                Node smallestNode = smallestNodeOptional.get();
                Node tupelEntry;
                //Die anliegende Kante des kleinsten Knotens gibt uns den zu entfernenden Nachbarn
                //entweder ist Node0 oder Node1 der Nachbar
                if (smallestNode.getEdge(0).getNode0() == smallestNode)tupelEntry = smallestNode.getEdge(0).getNode1();
                else tupelEntry = smallestNode.getEdge(0).getNode0();
                //Der Nachbar wird eingesetzt und der kleinste Knoten wird entfernt
                result[counter] = Long.parseLong(tupelEntry.getId().replaceAll("[^0-9]",""));
                g.removeNode(smallestNode);
                nodesWithD1.remove(smallestNode);
                //Falls der Nachbar nach dem Entfernen des Blattknoten zu einem Blattknoten wird in nodesWithD1 hinzufügen
                if (tupelEntry.getDegree() == 1)nodesWithD1.add(tupelEntry);
            }
            //inkrement zum nächsten Tupel Eintrag
            counter++;
        }
        return result;
    }

    public static boolean treeCheck(Graph g){
        int nodesCounted = 0; //Knoten einer Komponente
        //Wenn zusammenhängend, findet BFS alle Knoten des Graphen
        BreadthFirstIterator bfi = new BreadthFirstIterator(g.getNode(0));

        while(bfi.hasNext()) {
            Node node = bfi.next();
            nodesCounted++;
        }
        int nodeCount = g.getNodeCount(); //alle Knoten im Graph
        if (nodeCount != nodesCounted)return false;
        if (nodeCount-1 != g.edges().count())return false;
        return true;
    }

    public static void main(String[] args) {
        System.setProperty("org.graphstream.ui", "swing");
        long[] tupel = {1,2,1,3,5,7,2};
        Graph graph = fromPrueferCodeToTree(tupel);

        graph.setAttribute("ui.stylesheet", "graph { stroke-color: white; }");

        long[] tupel2 = fromTreeToPrueferCode(graph);
        for (long l: tupel2){
            System.out.println(l);
        }

        Graph graph2 = generateTree(7);


        for (Node node: graph2){
            node.setAttribute("ui.label", node.getId());
            node.setAttribute("ui.style", "fill-color: white;");
        }
        graph2.display();

        treeEquals(graph,graph2);

        //Farben/Labels der Noten setzen
//        for (Node node: graph){
//            node.setAttribute("ui.label", node.getId());
//            node.setAttribute("ui.style", "fill-color: white;");
//        }
//        graph.display();
    }
}
