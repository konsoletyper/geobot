package ru.geobot.resources;

import org.jbox2d.collision.shapes.PolygonShape;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public interface PolygonalBodyFactory {
    PolygonShape[] create(float scale);
}
