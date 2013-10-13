package ru.geobot;

import ru.geobot.caves.Cave1;
import ru.geobot.caves.Cave1Images;
import ru.geobot.graphics.Graphics;
import ru.geobot.graphics.ImageUtil;
import ru.geobot.objects.Robot;

/**
 *
 * @author Alexey Andreev
 */
public class GeobotGame extends Game {
    private Cave1Images images;
    private Robot robot;

    @Override
    public void start(EntryPointCallback callback) {
        robot = new Robot(this, 1, 4f);
        new Cave1(this);
        images = loadImages(Cave1Images.class);
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
        image.draw(graphics, 0, 6f, 10.66666f, 6f);
    }

    @Override
    public boolean idle() {
        return super.idle();
    }
}
