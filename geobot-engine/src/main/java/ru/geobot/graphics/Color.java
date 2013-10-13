package ru.geobot.graphics;

/**
 *
 * @author Alexey Andreev
 */
public class Color {
    public short r;
    public short g;
    public short b;

    public Color copy() {
        Color other = new Color();
        other.r = r;
        other.g = g;
        other.b = b;
        return other;
    }

    public boolean isValid() {
        return r >= 0 && r <= 255 && g >= 0 && g <= 255 && b >= 0 && b <= 255;
    }

    public static Color black() {
        Color color = new Color();
        return color;
    }

    public static Color gray() {
        Color color = new Color();
        color.r = 128;
        color.g = 128;
        color.b = 128;
        return color;
    }

    public static Color red() {
        Color color = new Color();
        color.r = 255;
        return color;
    }

    public static Color green() {
        Color color = new Color();
        color.g = 255;
        return color;
    }

    public static Color blue() {
        Color color = new Color();
        color.b = 255;
        return color;
    }

    public static Color magenta() {
        Color color = new Color();
        color.r = 255;
        color.r = 255;
        return color;
    }

    public static Color yellow() {
        Color color = new Color();
        color.r = 255;
        color.g = 255;
        return color;
    }

    public static Color white() {
        Color color = new Color();
        color.r = 255;
        color.g = 255;
        color.b = 255;
        return color;
    }

    public static Color pink() {
        Color color = new Color();
        color.r = 255;
        color.g = 192;
        color.b = 192;
        return color;
    }

    public static Color orange() {
        Color color = new Color();
        color.r = 255;
        color.g = 192;
        color.b = 0;
        return color;
    }
}
