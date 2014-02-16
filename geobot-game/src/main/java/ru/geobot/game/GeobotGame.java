package ru.geobot.game;

import ru.geobot.EntryPointCallback;
import ru.geobot.Game;
import ru.geobot.GameAdapter;
import ru.geobot.Key;
import ru.geobot.game.caves.Cave1;
import ru.geobot.game.objects.Robot;

/**
 *
 * @author Alexey Andreev
 */
public class GeobotGame extends Game {
    private Robot robot;

    @Override
    public void start(EntryPointCallback callback) {
        robot = new Robot(this, 2.1f, 1.7f);
        new Cave1(this);
        addListener(new GameAdapter() {
            @Override public void emptyAreaClicked(float x, float y) {
                robot.pointAt(x, y);
            }
        });
    }

    @Override
    public void keyDown(Key key) {
        robot.keyDown(key);
    }

    @Override
    public void keyUp(Key key) {
        robot.keyUp(key);
    }

    @Override
    public boolean idle() {
        return super.idle();
    }

    public Robot getRobot() {
        return robot;
    }
}
