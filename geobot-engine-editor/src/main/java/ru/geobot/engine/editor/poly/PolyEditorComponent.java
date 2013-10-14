package ru.geobot.engine.editor.poly;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.RenderedImage;
import javax.swing.JComponent;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class PolyEditorComponent extends JComponent {
    private byte ACTIVE_TYPE_VERTEX = 0;
    private byte ACTIVE_TYPE_EDGE = 1;
    private static final long serialVersionUID = 3078945964049110789L;
    private RenderedImage backgroundImage;
    private int scale = 1;
    private Polygon polygon = new Polygon();
    private int activeObjectIndex;
    private byte activeObjectType;
    private int leftBound;
    private int topBound;
    private int rightBound;
    private int bottomBound;

    public PolyEditorComponent() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                onMouseMoved(e);
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                onMousePressed(e);
            }
            @Override public void mouseReleased(MouseEvent e) {
                onMouseReleased(e);
            }
            @Override public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
            }
            @Override public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
            }
        });
    }

    public void setBackgroundImage(RenderedImage backgroundImage) {
        this.backgroundImage = backgroundImage;
        repaint();
        updateBounds();
    }

    private void updateBounds() {
        if (polygon.getVertices().isEmpty()) {
            topBound = 0;
            bottomBound = 0;
            if (backgroundImage != null) {
                rightBound = backgroundImage.getWidth();
                bottomBound = backgroundImage.getHeight();
            } else {
                leftBound = 0;
                rightBound = 0;
            }
            repaint();
            revalidate();
            return;
        }
        Vertex v = polygon.getVertices().get(0);
        topBound = v.getX();
        bottomBound = v.getY();
        leftBound = v.getX();
        rightBound = v.getX();
        for (int i = 1; i < polygon.getVertices().size(); ++i) {
            v = polygon.getVertices().get(i);
            leftBound = Math.min(leftBound, v.getX());
            topBound = Math.min(topBound, v.getY());
            rightBound = Math.max(rightBound, v.getX());
            bottomBound = Math.max(bottomBound, v.getX());
        }
        if (backgroundImage != null) {
            leftBound = Math.min(leftBound, 0);
            topBound = Math.min(topBound, 0);
            rightBound = Math.max(rightBound, backgroundImage.getWidth());
            bottomBound = Math.max(bottomBound, backgroundImage.getHeight());
        }
        repaint();
        revalidate();
    }

    public RenderedImage getBackgroundImage() {
        return backgroundImage;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        if (scale < 1 || scale > 1000) {
            throw new IllegalArgumentException("Wrong scale: " + scale);
        }
        this.scale = scale;
        revalidate();
        repaint();
    }

    public Polygon getPolygon() {
        return polygon.clone();
    }

    public void setPolygon(Polygon polygon) {
        this.polygon = polygon.clone();
        updateBounds();
        repaint();
        revalidate();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension((rightBound - leftBound) * scale, (bottomBound - topBound) * scale);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D graphics = (Graphics2D)g;
        graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(new Color(0, 0, 0, 255));
        graphics.fillRect(0, 0, getWidth(), getHeight());
        if (backgroundImage != null) {
            graphics.drawRenderedImage(backgroundImage, AffineTransform.getScaleInstance(scale, scale));
        }
        Path2D path = new Path2D.Float();
        Vertex v = polyToView(polygon.getVertices().get(0));
        path.moveTo(v.getX() + scale / 2, v.getY() + scale / 2);
        for (int i = 1; i < polygon.getVertices().size(); ++i) {
            v = polyToView(polygon.getVertices().get(i));
            path.lineTo(v.getX() + scale / 2, v.getY() + scale / 2);
        }
        if (polygon.isClosed()) {
            path.closePath();
        }
        graphics.setColor(new Color(255, 0, 0, 96));
        graphics.fill(path);
        graphics.setColor(new Color(255, 0, 0, 255));
        graphics.draw(path);
        for (int i = 0; i < polygon.getVertices().size(); ++i) {
            v = polyToView(polygon.getVertices().get(i));
            int x = v.getX() + scale / 2;
            int y = v.getY() + scale / 2;
            if (i == activeObjectIndex) {
                graphics.setColor(new Color(0, 255, 0, 255));
            } else {
                graphics.setColor(new Color(255, 0, 0, 255));
            }
            graphics.fillRect(x - 1, y - 1, 3, 3);
        }
    }

    private Vertex polyToView(Vertex v) {
        int x = v.getX();
        int y = v.getY();
        x = (x - leftBound) * scale;
        y = (bottomBound - topBound - y - 1) * scale;
        Vertex r = new Vertex();
        r.setX(x);
        r.setY(y);
        return r;
    }

    private void onMouseMoved(MouseEvent event) {
        if ((event.getModifiers() & InputEvent.BUTTON1_DOWN_MASK) == 0) {
            updateActiveVertex(event.getX(), event.getY());
        }
    }

    private void onMousePressed(MouseEvent event) {

    }

    private void onMouseReleased(MouseEvent event) {
        if (event.getButton() == MouseEvent.BUTTON1) {
            updateActiveVertex(event.getX(), event.getY());
        }
    }

    private void updateActiveVertex(int x, int y) {
        int h = getHeight();
        if (backgroundImage != null) {
            h = backgroundImage.getHeight() * scale;
        }
        int oldActiveVertexIndex = activeObjectIndex;
        activeObjectIndex = -1;
        for (int i = 0; i < polygon.getVertices().size(); ++i) {
            Vertex v = polygon.getVertices().get(i);
            int refX = v.getX() * scale + scale / 2;
            int refY = h - (v.getY() * scale + scale / 2);
            if (Math.abs(refX - x) <= 4 && Math.abs(refY - y) <= 4) {
                activeObjectIndex = i;
                break;
            }
        }
        if (activeObjectIndex != oldActiveVertexIndex) {
            repaint();
        }
    }
}
