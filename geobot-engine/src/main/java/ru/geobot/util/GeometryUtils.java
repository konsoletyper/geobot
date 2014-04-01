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
        PolygonNode node = queue.remove();
        result.add(node.getVertex());
        result.add(node.getPrevious().getVertex());
        result.add(node.getNext().getVertex());

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

    public static int getOrientation(List<Vertex> vertices) {
        int sum = 0;
        for (int i = 0; i < vertices.size(); ++i) {
            int j = (i + 1) % vertices.size();
            sum += vertices.get(i).crossProduct(vertices.get(j));
        }
        return sum > 0 ? 1 : sum < 0 ? -1 : 0;
    }
}
