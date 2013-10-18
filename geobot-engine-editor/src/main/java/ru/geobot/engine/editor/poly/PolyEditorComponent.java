package ru.geobot.engine.editor.poly;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.RenderedImage;
import java.util.List;
import javax.swing.JComponent;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class PolyEditorComponent extends JComponent {
    private static final byte ACTIVE_TYPE_VERTEX = 0;
    private static final byte ACTIVE_TYPE_EDGE = 1;
    private static final long serialVersionUID = 3078945964049110789L;
    private RenderedImage backgroundImage;
    private int scale = 1;
    private Polygon polygon = new Polygon();
    private int activeObjectIndex;
    private byte activeObjectType;
    private Vertex vertexOnActiveEdge;
    private int leftBound;
    private int topBound;
    private int rightBound;
    private int bottomBound;
    private int activeOffsetX;
    private int activeOffsetY;

    public PolyEditorComponent() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                onMouseMoved(e);
            }
            @Override public void mouseDragged(MouseEvent e) {
                onMouseDragged(e);
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
        topBound = v.getY();
        bottomBound = v.getY();
        leftBound = v.getX();
        rightBound = v.getX();
        for (int i = 1; i < polygon.getVertices().size(); ++i) {
            v = polygon.getVertices().get(i);
            leftBound = Math.min(leftBound, v.getX());
            topBound = Math.min(topBound, v.getY());
            rightBound = Math.max(rightBound, v.getX());
            bottomBound = Math.max(bottomBound, v.getY());
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
            AffineTransform transform = new AffineTransform();
            transform.translate(-leftBound, bottomBound - backgroundImage.getHeight());
            transform.scale(scale, scale);
            graphics.drawRenderedImage(backgroundImage, transform);
        }
        Path2D path = new Path2D.Float();
        List<Vertex> vertices = polygon.getVertices();
        if (activeObjectIndex < 0 || activeObjectType != ACTIVE_TYPE_EDGE) {
            Vertex v = polyToView(vertices.get(0));
            path.moveTo(v.getX() + scale / 2, v.getY() + scale / 2);
            for (int i = 1; i < vertices.size(); ++i) {
                v = polyToView(vertices.get(i));
                path.lineTo(v.getX() + scale / 2, v.getY() + scale / 2);
            }
            path.closePath();
            graphics.setColor(new Color(255, 0, 0, 96));
            graphics.fill(path);
            graphics.setColor(new Color(255, 0, 0, 255));
            graphics.draw(path);
            for (int i = 0; i < vertices.size(); ++i) {
                v = polyToView(vertices.get(i));
                int x = v.getX() + scale / 2;
                int y = v.getY() + scale / 2;
                if (i == activeObjectIndex) {
                    graphics.setColor(new Color(0, 255, 0, 255));
                } else {
                    graphics.setColor(new Color(255, 0, 0, 255));
                }
                graphics.fillRect(x - 1, y - 1, 3, 3);
            }
        } else {
            Vertex v = polyToView(vertices.get(activeObjectIndex));
            path.moveTo(v.getX() + scale / 2, v.getY() + scale / 2);
            for (int i = 1; i < vertices.size(); ++i) {
                v = polyToView(vertices.get((vertices.size() + activeObjectIndex - i) % vertices.size()));
                path.lineTo(v.getX() + scale / 2, v.getY() + scale / 2);
            }
            graphics.setColor(new Color(255, 0, 0, 96));
            graphics.fill(path);
            graphics.setColor(new Color(255, 0, 0, 255));
            graphics.draw(path);
            graphics.setColor(new Color(0, 255, 0, 255));
            v = polyToView(vertices.get(activeObjectIndex));
            Vertex w = polyToView(vertices.get((activeObjectIndex + 1) % vertices.size()));
            graphics.drawLine(v.getX() + scale / 2, v.getY() + scale / 2, w.getX() + scale / 2, w.getY() + scale / 2);
            graphics.setColor(new Color(255, 0, 0, 255));
            for (int i = 0; i < polygon.getVertices().size(); ++i) {
                v = polyToView(polygon.getVertices().get(i));
                int x = v.getX() + scale / 2;
                int y = v.getY() + scale / 2;
                graphics.fillRect(x - 1, y - 1, 3, 3);
            }
            graphics.setColor(new Color(0, 255, 0, 255));
            int x = vertexOnActiveEdge.getX() + scale / 2;
            int y = vertexOnActiveEdge.getY() + scale  /2;
            graphics.fillRect(x - 1, y - 1, 3, 3);
        }
    }

    private Vertex polyToView(Vertex v) {
        int x = v.getX();
        int y = v.getY();
        x = (x - leftBound) * scale;
        y = (bottomBound - y - 1) * scale;
        Vertex r = new Vertex();
        r.setX(x);
        r.setY(y);
        return r;
    }

    private Vertex viewToPoly(int x, int y) {
        y = (bottomBound - y) / scale;
        x = (x + leftBound) / scale;
        Vertex r = new Vertex();
        r.setX(x);
        r.setY(y);
        return r;
    }

    private void onMouseMoved(MouseEvent event) {
        updateActiveObject(event.getX(), event.getY());
    }

    private void onMouseDragged(MouseEvent event) {
        if ((event.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) == 0) {
            onMouseMoved(event);
            return;
        }
        if (activeObjectIndex >= 0 && activeObjectType == ACTIVE_TYPE_VERTEX) {
            Vertex v = viewToPoly(event.getX() - activeOffsetX, event.getY() - activeOffsetY);
            if (polygon.getVertices().size() == 1) {
                polygon.getVertices().add(v);
            } else {
                polygon.getVertices().set(activeObjectIndex, v);
            }
            repaint();
        }
    }

    private void onMousePressed(MouseEvent event) {
        if (activeObjectIndex < 0) {
            System.out.println(event.getButton());
            return;
        }
        if (event.getButton() == MouseEvent.BUTTON1) {
            if (activeObjectType == ACTIVE_TYPE_VERTEX) {
                Vertex v = polyToView(polygon.getVertices().get(activeObjectIndex));
                v.setX(v.getX() + scale / 2);
                v.setY(v.getY() + scale / 2);
                activeOffsetX = event.getX() - v.getX();
                activeOffsetY = event.getY() - v.getY();
            } else if (activeObjectType == ACTIVE_TYPE_EDGE) {
                Vertex v = vertexOnActiveEdge.clone();
                activeOffsetX = event.getX() - v.getX();
                activeOffsetY = event.getY() - v.getY();
                Vertex w = viewToPoly(v.getX(), v.getY());
                polygon.getVertices().add(activeObjectIndex + 1, w);
                activeObjectIndex++;
                activeObjectType = ACTIVE_TYPE_VERTEX;
                repaint();
            }
        } else if (event.getButton() == MouseEvent.BUTTON3) {
            if (activeObjectType == ACTIVE_TYPE_VERTEX) {
                polygon.getVertices().remove(activeObjectIndex);
                activeObjectIndex = -1;
                updateBounds();
                updateActiveObject(event.getX(), event.getY());
            }
        }
    }

    private void onMouseReleased(MouseEvent event) {
        if (event.getButton() == MouseEvent.BUTTON1) {
            updateActiveObject(event.getX(), event.getY());
            updateBounds();
            revalidate();
        }
    }

    private void updateActiveObject(int x, int y) {
        int oldActiveObjectIndex = activeObjectIndex;
        int oldActiveObjectType = activeObjectType;
        activeObjectIndex = -1;
        activeObjectType = ACTIVE_TYPE_VERTEX;
        int bestDistance = 5;
        for (int i = 0; i < polygon.getVertices().size(); ++i) {
            Vertex v = polyToView(polygon.getVertices().get(i));
            v.setX(v.getX() + scale / 2);
            v.setY(v.getY() + scale / 2);
            int distance = Math.max(Math.abs(v.getX() - x), Math.abs(v.getY() - y));
            if (distance <= 6 && bestDistance > distance) {
                activeObjectIndex = i;
            }
        }
        if (activeObjectIndex == -1) {
            Vertex v = new Vertex();
            v.setX(x);
            v.setY(y);
            for (int i = 0; i < polygon.getVertices().size(); ++i) {
                int j = (i + 1) % polygon.getVertices().size();
                Vertex a = polyToView(polygon.getVertices().get(i));
                Vertex b = polyToView(polygon.getVertices().get(j));
                Intersection intersection = getIntersection(a, b, v);
                int distance = intersectionDistance(intersection, a, b);
                if (distance <= 6 && bestDistance > distance) {
                    activeObjectIndex = i;
                    activeObjectType = ACTIVE_TYPE_EDGE;
                    Vertex active = new Vertex();
                    Vertex dir = sub(b, a);
                    active.setX(Math.round(a.getX() + dir.getX() * intersection.along));
                    active.setY(Math.round(a.getY() + dir.getY() * intersection.along));
                    vertexOnActiveEdge = active;
                }
            }
        }
        if (activeObjectIndex != oldActiveObjectIndex || activeObjectType != oldActiveObjectType ||
                (activeObjectIndex >= 0 && activeObjectType == ACTIVE_TYPE_EDGE)) {
            repaint();
        }
    }

    static class Intersection {
        float across;
        float along;
    }

    private int intersectionDistance(Intersection distance, Vertex a, Vertex b) {
        Vertex dir = sub(b, a);
        float length = (float)Math.sqrt(dotProduct(dir, dir));
        float along = distance.along < 0 ? Math.abs(distance.along) :
                distance.along > 1 ? Math.abs(distance.along - 1) : 0;
        float across = Math.abs(distance.across);
        return Math.round(Math.max(along, across) * length);
    }

    private Intersection getIntersection(Vertex a, Vertex b, Vertex v) {
        Vertex dir = sub(b, a);
        Vertex perp = rotate90degrees(dir);
        Vertex vdir = sub(v, a);
        float norm = dotProduct(dir, dir);
        Intersection dist = new Intersection();
        dist.across = dotProduct(perp, vdir) / norm;
        dist.along = dotProduct(dir, vdir) / norm;
        return dist;
    }

    private int dotProduct(Vertex a, Vertex b) {
        return a.getX() * b.getX() + a.getY() * b.getY();
    }

    private Vertex rotate90degrees(Vertex v) {
        Vertex r = new Vertex();
        r.setX(v.getY());
        r.setY(-v.getX());
        return r;
    }

    private Vertex sub(Vertex a, Vertex b) {
        Vertex v = new Vertex();
        v.setX(a.getX() - b.getX());
        v.setY(a.getY() - b.getY());
        return v;
    }
}
