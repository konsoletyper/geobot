package ru.geobot.game.objects;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.joints.RevoluteJoint;
import org.jbox2d.dynamics.joints.RevoluteJointDef;
import ru.geobot.GameAdapter;
import ru.geobot.GameObject;
import ru.geobot.game.GeobotGame;
import ru.geobot.game.caves.Cave2;
import ru.geobot.graphics.Graphics;
import ru.geobot.graphics.ImageUtil;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class ControlPanelHandle extends GameObject {
    private ControlPanelResources resources;
    private GeobotGame game;
    private Body body;
    private RevoluteJoint robotArmJoint;
    private float angle;

    public ControlPanelHandle(GeobotGame game, float x, float y) {
        super(game);
        this.game = game;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DYNAMIC;
        bodyDef.position.x = x;
        bodyDef.position.y = y;
        bodyDef.fixedRotation = true;
        body = game.getWorld().createBody(bodyDef);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 0.1f;
        CircleShape shape = new CircleShape();
        shape.m_radius = 21 * Cave2.SCALE;
        fixtureDef.shape = shape;
        fixtureDef.filter.categoryBits = 2;
        fixtureDef.filter.maskBits = 2;
        body.createFixture(fixtureDef);
        resources = game.loadResources(ControlPanelResources.class);
    }

    public Body getBody() {
        return body;
    }

    @Override
    protected void paint(Graphics graphics) {
        ImageUtil image = new ImageUtil(resources.handle());
        Vec2 pos = body.getPosition();
        graphics.pushTransform();
        graphics.translate(pos.x, pos.y);
        graphics.scale(Cave2.SCALE, Cave2.SCALE);
        graphics.rotate(body.getAngle());
        image.draw(graphics, 7, 7, 14, -14);
        graphics.popTransform();
    }

    @Override
    protected boolean hasPoint(float x, float y) {
        float radius = 25 * Cave2.SCALE;
        Vec2 pt = new Vec2(x, y);
        pt.subLocal(body.getPosition());
        return pt.lengthSquared() < radius * radius;
    }

    @Override
    protected boolean click() {
        if (robotArmJoint != null) {
            game.getRobot().setCarriesObject(false);
            game.getWorld().destroyJoint(robotArmJoint);
            robotArmJoint = null;
            game.removeListener(mouseMotionListener);
            game.getRobot().setArmForced(true);
            return true;
        }
        if (game.getRobot().isCarriesObject()) {
            return false;
        }
        Vec2 pt = body.getPosition();
        Vec2 dir = new Vec2((float)Math.cos(body.getAngle()), (float)Math.sin(body.getAngle()));
        final Vec2 rel = dir.mul(14 * Cave2.SCALE);
        pt.addLocal(rel);
        game.getRobot().pickAt(pt.x, pt.y, new Runnable() {
            @Override public void run() {
                RevoluteJointDef jointDef = new RevoluteJointDef();
                jointDef.bodyA = game.getRobot().getHand();
                jointDef.bodyB = body;
                jointDef.localAnchorA.set(game.getRobot().getHandPickPoint());
                jointDef.localAnchorB.set(new Vec2(Cave2.SCALE * 14, 0));
                robotArmJoint = (RevoluteJoint)game.getWorld().createJoint(jointDef);
                game.getRobot().setArmForced(false);
                game.addListener(mouseMotionListener);
            }
        });
        return true;
    }

    private GameAdapter mouseMotionListener = new GameAdapter() {
        @Override public void mouseMoved(float x, float y) {
            final Vec2 pt = body.getPosition();
            Vec2 dir = new Vec2(x, y).sub(pt);
            if (dir.length() < 1E-5f) {
                return;
            }
            float newAngle = (float)Math.atan2(dir.y, dir.x);
            float partialCurrentAngle = (float)Math.IEEEremainder(angle, Math.PI * 2);
            float delta = newAngle - partialCurrentAngle;
            if (delta < -Math.PI) {
                delta += Math.PI * 2;
            } else if (delta > Math.PI) {
                delta -= Math.PI * 2;
            }
            angle += delta;
            body.setTransform(body.getPosition(), angle);
        };
    };

    public float getAngle() {
        return angle;
    }
}
