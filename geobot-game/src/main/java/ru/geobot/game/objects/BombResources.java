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
public interface BombResources {
    @ResourcePath("bomb.png")
    Image image();

    @ResourcePath("bomb-inactive.png")
    Image inactiveImage();

    @ResourcePath("bomb-shape.txt")
    PolygonalBodyFactory shape();
}
