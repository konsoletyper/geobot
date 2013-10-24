package ru.geobot.game.objects;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.joints.*;
import ru.geobot.Game;
import ru.geobot.GameObject;
import ru.geobot.Key;
import ru.geobot.graphics.AffineTransform;
import ru.geobot.graphics.Color;
import ru.geobot.graphics.Graphics;
import ru.geobot.graphics.ImageUtil;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class Robot extends GameObject {
    private RobotImages images;
    private Body body;
    private Body leftWheel;
    private Body rightWheel;
    private Body leftSmallWheel;
    private Body rightSmallWheel;
    private Body[] antenna;
    private Body leftAxle;
    private Body leftSmallAxle;
    private Body rightAxle;
    private Body rightSmallAxle;
    private RevoluteJoint leftWheelJoint;
    private RevoluteJoint rightWheelJoint;
    private RevoluteJoint leftSmallWheelJoint;
    private RevoluteJoint rightSmallWheelJoint;
    private PrismaticJoint leftAxleJoint;
    private PrismaticJoint rightAxleJoint;
    private PrismaticJoint leftSmallAxleJoint;
    private PrismaticJoint rightSmallAxleJoint;
    private boolean movingRight;
    private boolean movingLeft;
    private float initialX;
    private float initialY;
    private static final float SCALE = 1.1f / 357f;
    private boolean movingUp;
    private float vertOffset = 0;

    public Robot(Game game, float x, float y) {
        super(game);
        images = game.loadResources(RobotImages.class);
        this.initialX = x;
        this.initialY = y;
        createBody();
        createLeftWheel();
        createRightWheel();
        createLeftSmallWheel();
        createRightSmallWheel();
        createAntenna();
        Vec2 pos = body.getWorldCenter();
        getGame().setOriginX(pos.x);
        getGame().setOriginY(pos.y + vertOffset + 1.4f);
        setZIndex(256);
    }

    private void createBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.x = initialX;
        bodyDef.position.y = initialY;
        bodyDef.type = BodyType.DYNAMIC;
        body = getWorld().createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 5;
        fixtureDef.filter.categoryBits = 1;
        fixtureDef.filter.maskBits = 1;
        fixtureDef.shape = scaledRectShape(54.678f, 70.223f, 152.500f, 102.500f);
        body.createFixture(fixtureDef);

        fixtureDef.density = 2;
        fixtureDef.shape = scaledRectShape(105.178f, 170.223f, 62.000f, 28.500f);
        body.createFixture(fixtureDef);

        fixtureDef.shape = scaledRectShape(63.178f, 191.223f, 126f, 89f);
        body.createFixture(fixtureDef);

        fixtureDef.shape = scaledRectShape(120.011f, 280.034f, 13.333f, 20.521f);
        body.createFixture(fixtureDef);
    }

    private void createLeftWheel() {
        BodyDef wheelDef = new BodyDef();
        wheelDef.position.x = initialX + scale(39.345f);
        wheelDef.position.y = initialY + scale(37.223f);
        wheelDef.type = BodyType.DYNAMIC;
        wheelDef.angularDamping = 0.1f;
        leftWheel = getWorld().createBody(wheelDef);

        FixtureDef wheelFixtureDef = new FixtureDef();
        wheelFixtureDef.density = 15;
        wheelFixtureDef.filter.categoryBits = 1;
        wheelFixtureDef.filter.maskBits = 1;
        CircleShape wheelShape = new CircleShape();
        wheelShape.m_radius = scale(33.17f);
        wheelFixtureDef.shape = wheelShape;
        wheelFixtureDef.restitution = 0.95f;
        wheelFixtureDef.friction = 0.95f;
        leftWheel.createFixture(wheelFixtureDef);

        BodyDef axleDef = new BodyDef();
        axleDef.position.x = initialX + scale(68.713f);
        axleDef.position.y = initialY + scale(74.075f);
        axleDef.angle = (float)Math.PI * 240 / 180;
        axleDef.type = BodyType.DYNAMIC;
        axleDef.linearDamping = 0.1f;
        leftAxle = getWorld().createBody(axleDef);

        FixtureDef axleFixtureDef = new FixtureDef();
        axleFixtureDef.density = 5;
        axleFixtureDef.shape = scaledPoly(new Vec2(0, 3.93f), new Vec2(0, -3.93f),
                new Vec2(53.54f, -3.93f), new Vec2(53.54f, 3.93f));
        axleFixtureDef.filter.categoryBits = 1;
        axleFixtureDef.filter.maskBits = 1;
        leftAxle.createFixture(axleFixtureDef);

        PrismaticJointDef axleJointDef = new PrismaticJointDef();
        axleJointDef.bodyA = body;
        axleJointDef.localAnchorA.x = scale(68.713f);
        axleJointDef.localAnchorA.y = scale(74.075f);
        axleJointDef.bodyB = leftAxle;
        axleJointDef.localAnchorB.x = 0;
        axleJointDef.localAnchorB.y = 0;
        axleJointDef.lowerTranslation = -0.02f;
        axleJointDef.upperTranslation = 0.002f;
        axleJointDef.motorSpeed = 2f;
        axleJointDef.maxMotorForce = 100f;
        axleJointDef.enableLimit = true;
        axleJointDef.enableMotor = true;
        axleJointDef.referenceAngle = 240f * (float)Math.PI / 180;
        axleJointDef.localAxis1.x = (float)Math.cos(axleJointDef.referenceAngle);
        axleJointDef.localAxis1.y = (float)Math.sin(axleJointDef.referenceAngle);
        leftAxleJoint = (PrismaticJoint)getWorld().createJoint(axleJointDef);

        RevoluteJointDef wheelJointDef = new RevoluteJointDef();
        wheelJointDef.bodyA = leftAxle;
        wheelJointDef.localAnchorA = scale(new Vec2(53.54f, 0));
        wheelJointDef.bodyB = leftWheel;
        wheelJointDef.localAnchorB = new Vec2(0, 0);
        wheelJointDef.maxMotorTorque = 2;
        wheelJointDef.enableMotor = true;
        leftWheelJoint = (RevoluteJoint)getWorld().createJoint(wheelJointDef);
    }

    private void createRightWheel() {
        BodyDef wheelDef = new BodyDef();
        wheelDef.position.x = initialX + scale(222.024f);
        wheelDef.position.y = initialY + scale(37.223f);
        wheelDef.type = BodyType.DYNAMIC;
        wheelDef.angularDamping = 0.1f;
        rightWheel = getWorld().createBody(wheelDef);

        FixtureDef wheelFixtureDef = new FixtureDef();
        wheelFixtureDef.density = 15;
        wheelFixtureDef.filter.categoryBits = 1;
        wheelFixtureDef.filter.maskBits = 1;
        CircleShape wheelShape = new CircleShape();
        wheelShape.m_radius = scale(33.17f);
        wheelFixtureDef.shape = wheelShape;
        wheelFixtureDef.restitution = 0.05f;
        wheelFixtureDef.friction = 0.95f;
        rightWheel.createFixture(wheelFixtureDef);

        BodyDef axleDef = new BodyDef();
        axleDef.position.x = initialX + scale(192.032f);
        axleDef.position.y = initialY + scale(74.075f);
        axleDef.angle = -(float)Math.PI * 60f / 180f;
        axleDef.type = BodyType.DYNAMIC;
        axleDef.linearDamping = 0.16f;
        rightAxle = getWorld().createBody(axleDef);

        FixtureDef axleFixtureDef = new FixtureDef();
        axleFixtureDef.density = 5;
        axleFixtureDef.shape = scaledPoly(new Vec2(0, 3.93f), new Vec2(0, -3.93f),
                new Vec2(53.54f, -3.93f), new Vec2(53.54f, 3.93f));
        axleFixtureDef.filter.categoryBits = 1;
        axleFixtureDef.filter.maskBits = 1;
        rightAxle.createFixture(axleFixtureDef);

        PrismaticJointDef axleJointDef = new PrismaticJointDef();
        axleJointDef.bodyA = body;
        axleJointDef.localAnchorA.x = scale(192.032f);
        axleJointDef.localAnchorA.y = scale(74.075f);
        axleJointDef.bodyB = rightAxle;
        axleJointDef.localAnchorB.x = 0;
        axleJointDef.localAnchorB.y = 0;
        axleJointDef.lowerTranslation = -0.02f;
        axleJointDef.upperTranslation = 0.002f;
        axleJointDef.motorSpeed = 3f;
        axleJointDef.maxMotorForce = 100f;
        axleJointDef.enableLimit = true;
        axleJointDef.enableMotor = true;
        axleJointDef.referenceAngle = -60f * (float)Math.PI / 180;
        axleJointDef.localAxis1.x = (float)Math.cos(axleJointDef.referenceAngle);
        axleJointDef.localAxis1.y = (float)Math.sin(axleJointDef.referenceAngle);
        rightAxleJoint = (PrismaticJoint)getWorld().createJoint(axleJointDef);

        RevoluteJointDef wheelJointDef = new RevoluteJointDef();
        wheelJointDef.bodyA = rightAxle;
        wheelJointDef.localAnchorA = scale(new Vec2(53.54f, 0));
        wheelJointDef.bodyB = rightWheel;
        wheelJointDef.localAnchorB = new Vec2(0, 0);
        wheelJointDef.maxMotorTorque = 2;
        wheelJointDef.enableMotor = true;
        rightWheelJoint = (RevoluteJoint)getWorld().createJoint(wheelJointDef);
    }

    private void createLeftSmallWheel() {
        BodyDef wheelDef = new BodyDef();
        wheelDef.position.x = initialX + scale(111.648f);
        wheelDef.position.y = initialY + scale(25.549f);
        wheelDef.type = BodyType.DYNAMIC;
        wheelDef.angularDamping = 0.1f;
        leftSmallWheel = getWorld().createBody(wheelDef);

        FixtureDef wheelFixtureDef = new FixtureDef();
        wheelFixtureDef.density = 3;
        CircleShape wheelShape = new CircleShape();
        wheelShape.m_radius = scale(15.167f);
        wheelFixtureDef.shape = wheelShape;
        wheelFixtureDef.restitution = 0.95f;
        wheelFixtureDef.friction = 0.95f;
        wheelFixtureDef.filter.categoryBits = 1;
        wheelFixtureDef.filter.maskBits = 1;
        leftSmallWheel.createFixture(wheelFixtureDef);

        BodyDef axleDef = new BodyDef();
        axleDef.position.x = initialX + scale(111.648f);
        axleDef.position.y = initialY + scale(74.197f);
        axleDef.angle = (float)Math.PI * 270 / 180;
        axleDef.type = BodyType.DYNAMIC;
        axleDef.linearDamping = 0.1f;
        leftSmallAxle = getWorld().createBody(axleDef);

        FixtureDef axleFixtureDef = new FixtureDef();
        axleFixtureDef.density = 5;
        axleFixtureDef.shape = scaledPoly(new Vec2(0, 3.166f), new Vec2(0, -3.166f),
                new Vec2(46f, -3.166f), new Vec2(46f, 3.166f));
        axleFixtureDef.filter.categoryBits = 1;
        axleFixtureDef.filter.maskBits = 1;
        leftSmallAxle.createFixture(axleFixtureDef);

        PrismaticJointDef axleJointDef = new PrismaticJointDef();
        axleJointDef.bodyA = body;
        axleJointDef.localAnchorA.x = scale(111.648f);
        axleJointDef.localAnchorA.y = scale(74.197f);
        axleJointDef.bodyB = leftSmallAxle;
        axleJointDef.localAnchorB.x = 0;
        axleJointDef.localAnchorB.y = 0;
        axleJointDef.lowerTranslation = 0.02f;
        axleJointDef.upperTranslation = 0.051f;
        axleJointDef.motorSpeed = 2f;
        axleJointDef.maxMotorForce = 100f;
        axleJointDef.enableLimit = true;
        axleJointDef.enableMotor = true;
        axleJointDef.referenceAngle = 270f * (float)Math.PI / 180;
        axleJointDef.localAxis1.x = (float)Math.cos(axleJointDef.referenceAngle);
        axleJointDef.localAxis1.y = (float)Math.sin(axleJointDef.referenceAngle);
        leftSmallAxleJoint = (PrismaticJoint)getWorld().createJoint(axleJointDef);

        RevoluteJointDef wheelJointDef = new RevoluteJointDef();
        wheelJointDef.bodyA = leftSmallAxle;
        wheelJointDef.localAnchorA = scale(new Vec2(46f, 0));
        wheelJointDef.bodyB = leftSmallWheel;
        wheelJointDef.localAnchorB = new Vec2(0, 0);
        leftSmallWheelJoint = (RevoluteJoint)getWorld().createJoint(wheelJointDef);

        GearJointDef bigWheelJointDef = new GearJointDef();
        bigWheelJointDef.bodyA = leftWheel;
        bigWheelJointDef.joint1 = leftWheelJoint;
        bigWheelJointDef.bodyB = leftSmallWheel;
        bigWheelJointDef.joint2 = leftSmallWheelJoint;
        bigWheelJointDef.ratio = -15.167f / 33.17f;
        getWorld().createJoint(bigWheelJointDef);
    }

    private void createRightSmallWheel() {
        BodyDef wheelDef = new BodyDef();
        wheelDef.position.x = initialX + scale(160.516f);
        wheelDef.position.y = initialY + scale(25.549f);
        wheelDef.type = BodyType.DYNAMIC;
        wheelDef.angularDamping = 0.1f;
        rightSmallWheel = getWorld().createBody(wheelDef);

        FixtureDef wheelFixtureDef = new FixtureDef();
        wheelFixtureDef.density = 3;
        CircleShape wheelShape = new CircleShape();
        wheelShape.m_radius = scale(15.167f);
        wheelFixtureDef.shape = wheelShape;
        wheelFixtureDef.restitution = 0.95f;
        wheelFixtureDef.friction = 0.95f;
        wheelFixtureDef.filter.categoryBits = 1;
        wheelFixtureDef.filter.maskBits = 1;
        rightSmallWheel.createFixture(wheelFixtureDef);

        BodyDef axleDef = new BodyDef();
        axleDef.position.x = initialX + scale(160.516f);
        axleDef.position.y = initialY + scale(74.197f);
        axleDef.angle = (float)Math.PI * 270 / 180;
        axleDef.type = BodyType.DYNAMIC;
        axleDef.linearDamping = 0.1f;
        rightSmallAxle = getWorld().createBody(axleDef);

        FixtureDef axleFixtureDef = new FixtureDef();
        axleFixtureDef.density = 5;
        axleFixtureDef.shape = scaledPoly(new Vec2(0, 3.166f), new Vec2(0, -3.166f),
                new Vec2(46f, -3.166f), new Vec2(46f, 3.166f));
        axleFixtureDef.filter.categoryBits = 1;
        axleFixtureDef.filter.maskBits = 1;
        rightSmallAxle.createFixture(axleFixtureDef);

        PrismaticJointDef axleJointDef = new PrismaticJointDef();
        axleJointDef.bodyA = body;
        axleJointDef.localAnchorA.x = scale(160.516f);
        axleJointDef.localAnchorA.y = scale(74.197f);
        axleJointDef.bodyB = rightSmallAxle;
        axleJointDef.localAnchorB.x = 0;
        axleJointDef.localAnchorB.y = 0;
        axleJointDef.lowerTranslation = 0.02f;
        axleJointDef.upperTranslation = 0.051f;
        axleJointDef.motorSpeed = 2f;
        axleJointDef.maxMotorForce = 100f;
        axleJointDef.enableLimit = true;
        axleJointDef.enableMotor = true;
        axleJointDef.referenceAngle = 270f * (float)Math.PI / 180;
        axleJointDef.localAxis1.x = (float)Math.cos(axleJointDef.referenceAngle);
        axleJointDef.localAxis1.y = (float)Math.sin(axleJointDef.referenceAngle);
        rightSmallAxleJoint = (PrismaticJoint)getWorld().createJoint(axleJointDef);

        RevoluteJointDef wheelJointDef = new RevoluteJointDef();
        wheelJointDef.bodyA = rightSmallAxle;
        wheelJointDef.localAnchorA = scale(new Vec2(46f, 0));
        wheelJointDef.bodyB = rightSmallWheel;
        wheelJointDef.localAnchorB = new Vec2(0, 0);
        rightSmallWheelJoint = (RevoluteJoint)getWorld().createJoint(wheelJointDef);

        GearJointDef bigWheelJointDef = new GearJointDef();
        bigWheelJointDef.bodyA = rightWheel;
        bigWheelJointDef.joint1 = rightWheelJoint;
        bigWheelJointDef.bodyB = rightSmallWheel;
        bigWheelJointDef.joint2 = rightSmallWheelJoint;
        bigWheelJointDef.ratio = -15.167f / 33.17f;
        getWorld().createJoint(bigWheelJointDef);
    }

    private void createAntenna() {
        float[] distances = { 22f, 26f };
        antenna = new Body[3];
        BodyDef partDef = new BodyDef();
        partDef.type = BodyType.DYNAMIC;
        partDef.angle = 100 / 180f * (float)Math.PI;
        partDef.position.x = initialX + scale(92.413f);
        partDef.position.y = initialY + scale(280.082f);
        antenna[0] = getWorld().createBody(partDef);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.restitution = 0.7f;
        fixtureDef.friction = 0.3f;
        fixtureDef.density = 0.5f;
        fixtureDef.shape = scaledRectShape(0, -9.230f / 2, 23.750f, 9.230f);
        fixtureDef.filter.categoryBits = 1;
        fixtureDef.filter.maskBits = 1;
        antenna[0].createFixture(fixtureDef);

        partDef.position = antenna[0].getWorldPoint(scale(new Vec2(22f, 0)));
        antenna[1] = getWorld().createBody(partDef);
        fixtureDef.shape = scaledRectShape(0, -5.292f / 2, 27.713f, 5.292f);
        antenna[1].createFixture(fixtureDef);

        partDef.position = antenna[1].getWorldPoint(scale(new Vec2(26, 0)));
        antenna[2] = getWorld().createBody(partDef);
        fixtureDef.shape = scaledRectShape(0, -2.372f, 28.324f, 2.372f);
        antenna[2].createFixture(fixtureDef);

        RevoluteJointDef jointDef = new RevoluteJointDef();
        jointDef.lowerAngle = -2f * (float)Math.PI / 180;
        jointDef.upperAngle = 2f * (float)Math.PI / 180;
        jointDef.enableLimit = true;
        for (int i = 1; i < antenna.length; ++i) {
            jointDef.bodyA = antenna[i - 1];
            jointDef.bodyB = antenna[i];
            jointDef.localAnchorA = scale(new Vec2(distances[i - 1], 0));
            getWorld().createJoint(jointDef);
        }
        jointDef.bodyA = body;
        jointDef.bodyB = antenna[0];
        jointDef.localAnchorA = scale(new Vec2(92.413f, 280.082f));
        jointDef.referenceAngle = partDef.angle;
        getWorld().createJoint(jointDef);
    }

    private PolygonShape scaledRectShape(float x, float y, float w, float h) {
        PolygonShape shape = new PolygonShape();
        float x1 = scale(x);
        float y1 = scale(y);
        float x2 = scale(x + w);
        float y2 = scale(y + h);
        shape.set(new Vec2[] { new Vec2(x1, y1), new Vec2(x2, y1), new Vec2(x2, y2),
                new Vec2(x1, y2)}, 4);
        return shape;
    }

    private PolygonShape scaledPoly(Vec2... vs) {
        PolygonShape shape = new PolygonShape();
        shape.set(scale(vs), vs.length);
        return shape;
    }

    private float scale(float x) {
        return x * SCALE;
    }

    private Vec2 scale(Vec2 v) {
        return new Vec2(scale(v.x), scale(v.y));
    }

    private Vec2[] scale(Vec2[] vs) {
        Vec2[] result = new Vec2[vs.length];
        for (int i = 0; i < vs.length; ++i) {
            result[i] = scale(vs[i]);
        }
        return result;
    }

    public void keyDown(Key key) {
        switch (key) {
            case RIGHT:
                movingRight = true;
                break;
            case LEFT:
                movingLeft = true;
                break;
            case UP:
                movingUp = true;
                break;
            default:
                break;
        }
    }

    public void keyUp(Key key) {
        switch (key) {
            case RIGHT:
                movingRight = false;
                break;
            case LEFT:
                movingLeft = false;
                break;
            case UP:
                movingUp = false;
                break;
            default:
                break;
        }
    }

    @Override
    protected void time(long time) {
        Vec2 velocity = body.getLocalVector(body.getLinearVelocity());
        if (movingRight) {
            if (velocity.x < 1.5f) {
                leftWheelJoint.setMotorSpeed(-10f);
                rightWheelJoint.setMotorSpeed(-10f);
            }
        } else if (movingLeft) {
            if (velocity.x > -1.5f) {
                leftWheelJoint.setMotorSpeed(10f);
                rightWheelJoint.setMotorSpeed(10f);
            }
        } else {
            leftWheelJoint.setMotorSpeed(0);
            rightWheelJoint.setMotorSpeed(0);
        }
        if (movingUp) {
            vertOffset = Math.min(vertOffset + 0.1f, 4);
        } else {
            vertOffset = Math.max(vertOffset - 0.1f, 0);
        }
        Vec2 pos = body.getWorldCenter();
        getGame().setOriginX(pos.x);
        getGame().setOriginY(pos.y + vertOffset + 1.4f);
        float delta = leftAxleJoint.getUpperLimit() - leftAxleJoint.getJointTranslation();
        leftAxleJoint.setMotorSpeed(0.2f + 3 * delta);
        leftAxleJoint.setMaxMotorForce(10 + 50 * Math.abs(delta));
        delta = rightAxleJoint.getUpperLimit() - rightAxleJoint.getJointTranslation();
        rightAxleJoint.setMotorSpeed(0.2f + 3 * delta);
        rightAxleJoint.setMaxMotorForce(10 + 50 * Math.abs(delta));
        delta = leftSmallAxleJoint.getUpperLimit() - leftSmallAxleJoint.getJointTranslation();
        leftSmallAxleJoint.setMotorSpeed(0.2f + 3 * delta);
        leftSmallAxleJoint.setMaxMotorForce(5 + 25 * Math.abs(delta));
        delta = leftSmallAxleJoint.getUpperLimit() - rightSmallAxleJoint.getJointTranslation();
        rightSmallAxleJoint.setMotorSpeed(0.2f + 3 * delta);
        rightSmallAxleJoint.setMaxMotorForce(5 + 25 * Math.abs(delta));
    }

    @Override
    protected void paint(Graphics graphics) {
        Vec2 pos;
        AffineTransform transform = graphics.getTransform();
        ImageUtil bodyImage = new ImageUtil(images.body());
        ImageUtil wheelImage = new ImageUtil(images.wheel());
        ImageUtil damperImage = new ImageUtil(images.damper());
        ImageUtil axleImage = new ImageUtil(images.axle());
        ImageUtil smallWheelImage = new ImageUtil(images.smallWheel());
        ImageUtil smallAxleImage = new ImageUtil(images.smallAxle());
        ImageUtil smallDamperImage = new ImageUtil(images.smallDamper());

        graphics.setTransform(transform);
        drawTrack(graphics, leftWheel.getPosition(), scale(33.17f), leftSmallWheel.getPosition(),
                scale(15.167f), scale(3.5f));
        drawTrack(graphics, rightWheel.getPosition(), scale(33.17f), rightSmallWheel.getPosition(),
                scale(15.167f), scale(3.5f));

        graphics.setTransform(transform);
        pos = leftWheel.getPosition();
        graphics.translate(pos.x, pos.y);
        graphics.rotate(leftWheel.getAngle());
        graphics.scale(SCALE, SCALE);
        graphics.translate(-33.166f, -33.166f);
        wheelImage.draw(graphics, -6.178f, -4.056f, 77.386f, 75.832f);

        graphics.setTransform(transform);
        pos = rightWheel.getWorldCenter();
        graphics.translate(pos.x, pos.y);
        graphics.rotate(rightWheel.getAngle());
        graphics.scale(SCALE, SCALE);
        graphics.translate(-33.166f, -33.166f);
        wheelImage.draw(graphics, -6.178f, -4.056f, 77.386f, 75.832f);

        graphics.setTransform(transform);
        pos = leftSmallWheel.getPosition();
        graphics.translate(pos.x, pos.y);
        graphics.rotate(leftSmallWheel.getAngle());
        graphics.scale(SCALE, SCALE);
        graphics.translate(-30.334f / 2f, -30.334f / 2f);
        smallWheelImage.draw(graphics, 0, 0, 30.334f, 30.334f);

        graphics.setTransform(transform);
        pos = rightSmallWheel.getPosition();
        graphics.translate(pos.x, pos.y);
        graphics.rotate(rightSmallWheel.getAngle());
        graphics.scale(SCALE, SCALE);
        graphics.translate(-30.334f / 2f, -30.334f / 2f);
        smallWheelImage.draw(graphics, 0, 0, 30.334f, 30.334f);

        graphics.setTransform(transform);
        pos = leftAxle.getPosition();
        graphics.translate(pos.x, pos.y);
        graphics.rotate(leftAxle.getAngle());
        graphics.scale(SCALE, SCALE);
        axleImage.draw(graphics, 0, -7.863f / 2, 53.547f, 7.863f);

        pos = rightAxle.getPosition();
        graphics.setTransform(transform);
        graphics.translate(pos.x, pos.y);
        graphics.rotate(rightAxle.getAngle());
        graphics.scale(SCALE, SCALE);
        axleImage.draw(graphics, 0, -7.863f / 2, 53.547f, 7.863f);

        graphics.setTransform(transform);
        pos = leftAxle.getPosition();
        graphics.translate(pos.x, pos.y);
        graphics.rotate(leftAxle.getAngle());
        graphics.scale(SCALE, SCALE);
        axleImage.draw(graphics, 0, -7.863f / 2, 53.547f, 7.863f);

        pos = leftSmallAxle.getPosition();
        graphics.setTransform(transform);
        graphics.translate(pos.x, pos.y);
        graphics.rotate(leftSmallAxle.getAngle());
        graphics.scale(SCALE, SCALE);
        smallAxleImage.draw(graphics, 0, -6.332f / 2, 46f, 6.332f);

        pos = rightSmallAxle.getPosition();
        graphics.setTransform(transform);
        graphics.translate(pos.x, pos.y);
        graphics.rotate(rightSmallAxle.getAngle());
        graphics.scale(SCALE, SCALE);
        smallAxleImage.draw(graphics, 0, -6.332f / 2, 46f, 6.332f);

        ImageUtil[] antennaImages = { new ImageUtil(images.antenna1()),
                new ImageUtil(images.antenna2()), new ImageUtil(images.antenna3()) };
        for (int i = 0; i < antenna.length; ++i) {
            ImageUtil partImage = antennaImages[i];
            Body part = antenna[i];
            pos = part.getPosition();
            graphics.setTransform(transform);
            graphics.translate(pos.x, pos.y);
            graphics.rotate(part.getAngle());
            graphics.scale(SCALE, SCALE);
            partImage.draw(graphics, 0, -partImage.getHeight() / 2, partImage.getWidth(),
                    partImage.getHeight());
        }

        pos = body.getPosition();
        graphics.setTransform(transform);
        graphics.translate(pos.x, pos.y);
        graphics.rotate(body.getAngle());
        graphics.scale(SCALE, SCALE);
        bodyImage.draw(graphics, 54.678f, 69.723f, 157.833f, 230.832f);

        pos = new Vec2();
        leftAxleJoint.getAnchorA(pos);
        graphics.setTransform(transform);
        graphics.translate(pos.x, pos.y);
        graphics.rotate(leftAxle.getAngle());
        graphics.scale(SCALE, SCALE);
        damperImage.draw(graphics, 0, -15.354f / 2, 24.066f, 15.354f);

        pos = new Vec2();
        rightAxleJoint.getAnchorA(pos);
        graphics.setTransform(transform);
        graphics.translate(pos.x, pos.y);
        graphics.rotate(rightAxle.getAngle());
        graphics.scale(SCALE, SCALE);
        damperImage.draw(graphics, 0, -15.354f / 2, 24.066f, 15.354f);

        pos = new Vec2();
        leftSmallAxleJoint.getAnchorA(pos);
        graphics.setTransform(transform);
        graphics.translate(pos.x, pos.y);
        graphics.rotate(leftSmallAxle.getAngle());
        graphics.scale(SCALE, SCALE);
        smallDamperImage.draw(graphics, 0, -11.668f / 2, 20.666f, 11.668f);

        pos = new Vec2();
        rightSmallAxleJoint.getAnchorA(pos);
        graphics.setTransform(transform);
        graphics.translate(pos.x, pos.y);
        graphics.rotate(rightSmallAxle.getAngle());
        graphics.scale(SCALE, SCALE);
        smallDamperImage.draw(graphics, 0, -11.668f / 2, 20.666f, 11.668f);

        graphics.setTransform(transform);
    }

    private void drawTrack(Graphics graphics, Vec2 p1, float r1, Vec2 p2, float r2,
            float thickness) {
        r1 += thickness / 2;
        r2 += thickness / 2;
        float x = p1.x - p2.x;
        float y = p1.y - p2.y;
        float r = r1 - r2;
        float dist = x * x + y * y;
        float d = (float)Math.sqrt(dist - r * r);
        float a1 = (-r * x - y * d) / dist;
        float a2 = (-r * x + y * d) / dist;
        float b1 = (-r * y - x * d) / dist;
        float b2 = (-r * y + x * d) / dist;
        if (Math.abs(a1 * b1 + a1 * a1 - 1) > 0.001) {
            float tmp = b1;
            b1 = b2;
            b2 = tmp;
        }
        graphics.setColor(Color.black());
        graphics.setStrokeWidth(thickness);
        graphics.moveTo(p1.x + a1 * r1, p1.y + b1 * r1);
        graphics.lineTo(p2.x + a1 * r2, p2.y + b1 * r2);
        graphics.stroke();
        graphics.moveTo(p1.x + a2 * r1, p1.y + b2 * r1);
        graphics.lineTo(p2.x + a2 * r2, p2.y + b2 * r2);
        graphics.stroke();
        float angle1 = (float)Math.acos(a1);
        if (b1 > 0) {
            angle1 = (float)Math.PI * 2 - angle1;
        }
        float angle2 = (float)Math.acos(a2);
        if (b2 > 0) {
            angle2 = (float)Math.PI * 2 - angle2;
        }
        if ((x * a1 + y * b1) * (x * a2 + y * b2) < 0) {
            float tmp = angle1;
            angle1 = angle2;
            angle2 = tmp;
        }
        if (angle2 < angle1) {
            angle2 += (float)Math.PI * 2;
        }
        graphics.drawArc(p1.x - r1, p1.y - r1, r1 * 2, r1 * 2, angle1, angle2 - angle1);
        graphics.drawArc(p2.x - r2, p2.y - r2, r2 * 2, r2 * 2, angle2,
                2 * (float)Math.PI - (angle2 - angle1));
    }
}
