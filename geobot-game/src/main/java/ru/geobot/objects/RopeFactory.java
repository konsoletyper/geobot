package ru.geobot.objects;

import java.util.ArrayList;
import java.util.List;
import ru.geobot.Game;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class RopeFactory {
    float width = 0.05f;
    float chunkLength = 0.1f;
    float startX;
    float startY;
    List<Float> angles = new ArrayList<>();
    float restitution = 1f;
    float density = 1f;
    float resistance = 0f;

    public void clearChunks() {
        angles.clear();
    }

    public void addChunk(float angle) {
        angles.add(angle);
    }

    public float getChunkLength() {
        return chunkLength;
    }

    public void setChunkLength(float chunkLength) {
        this.chunkLength = chunkLength;
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

    public Rope create(Game game) {
        if (chunkLength < width) {
            throw new IllegalStateException("Chunk length must be at least as long " +
                    "as width");
        }
        Rope rope = new Rope(game, this);
        return rope;
    }
}
