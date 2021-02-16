

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;

public class Dot {
    public HashSet<String> nodes = new HashSet<String>();
    public HashMap<String, HashSet<String>> clusters = new HashMap<String, HashSet<String>>(); // no nesting or overlap allowed
    public HashSet<Edge> edges = new HashSet<Edge>();

    public HashMap<String, String> graphAttributes = new HashMap<String, String>();
    public HashMap<String, HashMap<String, String>> nodeAttributes = new HashMap<String, HashMap<String, String>>();
    public HashMap<Edge, HashMap<String, String>> edgeAttributes = new HashMap<Edge, HashMap<String, String>>();
    public HashMap<String, HashMap<String, String>> clusterAttributes = new HashMap<String, HashMap<String, String>>();

    public static class Edge {
        String src;
        String dst;

        public Edge(String src, String dst) {
            this.src = src;
            this.dst = dst;
        }

        @Override
        public boolean equals(java.lang.Object other) {
            if (other == null) {
                return false;
            }
            if (!(other instanceof Edge)) {
                return false;
            }
            Edge otherEdge = (Edge)other;
            return (this.src.equals(otherEdge.src) && this.dst.equals(otherEdge.dst));
        }

        @Override
        public int hashCode() {
            int res = 1;
            res = 31*res + (src==null ? 0 : src.hashCode());
            res = 31*res + (dst==null ? 0 : dst.hashCode());
            return res;
        }
    }

    public void addNode(String node) {
        nodes.add(node);
    }
    public void addEdge(String src, String dst) {
        nodes.add(src);
        nodes.add(dst);
        edges.add(new Edge(src, dst));
    }

    public void addCluster(String cluster) {
        if (!clusters.containsKey(cluster)) {
            clusters.put(cluster, new HashSet<String>());
        }
    }

    public void addClusterNode(String cluster, String node) {
        // TODO vs translate nodename "a.b" into "a{b}"?
        for (var otherCluster : clusters.keySet()) {
            if (!otherCluster.equals(cluster)) {
                if (clusters.get(otherCluster).contains(node)) {
                    throw new RuntimeException("TODO" + " " + cluster + " " + node + " " + otherCluster);
                }
            }
        }
        addCluster(cluster);
        nodes.add(node);
        clusters.get(cluster).add(node);
    }

    public void putGraphAttribute(String key, String value) {
        graphAttributes.put(key, value);
    }

    public void putNodeAttribute(String node, String key, String value) {
        addNode(node);
        if (!nodeAttributes.containsKey(node)) {
            nodeAttributes.put(node, new HashMap<String, String>());
        }
        nodeAttributes.get(node).put(key, value);
    }


    public void putEdgeAttribute(String src, String dst, String key, String value) {
        addEdge(src, dst);
        var edge = new Edge(src, dst);
        if (!edgeAttributes.containsKey(edge)) {
            edgeAttributes.put(edge, new HashMap<String, String>());
        }
        edgeAttributes.get(edge).put(key, value);
    }

    public void putClusterAttribute(String cluster, String key, String value) {
        addCluster(cluster);
        if (!clusterAttributes.containsKey(cluster)) {
            clusterAttributes.put(cluster, new HashMap<String, String>());
        }
        clusterAttributes.get(cluster).put(key, value);
    }

    public void writeFile(Path path) throws IOException {
        try (BufferedWriter br = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            var out = new PrintWriter(br);

            out.println("digraph {");

            out.println(String.format("graph [%s];", Dot.getAttributeString(this.graphAttributes)));

            var clusteredNodes = new HashSet<String>();
            for (var clusterSet : this.clusters.values()) {
                clusteredNodes.addAll(clusterSet);
            }


            out.println();

            for (var cluster : this.clusters.keySet()) {

                out.println(String.format("subgraph cluster_%s {", cluster));

                var clusterAttributes = this.clusterAttributes.getOrDefault(cluster, null);
                var clusterAttributeString = Dot.getAttributeString(clusterAttributes);
                out.println(String.format("  graph [%s];", clusterAttributeString));

                for (var node : this.clusters.get(cluster)) {
                    var attributes = this.nodeAttributes.getOrDefault(node, null);
                    var attributeString = Dot.getAttributeString(attributes);
                    out.println(String.format("  %s [%s];", node, attributeString));
                }
                out.println("}");
            }

            out.println();

            for (var node : this.nodes) {
                if (clusteredNodes.contains(node)) {
                    continue;
                }
                var attributes = this.nodeAttributes.getOrDefault(node, null);
                var attributeString = Dot.getAttributeString(attributes);
                out.println(String.format("%s [%s];", node, attributeString));
            }

            out.println();

            for (var edge : this.edges) {
                var attributes = this.edgeAttributes.getOrDefault(edge, null);
                var attributeString = Dot.getAttributeString(attributes);
                out.println(String.format("%s -> %s [%s];", edge.src, edge.dst, attributeString));
            }
            out.println("}");
        }
    }

    public static String getAttributeString(HashMap<String, String> attributes) {
        if (attributes == null) {
            return "";
        }
        var sb = new StringBuilder();
        int i = 0;
        for (var entry : attributes.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();

            if (i != 0) {
                sb.append(", ");
            }

            sb.append(key);
            sb.append("=");
            sb.append('"');
            sb.append(value);
            sb.append('"');
            ++i;
        }
        return sb.toString();
    }
}
