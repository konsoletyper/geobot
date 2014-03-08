package ru.geobot;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public interface GameListener {
    void objectClicked(GameObject object);

    void emptyAreaClicked(float x, float y);

    void mouseMoved(float x, float y);
}
