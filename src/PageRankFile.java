import java.util.*;
import java.io.*;
import ir.utilities.*;
import ir.webutils.Graph;
import ir.webutils.Node;

public class PageRankFile {
    private HashMap<String, Double> scores;

    public HashMap<String, Double> getScores() {
        return scores;
    }

    public void run(Graph graph, double alpha, int iterations) {
        scores = new HashMap<>();
        Node[] nodes = graph.nodeArray();
        HashMap indexMap = new HashMap((int)1.4*nodes.length);
        double[] r = new double[nodes.length];
        double[] rp = new double[nodes.length];
        double[] e = new double[nodes.length];
        for(int i = 0; i < nodes.length; i++) {
            indexMap.put(nodes[i].toString(), new Integer(i));
//            System.out.print(nodes[i] + " ");
            r[i] = 1.0/nodes.length;
            e[i] = alpha/nodes.length;
        }
//        System.out.print("\nR = ");
//        MoreMath.printVector(r);
//        System.out.print("\nE = ");
//        MoreMath.printVector(e);
        for(int j = 0; j < iterations; j++) {
//            System.out.println("\nIteration " + (j+1) + ":");
            for(int i = 0; i < nodes.length; i++) {
                ArrayList inNodes = nodes[i].getEdgesIn();
                rp[i] = 0;
                for(int k = 0; k < inNodes.size(); k++) {
                    Node inNode = (Node)inNodes.get(k);
                    String inName = inNode.toString();
                    int fanOut = inNode.getEdgesOut().size();
                    rp[i] =  rp[i] + r[((Integer)indexMap.get(inName)).intValue()]/fanOut;
                }
                rp[i] = rp[i] + e[i];
            }
//            System.out.println("R' = ");
//            MoreMath.printVector(rp);
            for(int i = 0; i < r.length; i++)
                r[i] = rp[i];
            normalize(r);
//            System.out.println("\nNorm R = ");
//            MoreMath.printVector(r);
//            System.out.println("");
        }
        for (int i = 0; i < nodes.length; i++) {
            scores.put(nodes[i].toString(), r[i]);
        }
    }

    public static void normalize(double[] x) {
        double length = MoreMath.vectorOneNorm(x);
        for(int i = 0; i < x.length; i++)
            x[i] = x[i]/length;
    }


    public void main(String[] args) throws IOException{
        Graph graph = new Graph();
        graph.readFromFile(args[0]);
        run(graph, Double.parseDouble(args[1]), Integer.parseInt(args[2]));
    }
}
