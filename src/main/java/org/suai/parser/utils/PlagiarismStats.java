package org.suai.parser.utils;

import com.github.javaparser.ast.Node;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class PlagiarismStats {

    private static final Set<Node> totalNodes = new HashSet<>();
    private static final Set<Node> hasTwinNodes = new HashSet<>();

    private static final PlagiarismStats SINGLETON = new PlagiarismStats();

    public long countPlagiarismPercent() {
        long totalNodesCount = totalNodes.stream().filter(Objects::nonNull).count();
        long hasTwinNodesCount = hasTwinNodes.stream().filter(Objects::nonNull).count();
        return (long)(100 * (((double)hasTwinNodesCount / totalNodesCount)));
    }

    public void addNode(Node node) {
        totalNodes.add(node);
    }

    public void addHasTwin(Node node) {
        hasTwinNodes.add(node);
    }

    public void refreshStats() {
        totalNodes.clear();
        hasTwinNodes.clear();
    }

    private PlagiarismStats() { }

    public static PlagiarismStats getInstance() {
        return SINGLETON;
    }
}
