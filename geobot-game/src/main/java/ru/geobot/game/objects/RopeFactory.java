package ru.geobot.game.objects;

import java.util.ArrayList;
import java.util.List;
import ru.geobot.Game;
import ru.geobot.resources.Image;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class RopeFactory {
    float width = 0.05f;
    float startX;
    float startY;
    List<Float> angles = new ArrayList<>();
    float restitution = 1f;
    float density = 1f;
    float resistance = 0f;
    Image image;

    public void clearChunks() {
        angles.clear();
    }

    public void addChunk(float angle) {
        angles.add(angle);
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getResistance() {
        return resistance;
    }

    public void setResistance(float resistance) {
        this.resistance = resistance;
    }

    public float getDensity() {
        return density;
    }

    public void setDensity(float density) {
        this.density = density;
    }

    public float getRestitution() {
        return restitution;
    }

    public void setRestitution(float restitution) {
        this.restitution = restitution;
    }

    public float getStartX() {
        return startX;
    }

    public void setStartX(float startX) {
        this.startX = startX;
    }

    public float getStartY() {
        return startY;
    }

    public void setStartY(float startY) {
        this.startY = startY;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public Rope create(Game game) {
        if (image == null) {
            image = game.loadResources(RopeImages.class).texture();
        }
        Rope rope = new Rope(game, this);
        return rope;
    }
}
