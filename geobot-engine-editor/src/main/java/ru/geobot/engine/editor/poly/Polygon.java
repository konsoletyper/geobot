package ru.geobot.engine.editor.poly;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class Polygon {
    private List<Vertex> vertices = new ArrayList<>();
    private boolean closed;

    public List<Vertex> getVertices() {
        return vertices;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    @Override
    public Polygon clone() {
        Polygon copy = new Polygon();
        for (Vertex vertex : vertices) {
            copy.vertices.add(vertex.clone());
        }
        copy.closed = closed;
        return copy;
    }
}
