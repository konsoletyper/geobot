package ru.geobot.game.objects;

import ru.geobot.ResourceSet;
import ru.geobot.resources.Image;
import ru.geobot.resources.ResourcePath;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
@ResourceSet
public interface SafeResources {
    @ResourcePath("safe-open.png")
    Image opened();

    @ResourcePath("safe-closed.png")
    Image closed();

    @ResourcePath("safe-door1.png")
    Image door1();

    @ResourcePath("safe-door2.png")
    Image door2();
}
