package ru.geobot;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public interface GameObjectListener {
    void click();

    void mouseEnter();

    void mouseLeave();

    void keyDown(Key key);

    void keyUp(Key key);
}
