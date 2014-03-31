package ru.geobot.game;

import ru.geobot.EntryPoint;
import ru.geobot.EntryPointCallback;
import ru.geobot.Key;
import ru.geobot.graphics.Color;
import ru.geobot.graphics.Graphics;
import ru.geobot.resources.ResourceReader;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class GeobotEntryPoint implements EntryPoint, GeobotGameManager {
    private EntryPoint oldGame;
    private EntryPoint game;
    private long timeOffset;
    private long fadeLevel = 255;
    private long currentTime;
    private int width;
    private int height;
    private ResourceReader resourceReader;
    private boolean started;
    private EntryPointCallback callback;

    @Override
    public void mouseMove(int x, int y) {
        if (game != null && oldGame == null) {
            game.mouseMove(x, y);
        }
    }

    @Override
    public void mouseDown() {
        if (game != null && oldGame == null) {
            game.mouseDown();
        }
    }

    @Override
    public void mouseUp() {
        if (game != null && oldGame == null) {
            game.mouseUp();
        }
    }

    @Override
    public void keyDown(Key key) {
        if (game != null && oldGame == null) {
            game.keyDown(key);
        }
    }

    @Override
    public void keyUp(Key key) {
        if (game != null && oldGame == null) {
            game.keyUp(key);
        }
    }

    @Override
    public boolean idle(long time) {
        if (timeOffset == 0) {
            timeOffset = time;
        }
        currentTime = time;
        if (oldGame != null) {
            if (fadeLevel < 255) {
                fadeLevel = Math.min(255, (time - timeOffset) / 6);
            }
            if (oldGame != null && fadeLevel == 255) {
                oldGame = null;
                timeOffset = time;
            }
            return false;
        } else {
            if (fadeLevel > 0) {
                fadeLevel = Math.max(0, 255 - (time - timeOffset) / 6);
            }
            return game != null ? game.idle(time - timeOffset) : false;
        }
    }

    @Override
    public void resize(int width, int height) {
        if (game != null) {
            game.resize(width, height);
        }
        if (oldGame != null) {
            oldGame.resize(width, height);
        }
        this.width = width;
        this.height = height;
    }

    @Override
    public void paint(Graphics graphics) {
        if (fadeLevel == 255) {
            graphics.setColor(Color.black());
            graphics.fillRectangle(0, 0, width, height);
            return;
        }
        if (oldGame != null) {
            oldGame.paint(graphics);
        } else if (game != null) {
            game.paint(graphics);
        }

        if (fadeLevel > 0) {
            Color color = Color.black();
            color.a = (short)fadeLevel;
            graphics.setColor(color);
            graphics.fillRectangle(0, 0, width, height);
        }
    }

    @Override
    public void start(EntryPointCallback callback) {
        this.callback = callback;
        if (game != null) {
            game.start(callback);
        }
        started = true;
    }

    @Override
    public void setResourceReader(ResourceReader resourceReader) {
        if (game != null) {
            game.setResourceReader(resourceReader);
        }
        this.resourceReader = resourceReader;
    }

    @Override
    public void interrupt() {
        if (game != null) {
            game.interrupt();
        }
    }

    @Override
    public EntryPoint getGame() {
        return game;
    }

    @Override
    public void setGame(EntryPoint game) {
        if (game == this.game) {
            return;
        }
        oldGame = this.game;
        this.game = game;
        timeOffset = currentTime;
        if (game != null) {
            if (resourceReader != null) {
                game.setResourceReader(resourceReader);
            }
            if (started) {
                game.resize(width, height);
                game.start(callback);
                game.idle(0);
            }
        }
    }
}
