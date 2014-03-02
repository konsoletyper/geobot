package ru.geobot;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Deque;
import java.util.LinkedList;
import ru.geobot.graphics.AffineTransform;
import ru.geobot.graphics.Color;
import ru.geobot.graphics.Graphics;
import ru.geobot.graphics.Rectangle;

/**
 *
 * @author Alexey Andreev
 */
public class AWTGraphics implements Graphics {
    private Graphics2D innerGraphics;
    private Deque<AffineTransform> transformStack = new LinkedList<>();
    private java.awt.geom.Path2D.Float currentPath;
    private Rectangle clipRectangle;

    public AWTGraphics(Graphics2D innerGraphics, Rectangle clipRectangle) {
        this.innerGraphics = innerGraphics;
        this.clipRectangle = clipRectangle != null ? clipRectangle.copy() : null;
        updateClip();
    }

    private void updateClip() {
        if (clipRectangle != null) {
            innerGraphics.clip(new Rectangle2D.Float(clipRectangle.x, clipRectangle.y,
                    clipRectangle.width, clipRectangle.height));
        } else {
            innerGraphics.clipRect(0, 0, 0, 0);
        }
    }

    @Override
    public void setColor(Color color) {
        innerGraphics.setColor(new java.awt.Color(color.r, color.g, color.b, color.a));
    }

    @Override
    public void setStrokeWidth(float width) {
        innerGraphics.setStroke(new BasicStroke(width));
    }

    @Override
    public void setTransform(AffineTransform transform) {
        innerGraphics.setTransform(new java.awt.geom.AffineTransform(transform.a, transform.b,
                transform.c, transform.d, transform.e, transform.f));
    }

    @Override
    public AffineTransform getTransform() {
        java.awt.geom.AffineTransform innerTransform = innerGraphics.getTransform();
        AffineTransform transform = new AffineTransform();
        transform.a = (float)innerTransform.getScaleX();
        transform.b = (float)innerTransform.getShearX();
        transform.c = (float)innerTransform.getShearY();
        transform.d = (float)innerTransform.getScaleY();
        transform.e = (float)innerTransform.getTranslateX();
        transform.f = (float)innerTransform.getTranslateY();
        return transform;
    }

    @Override
    public void scale(float x, float y) {
        innerGraphics.scale(x, y);
    }

    @Override
    public void translate(float x, float y) {
        innerGraphics.translate(x, y);
    }

    @Override
    public void rotate(float angle) {
        innerGraphics.rotate(angle);
    }

    @Override
    public void transform(AffineTransform transform) {
        AffineTransform currentTransform = getTransform();
        currentTransform.transform(transform);
        setTransform(currentTransform);
    }

    @Override
    public void pushTransform() {
        transformStack.push(getTransform());
    }

    @Override
    public void popTransform() {
        setTransform(transformStack.pop());
    }

    @Override
    public void fillRectangle(float x, float y, float w, float h) {
        innerGraphics.fill(new java.awt.geom.Rectangle2D.Float(x, y, w, h));
    }

    @Override
    public void moveTo(float x, float y) {
        if (currentPath == null) {
            currentPath = new java.awt.geom.Path2D.Float();
        }
        currentPath.moveTo(x, y);
    }

    @Override
    public void lineTo(float x, float y) {
        if (currentPath == null) {
            throw new IllegalStateException("Not drawing a path");
        }
        currentPath.lineTo(x, y);
    }

    @Override
    public void bezierCurveTo(float cp1x, float cp1y, float cp2x, float cp2y, float x, float y) {
        if (currentPath == null) {
            throw new IllegalStateException("Not drawing a path");
        }
        currentPath.curveTo(cp1x, cp1y, cp2x, cp2y, x, y);
    }

    @Override
    public void quadraticCurveTo(float cpx, float cpy, float x, float y) {
        if (currentPath == null) {
            throw new IllegalStateException("Not drawing a path");
        }
        currentPath.quadTo(cpx, cpy, x, y);
    }

    @Override
    public void stroke() {
        if (currentPath == null) {
            throw new IllegalStateException("Not drawing a path");
        }
        innerGraphics.draw(currentPath);
        currentPath = null;
    }

    @Override
    public void fill() {
        if (currentPath == null) {
            throw new IllegalStateException("Not drawing a path");
        }
        innerGraphics.fill(currentPath);
        currentPath = null;
    }

    @Override
    public void drawArc(float x, float y, float w, float h, float startAngle, float endAngle) {
        startAngle *= 180 / Math.PI;
        endAngle *= 180 / Math.PI;
        innerGraphics.draw(new java.awt.geom.Arc2D.Float(x, y, w, h, startAngle, endAngle,
                java.awt.geom.Arc2D.OPEN));
    }

    @Override
    public void drawEllipse(float x, float y, float w, float h) {
        innerGraphics.draw(new java.awt.geom.Ellipse2D.Float(x, y, w, h));
    }

    @Override
    public void fillEllipse(float x, float y, float w, float h) {
        innerGraphics.fill(new java.awt.geom.Ellipse2D.Float(x, y, w, h));
    }

    public Graphics2D getInnerGraphics() {
        return innerGraphics;
    }

    @Override
    public void clip(Rectangle rectangle) {
        if (clipRectangle == null) {
            return;
        }
        float left = Math.max(clipRectangle.x, rectangle.x);
        float top = Math.max(clipRectangle.y, rectangle.y);
        float right = Math.min(clipRectangle.right(), rectangle.right());
        float bottom = Math.min(clipRectangle.bottom(), rectangle.bottom());
        if (left > right || top > bottom) {
            clipRectangle = null;
        } else {
            clipRectangle = new Rectangle(left, top, right - left, bottom - top);
        }
        updateClip();
    }

    @Override
    public Rectangle getClip() {
        return clipRectangle != null ? clipRectangle.copy() : null;
    }

    @Override
    public void setClip(Rectangle rectangle) {
        clipRectangle = rectangle != null ? rectangle.copy() : null;
        updateClip();
    }
}
