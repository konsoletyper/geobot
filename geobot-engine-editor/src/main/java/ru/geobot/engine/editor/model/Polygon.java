package ru.geobot.engine.editor.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class Polygon {
    private List<Vertex> vertices = new ArrayList<>();

    public List<Vertex> getVertices() {
        return vertices;
    }

    @Override
    public Polygon clone() {
        Polygon copy = new Polygon();
        for (Vertex vertex : vertices) {
            copy.vertices.add(vertex.clone());
        }
        return copy;
    }
}
