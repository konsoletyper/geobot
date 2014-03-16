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
public interface GunResources {
    @ResourcePath("gun.png")
    Image image();

    @ResourcePath("gun-shape.txt")
    PolygonalBodyFactory shape();

    @ResourcePath("bullet.png")
    Image bulletImage();
}
