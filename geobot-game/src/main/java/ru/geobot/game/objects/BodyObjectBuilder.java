package ru.geobot.game.objects;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.FixtureDef;
import ru.geobot.Game;
import ru.geobot.resources.Image;
import ru.geobot.resources.PolygonalBodyFactory;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class BodyObjectBuilder {
    private Game game;
    private BodyDef bodyDef = new BodyDef();
    private FixtureDef fixtureDef = new FixtureDef();
    private Image image;
    private PolygonalBodyFactory shape;
    private float realHeight;

    public BodyObjectBuilder(Game game) {
        this.game = game;
    }

    public BodyDef getBodyDef() {
        return bodyDef;
    }

    public FixtureDef getFixtureDef() {
        return fixtureDef;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public void setShape(PolygonalBodyFactory shape) {
        this.shape = shape;
    }

    public void setRealHeight(float realHeight) {
        this.realHeight = realHeight;
    }

    public BodyObject build() {
        BodyObject object = new BodyObject(game);
        object.scale = realHeight / image.getHeight();
        object.image = image;
        object.body = game.getWorld().createBody(bodyDef);
        for (PolygonShape part : shape.create(object.scale)) {
            fixtureDef.shape = part;
            object.body.createFixture(fixtureDef);
        }
        return object;
    }
}
