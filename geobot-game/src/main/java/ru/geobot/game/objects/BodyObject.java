package ru.geobot.game.objects;

import java.util.List;
import org.jbox2d.collision.shapes.Shape;
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
    List<Shape> selectionShapes;
    Image image;
    float scale;
    int zIndex = 1;

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
        if (selectionShapes != null) {
            for (Shape shape : selectionShapes) {
                if (shape.testPoint(body.getTransform(), v)) {
                    return true;
                }
            }
        } else {
            for (Fixture fixture = body.getFixtureList(); fixture != null; fixture = fixture.getNext()) {
                if (fixture.testPoint(v)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    @Override
    protected void destroy() {
        getGame().getWorld().destroyBody(body);
    }

    public void changeZIndex(int zIndex) {
        super.setZIndex(zIndex);
    }
}
