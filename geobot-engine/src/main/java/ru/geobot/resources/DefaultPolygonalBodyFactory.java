package ru.geobot.resources;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
class DefaultPolygonalBodyFactory implements PolygonalBodyFactory {
    private PolygonShape[] prototype;

    public DefaultPolygonalBodyFactory(PolygonShape[] prototype) {
        this.prototype = prototype;
    }

    @Override
    public PolygonShape[] create(float scale) {
        PolygonShape[] result = new PolygonShape[prototype.length];
        for (int i = 0; i < result.length; ++i) {
            PolygonShape copy = new PolygonShape();
            result[i] = copy;
            Vec2[] vertices = prototype[i].getVertices();
            Vec2[] verticesCopy = new Vec2[vertices.length];
            for (int j = 0; j < prototype[i].getVertexCount(); ++j) {
                verticesCopy[j] = vertices[j].mul(scale);
            }
            copy.set(verticesCopy, prototype[i].getVertexCount());
        }
        return result;
    }
}
