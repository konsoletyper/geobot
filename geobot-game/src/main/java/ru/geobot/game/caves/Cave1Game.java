package ru.geobot.game.caves;

import org.jbox2d.common.Vec2;
import ru.geobot.game.GeobotGame;
import ru.geobot.game.GeobotGameManager;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class Cave1Game extends GeobotGame {
    public Cave1Game(GeobotGameManager gameManager) {
        super(gameManager);
    }

    @Override
    protected Vec2 getInitialRobotLocation() {
        return new Vec2(2.1f, 1.7f);
    }

    @Override
    protected void initCave() {
        new Cave1(this);
    }
}
