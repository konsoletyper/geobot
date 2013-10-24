package ru.geobot.game.objects;

import ru.geobot.ResourceSet;
import ru.geobot.resources.Image;
import ru.geobot.resources.ResourcePath;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
@ResourceSet
public interface RopeImages {
    @ResourcePath("rope_segment.png")
    Image texture();
}
