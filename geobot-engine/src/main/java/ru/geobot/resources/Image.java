package ru.geobot.resources;

import ru.geobot.graphics.Graphics;


/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public interface Image {
    void draw(Graphics graphics);

    void draw(Graphics graphics, float alpha);

    int getWidth();

    int getHeight();
}
