package ru.geobot.caves;

import ru.geobot.graphics.Image;
import ru.geobot.graphics.ImagePath;
import ru.geobot.graphics.ImageSet;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
@ImageSet
public interface Cave1Images {
    @ImagePath("Bug_blank.svg")
    Image ball();

    @ImagePath("cave.png")
    Image background();
}
