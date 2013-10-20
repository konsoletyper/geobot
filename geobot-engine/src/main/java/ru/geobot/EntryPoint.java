package ru.geobot;

import ru.geobot.graphics.Graphics;
import ru.geobot.resources.ResourceReader;

/**
 *
 * @author Alexey Andreev
 */
public interface EntryPoint {
    void mouseMove(int x, int y);

    void mouseDown();

    void mouseUp();

    void keyDown(Key key);

    void keyUp(Key key);

    boolean idle();

    void resize(int width, int height);

    void paint(Graphics graphics);

    void start(EntryPointCallback callback);

    void setResourceReader(ResourceReader resourceReader);

    void interrupt();
}
