package ru.geobot.game.caves;

import ru.geobot.ResourceSet;
import ru.geobot.resources.Image;
import ru.geobot.resources.PolygonalBodyFactory;
import ru.geobot.resources.ResourcePath;
import ru.geobot.resources.Large;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
@ResourceSet
public interface Cave1Resources {
    @ResourcePath("cave.png")
    @Large
    Image background();

    @ResourcePath("cave-without-column.png")
    @Large
    Image backgroundWithoutColumn();

    @ResourcePath("cave1-shape.txt")
    PolygonalBodyFactory shape();

    @ResourcePath("column.txt")
    PolygonalBodyFactory columnShape();
}
