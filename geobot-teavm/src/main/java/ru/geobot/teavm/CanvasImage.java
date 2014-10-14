package ru.geobot.teavm;

import org.teavm.dom.canvas.CanvasRenderingContext2D;
import org.teavm.dom.html.HTMLImageElement;
import ru.geobot.graphics.AffineTransform;
import ru.geobot.graphics.Graphics;
import ru.geobot.resources.Image;

/**
 *
 * @author Alexey Andreev
 */
public class CanvasImage implements Image {
    private HTMLImageElement[] scaledImages;
    private int width;
    private int height;

    public CanvasImage(HTMLImageElement[] scaledImages, int width, int height) {
        this.scaledImages = scaledImages;
        this.width = width;
        this.height = height;
    }

    @Override
    public void draw(Graphics graphics) {
        AffineTransform transformation = graphics.getTransform();
        double factor = Math.sqrt(Math.abs(transformation.getDeterminant()));
        int index = 0;
        int intFactor = 1;
        while (index < scaledImages.length - 1 && factor < 0.5) {
            factor *= 2;
            intFactor *= 2;
            ++index;
        }
        HTMLImageElement original = scaledImages[0];
        HTMLImageElement scaled = scaledImages[index];
        graphics.scale(intFactor, intFactor);
        graphics.scale(original.getWidth() / (float)(scaled.getWidth() * intFactor),
                original.getHeight() / (float)(scaled.getHeight() * intFactor));
        ((CanvasGraphics)graphics).getContext().drawImage(scaled, 0, 0);
        graphics.setTransform(transformation);
    }

    @Override
    public void draw(Graphics graphics, float alpha) {
        CanvasRenderingContext2D context = ((CanvasGraphics)graphics).getContext();
        double oldAlpha = context.getGlobalAlpha();
        context.setGlobalAlpha(alpha);
        draw(graphics);
        context.setGlobalAlpha(oldAlpha);
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
