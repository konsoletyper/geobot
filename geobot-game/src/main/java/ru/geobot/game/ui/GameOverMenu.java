package ru.geobot.game.ui;

import ru.geobot.game.GeobotMainScreen;
import ru.geobot.resources.ResourceReader;

/**
 *
 * @author Alexey Andreev
 */
public class GameOverMenu extends Menu {
    private MainMenu mainMenu;
    private GeobotMainScreen mainScreen;

    public GameOverMenu(MainMenu mainMenu, GeobotMainScreen mainScreen) {
        this.mainMenu = mainMenu;
        this.mainScreen = mainScreen;
    }

    @Override
    protected int getRealWidth() {
        return Math.max(1, screenWidth / 3);
    }

    @Override
    protected int getRealHeight() {
        return Math.max(1, background.getHeight() * getRealWidth() / background.getWidth());
    }

    @Override
    public void setResourceReader(ResourceReader resourceReader) {
        GameOverMenuResources resources = resourceReader.getResourceSet(GameOverMenuResources.class);
        setBackground(resources.background());
        Button exitButton = new Button(94, 94, resources.button());
        exitButton.setClickHandler(new Runnable() {
            @Override public void run() {
                mainMenu.disableContinue();
                mainScreen.setMenu(mainMenu);
            }
        });
        addButton(exitButton);
    }
}
