package ru.geobot.graphics;

/**
 *
 * @author Alexey Andreev
 */
public interface Graphics {
    void setColor(Color color);

    void setStrokeWidth(float width);

    void setTransform(AffineTransform transform);

    AffineTransform getTransform();

    void scale(float x, float y);

    void translate(float x, float y);

    void rotate(float angle);

    void transform(AffineTransform transform);

    void pushTransform();

    void popTransform();

    void fillRectangle(float x, float y, float w, float h);

    void moveTo(float x, float y);

    void lineTo(float x, float y);

    void bezierCurveTo(float cp1x, float cp1y, float cp2x, float cp2y, float x, float y);

    void quadraticCurveTo(float cpx, float cpy, float x, float y);

    void stroke();

    void fill();

    void drawArc(float x, float y, float w, float h, float startAngle, float endAngle);

    void drawEllipse(float x, float y, float w, float h);

    void fillEllipse(float x, float y, float w, float h);

    void clip(Rectangle rectangle);

    Rectangle getClip();

    void setClip(Rectangle rectangle);
}
