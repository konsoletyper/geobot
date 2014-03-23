package ru.geobot.game.ui;

import ru.geobot.game.GeobotMainScreen;
import ru.geobot.resources.ResourceReader;

/**
 *
 * @author Alexey Andreev
 */
public class ControlsMenu extends Menu {
    private MainMenu mainMenu;
    private GeobotMainScreen mainScreen;

    public ControlsMenu(MainMenu mainMenu, GeobotMainScreen mainScreen) {
        this.mainMenu = mainMenu;
        this.mainScreen = mainScreen;
    }

    @Override
    public void setResourceReader(ResourceReader resourceReader) {
        ControlsMenuResources resources = resourceReader.getResourceSet(ControlsMenuResources.class);
        setBackground(resources.background());
        Button backButton = new Button(305, 773, resources.backButton());
        addButton(backButton);
        backButton.setClickHandler(new Runnable() {
            @Override public void run() {
                mainScreen.setMenu(mainMenu);
            }
        });
    }
}
