package ru.geobot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import ru.geobot.graphics.AffineTransform;
import ru.geobot.graphics.Color;
import ru.geobot.graphics.Graphics;
import ru.geobot.graphics.Rectangle;
import ru.geobot.resources.ResourceReader;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class Game implements EntryPoint {
    private World world;
    List<GameObject> objects = new ArrayList<>();
    private List<GameListener> listeners = new ArrayList<>();
    private List<ContactListener> contactListeners = new ArrayList<>();
    boolean hasRemovedObjects;
    GameObject objectUnderMouse;
    private long timeSlice = 17;
    private long currentTime;
    private long currentSlicedTime;
    private float mouseX;
    private float mouseY;
    private float originX;
    private float originY;
    private float scale = 1.2f;
    private float naturalScale = 1;
    private float worldWidth = 1;
    private float worldHeight = 1;
    float width;
    float height;
    private ResourceReader resourceReader;
    private boolean suspended;
    private long timeOffset;
    private long suspendTime;
    private volatile boolean outlinePainted;

    public Game() {
        currentSlicedTime = (currentTime / timeSlice) * timeSlice;
        Vec2 gravity = new Vec2(0, -9.8f);
        world = new World(gravity, false);
        suspendTime = System.currentTimeMillis();
        timeOffset = suspendTime;
        suspended = true;
        world.setContactListener(new ContactListener() {
            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
                for (ContactListener listener : contactListeners) {
                    listener.preSolve(contact, oldManifold);
                }
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
                for (ContactListener listener : contactListeners) {
                    listener.postSolve(contact, impulse);
                }
            }

            @Override
            public void endContact(Contact contact) {
                for (ContactListener listener : contactListeners) {
                    listener.endContact(contact);
                }
            }

            @Override
            public void beginContact(Contact contact) {
                for (ContactListener listener : contactListeners) {
                    listener.beginContact(contact);
                }
            }
        });
    }

    public void addListener(GameListener listener) {
        listeners.add(listener);
    }

    public void removeListener(GameListener listener) {
        listeners.remove(listener);
    }

    public void addContactListener(ContactListener listener) {
        contactListeners.add(listener);
    }

    public void removeContactListener(ContactListener listener) {
        contactListeners.remove(listener);
    }

    public boolean isOutlinePainted() {
        return outlinePainted;
    }

    public void setOutlinePainted(boolean outlinePainted) {
        this.outlinePainted = outlinePainted;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void suspend() {
        if (suspended) {
            return;
        }
        suspended = true;
        suspendTime = System.currentTimeMillis();
    }

    public void resume() {
        if (!suspended) {
            return;
        }
        suspended = false;
        timeOffset += System.currentTimeMillis() - suspendTime;
    }

    void cleanRemovedObjects() {
        if (!hasRemovedObjects) {
            return;
        }
        hasRemovedObjects = false;
        for (int i = 0; i < objects.size(); ++i) {
            if (objects.get(i).game != this) {
                objects.remove(i);
                --i;
            }
        }
    }

    private boolean actUntil(long time) {
        if (time <= this.currentTime) {
            return false;
        }
        boolean acted = false;
        while (true) {
            long nextTime = currentSlicedTime + timeSlice;
            if (nextTime > time) {
                break;
            }
            cleanRemovedObjects();
            world.clearForces();
            updateMouse();
            acted = true;
            for (GameObject object : new ArrayList<>(objects)) {
                object.time(nextTime);
            }
            currentSlicedTime = nextTime;
            world.step(timeSlice / 1000f, 25, 16);
        }
        currentTime = time;
        return acted;
    }

    private void updateMouse() {
        GameObject[] objectArray = objects.toArray(new GameObject[objects.size()]);
        Arrays.sort(objectArray, new Comparator<GameObject>() {
            @Override
            public int compare(GameObject o1, GameObject o2) {
                return o2.zIndex - o1.zIndex;
            }
        });
        GameObject selectedObject = null;
        for (GameObject object : objectArray) {
            if (object.hasPoint(mouseX, mouseY)) {
                selectedObject = object;
                break;
            }
        }
        if (selectedObject != objectUnderMouse) {
            if (objectUnderMouse != null) {
                objectUnderMouse.mouseLeave();
            }
            objectUnderMouse = selectedObject;
            if (objectUnderMouse != null) {
                objectUnderMouse.mouseEnter();
            }
        }
    }

    public World getWorld() {
        return world;
    }

    public long getTimeSlice() {
        return timeSlice;
    }

    public float getOriginX() {
        return originX;
    }

    public void setOriginX(float originX) {
        this.originX = originX;
    }

    public float getOriginY() {
        return originY;
    }

    public void setOriginY(float originY) {
        this.originY = originY;
    }

    public <T> T loadResources(Class<T> imagesType) {
        return resourceReader.getResourceSet(imagesType);
    }

    @Override
    public void mouseMove(int x, int y) {
        if (suspended) {
            return;
        }
        Rectangle rect = getViewRectangle();
        float tx = x - rect.x;
        float ty = this.height - y - rect.y;
        mouseX = tx / (scale * naturalScale);
        mouseY = ty / (scale * naturalScale);
        updateMouse();
    }

    protected float getMouseX() {
        return mouseX;
    }

    protected float getMouseY() {
        return mouseY;
    }

    @Override
    public void mouseDown() {
        if (suspended) {
            return;
        }
        GameObject[] objectArray = objects.toArray(new GameObject[objects.size()]);
        Arrays.sort(objectArray, new Comparator<GameObject>() {
            @Override public int compare(GameObject o1, GameObject o2) {
                return o2.zIndex - o1.zIndex;
            }
        });
        GameObject clickedObject = null;
        for (GameObject object : objectArray) {
            if (object.hasPoint(mouseX, mouseY)) {
                if (object.click()) {
                    clickedObject = object;
                    break;
                }
            }
        }
        if (clickedObject != null) {
            for (GameListener listener : listeners) {
                listener.objectClicked(clickedObject);
            }
        } else {
            for (GameListener listener : listeners) {
                listener.emptyAreaClicked(mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean idle() {
        return !suspended && actUntil((System.currentTimeMillis() - timeOffset) / 1);
    }

    @Override
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        rescale();
    }

    public void resizeWorld(float width, float height) {
        this.worldWidth = width;
        this.worldHeight = height;
        rescale();
    }

    private void rescale() {
        float horzScale = width / worldWidth;
        float vertScale = height / worldHeight;
        naturalScale = Math.min(horzScale, vertScale) * 1.25f;
    }

    @Override
    public void paint(Graphics graphics) {
        boolean outlinePainted = this.outlinePainted;
        Rectangle rect = getViewRectangle();
        graphics.setColor(Color.gray());
        graphics.fillRectangle(0, 0, width, height);
        graphics.translate(rect.x, height - rect.y);
        graphics.scale(scale * naturalScale, -scale * naturalScale);

        AffineTransform orig = graphics.getTransform();
        GameObject[] objectArray = objects.toArray(new GameObject[objects.size()]);
        Arrays.sort(objectArray, new Comparator<GameObject>() {
            @Override public int compare(GameObject o1, GameObject o2) {
                return Integer.compare(o1.getZIndex(), o2.getZIndex());
            }
        });
        paintBackground(graphics);
        for (GameObject object : objectArray) {
            object.paint(graphics);
        }
        graphics.setTransform(orig);

        if (outlinePainted) {
            graphics.setStrokeWidth(0.01f);
            for (Body body = world.getBodyList(); body != null; body = body.getNext()) {
                Vec2 pos = body.getPosition();
                graphics.translate(pos.x, pos.y);
                graphics.rotate(body.getAngle());
                for (Fixture fixture = body.getFixtureList(); fixture != null; fixture = fixture.getNext()) {
                    Shape shape = fixture.getShape();
                    if (shape instanceof PolygonShape) {
                        PolygonShape poly = (PolygonShape)fixture.getShape();
                        Vec2[] vertices = poly.getVertices();
                        Vec2 v = vertices[poly.getVertexCount() - 1];
                        graphics.setColor(Color.red());
                        graphics.moveTo(v.x, v.y);
                        for (int i = 0; i < poly.getVertexCount(); ++i) {
                            graphics.lineTo(vertices[i].x, vertices[i].y);
                        }
                        graphics.stroke();
                    } else if (shape instanceof CircleShape) {
                        CircleShape circle = (CircleShape)shape;
                        Vec2 v = circle.m_p;
                        graphics.drawEllipse(v.x - circle.m_radius, v.y - circle.m_radius,
                                2 * circle.m_radius, 2 * circle.m_radius);
                    }
                }
                graphics.setTransform(orig);
            }
        }
    }

    protected void paintBackground(@SuppressWarnings("unused") Graphics graphics) {
    }

    private Rectangle getViewRectangle() {
        float width = this.width;
        float height = this.height;
        float x = this.width / 2f - (originX * scale * naturalScale);
        float y = this.height / 2f - (originY * scale * naturalScale);
        x = Math.min(0, Math.max(x, -this.worldWidth * scale * naturalScale + this.width));
        y = Math.min(0, Math.max(y, -this.worldHeight * scale * naturalScale + this.height));
        return new Rectangle(x, y, width, height);
    }

    @Override
    public void start(EntryPointCallback callback) {
    }

    @Override
    public void setResourceReader(ResourceReader resourceReader) {
        this.resourceReader = resourceReader;
    }

    @Override
    public void interrupt() {
    }

    @Override
    public void keyDown(Key key) {
    }

    @Override
    public void keyUp(Key key) {
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    @Override
    public void mouseUp() {
    }
}
