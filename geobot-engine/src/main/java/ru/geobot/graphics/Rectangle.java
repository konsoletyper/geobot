package ru.geobot.graphics;

/**
 *
 * @author Alexey Andreev
 */
public class Rectangle {
    public float x;
    public float y;
    public float width;
    public float height;

    private Rectangle() {
    }

    public Rectangle(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Rectangle copy() {
        Rectangle copy = new Rectangle();
        copy.x = x;
        copy.y = y;
        copy.width = width;
        copy.height = height;
        return copy;
    }

    public float right() {
        return x + width;
    }

    public float bottom() {
        return y + height;
    }

    public boolean contains(float x, float y) {
        x -= this.x;
        y -= this.y;
        return x >= 0 && x < width && y >= 0 && y < height;
    }
}
