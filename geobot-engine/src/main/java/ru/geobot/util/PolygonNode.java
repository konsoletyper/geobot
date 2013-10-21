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

    private PolygonNode(Vertex vertex) {
        this.vertex = vertex;
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
        for (int i = 0; i < vertices.size(); ++i) {
            nodes.add(new PolygonNode(vertices.get(i)));
        }
        for (int i = 0; i < vertices.size(); ++i) {
            PolygonNode a = nodes.get(i);
            PolygonNode b = nodes.get((i + 1) % vertices.size());
            a.next = b;
            b.previous = a;
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
