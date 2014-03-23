package ru.geobot.game.ui;

import ru.geobot.graphics.Graphics;
import ru.geobot.graphics.ImageUtil;
import ru.geobot.resources.Image;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class Button {
    int left;
    int top;
    int right;
    int bottom;
    private Image hoverImage;
    private boolean hover;
    private Runnable clickHandler;
    private boolean enabled = true;

    public Button(int left, int top, int right, int bottom, Image hoverImage) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.hoverImage = hoverImage;
    }

    public Button(int left, int top, Image hoverImage) {
        this(left, top, left + hoverImage.getWidth(), top + hoverImage.getHeight(), hoverImage);
    }

    public void setClickHandler(Runnable clickHandler) {
        this.clickHandler = clickHandler;
    }

    void mouseEnter() {
        hover = true;
    }

    void mouseLeave() {
        hover = false;
    }

    void mouseClick() {
        if (clickHandler != null && hover) {
            clickHandler.run();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    void paint(Graphics graphics) {
        if (hover) {
            new ImageUtil(hoverImage).draw(graphics, left, top, right - left, bottom - top);
        }
    }
}
