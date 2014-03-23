package ru.geobot.game;

import org.jbox2d.common.Vec2;
import ru.geobot.EntryPointCallback;
import ru.geobot.Game;
import ru.geobot.GameAdapter;
import ru.geobot.Key;
import ru.geobot.game.objects.Robot;

/**
 *
 * @author Alexey Andreev
 */
public abstract class GeobotGame extends Game {
    private GeobotGameManager gameManager;
    private Robot robot;
    private EntryPointCallback callback;

    public GeobotGame(GeobotGameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public void start(EntryPointCallback callback) {
        Vec2 loc = getInitialRobotLocation();
        robot = new Robot(this, loc.x, loc.y);
        initCave();
        addListener(new GameAdapter() {
            @Override public void emptyAreaClicked(float x, float y) {
                robot.pointAt(x, y);
            }
        });
        this.callback = callback;
    }

    protected abstract Vec2 getInitialRobotLocation();

    protected abstract void initCave();

    @Override
    public void keyDown(Key key) {
        robot.keyDown(key);
    }

    @Override
    public void keyUp(Key key) {
        robot.keyUp(key);
    }

    public Robot getRobot() {
        return robot;
    }

    public GeobotGameManager getGameManager() {
        return gameManager;
    }

    public void stop() {
        callback.stop();
    }
}
