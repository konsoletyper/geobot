package ru.geobot.game.ui;

import ru.geobot.ResourceSet;
import ru.geobot.resources.Image;
import ru.geobot.resources.ResourcePath;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
@ResourceSet
public interface MainMenuResources {
    @ResourcePath("main-menu.png")
    Image background();

    @ResourcePath("start-game-hover.png")
    Image startButton();

    @ResourcePath("continue-game-hover.png")
    Image continueButton();

    @ResourcePath("controls-hover.png")
    Image controlsButton();

    @ResourcePath("exit.png")
    Image exitButton();
}
