package ru.geobot.engine.editor.poly;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import ru.geobot.engine.editor.model.Polygon;
import ru.geobot.engine.editor.model.PolygonalObject;
import ru.geobot.engine.editor.model.Vertex;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class PolygonalObjectEditor extends JComponent {
    private static final byte ACTIVE_TYPE_VERTEX = 0;
    private static final byte ACTIVE_TYPE_EDGE = 1;
    private static final long serialVersionUID = 3078945964049110789L;
    private RenderedImage backgroundImage;
    private int scale = 1;
    private List<PolygonInfo> polygonList = new ArrayList<>();
    private int activePolygonIndex;
    private int activeObjectIndex;
    private byte activeObjectType;
    private Vertex vertexOnActiveEdge;
    private int leftBound;
    private int topBound;
    private int rightBound;
    private int bottomBound;
    private int activeOffsetX;
    private int activeOffsetY;

    private static class PolygonInfo {
        Polygon polygon;
        int left;
        int top;
        int right;
        int bottom;
    }

    public PolygonalObjectEditor() {
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

    private void updateBounds(PolygonInfo info) {
        List<Vertex> vertices = info.polygon.getVertices();
        Vertex v = vertices.get(0);
        info.left = v.getX();
        info.right = v.getX();
        info.top = v.getY();
        info.bottom = v.getY();
        for (int i = 1; i < vertices.size(); ++i) {
            v = vertices.get(i);
            info.left = Math.min(info.left, v.getX());
            info.top = Math.min(info.top, v.getY());
            info.right = Math.max(info.right, v.getX());
            info.bottom = Math.max(info.bottom, v.getY());
        }
    }

    private void updateBounds() {
        if (polygonList.isEmpty()) {
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
        PolygonInfo p = polygonList.get(0);
        topBound = p.top;
        bottomBound = p.bottom;
        leftBound = p.left;
        rightBound = p.right;
        for (int i = 1; i < polygonList.size(); ++i) {
            p = polygonList.get(i);
            leftBound = Math.min(leftBound, p.left);
            topBound = Math.min(topBound, p.top);
            rightBound = Math.max(rightBound, p.right);
            bottomBound = Math.max(bottomBound, p.bottom);
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

    public PolygonalObject getEditObject() {
        PolygonalObject obj = new PolygonalObject();
        obj.setBackgroundImage(backgroundImage);
        for (PolygonInfo polyInfo : polygonList) {
            obj.getPolygons().add(polyInfo.polygon.clone());
        }
        return obj;
    }

    public void setEditObject(PolygonalObject object) {
        backgroundImage = object.getBackgroundImage();
        polygonList.clear();
        for (Polygon polygon : object.getPolygons()) {
            PolygonInfo polygonInfo = new PolygonInfo();
            polygonInfo.polygon = polygon;
            updateBounds(polygonInfo);
            polygonList.add(polygonInfo);
        }
        updateBounds();
        repaint();
        revalidate();
    }

    public RenderedImage getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(RenderedImage image) {
        backgroundImage = image;
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
        for (int i = 0; i < polygonList.size(); ++i) {
            PolygonInfo polygonInfo = polygonList.get(i);
            drawPolygon(graphics, polygonInfo.polygon, i);
        }
    }

    private void drawPolygon(Graphics2D graphics, Polygon polygon, int index) {
        Path2D path = new Path2D.Float();
        List<Vertex> vertices = polygon.getVertices();
        if (activeObjectIndex < 0 || activeObjectType != ACTIVE_TYPE_EDGE || activePolygonIndex != index) {
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
                if (i == activeObjectIndex && index == activePolygonIndex) {
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
            PolygonInfo polygonInfo = polygonList.get(activePolygonIndex);
            Polygon polygon = polygonInfo.polygon;
            Vertex v = viewToPoly(event.getX() - activeOffsetX, event.getY() - activeOffsetY);
            if (polygon.getVertices().size() == 1) {
                polygon.getVertices().add(v);
            } else {
                polygon.getVertices().set(activeObjectIndex, v);
            }
            updateBounds(polygonInfo);
            repaint();
        }
    }

    private void onMousePressed(MouseEvent event) {
        if (activeObjectIndex < 0) {
            if (event.getButton() == MouseEvent.BUTTON1) {
                Polygon poly = new Polygon();
                Vertex v = viewToPoly(event.getX(), event.getY());
                poly.getVertices().add(v);
                activeObjectIndex = 0;
                activeObjectType = ACTIVE_TYPE_VERTEX;
                activePolygonIndex = polygonList.size();
                PolygonInfo polyInfo = new PolygonInfo();
                polyInfo.polygon = poly;
                updateBounds(polyInfo);
                polygonList.add(polyInfo);
            }
            return;
        }
        if (event.getButton() == MouseEvent.BUTTON1) {
            if (activeObjectType == ACTIVE_TYPE_VERTEX) {
                PolygonInfo polygonInfo = polygonList.get(activePolygonIndex);
                Polygon polygon = polygonInfo.polygon;
                Vertex v = polyToView(polygon.getVertices().get(activeObjectIndex));
                v.setX(v.getX() + scale / 2);
                v.setY(v.getY() + scale / 2);
                activeOffsetX = event.getX() - v.getX();
                activeOffsetY = event.getY() - v.getY();
                updateBounds(polygonInfo);
            } else if (activeObjectType == ACTIVE_TYPE_EDGE) {
                PolygonInfo polygonInfo = polygonList.get(activePolygonIndex);
                Polygon polygon = polygonInfo.polygon;
                Vertex v = vertexOnActiveEdge.clone();
                activeOffsetX = event.getX() - v.getX();
                activeOffsetY = event.getY() - v.getY();
                Vertex w = viewToPoly(v.getX(), v.getY());
                polygon.getVertices().add(activeObjectIndex + 1, w);
                activeObjectIndex++;
                activeObjectType = ACTIVE_TYPE_VERTEX;
                repaint();
                updateBounds(polygonInfo);
            }
        } else if (event.getButton() == MouseEvent.BUTTON3) {
            if (activeObjectType == ACTIVE_TYPE_VERTEX) {
                PolygonInfo polygonInfo = polygonList.get(activePolygonIndex);
                Polygon polygon = polygonInfo.polygon;
                if (polygon.getVertices().size() == 1) {
                    polygonList.remove(activePolygonIndex);
                    activeObjectIndex = -1;
                } else {
                    polygon.getVertices().remove(activeObjectIndex);
                    activeObjectIndex = -1;
                }
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
        int oldActivePolygonIndex = activePolygonIndex;
        activeObjectIndex = -1;
        activeObjectType = ACTIVE_TYPE_VERTEX;
        int bestDistance = 5;
        for (int i = 0; i < polygonList.size(); ++i) {
            PolygonInfo polyInfo = polygonList.get(i);
            Vertex leftTop = new Vertex();
            leftTop.setX(polyInfo.left - 6);
            leftTop.setY(polyInfo.bottom + 6);
            leftTop = polyToView(leftTop);
            Vertex rightBottom = new Vertex();
            rightBottom.setX(polyInfo.right + 6);
            rightBottom.setY(polyInfo.top - 6);
            rightBottom = polyToView(rightBottom);
            if (x < leftTop.getX() || x >= rightBottom.getX() || y < leftTop.getY() || y >= rightBottom.getY()) {
                continue;
            }
            Polygon polygon = polyInfo.polygon;
            for (int j = 0; j < polygon.getVertices().size(); ++j) {
                Vertex v = polyToView(polygon.getVertices().get(j));
                v.setX(v.getX() + scale / 2);
                v.setY(v.getY() + scale / 2);
                int distance = Math.max(Math.abs(v.getX() - x), Math.abs(v.getY() - y));
                if (distance <= 6 && bestDistance > distance) {
                    activeObjectIndex = j;
                    activePolygonIndex = i;
                }
            }
            if (activeObjectIndex == -1) {
                Vertex v = new Vertex();
                v.setX(x);
                v.setY(y);
                for (int j = 0; j < polygon.getVertices().size(); ++j) {
                    int k = (j + 1) % polygon.getVertices().size();
                    Vertex a = polyToView(polygon.getVertices().get(j));
                    Vertex b = polyToView(polygon.getVertices().get(k));
                    Intersection intersection = getIntersection(a, b, v);
                    int distance = intersectionDistance(intersection, a, b);
                    if (distance <= 6 && bestDistance > distance) {
                        activeObjectIndex = j;
                        activeObjectType = ACTIVE_TYPE_EDGE;
                        activePolygonIndex = i;
                        bestDistance = distance;
                        Vertex active = new Vertex();
                        Vertex dir = sub(b, a);
                        active.setX(Math.round(a.getX() + dir.getX() * intersection.along));
                        active.setY(Math.round(a.getY() + dir.getY() * intersection.along));
                        vertexOnActiveEdge = active;
                    }
                }
            }
        }
        if (activeObjectIndex != oldActiveObjectIndex || activeObjectType != oldActiveObjectType ||
                (activeObjectIndex >= 0 && activeObjectType == ACTIVE_TYPE_EDGE) ||
                activePolygonIndex != oldActivePolygonIndex) {
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
