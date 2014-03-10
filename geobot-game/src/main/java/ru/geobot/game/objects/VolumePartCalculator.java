package ru.geobot.game.objects;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class VolumePartCalculator {
    public static PolygonShape calculate(PolygonShape shape, Transform transform, float threshold) {
        Vec2[] vertices = getVertices(shape, transform);
        int left = findBottomVertex(vertices);
        int right = left;
        if (vertices[left].y > threshold) {
            return null;
        }
        while (true) {
            int leftNext = left > 0 ? left - 1 : vertices.length - 1;
            int rightNext = (right + 1) % vertices.length;
            float leftY = vertices[leftNext].y;
            float rightY = vertices[rightNext].y;
            float minY = Math.min(leftY, rightY);
            if (minY >= threshold) {
                break;
            }
            if (leftY < rightY) {
                left = leftNext;
            } else {
                right = rightNext;
            }
            if (left == right) {
                PolygonShape result = new PolygonShape();
                result.set(vertices, vertices.length);
                return result;
            }
        }

        int leftNext = left > 0 ? left - 1 : vertices.length - 1;
        int rightNext = (right + 1) % vertices.length;
        int count = right >= left ? right - left : right - left + vertices.length;
        ++count;
        Vec2[] copy = new Vec2[count + 2];
        copy[0] = intersection(vertices[left], vertices[leftNext], threshold);
        for (int i = 1; i <= count; ++i) {
            copy[i] = vertices[left];
            left = (left + 1) % vertices.length;
        }
        copy[copy.length - 1] = intersection(vertices[right], vertices[rightNext], threshold);
        PolygonShape result = new PolygonShape();
        result.set(copy, copy.length);
        return result;
    }

    private static Vec2[] getVertices(PolygonShape shape, Transform transform) {
        Vec2[] vertices = new Vec2[shape.getVertexCount()];
        for (int i = 0; i < shape.getVertexCount(); ++i) {
            vertices[i] = Transform.mul(transform, shape.getVertex(i));
        }
        return vertices;
    }

    private static int findBottomVertex(Vec2[] vertices) {
        int index = 0;
        float y = vertices[0].y;
        for (int i = 1; i < vertices.length; ++i) {
            if (vertices[i].y < y) {
                y = vertices[i].y;
                index = i;
            }
        }
        return index;
    }

    private static Vec2 intersection(Vec2 a, Vec2 b, float threshold) {
        if (b.y - a.y < 1E-10) {
            return b;
        }
        float x = a.x + (b.x - a.x) * (threshold - a.y) / (b.y - a.y);
        return new Vec2(x, threshold);
    }
}
