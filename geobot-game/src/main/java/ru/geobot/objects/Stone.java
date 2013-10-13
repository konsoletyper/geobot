package ru.geobot.objects;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import ru.geobot.Game;
import ru.geobot.GameObject;
import ru.geobot.graphics.Color;
import ru.geobot.graphics.Graphics;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class Stone extends GameObject {
    private Body body;
    private float radius;

    public Stone(Game game, float initialX, float initialY, float radius) {
        super(game);
        this.radius = radius;
        BodyDef bodyDef = new BodyDef();
        bodyDef.position = new Vec2(initialX, initialY);
        bodyDef.type = BodyType.DYNAMIC;
        body = getWorld().createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 0.2f;
        CircleShape shape = new CircleShape();
        shape.m_radius = radius;
        /*PolygonShape shape = new PolygonShape();
        shape.setAsBox(radius, radius);*/
        fixtureDef.shape = shape;
        fixtureDef.restitution = 0.7f;
        fixtureDef.friction = 0.9f;
        body.createFixture(fixtureDef);
    }

    @Override
    protected void destroy() {
        getWorld().destroyBody(body);
        super.destroy();
    }

    @Override
    protected void paint(Graphics graphics) {
        graphics.setColor(Color.orange());
        Vec2 center = body.getWorldCenter();
        graphics.fillEllipse(center.x - radius, center.y - radius,
                radius * 2, radius * 2);
    }
}
