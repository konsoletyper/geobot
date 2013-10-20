package ru.geobot.resources;

import ru.geobot.graphics.Graphics;


/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public interface Image {
    void draw(Graphics graphics);

    int getWidth();

    int getHeight();
}
