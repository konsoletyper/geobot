package ru.geobot.util;

import java.util.List;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class GeometryUtils {
    public static int[] triangulate(List<Vertex> vertices) {
        PolygonNode node = PolygonNode.create(vertices);
        int[] result = new int[(vertices.size() - 2) * 3];
        int j = 0;
        for (int i = 2; i < vertices.size(); ++i) {
            while (!node.canCut()) {
                node = node.getNext();
            }
            result[j++] = node.getPrevious().getIndex();
            result[j++] = node.getIndex();
            result[j++] = node.getNext().getIndex();
            node = node.delete();
        }
        return result;
    }
}
