package ru.geobot.game.objects;

import ru.geobot.ResourceSet;
import ru.geobot.resources.Image;
import ru.geobot.resources.PolygonalBodyFactory;
import ru.geobot.resources.ResourcePath;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
@ResourceSet
public interface BobblerResources {
    @ResourcePath("bobbler.png")
    Image image();

    @ResourcePath("bobbler-shape.txt")
    PolygonalBodyFactory shape();

    @ResourcePath("bobbler-load-shape.txt")
    PolygonalBodyFactory loadShape();
}
