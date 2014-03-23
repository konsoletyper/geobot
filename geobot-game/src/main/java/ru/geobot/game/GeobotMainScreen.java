package ru.geobot.game;

import ru.geobot.EntryPoint;
import ru.geobot.EntryPointCallback;
import ru.geobot.Key;
import ru.geobot.game.ui.MainMenu;
import ru.geobot.graphics.Graphics;
import ru.geobot.graphics.ImageUtil;
import ru.geobot.resources.ResourceReader;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class GeobotMainScreen implements EntryPoint {
    private static int buttonWidth = 120;
    private static int buttonHeight = 111;
    private static int buttonPadding = 20;
    private EntryPoint inner;
    private EntryPoint menu;
    private boolean displayingMenu = true;
    private long timeOffset;
    private long currentTime;
    private long suspendTime;
    private int width;
    private int height;
    private EntryPointCallback entryCallback;
    private ResourceReader resourceReader;
    private GameResources resources;
    private int mouseX;
    private int mouseY;

    public GeobotMainScreen() {
        menu = new MainMenu(this);
    }

    @Override
    public void mouseMove(int x, int y) {
        mouseX = x;
        mouseY = y;
        if (!displayingMenu) {
            inner.mouseMove(x, y);
        } else {
            menu.mouseMove(x, y);
        }
    }

    @Override
    public void mouseDown() {
        if (!displayingMenu) {
            if (!menuButtonHover()) {
                inner.mouseDown();
            } else {
                showMenu();
            }
        } else {
            menu.mouseDown();
        }
    }

    @Override
    public void mouseUp() {
        if (!displayingMenu) {
            if (!menuButtonHover()) {
                inner.mouseUp();
            }
        } else {
            menu.mouseUp();
        }
    }

    @Override
    public void keyDown(Key key) {
        if (!displayingMenu) {
            inner.keyDown(key);
        } else {
            menu.keyDown(key);
        }
    }

    @Override
    public void keyUp(Key key) {
        if (!displayingMenu) {
            inner.keyUp(key);
        } else {
            menu.keyUp(key);
        }
    }

    @Override
    public boolean idle(long time) {
        currentTime = time;
        if (!displayingMenu) {
            return inner.idle(time + timeOffset);
        } else {
            return false;
        }
    }

    @Override
    public void resize(int width, int height) {
        if (!displayingMenu) {
            inner.resize(width, height);
        } else {
            menu.resize(width, height);
        }
        this.width = width;
        this.height = height;
    }

    @Override
    public void paint(Graphics graphics) {
        if (!displayingMenu) {
            graphics.pushTransform();
            inner.paint(graphics);
            graphics.popTransform();
            ImageUtil menuButton = new ImageUtil(menuButtonHover() ? resources.menuButtonHover() :
                    resources.menuButton());
            menuButton.draw(graphics, width - buttonWidth - buttonPadding, height - buttonHeight - buttonPadding,
                    buttonWidth, buttonHeight);
        } else {
            menu.paint(graphics);
        }
    }

    private boolean menuButtonHover() {
        int x = mouseX - width + buttonPadding;
        int y = mouseY - height + buttonPadding;
        return x <= 0 && x > -buttonWidth && y <= 0 && y >= -buttonHeight;
    }

    @Override
    public void start(EntryPointCallback callback) {
        menu.start(callback);
        if (!displayingMenu) {
            inner.start(callback);
        } else {
            entryCallback = callback;
        }
    }

    @Override
    public void setResourceReader(ResourceReader resourceReader) {
        this.resourceReader = resourceReader;
        resources = resourceReader.getResourceSet(GameResources.class);
        if (inner != null) {
            inner.setResourceReader(resourceReader);
        }
        menu.setResourceReader(resourceReader);
    }

    @Override
    public void interrupt() {
        inner.interrupt();
    }

    public void showGame() {
        if (!displayingMenu) {
            return;
        }
        displayingMenu = false;
        timeOffset += currentTime - suspendTime;
        if (entryCallback != null) {
            inner.setResourceReader(resourceReader);
            inner.start(entryCallback);
            inner.resize(width, height);
            entryCallback = null;
        }
        inner.resize(width, height);
    }

    public void showMenu() {
        if (displayingMenu) {
            return;
        }
        displayingMenu = true;
        suspendTime = currentTime;
    }

    public void setMenu(EntryPoint menu) {
        this.menu = menu;
        menu.resize(width, height);
    }

    public void setInner(EntryPoint inner) {
        this.inner = inner;
        inner.setResourceReader(resourceReader);
        inner.resize(width, height);
    }
}
