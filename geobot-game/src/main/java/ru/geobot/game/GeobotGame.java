package ru.geobot.game;

import ru.geobot.EntryPointCallback;
import ru.geobot.Game;
import ru.geobot.Key;
import ru.geobot.game.caves.Cave1;
import ru.geobot.game.caves.Cave1Resources;
import ru.geobot.game.objects.Robot;
import ru.geobot.graphics.Graphics;
import ru.geobot.graphics.ImageUtil;

/**
 *
 * @author Alexey Andreev
 */
public class GeobotGame extends Game {
    private Cave1Resources images;
    private Robot robot;

    @Override
    public void start(EntryPointCallback callback) {
        robot = new Robot(this, 2.1f, 1.7f);
        new Cave1(this);
        images = loadResources(Cave1Resources.class);
    }

    @Override
    public void keyDown(Key key) {
        robot.keyDown(key);
    }

    @Override
    public void keyUp(Key key) {
        robot.keyUp(key);
    }

    @Override
    protected void paintBackground(Graphics graphics) {
        super.paintBackground(graphics);
        ImageUtil image = new ImageUtil(images.background());
        image.draw(graphics, 0, 7.5f, 13.3333f, 7.5f);
    }

    @Override
    public boolean idle() {
        return super.idle();
    }

    @Override
    public void mouseDown() {
        super.mouseDown();
        robot.pointAt(getMouseX(), getMouseY());
    }
}
