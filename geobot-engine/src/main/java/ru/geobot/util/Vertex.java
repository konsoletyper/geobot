package ru.geobot.util;

import java.util.List;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class Vertex {
    public int x;
    public int y;

    public Vertex(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vertex() {
    }

    @Override
    public Vertex clone() {
        return new Vertex(x, y);
    }

    public Vertex add(Vertex other) {
        return new Vertex(x + other.x, y + other.y);
    }

    public Vertex subtract(Vertex other) {
        return new Vertex(x - other.x, y - other.y);
    }

    public Vertex negate() {
        return new Vertex(-x, -y);
    }

    public Vertex multiply(int quotient) {
        return new Vertex(quotient * x, quotient * y);
    }

    public int dotProduct(Vertex other) {
        return x * other.x + y * other.y;
    }

    public int crossProduct(Vertex other) {
        return x * other.y - y * other.x;
    }

    public boolean isInsideConvexPolygon(List<Vertex> vertices) {
        int lastSign = 0;
        for (int i = 0; i < vertices.size(); ++i) {
            int j = (i + 1) % vertices.size();
            Vertex a = vertices.get(i).subtract(this);
            Vertex b = vertices.get(j).subtract(this);
            int sign = signum(b.crossProduct(a));
            if (lastSign != 0 && lastSign != sign) {
                return false;
            }
            lastSign = sign;
        }
        return true;
    }

    private static int signum(int num) {
        return num > 0 ? 1 : num < 0 ? -1 : 0;
    }
}
