package ru.geobot.game.objects;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import ru.geobot.Game;
import ru.geobot.GameObject;
import ru.geobot.graphics.Graphics;
import ru.geobot.resources.Image;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class BodyObject extends GameObject {
    Body body;
    Image image;
    float scale;

    BodyObject(Game game) {
        super(game);
    }

    @Override
    protected void paint(Graphics graphics) {
        graphics.pushTransform();
        Vec2 pos = body.getPosition();
        graphics.translate(pos.x, pos.y);
        graphics.rotate(body.getAngle());
        graphics.scale(scale, -scale);
        graphics.translate(0, -image.getHeight());
        image.draw(graphics);
        graphics.popTransform();
    }

    public Body getBody() {
        return body;
    }

    @Override
    protected boolean hasPoint(float x, float y) {
        Vec2 v = new Vec2(x, y);
        for (Fixture fixture = body.getFixtureList(); fixture != null; fixture = fixture.getNext()) {
            if (fixture.testPoint(v)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void destroy() {
        getGame().getWorld().destroyBody(body);
    }
}
