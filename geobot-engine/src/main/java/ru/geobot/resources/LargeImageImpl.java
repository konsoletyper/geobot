package ru.geobot.resources;

import ru.geobot.AWTGraphics;
import ru.geobot.graphics.AffineTransform;
import ru.geobot.graphics.Graphics;

/**
 *
 * @author Alexey Andreev
 */
class LargeImageImpl implements Image {
    private LargeImageScale[] scales;
    private int width;
    private int height;

    public LargeImageImpl(LargeImageScale[] scales, int width, int height) {
        this.scales = scales;
        this.width = width;
        this.height = height;
    }

    @Override
    public void draw(Graphics graphics) {
        AffineTransform transformation = graphics.getTransform();
        double factor = Math.sqrt(Math.abs(transformation.getDeterminant()));
        int index = 0;
        int intFactor = 1;
        while (index < scales.length - 1 && factor < 0.5) {
            factor *= 2;
            intFactor *= 2;
            ++index;
        }
        LargeImageScale scaled = scales[index];
        graphics.scale(intFactor, intFactor);
        graphics.scale(width / (float)(scaled.width * intFactor), height / (float)(scaled.height * intFactor));
        scaled.draw(((AWTGraphics)graphics).getInnerGraphics());
        graphics.setTransform(transformation);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }
}
