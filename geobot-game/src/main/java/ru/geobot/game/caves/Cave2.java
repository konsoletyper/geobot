package ru.geobot.game.caves;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.joints.RevoluteJoint;
import org.jbox2d.dynamics.joints.RevoluteJointDef;
import org.jbox2d.dynamics.joints.WeldJointDef;
import ru.geobot.Game;
import ru.geobot.GameObject;
import ru.geobot.game.GeobotGame;
import ru.geobot.game.objects.*;
import ru.geobot.graphics.Graphics;
import ru.geobot.graphics.ImageUtil;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class Cave2 {
    public static final float SCALE = 10f / 2500;
    private GeobotGame game;
    private Cave2Resources caveResources;
    private CraneResources craneResources;
    private Environment environment;
    private Body leftCraneRoller;
    private Body rightCraneRoller;
    private Rope leftCraneRope;
    private Rope rightCraneRope;
    private Body crane;
    private Rope hangerRope;
    private Body hanger;
    private ControlPanelHandle heightHandle;
    private ControlPanelHandle positionHandle;
    private RevoluteJoint hangerJoint;
    private RevoluteJoint leftCraneRollerJoint;
    private RevoluteJoint rightCraneRollerJoint;

    public Cave2(GeobotGame game) {
        this.game = game;
        caveResources = game.loadResources(Cave2Resources.class);
        craneResources = game.loadResources(CraneResources.class);
        environment = new Environment(game);
        initControlPanel();
        game.setScale(1.1f);
        game.resizeWorld(2500 * SCALE, 1406 * SCALE);
        initCrane();
        new Crane();
    }

    private void initControlPanel() {
        heightHandle = new ControlPanelHandle(game, SCALE * 861, SCALE * 760);
        RevoluteJointDef jointDef = new RevoluteJointDef();
        jointDef.bodyA = environment.getBody();
        jointDef.bodyB = heightHandle.getBody();
        jointDef.localAnchorA.set(heightHandle.getBody().getPosition());
        game.getWorld().createJoint(jointDef);

        positionHandle = new ControlPanelHandle(game, SCALE * 951, SCALE * 760);
        jointDef.bodyB = positionHandle.getBody();
        jointDef.localAnchorA.set(positionHandle.getBody().getPosition());
        game.getWorld().createJoint(jointDef);
    }

    private void initCrane() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.STATIC;
        bodyDef.position.set(SCALE * 236, SCALE * 1283);
        leftCraneRoller = game.getWorld().createBody(bodyDef);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.filter.categoryBits = 0x100;
        fixtureDef.filter.maskBits = 0x100;
        fixtureDef.density = 0.1f;
        fixtureDef.restitution = 0.9f;
        fixtureDef.friction = 0.9f;
        PolygonShape box = new PolygonShape();
        box.setAsBox(SCALE * 250, SCALE * 37);
        fixtureDef.shape = box;
        leftCraneRoller.createFixture(fixtureDef);
        bodyDef.position.x = SCALE * 1580;
        rightCraneRoller = game.getWorld().createBody(bodyDef);
        rightCraneRoller.createFixture(fixtureDef);

        RopeFactory ropeFactory = new RopeFactory();
        ropeFactory.setWidth(SCALE * 2);
        ropeFactory.setStartX(SCALE * 900);
        ropeFactory.setStartY(SCALE * 1348);
        ropeFactory.setDensity(0.0001f);
        ropeFactory.setImage(craneResources.cable());
        ropeFactory.setMaskBits(0x100);
        ropeFactory.setCategoryBits(0x100);
        ropeFactory.getDrawFilters().add(leftRopeFilter);
        for (int i = 0; i < 30; ++i) {
            ropeFactory.addChunk((float)Math.PI / 2);
        }
        leftCraneRope = ropeFactory.create(game);

        ropeFactory.clearChunks();
        ropeFactory.getDrawFilters().clear();
        ropeFactory.setStartX(SCALE * 960);
        for (int i = 0; i < 27; ++i) {
            ropeFactory.addChunk((float)Math.PI * 3 / 2);
        }
        ropeFactory.getDrawFilters().add(rightRopeFilter);
        rightCraneRope = ropeFactory.create(game);

        bodyDef.type = BodyType.STATIC;
        bodyDef.position.set(SCALE * 950, SCALE * 1240);
        crane = game.getWorld().createBody(bodyDef);
        Vec2[] vertices = { new Vec2(SCALE * 8, 0), new Vec2(SCALE * 25, 0), new Vec2(SCALE * 25, SCALE * 51),
                new Vec2(SCALE * 8, SCALE * 51) };
        box.set(vertices, vertices.length);
        fixtureDef.shape = box;
        fixtureDef.density = 0.0001f;
        crane.createFixture(fixtureDef);
        vertices = new Vec2[] { new Vec2(SCALE * -25, 0), new Vec2(SCALE * -8, 0), new Vec2(SCALE * -8, SCALE * 51),
            new Vec2(SCALE * -25, SCALE * 51) };
        box.set(vertices, vertices.length);
        crane.createFixture(fixtureDef);

        bodyDef.type = BodyType.DYNAMIC;
        bodyDef.position.set(SCALE * 950, SCALE * 900);
        hanger = game.getWorld().createBody(bodyDef);
        for (PolygonShape shape : craneResources.hangerShape().create(SCALE)) {
            fixtureDef.shape = shape;
            fixtureDef.density = 0.0001f;
            fixtureDef.filter.categoryBits = 0x102;
            fixtureDef.filter.maskBits = 0x102;
            hanger.createFixture(fixtureDef);
        }

        RevoluteJointDef jointDef = new RevoluteJointDef();
        jointDef.bodyA = crane;
        jointDef.bodyB = leftCraneRope.part(0);
        jointDef.localAnchorA.set(SCALE * -30, 25 * SCALE);
        game.getWorld().createJoint(jointDef);

        jointDef.bodyB = rightCraneRope.part(0);
        jointDef.localAnchorA.set(SCALE * 30, 25 * SCALE);
        game.getWorld().createJoint(jointDef);

        jointDef.bodyA = leftCraneRoller;
        jointDef.bodyB = leftCraneRope.part(leftCraneRope.partCount() - 1);
        jointDef.localAnchorA.set(-160 * SCALE, 42 * SCALE);
        jointDef.localAnchorB.set(0, leftCraneRope.getChunkLength());
        leftCraneRollerJoint = (RevoluteJoint)game.getWorld().createJoint(jointDef);

        jointDef.bodyA = rightCraneRoller;
        jointDef.bodyB = rightCraneRope.part(rightCraneRope.partCount() - 1);
        jointDef.localAnchorA.set(160 * SCALE, 42 * SCALE);
        rightCraneRollerJoint = (RevoluteJoint)game.getWorld().createJoint(jointDef);

        ropeFactory.setStartX(SCALE * 950);
        ropeFactory.setStartY(SCALE * 1900);
        ropeFactory.clearChunks();
        ropeFactory.getDrawFilters().clear();
        ropeFactory.getDrawFilters().add(centralRopeFilter);
        ropeFactory.setDensity(0.001f);
        for (int i = 0; i < 35; ++i) {
            ropeFactory.addChunk((float)Math.PI);
        }
        hangerRope = ropeFactory.create(game);

        jointDef.bodyA = environment.getBody();
        jointDef.bodyB = hangerRope.part(0);
        jointDef.localAnchorA.set(SCALE * 950, SCALE * 1900);
        jointDef.localAnchorB.set(0, 0);
        hangerJoint = (RevoluteJoint)game.getWorld().createJoint(jointDef);

        WeldJointDef weldJointDef = new WeldJointDef();
        weldJointDef.bodyA = hangerRope.part(hangerRope.partCount() - 1);
        weldJointDef.bodyB = hanger;
        weldJointDef.localAnchorA.set(0, hangerRope.getChunkLength());
        weldJointDef.localAnchorB.set(SCALE * 26, SCALE * 74);
        weldJointDef.referenceAngle = -(float)Math.PI;
        game.getWorld().createJoint(weldJointDef);
    }

    private Rope.DrawFilter leftRopeFilter = new Rope.DrawFilter() {
        @Override public boolean filter(Body body) {
            return body.getPosition().x >= SCALE * 500;
        }
    };

    private Rope.DrawFilter rightRopeFilter = new Rope.DrawFilter() {
        @Override public boolean filter(Body body) {
            return body.getPosition().x <= SCALE * 1330;
        }
    };

    private Rope.DrawFilter centralRopeFilter = new Rope.DrawFilter() {
        @Override public boolean filter(Body body) {
            return body.getPosition().y < crane.getPosition().y + SCALE * 30;
        }
    };

    private class Crane extends GameObject {
        public Crane() {
            super(game);
        }

        @Override
        protected void paint(Graphics graphics) {
            Vec2 pos = leftCraneRoller.getPosition();
            ImageUtil platform = new ImageUtil(craneResources.platform());
            platform.draw(graphics, pos.x + SCALE * 195, pos.y + SCALE * 63, SCALE * 947, SCALE * 46);

            pos = crane.getPosition();
            ImageUtil craneImage = new ImageUtil(craneResources.crane());
            graphics.pushTransform();
            graphics.translate(pos.x, pos.y + SCALE * 25.5f);
            graphics.rotate(crane.getAngle());
            craneImage.draw(graphics, -SCALE * 25, SCALE * 25, SCALE * 51, SCALE * 51);
            graphics.popTransform();

            graphics.pushTransform();
            pos = hanger.getPosition();
            ImageUtil hangerImage = new ImageUtil(craneResources.hanger());
            graphics.translate(pos.x, pos.y);
            graphics.rotate(hanger.getAngle());
            hangerImage.draw(graphics, 0, SCALE * 72, SCALE * 53, SCALE * 72);
            graphics.popTransform();
        }

        @Override
        protected void time(long time) {
            hangerJoint.m_localAnchor1.y = SCALE * (1900 + 4 * heightHandle.getAngle());
            float offset = 4 * positionHandle.getAngle();
            hangerJoint.m_localAnchor1.x = SCALE * (950 + offset);
            crane.setTransform(new Vec2(SCALE * (950 + offset), SCALE * 1240), 0);
            leftCraneRollerJoint.m_localAnchor1.x = SCALE * (-160 + offset);
            rightCraneRollerJoint.m_localAnchor1.x = SCALE * (160 + offset);
        }
    }

    private class Environment extends GameObject {
        private Body body;

        public Environment(Game game) {
            super(game);
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyType.STATIC;
            body = getWorld().createBody(bodyDef);
            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.filter.categoryBits = 0xFF;
            fixtureDef.filter.maskBits = 0xFF;
            fixtureDef.density = 1;
            fixtureDef.restitution = 0.1f;
            fixtureDef.friction = 0.4f;

            for (PolygonShape shape : caveResources.shape().create(SCALE)) {
                fixtureDef.shape = shape;
                body.createFixture(fixtureDef);
            }

            fixtureDef.filter.categoryBits = 1;
            fixtureDef.filter.maskBits = 1;
            for (PolygonShape shape : caveResources.bridgeShape().create(SCALE)) {
                fixtureDef.shape = shape;
                body.createFixture(fixtureDef);
            }
            setZIndex(-1);
        }

        @Override
        protected void destroy() {
            getWorld().destroyBody(body);
        }

        @Override
        protected void paint(Graphics graphics) {
            graphics.pushTransform();
            graphics.scale(SCALE, SCALE);
            ImageUtil image = new ImageUtil(caveResources.background());
            image.draw(graphics, 0, 0, 2500, 1406);
            ImageUtil hole = new ImageUtil(caveResources.hole());
            hole.draw(graphics, 514, 1406 - 912 - 494, 233, 494);
            ImageUtil patch = new ImageUtil(caveResources.holePatch());
            float alpha = -0.5f + Math.abs(game.getRobot().getPosition().x - 600 * SCALE) / 1.2f;
            alpha = Math.max(0f, Math.min(1f, alpha));
            patch.draw(graphics, 494, 1406 - 904 - 500, 278, 500, alpha);
            ControlPanelResources controlPanelRes = game.loadResources(ControlPanelResources.class);
            ImageUtil controlPanel = new ImageUtil(controlPanelRes.panel());
            controlPanel.draw(graphics, 831, 1406 - 633, 180, 149);
            graphics.popTransform();
        }

        @Override
        protected boolean hasPoint(float x, float y) {
            Fixture fixture = body.getFixtureList();
            while (fixture != null) {
                if (fixture.testPoint(new Vec2(x, y))) {
                    return true;
                }
                fixture = fixture.getNext();
            }
            return false;
        }

        public Body getBody() {
            return body;
        }
    }
}
