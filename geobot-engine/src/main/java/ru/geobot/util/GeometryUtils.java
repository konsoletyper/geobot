package ru.geobot.util;

import java.util.*;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class GeometryUtils {
    public static List<Vertex> triangulate(List<Vertex> vertices) {
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
        List<Vertex> result = new ArrayList<>();

        int count = vertices.size();
        while (count > 3) {
           PolygonNode node = queue.remove();
           Vertex a = node.getNext().getVertex().subtract(node.getVertex());
           Vertex b = node.getPrevious().getVertex().subtract(node.getVertex());
           float alen = (float)Math.sqrt(a.dotProduct(a));
           float blen = (float)Math.sqrt(b.dotProduct(b));
           if (alen >= blen * 8 && blen > 20) {
               queue.remove(node.getPrevious());
               int quotient = (int)Math.ceil(alen / blen);
               Vertex v = node.getVertex().add(divide(a, quotient));
               result.add(node.getVertex());
               result.add(v);
               result.add(node.getPrevious().getVertex());
               node.setVertex(v);
               updateNodes(queue, node, node.getPrevious());
           } else if (blen >= alen * 8 && alen > 20) {
               queue.remove(node.getNext());
               int quotient = (int)Math.ceil(blen / alen);
               Vertex v = node.getVertex().add(divide(b, quotient));
               result.add(node.getVertex());
               result.add(v);
               result.add(node.getNext().getVertex());
               node.setVertex(v);
               updateNodes(queue, node, node.getNext());
           } else {
               PolygonNode next = node.getNext();
               PolygonNode previous = node.getPrevious();
               result.add(node.getVertex());
               result.add(node.getPrevious().getVertex());
               result.add(node.getNext().getVertex());
               queue.remove(next);
               queue.remove(previous);
               node.delete();
               --count;
               updateNodes(queue, next, previous);
           }
        }
        PolygonNode node = queue.remove();
        result.add(node.getVertex());
        result.add(node.getPrevious().getVertex());
        result.add(node.getNext().getVertex());

        return result;
    }

    private static Vertex divide(Vertex v, int quotient) {
        return new Vertex(Math.round(v.x / (float)quotient), Math.round(v.y / (float)quotient));
    }

    private static void updateNodes(PriorityQueue<PolygonNode> queue, PolygonNode... nodes) {
        for (PolygonNode node : nodes) {
            node.update();
            if (node.isEar()) {
                queue.add(node);
            }
        }
    }

    public static int getOrientation(List<Vertex> vertices) {
        int sum = 0;
        for (int i = 0; i < vertices.size(); ++i) {
            int j = (i + 1) % vertices.size();
            sum += vertices.get(i).crossProduct(vertices.get(j));
        }
        return sum > 0 ? 1 : sum < 0 ? -1 : 0;
    }
}
