package ru.geobot.game;

import ru.geobot.ResourceSet;
import ru.geobot.resources.Image;
import ru.geobot.resources.ResourcePath;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
@ResourceSet
public interface GameResources {
    @ResourcePath("main-screen.png")
    Image background();

    @ResourcePath("menu-button-default.png")
    Image menuButton();

    @ResourcePath("menu-button.png")
    Image menuButtonHover();
}
