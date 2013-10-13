package ru.geobot.graphics;


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
        graphics.scale(w / getWidth(), -h / getHeight());
        graphics.translate(0, -h);
        draw(graphics);
        graphics.setTransform(transform);
    }
}
