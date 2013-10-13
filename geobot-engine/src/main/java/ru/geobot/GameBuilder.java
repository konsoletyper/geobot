package ru.geobot;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public abstract class GameBuilder {
    private float width = 10;
    private float height = 5;
    private long timeSlice = 17;

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public long getTimeSlice() {
        return timeSlice;
    }

    public void setTimeSlice(long timeSlice) {
        this.timeSlice = timeSlice;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public Game build() {
        Game game = createGame(timeSlice);
        game.width = width;
        game.height = height;
        return game;
    }

    protected abstract Game createGame(long timeSlice);
}
