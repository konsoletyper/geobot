package ru.geobot.game.caves;

import org.jbox2d.common.Vec2;
import ru.geobot.game.GeobotGame;
import ru.geobot.game.GeobotGameManager;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class Cave2Game extends GeobotGame {
    public Cave2Game(GeobotGameManager gameManager) {
        super(gameManager);
    }

    @Override
    protected Vec2 getInitialRobotLocation() {
        return new Vec2(0.4f, 2.4f);
    }

    @Override
    protected void initCave() {
        new Cave2(this);
    }
}
