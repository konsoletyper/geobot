package ru.geobot.caves;

import ru.geobot.graphics.Image;
import ru.geobot.graphics.ImagePath;
import ru.geobot.graphics.ResourceSet;
import ru.geobot.graphics.Large;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
@ResourceSet
public interface Cave1Images {
    @ImagePath("Bug_blank.svg")
    Image ball();

    @ImagePath("cave.png")
    @Large
    Image background();
}
