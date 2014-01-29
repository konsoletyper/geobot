package ru.geobot;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public interface GameObjectListener {
    boolean click();

    void mouseEnter();

    void mouseLeave();

    void keyDown(Key key);

    void keyUp(Key key);
}
