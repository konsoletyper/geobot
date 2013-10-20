package ru.geobot.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
class PolygonNode {
    private Vertex vertex;
    private int index;
    private PolygonNode next;
    private PolygonNode previous;

    private PolygonNode(Vertex vertex, int index) {
        this.vertex = vertex;
        this.index = index;
    }

    public PolygonNode delete() {
        if (next == this) {
            return null;
        }
        PolygonNode next = this.next;
        PolygonNode previous = this.previous;
        next.previous = previous;
        previous.next = next;
        return previous;
    }

    public int getIndex() {
        return index;
    }

    public PolygonNode getNext() {
        return next;
    }

    public PolygonNode getPrevious() {
        return previous;
    }

    public Vertex getVertex() {
        return vertex;
    }

    public static PolygonNode create(List<Vertex> vertices) {
        List<PolygonNode> nodes = new ArrayList<>();
        for (int i = 0; i < vertices.size(); ++i) {
            nodes.add(new PolygonNode(vertices.get(i), i));
        }
        for (int i = 0; i < vertices.size(); ++i) {
            PolygonNode a = nodes.get(i);
            PolygonNode b = nodes.get((i + 1) % vertices.size());
            a.next = b;
            b.previous = a;
        }
        return nodes.get(0);
    }

    public boolean canCut() {
        PolygonNode testNode = getNext().getNext();
        List<Vertex> triangle = Arrays.asList(getPrevious().getVertex(), getVertex(), getNext().getVertex());
        while (testNode != getPrevious()) {
            if (testNode.getVertex().isInsideConvexPolygon(triangle)) {
                return false;
            }
            testNode = testNode.getNext();
        }
        return true;
    }
}
