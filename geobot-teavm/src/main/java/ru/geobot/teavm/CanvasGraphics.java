package ru.geobot.teavm;

import java.util.ArrayDeque;
import java.util.Deque;
import org.teavm.dom.canvas.CanvasRenderingContext2D;
import ru.geobot.graphics.AffineTransform;
import ru.geobot.graphics.Color;
import ru.geobot.graphics.Graphics;
import ru.geobot.graphics.Rectangle;

/**
 *
 * @author Alexey Andreev
 */
public class CanvasGraphics implements Graphics {
    private CanvasRenderingContext2D context;
    private AffineTransform transform = AffineTransform.identity();
    private Deque<AffineTransform> transformStack = new ArrayDeque<>();
    private Deque<ClipRegion> clipStack = new ArrayDeque<>();

    private static class ClipRegion {
        AffineTransform transform;
        Rectangle rect;
    }

    public CanvasGraphics(CanvasRenderingContext2D context) {
        this.context = context;
    }

    @Override
    public void setColor(Color color) {
        String colorText = mapColor(color);
        context.setFillStyle(colorText);
        context.setStrokeStyle(colorText);
    }

    private String mapColor(Color color) {
        StringBuilder sb = new StringBuilder();
        sb.append("rgba(" + color.r + "," + color.g + "," + color.b + "," + (color.a / 255.0) + ")");
        return sb.toString();
    }

    @Override
    public void setStrokeWidth(float width) {
        context.setLineWidth(width);
    }

    @Override
    public void setTransform(AffineTransform transform) {
        this.transform = transform.copy();
        updateTransform();
    }

    private void updateTransform() {
        context.setTransform(transform.a, transform.b, transform.c, transform.d, transform.e, transform.f);
    }

    @Override
    public AffineTransform getTransform() {
        return transform.copy();
    }

    @Override
    public void scale(float x, float y) {
        transform.scale(x, y);
        updateTransform();
    }

    @Override
    public void translate(float x, float y) {
        transform.translate(x, y);
        updateTransform();
    }

    @Override
    public void rotate(float angle) {
        transform.rotate(angle);
        updateTransform();
    }

    @Override
    public void transform(AffineTransform transform) {
        transform.transform(transform);
        updateTransform();
    }

    @Override
    public void pushTransform() {
        transformStack.push(transform.copy());
    }

    @Override
    public void popTransform() {
        transform = transformStack.pop();
        updateTransform();
    }

    @Override
    public void fillRectangle(float x, float y, float w, float h) {
        context.fillRect(x, y, w, h);
    }

    @Override
    public void moveTo(float x, float y) {
        context.moveTo(x, y);
    }

    @Override
    public void lineTo(float x, float y) {
        context.lineTo(x, y);
    }

    @Override
    public void bezierCurveTo(float cp1x, float cp1y, float cp2x, float cp2y, float x, float y) {
        context.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, x, y);
    }

    @Override
    public void quadraticCurveTo(float cpx, float cpy, float x, float y) {
        context.quadraticCurveTo(cpx, cpy, x, y);
    }

    @Override
    public void stroke() {
        context.stroke();
        context.beginPath();
    }

    @Override
    public void fill() {
        context.fill();
        context.beginPath();
    }

    @Override
    public void drawArc(float x, float y, float w, float h, float startAngle, float endAngle) {
        context.save();
        context.scale(w, h);
        context.beginPath();
        context.arc(x / w + 0.5f, y / h + 0.5f, 0.5f, startAngle, endAngle);
        context.restore();
        context.stroke();
    }

    @Override
    public void drawEllipse(float x, float y, float w, float h) {
        context.save();
        context.scale(w, h);
        context.beginPath();
        context.arc(x / w + 0.5f, y / h + 0.5f, 0.5f, 0, 2 * (float)Math.PI);
        context.restore();
        context.stroke();
    }

    @Override
    public void fillEllipse(float x, float y, float w, float h) {
        context.save();
        context.scale(w, h);
        context.beginPath();
        context.arc(x / w + 0.5f, y / h + 0.5f, 0.5f, 0, 2 * (float)Math.PI);
        context.restore();
        context.fill();
    }

    @Override
    public void clip(Rectangle rectangle) {
        /*applyClip(rectangle);
        ClipRegion region = new ClipRegion();
        region.transform = transform.copy();
        region.rect = rectangle.copy();
        clipStack.push(region);*/
    }

    private void applyClip(Rectangle rectangle) {
        /*context.beginPath();
        context.moveTo(rectangle.x, rectangle.y);
        context.lineTo(rectangle.x + rectangle.width, rectangle.y);
        context.lineTo(rectangle.x + rectangle.width, rectangle.y + rectangle.height);
        context.lineTo(rectangle.x, rectangle.y + rectangle.height);
        context.closePath();
        context.clip();*/
    }

    @Override
    public void popClip() {
        /*clipStack.pop();
        context.resetClip();
        context.setTransform(1, 0, 0, 1, 0, 0);
        for (ClipRegion region : clipStack) {
            AffineTransform t = region.transform;
            context.setTransform(t.a, t.b, t.c, t.d, t.e, t.f);
            applyClip(region.rect);
        }
        updateTransform();*/
    }

    public CanvasRenderingContext2D getContext() {
        return context;
    }
}
