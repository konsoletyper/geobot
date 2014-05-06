package ru.geobot.util;

import java.util.*;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class GeometryUtils {
    public static List<List<Vertex>> triangulate(List<Vertex> vertices) {
        int orientation = getOrientation(vertices);
        if (orientation < 0) {
            vertices = new ArrayList<>(vertices);
            Collections.reverse(vertices);
        }

        PolygonNode firstNode = PolygonNode.create(vertices);
        PriorityQueue<PolygonNode> queue = new PriorityQueue<>(vertices.size(), new Comparator<PolygonNode>() {
            @Override public int compare(PolygonNode o1, PolygonNode o2) {
                return Float.compare(o2.getAngle(), o1.getAngle());
            }
        });
        for (PolygonNode node = firstNode; node.getNext() != firstNode; node = node.getNext()) {
            node.update();
            if (node.isEar()) {
                queue.add(node);
            }
        }
        List<List<Vertex>> result = new ArrayList<>();

        int count = vertices.size();
        List<Edge> edges = new ArrayList<>();
        List<Edge> outerEdges = new ArrayList<>();
        for (Edge iter = firstNode.nextEdge; iter.next != firstNode.nextEdge; iter = iter.next) {
            outerEdges.add(iter);
        }
        while (count > 3) {
           PolygonNode node = queue.remove();
           PolygonNode next = node.getNext();
           PolygonNode previous = node.getPrevious();
           if (next.isEar()) {
               queue.remove(next);
           }
           if (previous.isEar()) {
               queue.remove(previous);
           }
           Edge cutEdge = node.cut();
           edges.add(cutEdge);
           edges.add(cutEdge.opposite);
           --count;
           updateNodes(queue, next, previous);
        }
        outerEdges.addAll(edges);

        for (Edge edge : edges) {
            if (edge.isDestroyed() || edge.opposite == null) {
                continue;
            }
            Edge iter = edge.next;
            List<Vertex> piece = new ArrayList<>();
            while (iter != edge) {
                piece.add(iter.first);
                iter = iter.next;
            }
            iter = edge.opposite.next;
            while (iter != edge.opposite) {
                piece.add(iter.first);
                iter = iter.next;
            }
            if (isConvex(piece)) {
                edge.merge();
            }
        }

        for (Edge edge : outerEdges) {
            if (edge.isDestroyed()) {
                continue;
            }
            List<Vertex> piece = new ArrayList<>();
            Edge iter = edge;
            do {
                piece.add(iter.first);
                iter = iter.next;
                iter.previous.destroy();
            } while (iter != edge);
            result.add(piece);
        }

        return result;
    }

    private static void updateNodes(PriorityQueue<PolygonNode> queue, PolygonNode... nodes) {
        for (PolygonNode node : nodes) {
            node.update();
            if (node.isEar()) {
                queue.add(node);
            }
        }
    }

    public static boolean isConvex(List<Vertex> vertices) {
        int dir = 0;
        for (int i = 0; i < vertices.size(); ++i) {
            int j = (i + 1) % vertices.size();
            int k = (j + 1) % vertices.size();
            Vertex a = vertices.get(j).subtract(vertices.get(i));
            Vertex b = vertices.get(k).subtract(vertices.get(j));
            int nextDir = Integer.signum(a.crossProduct(b));
            if (nextDir == 0) {
                continue;
            }
            if (dir != 0 && dir != nextDir) {
                return false;
            }
            dir = nextDir;
        }
        return true;
    }

    public static int getOrientation(List<Vertex> vertices) {
        int sum = 0;
        for (int i = 0; i < vertices.size(); ++i) {
            int j = (i + 1) % vertices.size();
            sum += vertices.get(i).crossProduct(vertices.get(j));
        }
        return Integer.signum(sum);
    }
}
