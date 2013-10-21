package ru.geobot.util.tests;

import java.util.Arrays;
import java.util.List;
import org.junit.Ignore;
import org.junit.Test;
import ru.geobot.util.GeometryUtils;
import ru.geobot.util.Vertex;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class GeometryTest {
    @Test
    @Ignore
    public void triangulates() {
        List<Vertex> vertices = Arrays.asList(new Vertex(0, 100), new Vertex(200, 200), new Vertex(400, 100),
                new Vertex(200, 0));
        List<Vertex> triangles = GeometryUtils.triangulate(vertices);
        System.out.println(triangles);
    }

    @Test
    public void triangulatesLong() {
        List<Vertex> vertices = Arrays.asList(new Vertex(0, 0), new Vertex(0, 100), new Vertex(400, 100),
                new Vertex(400, 0));
        List<Vertex> triangles = GeometryUtils.triangulate(vertices);
        System.out.println(triangles);
    }
}
