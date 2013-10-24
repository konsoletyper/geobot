package ru.geobot.game.objects;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.joints.RevoluteJointDef;
import ru.geobot.Game;
import ru.geobot.GameObject;
import ru.geobot.graphics.AffineTransform;
import ru.geobot.graphics.Color;
import ru.geobot.graphics.Graphics;
import ru.geobot.resources.Image;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class Rope extends GameObject {
    private Body[] parts;
    private Color color = Color.yellow();
    private float width;
    private Image image;
    private float chunkLength;
    private float imageScale;

    Rope(Game game, RopeFactory factory) {
        super(game);
        image = factory.image;
        this.chunkLength = factory.width / image.getHeight() * image.getWidth();
        this.width = factory.width;
        imageScale = factory.width / image.getHeight();

        BodyDef partDef = new BodyDef();
        partDef.type = BodyType.DYNAMIC;
        partDef.position = new Vec2(factory.startX, factory.startY);
        partDef.linearDamping = 0.05f;
        partDef.angularDamping = 0.05f;

        CircleShape endShape = new CircleShape();
        endShape.m_radius = factory.width / 2;
        PolygonShape axisShape = new PolygonShape();
        float axisLength = chunkLength - factory.width;
        float halfWidth = factory.width / 2;
        axisShape.set(new Vec2[] {
            new Vec2(-halfWidth, 0),
            new Vec2(halfWidth, 0),
            new Vec2(halfWidth, axisLength),
            new Vec2(-halfWidth, axisLength)
        }, 4);

        FixtureDef partFixtureDef = new FixtureDef();
        partFixtureDef.density = factory.density;
        partFixtureDef.restitution = factory.restitution;
        parts = new Body[factory.angles.size()];
        for (int i = 0; i < parts.length; ++i) {
            partDef.angle = factory.angles.get(i);
            parts[i] = getWorld().createBody(partDef);
            endShape.m_p.y = 0;
            partFixtureDef.shape = endShape;
            parts[i].createFixture(partFixtureDef);
            partFixtureDef.shape = axisShape;
            parts[i].createFixture(partFixtureDef);
            partFixtureDef.shape = endShape;
            endShape.m_p.y = axisLength;
            Fixture endFixture = parts[i].createFixture(partFixtureDef);
            partDef.position = endFixture.getBody().getWorldPoint(new Vec2(0, axisLength));
        }
        RevoluteJointDef jointDef = new RevoluteJointDef();
        jointDef.collideConnected = false;
        jointDef.localAnchorA = new Vec2(0, axisLength);
        jointDef.localAnchorB = new Vec2(0, 0);
        jointDef.lowerAngle = (float)-Math.PI / 4;
        jointDef.upperAngle = (float)Math.PI / 4;
        jointDef.enableLimit = true;
        for (int i = 1; i < parts.length; ++i) {
            jointDef.bodyA = parts[i - 1];
            jointDef.bodyB = parts[i];
            getWorld().createJoint(jointDef);
        }
    }

    public float getChunkLength() {
        return chunkLength;
    }

    public int partCount() {
        return parts.length;
    }

    public Body part(int index) {
        return parts[index];
    }

    @Override
    protected boolean hasPoint(float x, float y) {
        for (Body part : parts) {
            for (Fixture fixture = part.getFixtureList(); fixture != null;
                    fixture = fixture.getNext()) {
                if (fixture.testPoint(new Vec2(x, y))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void paint(Graphics graphics) {
        graphics.setColor(color);
        graphics.setStrokeWidth(width);
        AffineTransform transform = graphics.getTransform();
        for (int i = 0; i < parts.length; i++) {
            Vec2 v = parts[i].getPosition();
            graphics.translate(v.x - width / 2, v.y);
            graphics.rotate((float)Math.PI / 2 + parts[i].getAngle());
            graphics.scale(imageScale, imageScale);
            image.draw(graphics);
            graphics.setTransform(transform);
        }
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
