package ru.geobot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import ru.geobot.graphics.*;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class Game implements EntryPoint {
    private World world;
    List<GameObject> objects = new ArrayList<>();
    private List<GameListener> listeners = new ArrayList<>();
    boolean hasRemovedObjects;
    GameObject objectUnderMouse;
    GameObject clickedObject;
    private long timeSlice = 17;
    private long currentTime;
    private long currentSlicedTime;
    private float mouseX;
    private float mouseY;
    private float originX;
    private float originY;
    private float scale = 1.2f;
    private float naturalScale = 1;
    float width;
    float height;
    private ImageSource imageSource;

    public Game() {
        currentTime = System.currentTimeMillis();
        currentSlicedTime = (currentTime / timeSlice) * timeSlice;
        Vec2 gravity = new Vec2(0, -9.8f);
        world = new World(gravity, false);
    }

    public void addListener(GameListener listener) {
        listeners.add(listener);
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

    private void clickMouse() {
        if (objectUnderMouse != null) {
            objectUnderMouse.click();
        }
        for (GameListener listener : listeners) {
            listener.objectClicked(objectUnderMouse);
        }
    }

    public boolean actUntil(long time) {
        if (time < this.currentTime) {
            throw new IllegalArgumentException("Can't act until past");
        }
        if (time == this.currentTime) {
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
            for (GameObject object : objects) {
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

    public <T> T loadImages(Class<T> imagesType) {
        return imageSource.getImages(imagesType);
    }

    @Override
    public void mouseMove(int x, int y) {
        Rectangle rect = getViewRectangle();
        if (!rect.contains(x, y)) {
            return;
        }
        float tx = x - rect.x;
        float ty = this.height - y - rect.y;
        mouseX = tx / (scale * naturalScale);
        mouseY = ty / (scale * naturalScale);
        updateMouse();
        if (clickedObject != objectUnderMouse) {
            clickedObject = null;
        }
    }

    @Override
    public void mouseDown() {
        clickedObject = objectUnderMouse;
    }

    @Override
    public void mouseUp() {
        if (clickedObject != null) {
            clickMouse();
        }
    }

    @Override
    public boolean idle() {
        return actUntil(System.currentTimeMillis());
    }

    @Override
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        float worldWidth = 10.6666f;
        float worldHeight = 6f;
        float horzScale = width / worldWidth;
        float vertScale = height / worldHeight;
        naturalScale = Math.min(horzScale, vertScale);
    }

    @Override
    public void paint(Graphics graphics) {
        Rectangle rect = getViewRectangle();
        graphics.setColor(Color.gray());
        graphics.fillRectangle(0, 0, width, height);
        graphics.translate(rect.x, height - rect.y);
        graphics.scale(scale * naturalScale, -scale * naturalScale);

        AffineTransform orig = graphics.getTransform();
        GameObject[] objectArray = objects.toArray(new GameObject[objects.size()]);
        Arrays.sort(objectArray, new Comparator<GameObject>() {
            @Override
            public int compare(GameObject o1, GameObject o2) {
                return o1.zIndex - o2.zIndex;
            }
        });
        paintBackground(graphics);
        for (GameObject object : objects) {
            object.paint(graphics);
        }
        graphics.setTransform(orig);
    }

    protected void paintBackground(@SuppressWarnings("unused") Graphics graphics) {
    }

    private Rectangle getViewRectangle() {
        float width = this.width;
        float height = this.height;
        float x = this.width / 2f - (originX * scale * naturalScale);
        float y = this.height / 2f - (originY * scale * naturalScale);
        return new Rectangle(x, y, width, height);
    }

    @Override
    public void start(EntryPointCallback callback) {
    }

    @Override
    public void setImageSource(ImageSource imageSource) {
        this.imageSource = imageSource;
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
}
