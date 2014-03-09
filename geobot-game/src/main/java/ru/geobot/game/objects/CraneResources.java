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
public interface CraneResources {
    @ResourcePath("crane-platform.png")
    Image platform();

    @ResourcePath("crane.png")
    Image crane();

    @ResourcePath("crane-hanger.png")
    Image hanger();

    @ResourcePath("crane-hanger-shape.txt")
    PolygonalBodyFactory hangerShape();

    @ResourcePath("cable.png")
    Image cable();
}
