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
public interface ObjectResources {
    @ResourcePath("bucket1.png")
    Image bucketImage();

    @ResourcePath("bucket1-shape.txt")
    PolygonalBodyFactory bucketShape();

    @ResourcePath("bucket2.png")
    Image bucketOnRopeImage();

    @ResourcePath("bucket2-shape.txt")
    PolygonalBodyFactory bucketOnRopeShape();

    @ResourcePath("bucket2-clickable-shape.txt")
    PolygonalBodyFactory bucketOnRopeClickableShape();

    @ResourcePath("pick.png")
    Image pickImage();

    @ResourcePath("pick-shape.txt")
    PolygonalBodyFactory pickShape();
}
