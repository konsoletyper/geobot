package ru.geobot.teavm;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import ru.geobot.resources.PolygonalBodyFactory;

/**
 *
 * @author Alexey Andreev
 */
public class HtmlPolygonalBodyFactory implements PolygonalBodyFactory {
    private float[][] prototype;

    public HtmlPolygonalBodyFactory(float[][] prototype) {
        this.prototype = prototype;
    }

    @Override
    public PolygonShape[] create(float scale) {
        PolygonShape[] result = new PolygonShape[prototype.length];
        for (int i = 0; i < result.length; ++i) {
            PolygonShape copy = new PolygonShape();
            result[i] = copy;
            float[] coords = prototype[i];
            Vec2[] verticesCopy = new Vec2[coords.length / 2];
            for (int j = 0; j < verticesCopy.length; ++j) {
                verticesCopy[j] = new Vec2(coords[j * 2], coords[j * 2 + 1]).mul(scale);
            }
            copy.set(verticesCopy, verticesCopy.length);
        }
        return result;
    }
}
