import ir.utilities.MoreMath;
import ir.utilities.MoreString;
import ir.webutils.*;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class PageRankSpider extends Spider {
    private HashMap<Link, List<String>> namesOfPagesWithLink;
    private HashMap<Link, String> nameByLink;
    private HashMap<String, List<String>> linkStructure;
    private Graph graph;
    private HashMap<String, Double> scores;

    public PageRankSpider() {
        super();
    }

    public static void main(String args[]) { new PageRankSpider().go(args); }

    @Override
    public void go(String[] args) {
        super.go(args);
        calcPageRank();
    }

    private void calcPageRank() {
        System.out.println("Loading graph...");
        loadGraph();
        graph.print();
        PageRankFile alg = new PageRankFile();
        System.out.println("Calculating PageRank...");
        alg.run(graph, 0.15, 50);
        scores = alg.getScores();
        printScores();
        writeScoresToFile();
    }

    private void loadGraph() {
        graph = new Graph();
        try {
            graph.readFromFile(Paths.get(saveDir.getName(), "structure.txt").toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printScores() {
        for (Map.Entry entry: scores.entrySet()) {
            System.out.println(String.format("PR(%s) %f", entry.getKey(), entry.getValue()));
        }
    }

    @Override
    public void doCrawl() {
        namesOfPagesWithLink = new HashMap<>();
        nameByLink = new HashMap();
        super.doCrawl();
        removeNotIndexedLinksFromGraph();
        saveGraphAsFile();
    }

    private void removeNotIndexedLinksFromGraph() {
        Set linksToRemove = new HashSet(namesOfPagesWithLink.keySet());
        linksToRemove.removeAll(visited);
        namesOfPagesWithLink.keySet().removeAll(linksToRemove);
    }

    private void saveGraphAsFile() {
        createLinkStructure();
        writeStructureToFile();
    }

    private void createLinkStructure() {
        this.linkStructure = new HashMap<>();
        Iterator iter = visited.iterator();
        while (iter.hasNext()) {
            Link link = (Link) iter.next();
            String pageName = nameByLink.get(link);
            List outLinks = namesOfPagesWithLink.entrySet()
                    .stream()
                    .filter(map -> map.getValue().contains(pageName))
                    .map(k -> nameByLink.get(k.getKey()))
                    .collect(Collectors.toList());
            linkStructure.put(pageName, outLinks);
        }
    }

    private void writeStructureToFile() {
        try {
            PrintWriter out = new PrintWriter(new FileWriter(new File(saveDir, "structure.txt")));
            for (Map.Entry entry: linkStructure.entrySet()) {
                String linksText = ((List<String>) entry.getValue())
                        .stream()
                        .collect(Collectors.joining(" "));
                out.printf("%s %s\n", entry.getKey(), linksText);
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeScoresToFile() {
        try {
            PrintWriter out = new PrintWriter(new FileWriter(new File(saveDir, "pageRank.txt")));
            for (Map.Entry entry: scores.entrySet()) {
                out.printf("%s.html %f\n", entry.getKey(), entry.getValue());
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void processPage(HTMLPage page) {
        // Extract links in order to change them to absolute URL's
        List<Link> outLinks =  new LinkExtractor(page).extractLinks();
        String name = "P" +  MoreString.padWithZeros(count,(int)Math.floor(MoreMath.log(maxCount, 10)) + 1);
        page.writeAbsolute(saveDir, name);
        nameByLink.put(page.getLink(), name);
        for (Link link: outLinks) {
            namesOfPagesWithLink.computeIfAbsent(link, k -> new ArrayList()).add(name);
        }
    }
}
