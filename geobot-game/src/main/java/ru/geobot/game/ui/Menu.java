package ru.geobot.game.ui;

import java.util.ArrayList;
import java.util.List;
import ru.geobot.EntryPoint;
import ru.geobot.EntryPointCallback;
import ru.geobot.Key;
import ru.geobot.graphics.Graphics;
import ru.geobot.resources.Image;
import ru.geobot.resources.ResourceReader;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public abstract class Menu implements EntryPoint {
    private List<Button> buttons = new ArrayList<>();
    private Button hoverButton;
    protected Image background;
    protected int screenWidth;
    protected int screenHeight;

    protected final void addButton(Button button) {
        buttons.add(button);
    }

    protected final void setBackground(Image image) {
        background = image;
    }

    @Override
    public void mouseMove(int x, int y) {
        x = unscale(x - getX());
        y = unscale(y - getY());
        Button newHoverButton = null;
        for (Button button : buttons) {
            if (!button.isEnabled()) {
                continue;
            }
            if (x >= button.left && x < button.right && y >= button.top && y < button.bottom) {
                newHoverButton = button;
                break;
            }
        }
        if (hoverButton != newHoverButton) {
            if (hoverButton != null) {
                hoverButton.mouseLeave();
            }
            hoverButton = newHoverButton;
            if (hoverButton != null) {
                hoverButton.mouseEnter();
            }
        }
    }

    @Override
    public void mouseDown() {
        if (hoverButton != null) {
            hoverButton.mouseClick();
        }
    }

    @Override
    public void mouseUp() {
    }

    @Override
    public void keyDown(Key key) {
    }

    @Override
    public void keyUp(Key key) {
    }

    @Override
    public boolean idle(long time) {
        return false;
    }

    @Override
    public void resize(int width, int height) {
        screenWidth = width;
        screenHeight = height;
    }

    private int getX() {
        return (screenWidth - getRealWidth()) / 2;
    }

    protected int getRealWidth() {
        return background != null ? scale(background.getWidth()) : 0;
    }

    private int getY() {
        return (screenHeight - getRealHeight()) / 2;
    }

    protected int getRealHeight() {
        return Math.max(16, screenHeight * 3 / 4);
    }

    private int scale(int n) {
        if (background == null) {
            return n;
        }
        return n * getRealHeight() / background.getHeight();
    }

    private int unscale(int n) {
        if (background == null) {
            return n;
        }
        return n * background.getHeight() / getRealHeight();
    }

    @Override
    public void paint(Graphics graphics) {
        if (background == null) {
            return;
        }
        graphics.pushTransform();
        graphics.translate(getX(), getY());
        graphics.scale((float)getRealWidth() / background.getWidth(), (float)getRealHeight() / background.getHeight());
        background.draw(graphics);
        for (Button button : buttons) {
            button.paint(graphics);
        }
        graphics.popTransform();
    }

    @Override
    public void start(EntryPointCallback callback) {
    }

    @Override
    public void setResourceReader(ResourceReader resourceReader) {
    }

    @Override
    public void interrupt() {
    }
}
