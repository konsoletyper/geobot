package ru.geobot.graphics;


/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public interface Image {
    void draw(Graphics graphics);

    int getWidth();

    int getHeight();
}
