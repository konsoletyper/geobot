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
    private float angle;
    private boolean ear;
    private PolygonNode next;
    private PolygonNode previous;
    Edge nextEdge;
    Edge previousEdge;

    private PolygonNode(Vertex vertex) {
        this.vertex = vertex;
    }

    public Edge cut() {
        if (next == this) {
            return null;
        }
        Edge edge = new Edge();
        edge.next = next.nextEdge;
        edge.previous = previous.previousEdge;
        edge.first = previous.vertex;
        edge.second = next.vertex;
        Edge cutEdge = new Edge();
        cutEdge.next = previous.nextEdge;
        cutEdge.previous = next.previousEdge;
        cutEdge.first = next.vertex;
        cutEdge.second = previous.vertex;
        cutEdge.opposite = edge;
        edge.opposite = cutEdge;
        edge.previous.next = edge;
        edge.next.previous = edge;
        cutEdge.previous.next = cutEdge;
        cutEdge.next.previous = cutEdge;
        PolygonNode next = this.next;
        PolygonNode previous = this.previous;
        next.previous = previous;
        next.previousEdge = edge;
        previous.next = next;
        previous.nextEdge = edge;
        return cutEdge;
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

    public float getAngle() {
        return angle;
    }

    public boolean isEar() {
        return ear;
    }

    public static PolygonNode create(List<Vertex> vertices) {
        List<PolygonNode> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < vertices.size(); ++i) {
            nodes.add(new PolygonNode(vertices.get(i)));
            edges.add(new Edge());
        }
        for (int i = 0; i < vertices.size(); ++i) {
            PolygonNode a = nodes.get(i);
            PolygonNode b = nodes.get((i + 1) % vertices.size());
            a.next = b;
            b.previous = a;
            Edge p = edges.get(i);
            Edge q = edges.get((i + 1) % vertices.size());
            p.next = q;
            q.previous = p;
            p.first = a.vertex;
            p.second = b.vertex;
            a.nextEdge = p;
            b.previousEdge = p;
        }
        return nodes.get(0);
    }

    public void setVertex(Vertex vertex) {
        this.vertex = vertex;
        update();
    }

    public void update() {
        Vertex a = getVertex().subtract(getPrevious().getVertex());
        Vertex b = getNext().getVertex().subtract(getVertex());
        ear = a.crossProduct(b) > 0 && canCut();
        if (ear) {
            a = getPrevious().getVertex().subtract(getVertex());
            angle = a.dotProduct(b) / (float)Math.sqrt((long)a.dotProduct(a) * b.dotProduct(b));
        } else {
            angle = -2;
        }
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
