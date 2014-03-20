package ru.geobot.graphics;

import ru.geobot.resources.Image;


/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class ImageUtil implements Image {
    private Image image;

    public ImageUtil(Image image) {
        this.image = image;
    }

    @Override
    public void draw(Graphics graphics) {
        image.draw(graphics);
    }

    @Override
    public void draw(Graphics graphics, float alpha) {
        image.draw(graphics, alpha);
    }

    @Override
    public int getHeight() {
        return image.getHeight();
    }

    @Override
    public int getWidth() {
        return image.getWidth();
    }

    public void draw(Graphics graphics, float x, float y, float w, float h) {
        AffineTransform transform = graphics.getTransform();
        graphics.translate(x, y);
        graphics.scale(w / getWidth(), h / getHeight());
        draw(graphics);
        graphics.setTransform(transform);
    }

    public void draw(Graphics graphics, float x, float y, float w, float h, float alpha) {
        AffineTransform transform = graphics.getTransform();
        graphics.translate(x, y);
        graphics.scale(w / getWidth(), h / getHeight());
        draw(graphics, alpha);
        graphics.setTransform(transform);
    }
}
