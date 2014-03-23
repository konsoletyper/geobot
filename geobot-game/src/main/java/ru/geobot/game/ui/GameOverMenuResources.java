package ru.geobot.game.ui;

import ru.geobot.ResourceSet;
import ru.geobot.resources.Image;
import ru.geobot.resources.ResourcePath;

/**
 *
 * @author Alexey Andreev
 */
@ResourceSet
public interface GameOverMenuResources {
    @ResourcePath("game-over.png")
    Image background();

    @ResourcePath("game-over-button.png")
    Image button();
}
