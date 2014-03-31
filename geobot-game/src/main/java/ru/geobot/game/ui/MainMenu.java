package ru.geobot.game.ui;

import ru.geobot.EntryPointCallback;
import ru.geobot.game.GeobotEntryPoint;
import ru.geobot.game.GeobotMainScreen;
import ru.geobot.game.caves.Cave1Game;
import ru.geobot.resources.ResourceReader;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class MainMenu extends Menu {
    private GeobotMainScreen menuScreen;
    private ControlsMenu controlsMenu;
    private EntryPointCallback callback;
    private Button continueButton;

    public MainMenu(GeobotMainScreen menuScreen) {
        this.menuScreen = menuScreen;
        controlsMenu = new ControlsMenu(this, menuScreen);
    }

    @Override
    public void setResourceReader(ResourceReader resourceReader) {
        MainMenuResources resources = resourceReader.getResourceSet(MainMenuResources.class);
        setBackground(resources.background());
        Button startButton = new Button(182, 142, resources.startButton());
        startButton.setClickHandler(new Runnable() {
            @Override public void run() {
                startGame();
            }
        });
        addButton(startButton);
        continueButton = new Button(182, 344, resources.continueButton());
        continueButton.setEnabled(false);
        continueButton.setClickHandler(new Runnable() {
            @Override public void run() {
                menuScreen.showGame();
            }
        });
        addButton(continueButton);
        Button controlsButton = new Button(186, 546, resources.controlsButton());
        controlsButton.setClickHandler(new Runnable() {
            @Override public void run() {
                menuScreen.setMenu(controlsMenu);
            }
        });
        addButton(controlsButton);
        Button exitButton = new Button(301, 749, resources.exitButton());
        exitButton.setClickHandler(new Runnable() {
            @Override public void run() {
                callback.stop();
            }
        });
        addButton(exitButton);
        controlsMenu.setResourceReader(resourceReader);
    }

    @Override
    public void start(EntryPointCallback callback) {
        this.callback = callback;
    }

    private void startGame() {
        GeobotEntryPoint entryPoint = new GeobotEntryPoint();
        entryPoint.setGame(new Cave1Game(entryPoint));
        menuScreen.setInner(entryPoint);
        menuScreen.showGame();
        continueButton.setEnabled(true);
    }

    public void disableContinue() {
        continueButton.setEnabled(false);
    }
}
