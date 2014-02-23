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
import ru.geobot.resources.Image;

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
    private Joint[] antennaJoints = new Joint[3];
    private Body[] armParts;
    private PrismaticJoint[] armJoints;
    private RevoluteJoint shoulderJoint;
    private boolean movingRight;
    private boolean movingLeft;
    private float initialX;
    private float initialY;
    private static final float SCALE = 1.1f / 800f;
    private boolean movingUp;
    private float vertOffset = 0;
    private Direction currentDirection = Direction.RIGHT;
    private Direction desiredDirection;
    private long directionSetTime = -1;
    private long currentTime;
    private float clawsAngle = 25 * (float)Math.PI / 180;
    private float targetArmAngle;
    private float targetArmLength;
    private boolean freeArm = true;
    private Runnable pickAction;

    private static enum Direction {
        LEFT,
        RIGHT,
        FACE
    }

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
        createArm();
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
        fixtureDef.filter.groupIndex = -1;
        fixtureDef.shape = scaledRectShape(0, 0, 336, 201);
        body.createFixture(fixtureDef);

        fixtureDef.density = 1;
        fixtureDef.shape = scaledRectShape(104, 201, 122, 37);
        body.createFixture(fixtureDef);

        fixtureDef.shape = scaledRectShape(21, 238, 289, 326);
        body.createFixture(fixtureDef);
    }

    private void createLeftWheel() {
        BodyDef wheelDef = new BodyDef();
        wheelDef.position.x = initialX + scale(-28);
        wheelDef.position.y = initialY + scale(-107);
        wheelDef.type = BodyType.DYNAMIC;
        wheelDef.angularDamping = 0.1f;
        leftWheel = getWorld().createBody(wheelDef);

        FixtureDef wheelFixtureDef = new FixtureDef();
        wheelFixtureDef.density = 15;
        wheelFixtureDef.filter.categoryBits = 1;
        wheelFixtureDef.filter.groupIndex = -1;
        wheelFixtureDef.filter.maskBits = 1;
        CircleShape wheelShape = new CircleShape();
        wheelShape.m_radius = scale(170f / 2);
        wheelFixtureDef.shape = wheelShape;
        wheelFixtureDef.restitution = 0.95f;
        wheelFixtureDef.friction = 0.95f;
        leftWheel.createFixture(wheelFixtureDef);

        BodyDef axleDef = new BodyDef();
        axleDef.position.x = initialX + scale(57);
        axleDef.position.y = initialY + scale(40);
        axleDef.angle = (float)Math.PI * 240 / 180;
        axleDef.type = BodyType.DYNAMIC;
        axleDef.linearDamping = 0.1f;
        leftAxle = getWorld().createBody(axleDef);

        FixtureDef axleFixtureDef = new FixtureDef();
        axleFixtureDef.density = 5;
        axleFixtureDef.shape = scaledPoly(new Vec2(0, 7), new Vec2(0, -7), new Vec2(170, -7), new Vec2(170, 7));
        axleFixtureDef.filter.categoryBits = 1;
        axleFixtureDef.filter.maskBits = 1;
        axleFixtureDef.filter.groupIndex = -1;
        leftAxle.createFixture(axleFixtureDef);

        PrismaticJointDef axleJointDef = new PrismaticJointDef();
        axleJointDef.bodyA = body;
        axleJointDef.localAnchorA.x = scale(57);
        axleJointDef.localAnchorA.y = scale(40);
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
        wheelJointDef.localAnchorA = scale(new Vec2(170f, 0));
        wheelJointDef.bodyB = leftWheel;
        wheelJointDef.localAnchorB = new Vec2(0, 0);
        wheelJointDef.maxMotorTorque = 2;
        wheelJointDef.enableMotor = true;
        leftWheelJoint = (RevoluteJoint)getWorld().createJoint(wheelJointDef);
    }

    private void createRightWheel() {
        BodyDef wheelDef = new BodyDef();
        wheelDef.position.x = initialX + scale(364);
        wheelDef.position.y = initialY + scale(-107);
        wheelDef.type = BodyType.DYNAMIC;
        wheelDef.angularDamping = 0.1f;
        rightWheel = getWorld().createBody(wheelDef);

        FixtureDef wheelFixtureDef = new FixtureDef();
        wheelFixtureDef.density = 15;
        wheelFixtureDef.filter.categoryBits = 1;
        wheelFixtureDef.filter.maskBits = 1;
        wheelFixtureDef.filter.groupIndex = -1;
        CircleShape wheelShape = new CircleShape();
        wheelShape.m_radius = scale(170f / 2);
        wheelFixtureDef.shape = wheelShape;
        wheelFixtureDef.restitution = 0.05f;
        wheelFixtureDef.friction = 0.95f;
        rightWheel.createFixture(wheelFixtureDef);

        BodyDef axleDef = new BodyDef();
        axleDef.position.x = initialX + scale(279);
        axleDef.position.y = initialY + scale(40);
        axleDef.angle = -(float)Math.PI * 60f / 180f;
        axleDef.type = BodyType.DYNAMIC;
        axleDef.linearDamping = 0.16f;
        rightAxle = getWorld().createBody(axleDef);

        FixtureDef axleFixtureDef = new FixtureDef();
        axleFixtureDef.density = 5;
        axleFixtureDef.shape = scaledPoly(new Vec2(0, 7), new Vec2(0, -7), new Vec2(170, -7), new Vec2(170, 7));
        axleFixtureDef.filter.categoryBits = 1;
        axleFixtureDef.filter.maskBits = 1;
        axleFixtureDef.filter.groupIndex = -1;
        rightAxle.createFixture(axleFixtureDef);

        PrismaticJointDef axleJointDef = new PrismaticJointDef();
        axleJointDef.bodyA = body;
        axleJointDef.localAnchorA.x = scale(279);
        axleJointDef.localAnchorA.y = scale(40);
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
        wheelJointDef.localAnchorA = scale(new Vec2(170f, 0));
        wheelJointDef.bodyB = rightWheel;
        wheelJointDef.localAnchorB = new Vec2(0, 0);
        wheelJointDef.maxMotorTorque = 2;
        wheelJointDef.enableMotor = true;
        rightWheelJoint = (RevoluteJoint)getWorld().createJoint(wheelJointDef);
    }

    private void createLeftSmallWheel() {
        BodyDef wheelDef = new BodyDef();
        wheelDef.position.x = initialX + scale(114);
        wheelDef.position.y = initialY + scale(-150);
        wheelDef.type = BodyType.DYNAMIC;
        wheelDef.angularDamping = 0.1f;
        leftSmallWheel = getWorld().createBody(wheelDef);

        FixtureDef wheelFixtureDef = new FixtureDef();
        wheelFixtureDef.density = 3;
        CircleShape wheelShape = new CircleShape();
        wheelShape.m_radius = scale(30);
        wheelFixtureDef.shape = wheelShape;
        wheelFixtureDef.restitution = 0.95f;
        wheelFixtureDef.friction = 0.99f;
        wheelFixtureDef.filter.categoryBits = 1;
        wheelFixtureDef.filter.maskBits = 1;
        wheelFixtureDef.filter.groupIndex = -1;
        leftSmallWheel.createFixture(wheelFixtureDef);

        BodyDef axleDef = new BodyDef();
        axleDef.position.x = initialX + scale(114);
        axleDef.position.y = initialY + scale(16);
        axleDef.angle = (float)Math.PI * 270 / 180;
        axleDef.type = BodyType.DYNAMIC;
        axleDef.linearDamping = 0.1f;
        leftSmallAxle = getWorld().createBody(axleDef);

        FixtureDef axleFixtureDef = new FixtureDef();
        axleFixtureDef.density = 5;
        axleFixtureDef.shape = scaledPoly(new Vec2(0, 7), new Vec2(0, -7), new Vec2(166, -7), new Vec2(166, 7));
        axleFixtureDef.filter.categoryBits = 1;
        axleFixtureDef.filter.maskBits = 1;
        axleFixtureDef.filter.groupIndex = -1;
        leftSmallAxle.createFixture(axleFixtureDef);

        PrismaticJointDef axleJointDef = new PrismaticJointDef();
        axleJointDef.bodyA = body;
        axleJointDef.localAnchorA.x = scale(114);
        axleJointDef.localAnchorA.y = scale(16);
        axleJointDef.bodyB = leftSmallAxle;
        axleJointDef.localAnchorB.x = 0;
        axleJointDef.localAnchorB.y = 0;
        axleJointDef.lowerTranslation = -0.06f;
        axleJointDef.upperTranslation = 0.001f;
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
        wheelJointDef.localAnchorA = scale(new Vec2(166, 0));
        wheelJointDef.bodyB = leftSmallWheel;
        wheelJointDef.localAnchorB = new Vec2(0, 0);
        leftSmallWheelJoint = (RevoluteJoint)getWorld().createJoint(wheelJointDef);

        GearJointDef bigWheelJointDef = new GearJointDef();
        bigWheelJointDef.bodyA = leftWheel;
        bigWheelJointDef.joint1 = leftWheelJoint;
        bigWheelJointDef.bodyB = leftSmallWheel;
        bigWheelJointDef.joint2 = leftSmallWheelJoint;
        bigWheelJointDef.ratio = -170 / 60f;
        getWorld().createJoint(bigWheelJointDef);
    }

    private void createRightSmallWheel() {
        BodyDef wheelDef = new BodyDef();
        wheelDef.position.x = initialX + scale(214);
        wheelDef.position.y = initialY + scale(-150);
        wheelDef.type = BodyType.DYNAMIC;
        wheelDef.angularDamping = 0.1f;
        rightSmallWheel = getWorld().createBody(wheelDef);

        FixtureDef wheelFixtureDef = new FixtureDef();
        wheelFixtureDef.density = 3;
        CircleShape wheelShape = new CircleShape();
        wheelShape.m_radius = scale(30);
        wheelFixtureDef.shape = wheelShape;
        wheelFixtureDef.restitution = 0.95f;
        wheelFixtureDef.friction = 0.99f;
        wheelFixtureDef.filter.categoryBits = 1;
        wheelFixtureDef.filter.maskBits = 1;
        wheelFixtureDef.filter.groupIndex = -1;
        rightSmallWheel.createFixture(wheelFixtureDef);

        BodyDef axleDef = new BodyDef();
        axleDef.position.x = initialX + scale(214);
        axleDef.position.y = initialY + scale(16);
        axleDef.angle = (float)Math.PI * 270 / 180;
        axleDef.type = BodyType.DYNAMIC;
        axleDef.linearDamping = 0.1f;
        rightSmallAxle = getWorld().createBody(axleDef);

        FixtureDef axleFixtureDef = new FixtureDef();
        axleFixtureDef.density = 5;
        axleFixtureDef.shape = scaledPoly(new Vec2(0, 7), new Vec2(0, -7), new Vec2(166, -7), new Vec2(166, 7));
        axleFixtureDef.filter.categoryBits = 1;
        axleFixtureDef.filter.maskBits = 1;
        axleFixtureDef.filter.groupIndex = -1;
        rightSmallAxle.createFixture(axleFixtureDef);

        PrismaticJointDef axleJointDef = new PrismaticJointDef();
        axleJointDef.bodyA = body;
        axleJointDef.localAnchorA.x = scale(214);
        axleJointDef.localAnchorA.y = scale(16);
        axleJointDef.bodyB = rightSmallAxle;
        axleJointDef.localAnchorB.x = 0;
        axleJointDef.localAnchorB.y = 0;
        axleJointDef.lowerTranslation = -0.06f;
        axleJointDef.upperTranslation = 0.001f;
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
        wheelJointDef.localAnchorA = scale(new Vec2(166, 0));
        wheelJointDef.bodyB = rightSmallWheel;
        wheelJointDef.localAnchorB = new Vec2(0, 0);
        rightSmallWheelJoint = (RevoluteJoint)getWorld().createJoint(wheelJointDef);

        GearJointDef bigWheelJointDef = new GearJointDef();
        bigWheelJointDef.bodyA = rightWheel;
        bigWheelJointDef.joint1 = rightWheelJoint;
        bigWheelJointDef.bodyB = rightSmallWheel;
        bigWheelJointDef.joint2 = rightSmallWheelJoint;
        bigWheelJointDef.ratio = -170 / 60f;
        getWorld().createJoint(bigWheelJointDef);
    }

    private void createAntenna() {
        float[] distances = { 66f, 63f };
        antenna = new Body[3];
        BodyDef partDef = new BodyDef();
        partDef.type = BodyType.DYNAMIC;
        switch (currentDirection) {
            case RIGHT:
                partDef.angle = 100 / 180f * (float)Math.PI;
                partDef.position.x = scale(220);
                break;
            case LEFT:
                partDef.angle = 80 / 180f * (float)Math.PI;
                partDef.position.x = scale(99);
                break;
            case FACE:
                partDef.angle = 90 / 180f * (float)Math.PI;
                partDef.position.x = scale(15);
                break;
        }
        partDef.position.y = scale(417);
        Vec2 anchor = partDef.position;
        partDef.position = body.getWorldPoint(anchor);
        partDef.angle += body.getAngle();
        antenna[0] = getWorld().createBody(partDef);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.restitution = 0.7f;
        fixtureDef.friction = 0.3f;
        fixtureDef.density = 0.5f;
        fixtureDef.shape = scaledRectShape(0, -16f / 2, 66, 16);
        fixtureDef.filter.categoryBits = 1;
        fixtureDef.filter.maskBits = 1;
        fixtureDef.filter.groupIndex = -1;
        fixtureDef.filter.groupIndex = -1;
        antenna[0].createFixture(fixtureDef);

        partDef.position = antenna[0].getWorldPoint(scale(new Vec2(66f, 0)));
        antenna[1] = getWorld().createBody(partDef);
        fixtureDef.shape = scaledRectShape(0, -8f / 2, 63, 8);
        antenna[1].createFixture(fixtureDef);

        partDef.position = antenna[1].getWorldPoint(scale(new Vec2(63, 0)));
        antenna[2] = getWorld().createBody(partDef);
        fixtureDef.shape = scaledRectShape(0, -11f / 2, 76, 11);
        antenna[2].createFixture(fixtureDef);

        RevoluteJointDef jointDef = new RevoluteJointDef();
        jointDef.lowerAngle = -2f * (float)Math.PI / 180;
        jointDef.upperAngle = 2f * (float)Math.PI / 180;
        jointDef.enableLimit = true;
        for (int i = 1; i < antenna.length; ++i) {
            jointDef.bodyA = antenna[i - 1];
            jointDef.bodyB = antenna[i];
            jointDef.localAnchorA = scale(new Vec2(distances[i - 1], 0));
            antennaJoints[i] = getWorld().createJoint(jointDef);
        }
        jointDef.bodyA = body;
        jointDef.bodyB = antenna[0];
        jointDef.collideConnected = false;
        jointDef.localAnchorA = anchor;
        jointDef.referenceAngle = partDef.angle - body.getAngle();
        antennaJoints[0] = getWorld().createJoint(jointDef);
    }

    private void destroyAntenna() {
        for (Body body : antenna) {
            getWorld().destroyBody(body);
        }
        for (Joint joint : antennaJoints) {
            getWorld().destroyJoint(joint);
        }
    }

    private void createArm() {
        Image[] partImages = { images.arm2(), images.arm3(), images.arm4() };
        armParts = new Body[3];
        BodyDef armPartDef = new BodyDef();
        armPartDef.angle = -(float)Math.PI / 2;
        armPartDef.position = body.getWorldPoint(new Vec2(scale(168), scale(146)));
        armPartDef.type = BodyType.DYNAMIC;
        armPartDef.active = true;
        armPartDef.bullet = true;
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 0.05f;
        fixtureDef.filter.maskBits = 1;
        fixtureDef.filter.categoryBits = 1;
        fixtureDef.filter.groupIndex = -1;
        PolygonShape shape = new PolygonShape();
        fixtureDef.shape = shape;
        for (int i = 0; i < 3; ++i) {
            armParts[i] = getWorld().createBody(armPartDef);
            Image partImage = partImages[i];
            float height = scale(partImage.getHeight()) / 2;
            float width = scale(partImage.getWidth());
            shape.set(new Vec2[] { new Vec2(0, -height), new Vec2(width, -height), new Vec2(width, height),
                    new Vec2(0, height)}, 4);
            armParts[i].createFixture(fixtureDef);
            armPartDef.position = armParts[i].getWorldPoint(new Vec2(scale(20), 0));
        }

        PrismaticJointDef armJointDef = new PrismaticJointDef();
        armJoints = new PrismaticJoint[2];
        for (int i = 0; i < 2; ++i) {
            armJointDef.bodyA = armParts[i];
            armJointDef.bodyB = armParts[i + 1];
            armJointDef.localAxis1.x = 1;
            armJointDef.localAxis1.y = 0;
            armJointDef.lowerTranslation = 0;
            armJointDef.upperTranslation = scale(250);
            armJointDef.enableLimit = true;
            armJointDef.enableMotor = true;
            armJoints[i] = (PrismaticJoint)getWorld().createJoint(armJointDef);
        }

        RevoluteJointDef shoulderJointDef = new RevoluteJointDef();
        shoulderJointDef.bodyA = body;
        shoulderJointDef.bodyB = armParts[0];
        shoulderJointDef.localAnchorA = new Vec2(scale(168), scale(146));
        shoulderJointDef.lowerAngle = -60f * (float)Math.PI / 180f;
        shoulderJointDef.upperAngle = -120f * (float)Math.PI / 180f;
        shoulderJointDef.enableLimit = true;
        shoulderJoint = (RevoluteJoint)getWorld().createJoint(shoulderJointDef);
    }

    private PolygonShape scaledRectShape(float x, float y, float w, float h) {
        PolygonShape shape = new PolygonShape();
        float x1 = scale(x);
        float y1 = scale(y);
        float x2 = scale(x + w);
        float y2 = scale(y + h);
        shape.set(new Vec2[] { new Vec2(x1, y1), new Vec2(x2, y1), new Vec2(x2, y2), new Vec2(x1, y2)}, 4);
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
                pickAction = null;
                if (currentDirection == Direction.LEFT) {
                    currentDirection = Direction.FACE;
                    desiredDirection = Direction.RIGHT;
                    directionSetTime = currentTime + 400;
                    destroyAntenna();
                    createAntenna();
                }
                break;
            case LEFT:
                movingLeft = true;
                pickAction = null;
                if (currentDirection == Direction.RIGHT) {
                    currentDirection = Direction.FACE;
                    desiredDirection = Direction.LEFT;
                    directionSetTime = currentTime + 400;
                    destroyAntenna();
                    createAntenna();
                }
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
        currentTime = time;
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
        leftSmallAxleJoint.setMotorSpeed(0.1f + 2 * delta);
        leftSmallAxleJoint.setMaxMotorForce(2 + 10 * Math.abs(delta));
        delta = leftSmallAxleJoint.getUpperLimit() - rightSmallAxleJoint.getJointTranslation();
        rightSmallAxleJoint.setMotorSpeed(0.1f + 2 * delta);
        rightSmallAxleJoint.setMaxMotorForce(2 + 10 * Math.abs(delta));

        if (directionSetTime >= 0) {
            if (time > directionSetTime) {
                directionSetTime = -1;
                currentDirection = desiredDirection;
                destroyAntenna();
                createAntenna();
            }
        }

        fixArm();
    }

    private void fixArm() {
        if (freeArm) {
            shoulderJoint.enableMotor(false);
            shoulderJoint.enableLimit(true);
            for (PrismaticJoint armJoint : armJoints) {
                armJoint.enableLimit(true);
                armJoint.setLimits(0, scale(200));
                armJoint.setMotorSpeed(-10);
                armJoint.setMaxMotorForce(50000);
            }
            return;
        }
        float actualAngle = shoulderJoint.getJointAngle();
        if (Math.abs(actualAngle - targetArmAngle) > (float)Math.PI) {
            if (actualAngle > targetArmAngle) {
                targetArmAngle += 2 * (float)Math.PI;
            } else {
                targetArmAngle -= 2 * (float)Math.PI;
            }
        }
        shoulderJoint.enableLimit(true);
        if (Math.abs(actualAngle - targetArmAngle) < 0.01f) {
            shoulderJoint.setLimits(targetArmAngle - 0.006f, targetArmAngle + 0.006f);
            shoulderJoint.enableMotor(false);
        }
        if (actualAngle < targetArmAngle) {
            float angle = Math.min(targetArmAngle, actualAngle + 0.03f);
            shoulderJoint.setLimits(angle, angle + 0.015f);
            shoulderJoint.setMotorSpeed(2f);
            shoulderJoint.setMaxMotorTorque(500000);
            shoulderJoint.enableMotor(true);
        } else {
            float angle = Math.max(targetArmAngle, actualAngle - 0.03f);
            shoulderJoint.setLimits(angle - 0.015f, angle);
            shoulderJoint.setMotorSpeed(-2f);
            shoulderJoint.setMaxMotorTorque(500000);
            shoulderJoint.enableMotor(true);
        }

        float targetLength = (targetArmLength - scale(250)) / 2;
        float fullArmLength = scale(250);
        for (PrismaticJoint armJoint : armJoints) {
            float actualLength = armJoint.getJointTranslation();
            if (Math.abs(actualLength - targetLength) < 0.02) {
                armJoint.setLimits(targetLength - 0.011f, targetLength + 0.011f);
            } else {
                if (actualLength < targetLength) {
                    float angle = Math.min(targetLength, actualLength + 0.01f);
                    armJoint.setLimits(angle, angle + 0.02f);
                } else {
                    float angle = Math.max(targetLength, actualLength - 0.01f);
                    armJoint.setLimits(angle - 0.02f, angle);
                }
            }
            armJoint.enableLimit(true);
            armJoint.enableMotor(false);
            fullArmLength += armJoint.getJointTranslation();
        }
        if (pickAction != null && Math.abs(targetArmLength - fullArmLength) < 0.04f &&
                Math.abs(targetArmAngle - actualAngle) < 0.015f) {
            pickAction.run();
            pickAction = null;
        }
    }

    public void pointAt(float x, float y) {
        Vec2 armTarget = new Vec2(x, y).sub(armParts[0].getPosition());
        targetArmLength = armTarget.length();
        if (Math.abs(armTarget.y) < Math.abs(armTarget.x)) {
            targetArmAngle = (float)Math.asin(armTarget.y / armTarget.length());
            if (armTarget.x < 0) {
                targetArmAngle = (float)Math.PI - targetArmAngle;
            }
        } else {
            targetArmAngle = (float)Math.acos(armTarget.x / armTarget.length());
            if (armTarget.y < 0) {
                targetArmAngle = -targetArmAngle;
            }
        }
        targetArmAngle = targetArmAngle - body.getAngle();
        float requiredDistance = armTarget.length() - scale(35);
        targetArmLength = Math.max(scale(250), Math.min(scale(750), requiredDistance));
        if (requiredDistance < scale(250) || requiredDistance > scale(750)) {
            pickAction = null;
        }
        freeArm = false;
    }

    public void pickAt(float x, float y, Runnable action) {
        if (movingRight || movingLeft) {
            return;
        }
        pickAction = action;
        Vec2 worldPointToPick = new Vec2(x, y);
        pointAt(worldPointToPick.x, worldPointToPick.y);
    }

    @Override
    protected void paint(Graphics graphics) {
        Vec2 pos;
        AffineTransform transform = graphics.getTransform();
        ImageUtil bodyImage = new ImageUtil(images.body());
        ImageUtil headRightImage = new ImageUtil(images.headRight());
        ImageUtil headLeftImage = new ImageUtil(images.headLeft());
        ImageUtil headFaceImage = new ImageUtil(images.headFace());
        ImageUtil leftWheelImage = new ImageUtil(images.bigLeftWheel());
        ImageUtil rightWheelImage = new ImageUtil(images.bigRightWheel());
        ImageUtil damperImage = new ImageUtil(images.damper());
        ImageUtil axleImage = new ImageUtil(images.axle());
        ImageUtil smallLeftWheelImage = new ImageUtil(images.smallLeftWheel());
        ImageUtil smallRightWheelImage = new ImageUtil(images.smallRightWheel());

        graphics.setTransform(transform);
        drawTrack(graphics, leftWheel.getPosition(), scale(71), leftSmallWheel.getPosition(),
                scale(30), scale(12));
        drawTrack(graphics, rightWheel.getPosition(), scale(71), rightSmallWheel.getPosition(),
                scale(30), scale(12));

        graphics.setTransform(transform);
        pos = leftWheel.getPosition();
        graphics.translate(pos.x, pos.y);
        graphics.rotate(leftWheel.getAngle());
        graphics.scale(SCALE, SCALE);
        graphics.translate(-168f / 2, -170f / 2);
        leftWheelImage.draw(graphics, 0, 0, 168, 170f);

        graphics.setTransform(transform);
        pos = rightWheel.getWorldCenter();
        graphics.translate(pos.x, pos.y);
        graphics.rotate(rightWheel.getAngle());
        graphics.scale(SCALE, SCALE);
        graphics.translate(-168f / 2, -170f / 2);
        rightWheelImage.draw(graphics, 0, 0, 168, 170f);

        graphics.setTransform(transform);
        pos = leftSmallWheel.getPosition();
        graphics.translate(pos.x, pos.y);
        graphics.rotate(leftSmallWheel.getAngle());
        graphics.scale(SCALE, SCALE);
        graphics.translate(-30, -30);
        smallLeftWheelImage.draw(graphics, 0, 0, 60, 60);

        graphics.setTransform(transform);
        pos = rightSmallWheel.getPosition();
        graphics.translate(pos.x, pos.y);
        graphics.rotate(rightSmallWheel.getAngle());
        graphics.scale(SCALE, SCALE);
        graphics.translate(-30, -30);
        smallRightWheelImage.draw(graphics, 0, 0, 60, 60);

        graphics.setTransform(transform);
        pos = leftAxle.getPosition();
        graphics.translate(pos.x, pos.y);
        graphics.rotate(leftAxle.getAngle() - (float)Math.PI / 2);
        graphics.scale(SCALE, SCALE);
        axleImage.draw(graphics, -7, 40, 14, 110);

        pos = rightAxle.getPosition();
        graphics.setTransform(transform);
        graphics.translate(pos.x, pos.y);
        graphics.rotate(rightAxle.getAngle() - (float)Math.PI / 2);
        graphics.scale(SCALE, SCALE);
        axleImage.draw(graphics, -7, 40, 14, 110);

        pos = leftSmallAxle.getPosition();
        graphics.setTransform(transform);
        graphics.translate(pos.x, pos.y);
        graphics.rotate(leftSmallAxle.getAngle() - (float)Math.PI / 2);
        graphics.scale(SCALE, SCALE);
        axleImage.draw(graphics, -7, 0, 14, 126);

        pos = rightSmallAxle.getPosition();
        graphics.setTransform(transform);
        graphics.translate(pos.x, pos.y);
        graphics.rotate(rightSmallAxle.getAngle() - (float)Math.PI / 2);
        graphics.scale(SCALE, SCALE);
        axleImage.draw(graphics, -7, 0, 14, 126);

        pos = body.getPosition();
        graphics.setTransform(transform);
        graphics.translate(pos.x, pos.y);
        graphics.rotate(body.getAngle());
        graphics.scale(SCALE, SCALE);
        bodyImage.draw(graphics, 0, -5, 336, 244);
        switch (currentDirection) {
            case LEFT:
                drawAntenna(graphics, transform);
                graphics.setTransform(transform);
                graphics.translate(pos.x, pos.y);
                graphics.rotate(body.getAngle());
                graphics.scale(SCALE, SCALE);
                headLeftImage.draw(graphics, -21, 238, 349, 326);
                break;
            case RIGHT:
                headRightImage.draw(graphics, 6, 238, 349, 326);
                drawAntenna(graphics, transform);
                break;
            case FACE:
                headFaceImage.draw(graphics, -12, 238, 360, 328);
                drawAntenna(graphics, transform);
                break;
        }

        pos = new Vec2();
        leftAxleJoint.getAnchorA(pos);
        graphics.setTransform(transform);
        graphics.translate(pos.x, pos.y);
        graphics.rotate(leftAxle.getAngle() - (float)Math.PI / 2);
        graphics.scale(SCALE, SCALE);
        damperImage.draw(graphics, -23f / 2, 0, 23f, 71);

        pos = new Vec2();
        rightAxleJoint.getAnchorA(pos);
        graphics.setTransform(transform);
        graphics.translate(pos.x, pos.y);
        graphics.rotate(rightAxle.getAngle() - (float)Math.PI / 2);
        graphics.scale(SCALE, SCALE);
        damperImage.draw(graphics, -23f / 2, 0, 23f, 71);

        pos = new Vec2();
        leftSmallAxleJoint.getAnchorA(pos);
        graphics.setTransform(transform);
        graphics.translate(pos.x, pos.y);
        graphics.rotate(leftSmallAxle.getAngle() - (float)Math.PI / 2);
        graphics.scale(SCALE, SCALE);
        damperImage.draw(graphics, -23f / 2, 0, 23f, 71);

        pos = new Vec2();
        rightSmallAxleJoint.getAnchorA(pos);
        graphics.setTransform(transform);
        graphics.translate(pos.x, pos.y);
        graphics.rotate(rightSmallAxle.getAngle() - (float)Math.PI / 2);
        graphics.scale(SCALE, SCALE);
        damperImage.draw(graphics, -23f / 2, 0, 23f, 71);

        graphics.setTransform(transform);
        drawArm(graphics, transform);
    }

    private void drawTrack(Graphics graphics, Vec2 p1, float r1, Vec2 p2, float r2, float thickness) {
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

    private void drawAntenna(Graphics graphics, AffineTransform transform) {
        ImageUtil[] antennaImages = { new ImageUtil(images.antenna1()), new ImageUtil(images.antenna2()),
                new ImageUtil(images.antenna3()) };
        for (int i = 0; i < antenna.length; ++i) {
            ImageUtil partImage = antennaImages[i];
            Body part = antenna[i];
            Vec2 pos = part.getPosition();
            graphics.setTransform(transform);
            graphics.translate(pos.x, pos.y);
            graphics.rotate(part.getAngle() - (float)Math.PI / 2);
            graphics.scale(SCALE, SCALE);
            partImage.draw(graphics, -partImage.getWidth() / 2, 0, partImage.getWidth(), partImage.getHeight());
        }
    }

    private void drawArm(Graphics graphics, AffineTransform transform) {
        ImageUtil[] armImages = { new ImageUtil(images.arm2()), new ImageUtil(images.arm3()),
                new ImageUtil(images.arm4())};
        for (int i = armParts.length - 1; i >= 0; --i) {
            Body armPart = armParts[i];
            graphics.setTransform(transform);
            Vec2 pos = armPart.getPosition();
            graphics.translate(pos.x, pos.y);
            graphics.rotate(armPart.getAngle());
            ImageUtil partImage = armImages[i];
            graphics.scale(SCALE, SCALE);
            partImage.draw(graphics, 0, -partImage.getHeight() / 2, partImage.getWidth(), partImage.getHeight());
        }

        ImageUtil armStartImage = new ImageUtil(images.arm1());
        armStartImage.draw(graphics, -15, -armStartImage.getHeight() / 2, armStartImage.getWidth(),
                armStartImage.getHeight());

        ImageUtil upperClawImage = new ImageUtil(images.upperClaw());
        ImageUtil lowerClawImage = new ImageUtil(images.lowerClaw());
        ImageUtil clawMountImage = new ImageUtil(images.clawMount());
        graphics.setTransform(transform);
        Vec2 pos = armParts[2].getPosition();
        graphics.translate(pos.x, pos.y);
        graphics.rotate(armParts[2].getAngle());
        graphics.scale(SCALE, SCALE);
        graphics.translate(armImages[2].getWidth(), 0);
        graphics.pushTransform();
        graphics.rotate(clawsAngle / 2);
        upperClawImage.draw(graphics, 0, 0, upperClawImage.getWidth(), upperClawImage.getHeight());
        graphics.popTransform();
        graphics.pushTransform();
        graphics.rotate(-clawsAngle / 2);
        lowerClawImage.draw(graphics, 2, -lowerClawImage.getHeight(), lowerClawImage.getWidth(),
                lowerClawImage.getHeight());
        graphics.popTransform();
        clawMountImage.draw(graphics, -clawMountImage.getWidth() / 2f, -clawMountImage.getHeight() / 2,
                clawMountImage.getWidth(), clawMountImage.getHeight());

    }

    public Body getHand() {
        return armParts[armParts.length - 1];
    }

    public Vec2 getHandPickPoint() {
        return new Vec2(scale(280), 0);
    }
}
