package ru.geobot.game.ui;

import ru.geobot.EntryPointCallback;
import ru.geobot.game.GeobotEntryPoint;
import ru.geobot.game.GeobotMenu;
import ru.geobot.game.caves.Cave2Game;
import ru.geobot.resources.ResourceReader;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class MainMenu extends Menu {
    private GeobotMenu menuScreen;
    private EntryPointCallback callback;

    public MainMenu(GeobotMenu menuScreen) {
        this.menuScreen = menuScreen;
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
        Button continueButton = new Button(182, 344, resources.continueButton());
        continueButton.setClickHandler(new Runnable() {
            @Override public void run() {
                menuScreen.showGame();
            }
        });
        addButton(continueButton);
        addButton(new Button(186, 546, resources.controlsButton()));
        Button exitButton = new Button(301, 749, resources.exitButton());
        exitButton.setClickHandler(new Runnable() {
            @Override public void run() {
                callback.stop();
            }
        });
        addButton(exitButton);
    }

    @Override
    public void start(EntryPointCallback callback) {
        this.callback = callback;
    }

    private void startGame() {
        GeobotEntryPoint entryPoint = new GeobotEntryPoint();
        entryPoint.setGame(new Cave2Game(entryPoint));
        menuScreen.setInner(entryPoint);
        menuScreen.showGame();
    }
}
