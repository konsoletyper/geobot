package ru.geobot.game.ui;

import ru.geobot.ResourceSet;
import ru.geobot.resources.Image;
import ru.geobot.resources.ResourcePath;

/**
 *
 * @author Alexey Andreev
 */
@ResourceSet
public interface ControlsMenuResources {
    @ResourcePath("controls-menu.png")
    Image background();

    @ResourcePath("back-button.png")
    Image backButton();
}
