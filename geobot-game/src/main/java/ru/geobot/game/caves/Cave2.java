package ru.geobot.game.caves;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.joints.RevoluteJoint;
import org.jbox2d.dynamics.joints.RevoluteJointDef;
import org.jbox2d.dynamics.joints.WeldJoint;
import org.jbox2d.dynamics.joints.WeldJointDef;
import ru.geobot.Game;
import ru.geobot.GameAdapter;
import ru.geobot.GameObject;
import ru.geobot.GameObjectAdapter;
import ru.geobot.game.GeobotGame;
import ru.geobot.game.objects.*;
import ru.geobot.graphics.Graphics;
import ru.geobot.graphics.ImageUtil;
import ru.geobot.graphics.Rectangle;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class Cave2 {
    public static final float SCALE = 10f / 2500;
    private GeobotGame game;
    private Cave2Resources caveResources;
    private CraneResources craneResources;
    private BobblerResources bobblerResources;
    private SafeResources safeResources;
    private GunResources gunResources;
    private NippersResources nippersResources;
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
    private Bobbler bobbler;
    private BodyObject secretCode;
    private RevoluteJoint secretCodeJoint;
    private RevoluteJoint secretCodeHandJoint;
    private BodyObject gun;
    private WeldJoint gunJoint;
    private GunClickSensor gunClickSensor;
    private BodyObject nippers;
    private float waterLevel = 0;
    private boolean waterLevelGrowing;
    private float vertCraneOffset;
    private float horzCraneOffset;
    private boolean nippersDropped;

    public Cave2(GeobotGame game) {
        this.game = game;
        caveResources = game.loadResources(Cave2Resources.class);
        craneResources = game.loadResources(CraneResources.class);
        bobblerResources = game.loadResources(BobblerResources.class);
        safeResources = game.loadResources(SafeResources.class);
        gunResources = game.loadResources(GunResources.class);
        nippersResources = game.loadResources(NippersResources.class);
        environment = new Environment(game);
        initControlPanel();
        game.setScale(1.1f);
        game.resizeWorld(2500 * SCALE, 1406 * SCALE);
        bobbler = new Bobbler(game);
        bobbler.setWaterLevel(0);
        new Safe();
        initSecretCode();
        initCrane();
        new Crane();
        new WaterTap();
        initGun();
        initNippers();
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
        ropeFactory.setDensity(0.1f);
        ropeFactory.setImage(craneResources.cable());
        ropeFactory.setMaskBits(0x100);
        ropeFactory.setCategoryBits(0x100);
        ropeFactory.getDrawFilters().add(leftRopeFilter);
        for (int i = 0; i < 32; ++i) {
            ropeFactory.addChunk((float)Math.PI / 2);
        }
        leftCraneRope = ropeFactory.create(game);

        ropeFactory.clearChunks();
        ropeFactory.getDrawFilters().clear();
        ropeFactory.setStartX(SCALE * 960);
        for (int i = 0; i < 29; ++i) {
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
            fixtureDef.density = 0.03f;
            fixtureDef.filter.categoryBits = 0x100;
            fixtureDef.filter.maskBits = 0x100;
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
        ropeFactory.setStartY(SCALE * 1950);
        ropeFactory.clearChunks();
        ropeFactory.getDrawFilters().clear();
        ropeFactory.getDrawFilters().add(centralRopeFilter);
        ropeFactory.setDensity(0.1f);
        for (int i = 0; i < 38; ++i) {
            ropeFactory.addChunk((float)Math.PI);
        }
        hangerRope = ropeFactory.create(game);

        jointDef.bodyA = environment.getBody();
        jointDef.bodyB = hangerRope.part(0);
        jointDef.localAnchorA.set(SCALE * 950, SCALE * 1950);
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

    private void initSecretCode() {
        BodyObjectBuilder builder = new BodyObjectBuilder(game);
        builder.setImage(bobblerResources.secretCodeImage());
        builder.setShape(bobblerResources.secretCodeShape());
        builder.setRealHeight(SCALE * 26);
        Vec2 vec = bobbler.getBody().getWorldPoint(new Vec2(SCALE * 26, 0));
        vec.y -= SCALE * 13;
        builder.getBodyDef().position.set(vec);
        builder.getBodyDef().type = BodyType.DYNAMIC;
        builder.getFixtureDef().density = 0.0001f;
        builder.getFixtureDef().filter.maskBits = 0x100;
        builder.getFixtureDef().filter.categoryBits = 0x100;
        secretCode = builder.build();
        RevoluteJointDef jointDef = new RevoluteJointDef();
        jointDef.bodyA = bobbler.getBody();
        jointDef.bodyB = secretCode.getBody();
        jointDef.localAnchorA.set(SCALE * 26, 0);
        jointDef.localAnchorB.set(0, 13 * SCALE);
        secretCodeJoint = (RevoluteJoint)game.getWorld().createJoint(jointDef);

        secretCode.addListener(new GameObjectAdapter() {
            @Override public boolean click() {
                return beginPickSecretCode();
            }
        });
    }

    private boolean beginPickSecretCode() {
        Robot robot = game.getRobot();
        if (robot.isCarriesObject()) {
            return false;
        }
        Vec2 pt = new Vec2(0, SCALE * 13);
        pt = secretCode.getBody().getWorldPoint(pt);
        robot.pickAt(pt.x, pt.y, new Runnable() {
            @Override public void run() {
                pickSecretCode();
            }
        });
        return true;
    }

    private void pickSecretCode() {
        if (secretCodeJoint == null) {
            return;
        }
        game.getWorld().destroyJoint(secretCodeJoint);
        secretCodeJoint = null;
        Robot robot = game.getRobot();
        RevoluteJointDef jointDef = new RevoluteJointDef();
        jointDef.bodyA = secretCode.getBody();
        jointDef.bodyB = robot.getHand();
        jointDef.localAnchorA.set(0, SCALE * 13);
        jointDef.localAnchorB.set(robot.getHandPickPoint());
        secretCodeHandJoint = (RevoluteJoint)game.getWorld().createJoint(jointDef);
    }

    private void initGun() {
        BodyObjectBuilder builder = new BodyObjectBuilder(game);
        builder.setImage(gunResources.image());
        builder.setShape(gunResources.shape());
        builder.getBodyDef().type = BodyType.DYNAMIC;
        builder.getBodyDef().position.set(SCALE * 1138, SCALE * 540);
        builder.getFixtureDef().filter.categoryBits = 4;
        builder.getFixtureDef().filter.maskBits = 4;
        builder.getFixtureDef().density = 0.01f;
        builder.setRealHeight(SCALE * 75);
        gun = builder.build();

        gun.addListener(new GameObjectAdapter() {
            @Override public boolean click() {
                return beginPickingGun();
            }
        });
    }

    private boolean beginPickingGun() {
        Robot robot = game.getRobot();
        if (robot.isCarriesObject() || nippersDropped) {
            return false;
        }
        Vec2 pt = new Vec2(SCALE * 105, SCALE * 50);
        pt = gun.getBody().getWorldPoint(pt);
        robot.pickAt(pt.x, pt.y, new Runnable() {
            @Override public void run() {
                pickGun();
            }
        });
        return true;
    }

    private void pickGun() {
        final Robot robot = game.getRobot();
        robot.setCarriesObject(true);
        WeldJointDef jointDef = new WeldJointDef();
        jointDef.bodyA = gun.getBody();
        jointDef.bodyB = robot.getHand();
        jointDef.localAnchorA.set(SCALE * 105, SCALE * 50);
        jointDef.localAnchorB.set(robot.getHandPickPoint());
        gunJoint = (WeldJoint)game.getWorld().createJoint(jointDef);
        game.addListener(gunGameAdapter);
        gunClickSensor = new GunClickSensor();
    }

    private void dropGun() {
        Robot robot = game.getRobot();
        robot.setCarriesObject(false);
        game.getWorld().destroyJoint(gunJoint);
        gunJoint = null;
        gunClickSensor.dispose();
        gunClickSensor = null;
        game.removeListener(gunGameAdapter);
    }

    private void shoot() {
        Vec2 pt = new Vec2(SCALE * 320, SCALE * 53);
        pt = gun.getBody().getWorldPoint(pt);
        new Bullet(pt.x, pt.y, gun.getBody().getAngle());

        //gun.getBody().setLinearVelocity(gun.getBody().getWorldVector(new Vec2(-10.5f, 0)));
        gun.getBody().applyLinearImpulse(gun.getBody().getWorldVector(new Vec2(-0.05f, 0)),
                gun.getBody().getWorldCenter());
    }

    private GameAdapter gunGameAdapter = new GameAdapter() {
        @Override public void mouseMoved(float x, float y) {
            Robot robot = game.getRobot();
            robot.pointAt(x, y);
        }
    };

    private class GunClickSensor extends GameObject {
        public GunClickSensor() {
            super(game);
            setZIndex(-1);
        }

        @Override
        protected boolean hasPoint(float x, float y) {
            return true;
        }

        @Override protected boolean click() {
            shoot();
            return true;
        }
    }

    private class Bullet extends GameObject {
        private Body body;
        private long creationTime = -1;
        private long currentTime;

        public Bullet(float x, float y, float angle) {
            super(game);
            BodyDef def = new BodyDef();
            def.type = BodyType.DYNAMIC;
            def.bullet = true;
            def.position.set(x, y);
            def.angle = angle;
            FixtureDef fixture = new FixtureDef();
            fixture.density = 1;
            fixture.friction = 0.01f;
            fixture.restitution = 0.2f;
            fixture.filter.maskBits = 2;
            fixture.filter.categoryBits = 2;
            PolygonShape shape = new PolygonShape();
            shape.set(new Vec2[] { new Vec2(0, SCALE * -2.5f), new Vec2(SCALE * 15, SCALE * -2.5f),
                    new Vec2(SCALE * 15, SCALE * 2.5f), new Vec2(0, SCALE * 2.5f) }, 4);
            fixture.shape = shape;
            body = game.getWorld().createBody(def);
            body.createFixture(fixture);
            Vec2 velocity = new Vec2(20, 0);
            body.setLinearVelocity(body.getWorldVector(velocity));
            body.setUserData(42);
        }

        @Override
        protected void paint(Graphics graphics) {
            graphics.pushTransform();
            Vec2 pos = body.getPosition();
            graphics.translate(pos.x, pos.y);
            graphics.rotate(body.getAngle());
            graphics.translate(0, SCALE * 2.5f);
            graphics.scale(SCALE, -SCALE);
            float alpha = Math.min(1, Math.max(0, (2000 + creationTime - currentTime) / 1000f));
            gunResources.bulletImage().draw(graphics, alpha);
            graphics.popTransform();
        }

        @Override
        protected void time(long time) {
            if (creationTime == -1) {
                creationTime = time;
            }
            currentTime = time;
            if (currentTime - creationTime > 3000) {
                dispose();
            }
            super.time(time);
        }

        @Override
        protected void destroy() {
            game.getWorld().destroyBody(body);
            super.destroy();
        }
    }

    private void initNippers() {
        BodyObjectBuilder builder = new BodyObjectBuilder(game);
        builder.setImage(nippersResources.image());
        builder.setShape(nippersResources.shape());
        builder.getBodyDef().type = BodyType.DYNAMIC;
        builder.getBodyDef().position.set(SCALE * 1693, SCALE * 1218);
        builder.getFixtureDef().filter.categoryBits = 2;
        builder.getFixtureDef().filter.maskBits = 2;
        builder.getFixtureDef().density = 0.01f;
        builder.getFixtureDef().friction = 0.7f;
        builder.getFixtureDef().restitution = 0.5f;
        builder.setRealHeight(SCALE * 60);
        nippers = builder.build();
        nippers.addListener(nippersLevelListener);
    }

    private GameObjectAdapter nippersLevelListener = new GameObjectAdapter() {
        @Override public void time(long time) {
            if (!nippersDropped && gunJoint != null && nippers.getBody().getPosition().y < SCALE * 700) {
                dropGun();
                nippers.removeListener(nippersLevelListener);
                nippersDropped = true;
            }
        };
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

            graphics.pushTransform();
            graphics.scale(SCALE, SCALE);
            graphics.clip(new Rectangle(514, 0, 233, waterLevel));
            ImageUtil water = new ImageUtil(caveResources.water());
            water.draw(graphics, 514, 0, 233, 494, 0.4f);
            graphics.popClip();
            ImageUtil patch = new ImageUtil(caveResources.holePatch());
            float alpha = -0.5f + Math.abs(game.getRobot().getPosition().x - 600 * SCALE) / 1.2f;
            alpha = Math.max(0f, Math.min(1f, alpha));
            patch.draw(graphics, 494, 2, 278, 500, alpha);
            graphics.popTransform();
        }

        @Override
        protected void time(long time) {
            float h = SCALE * (1950 + 4 * heightHandle.getAngle()) + vertCraneOffset;
            h = Math.min(SCALE * 2200, Math.max(SCALE * 1260, h));
            vertCraneOffset = h - SCALE * (1950 + 4 * heightHandle.getAngle());
            hangerJoint.m_localAnchor1.y = h;

            float pos = SCALE * (950 + 4 * positionHandle.getAngle()) + horzCraneOffset;
            pos = Math.min(SCALE * 1250, Math.max(SCALE * 550, pos));
            horzCraneOffset = pos - SCALE * (950 + 4 * positionHandle.getAngle());
            hangerJoint.m_localAnchor1.x = pos;
            crane.setTransform(new Vec2(pos, SCALE * 1240), 0);
            leftCraneRollerJoint.m_localAnchor1.x = pos + SCALE * (-160 - 950);
            rightCraneRollerJoint.m_localAnchor1.x = pos + SCALE * (160 - 950);
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

            fixtureDef.filter.categoryBits = 0x100;
            fixtureDef.filter.maskBits = 0x100;
            for (PolygonShape shape : caveResources.holeShape().create(SCALE)) {
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
            hole.draw(graphics, 514, 0, 233, 494);
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

    private class WaterTap extends GameObject {
        private RevoluteJoint handJoint;

        public WaterTap() {
            super(game);
        }

        @Override
        protected boolean hasPoint(float x, float y) {
            return x >= SCALE * 140 && y >= SCALE * 560 && x <= SCALE * 210 && y <= SCALE * 680;
        }

        @Override
        protected boolean click() {
            if (handJoint != null) {
                game.getRobot().setArmForced(true);
                game.getRobot().setCarriesObject(false);
                game.getWorld().destroyJoint(handJoint);
                handJoint = null;
                waterLevelGrowing = false;
                return true;
            }
            if (game.getRobot().isCarriesObject()) {
                return false;
            }
            Vec2 pt = new Vec2(SCALE * 175, SCALE * 620);
            game.getRobot().pickAt(pt.x, pt.y, new Runnable() {
                @Override public void run() {
                    connectToHand();
                }
            });
            return true;
        }

        private void connectToHand() {
            RevoluteJointDef jointDef = new RevoluteJointDef();
            jointDef.bodyA = environment.getBody();
            jointDef.bodyB = game.getRobot().getHand();
            jointDef.localAnchorA.x = SCALE * 175;
            jointDef.localAnchorA.y = SCALE * 620;
            jointDef.localAnchorB.set(game.getRobot().getHandPickPoint());
            handJoint = (RevoluteJoint)game.getWorld().createJoint(jointDef);
            waterLevelGrowing = true;
            game.getRobot().setArmForced(false);
            game.getRobot().setCarriesObject(true);
        }

        @Override
        protected void time(long time) {
            if (waterLevelGrowing) {
                waterLevel = Math.min(waterLevel + 0.1f, 120);
                bobbler.setWaterLevel(waterLevel * SCALE);
            }
        }
    }

    private class Safe extends GameObject {
        private long currentTime;
        private long openStartTime;
        private boolean opened;

        public Safe() {
            super(game);
        }

        @Override
        protected void paint(Graphics graphics) {
            graphics.pushTransform();
            graphics.scale(SCALE, SCALE);
            graphics.translate(2208, 587);
            graphics.translate(0, 296);
            graphics.scale(1, -1);
            if (opened) {
                safeResources.opened().draw(graphics);
                if (currentTime > openStartTime + 400) {
                    graphics.translate(233, -55);
                    safeResources.door2().draw(graphics);
                } else {
                    graphics.translate(151, -36);
                    safeResources.door1().draw(graphics);
                }
            } else {
                safeResources.closed().draw(graphics);
            }
            graphics.popTransform();
        }

        public void open() {
            if (opened) {
                return;
            }
            opened = true;
            openStartTime = currentTime;
        }

        @Override
        protected void time(long time) {
            currentTime = time;
        }

        @Override
        protected boolean click() {
            if (secretCodeHandJoint == null) {
                return false;
            }
            Robot robot = game.getRobot();
            robot.pickAt(SCALE * 2410, SCALE * 774, new Runnable() {
                @Override public void run() {
                    useSecretCode();
                }
            });
            return true;
        }

        @Override
        protected boolean hasPoint(float x, float y) {
            x /= SCALE;
            y /= SCALE;
            return x >= 2201 && x < 2479 && y >= 587 && y <= 987;
        }

        private void useSecretCode() {
            Robot robot = game.getRobot();
            robot.setArmForced(true);
            robot.setCarriesObject(false);
            getWorld().destroyJoint(secretCodeHandJoint);
            secretCodeHandJoint = null;
            secretCode.dispose();
            open();
        }
    }
}
