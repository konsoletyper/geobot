package ru.geobot.game.objects;

import org.jbox2d.collision.shapes.MassData;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import ru.geobot.Game;
import ru.geobot.GameObject;
import ru.geobot.game.caves.Cave2;
import ru.geobot.graphics.Graphics;
import ru.geobot.graphics.ImageUtil;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class Bobbler extends GameObject {
    private Body body;
    private BobblerResources resources;
    private float waterLevel;

    public Bobbler(Game game) {
        super(game);
        resources = game.loadResources(BobblerResources.class);
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DYNAMIC;
        bodyDef.angle = (float)Math.PI / 2;
        bodyDef.position.set(Cave2.SCALE * 640, Cave2.SCALE * 20);
        body = game.getWorld().createBody(bodyDef);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 0.0004f;
        fixtureDef.filter.maskBits = 0x100;
        fixtureDef.filter.categoryBits = 0x100;
        fixtureDef.friction = 3;
        for (Shape shape : resources.shape().create(Cave2.SCALE)) {
            fixtureDef.shape = shape;
            body.createFixture(fixtureDef);
        }
        fixtureDef.density = 0.0022f;
        for (Shape shape : resources.loadShape().create(Cave2.SCALE)) {
            fixtureDef.shape = shape;
            body.createFixture(fixtureDef);
        }
    }

    public Body getBody() {
        return body;
    }

    public float getWaterLevel() {
        return waterLevel;
    }

    public void setWaterLevel(float waterLevel) {
        this.waterLevel = waterLevel;
    }

    @Override
    protected void paint(Graphics graphics) {
        graphics.pushTransform();
        ImageUtil image = new ImageUtil(resources.image());
        graphics.translate(body.getPosition().x, body.getPosition().y);
        graphics.rotate(body.getAngle());
        graphics.scale(Cave2.SCALE, -Cave2.SCALE);
        graphics.translate(0, -image.getHeight());
        image.draw(graphics);
        graphics.popTransform();
    }

    @Override
    protected boolean hasPoint(float x, float y) {
        return super.hasPoint(x, y);
    }

    @Override
    protected void time(long time) {
        Vec2 massCenter = new Vec2();
        float mass = 0;
        MassData md = new MassData();
        for (Fixture fixture = body.getFixtureList(); fixture != null; fixture = fixture.getNext()) {
            PolygonShape underwaterShape = VolumePartCalculator.calculate((PolygonShape)fixture.getShape(),
                    body.getTransform(), waterLevel);
            if (underwaterShape == null) {
                continue;
            }
            underwaterShape.computeMass(md, 0.002f);
            if (md.mass < 1E-10) {
                continue;
            }
            mass += md.mass;
            massCenter.addLocal(md.center.mul(md.mass));
        }
        if (md.mass < 1E-10) {
            return;
        }
        massCenter.mulLocal(1 / mass);
        body.applyForce(new Vec2(0, 4.5f * mass), massCenter);
    }
}
