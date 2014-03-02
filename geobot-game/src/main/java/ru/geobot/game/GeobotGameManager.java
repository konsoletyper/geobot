package ru.geobot.game;

import ru.geobot.EntryPoint;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public interface GeobotGameManager {
    void setGame(EntryPoint game);

    EntryPoint getGame();
}
