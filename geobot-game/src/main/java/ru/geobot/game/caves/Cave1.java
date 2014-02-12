package ru.geobot.game.caves;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.RevoluteJointDef;
import org.jbox2d.dynamics.joints.WeldJointDef;
import ru.geobot.Game;
import ru.geobot.GameObject;
import ru.geobot.GameObjectAdapter;
import ru.geobot.game.GeobotGame;
import ru.geobot.game.objects.*;
import ru.geobot.graphics.Graphics;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class Cave1 {
    private static final Vec2 ropeConn = new Vec2(8.5f, 5.0f);
    private static final Vec2 bucketPos = new Vec2(7.8f, 1.25f);
    private static final Vec2 pickPos = new Vec2(4.4f, 1.25f);
    private static final float bucketSize = 0.45f;
    private static final float pickSize = 0.6f;
    private GeobotGame game;
    private Environment env;
    private Rope rope;
    private BodyObject bucket;
    private BodyObject pick;
    private ObjectResources resources;
    private Joint bucketPickJoint;
    private Joint ropeBucketJoint;
    private Joint pickHandJoint;

    public Cave1(GeobotGame game) {
        this.game = game;
        resources = game.loadResources(ObjectResources.class);
        env = new Environment(game);
        createRope();
        createPick();
        createBucket();
    }

    private void createRope() {
        RopeFactory ropeFactory = new RopeFactory();
        ropeFactory.setResistance(0.01f);
        ropeFactory.setWidth(0.05f);
        ropeFactory.setStartX(ropeConn.x);
        ropeFactory.setStartY(ropeConn.y);
        ropeFactory.setCategoryBits(2);
        ropeFactory.setMaskBits(2);
        ropeFactory.setDensity(0.9f);
        for (int i = 0; i < 22; ++i) {
            ropeFactory.addChunk((float)Math.PI);
        }
        rope = ropeFactory.create(game);
        RevoluteJointDef ropeJointDef = new RevoluteJointDef();
        ropeJointDef.bodyA = env.getBody();
        ropeJointDef.bodyB = rope.part(0);
        ropeJointDef.localAnchorA = ropeConn;
        ropeJointDef.localAnchorB = new Vec2(0, 0);
        game.getWorld().createJoint(ropeJointDef);
        rope.addListener(ropeClickListener);
    }

    private void createBucket() {
        BodyObjectBuilder builder = new BodyObjectBuilder(game);
        builder.setImage(resources.bucketImage());
        builder.setShape(resources.bucketShape());
        builder.setRealHeight(bucketSize);
        builder.getBodyDef().position = bucketPos;
        builder.getBodyDef().type = BodyType.DYNAMIC;
        builder.getFixtureDef().filter.categoryBits = 2;
        builder.getFixtureDef().filter.maskBits = 2;
        builder.getFixtureDef().density = 0.005f;
        bucket = builder.build();
        bucket.addListener(bucketPickListener);
        bucket.changeZIndex(2);
    }

    private GameObjectAdapter bucketPickListener = new GameObjectAdapter() {
        @Override public boolean click() {
            Vec2 pickPoint = bucket.getBody().getWorldPoint(new Vec2(0.2083f, 0.6815f));
            if (bucketPickJoint != null) {
                game.getWorld().destroyJoint(bucketPickJoint);
                bucketPickJoint = null;
                if (ropeBucketJoint == null) {
                    bucket.setImage(resources.bucketImage());
                }
            } else if (pickHandJoint != null) {
                game.getRobot().pickAt(pickPoint.x, pickPoint.y, new Runnable() {
                    @Override public void run() {
                        game.getWorld().destroyJoint(pickHandJoint);
                        pickHandJoint = null;
                        putPickIntoBucket();
                    }
                });
            } else if (ropeBucketJoint != null) {
                pickPoint = bucket.getBody().getWorldPoint(new Vec2(0.2083f, 0.2f));
                game.getRobot().pickAt(pickPoint.x, pickPoint.y, new Runnable() {
                    @Override public void run() {
                        pickBucket(new Vec2(0.2083f, 0.2f));
                    }
                });
            } else {
                game.getRobot().pickAt(pickPoint.x, pickPoint.y, new Runnable() {
                    @Override public void run() {
                        pickBucket(new Vec2(0.2083f, 0.6815f));
                    }
                });
            }
            return true;
        }
    };

    private GameObjectAdapter ropeClickListener = new GameObjectAdapter() {
        @Override public boolean click() {
            if (bucketPickJoint != null) {
                Body ropePart = rope.part(rope.partCount() - 1);
                Vec2 pickPoint = ropePart.getWorldPoint(new Vec2(0, rope.getChunkLength()));
                game.getRobot().pickAt(pickPoint.x, pickPoint.y, new Runnable() {
                    @Override public void run() {
                        game.getWorld().destroyJoint(bucketPickJoint);
                        bucketPickJoint = null;
                        hangBucket();
                    }
                });
                return true;
            }
            return false;
        }
    };

    private void pickBucket(Vec2 pt) {
        RevoluteJointDef jointDef = new RevoluteJointDef();
        jointDef.bodyA = bucket.getBody();
        jointDef.bodyB = game.getRobot().getHand();
        jointDef.collideConnected = false;
        jointDef.localAnchorA.x = pt.x;
        jointDef.localAnchorA.y = pt.y;
        jointDef.localAnchorB.set(game.getRobot().getHandPickPoint());
        bucket.setImage(resources.bucketOnRopeImage());
        bucketPickJoint = game.getWorld().createJoint(jointDef);
    }

    private void hangBucket() {
        RevoluteJointDef jointDef = new RevoluteJointDef();
        jointDef.bodyA = bucket.getBody();
        jointDef.bodyB = rope.part(rope.partCount() - 1);
        jointDef.collideConnected = false;
        jointDef.localAnchorA.x = 0.2083f;
        jointDef.localAnchorA.y = 0.6815f;
        jointDef.localAnchorB.y = rope.getChunkLength();
        bucket.setImage(resources.bucketOnRopeImage());
        ropeBucketJoint = game.getWorld().createJoint(jointDef);
    }

    private void createPick() {
        BodyObjectBuilder builder = new BodyObjectBuilder(game);
        builder.setImage(resources.pickImage());
        builder.setShape(resources.pickShape());
        builder.setRealHeight(pickSize);
        builder.getBodyDef().position = pickPos;
        builder.getBodyDef().type = BodyType.DYNAMIC;
        builder.getFixtureDef().density = 0.007f;
        builder.getFixtureDef().filter.categoryBits = 2;
        builder.getFixtureDef().filter.maskBits = 2;
        pick = builder.build();
        pick.addListener(pickListener);
        pick.changeZIndex(1);
    }

    private GameObjectAdapter pickListener = new GameObjectAdapter() {
        @Override public boolean click() {
            if (bucketPickJoint != null) {
                return false;
            } else if (pickHandJoint == null) {
                Vec2 pickPoint = pick.getBody().getWorldPoint(new Vec2(1135 * pickSize / 1200, 591 * pickSize / 1200));
                game.getRobot().pickAt(pickPoint.x, pickPoint.y, new Runnable() {
                    @Override public void run() {
                        carryPick();
                    }
                });
            } else {
                game.getWorld().destroyJoint(pickHandJoint);
                pickHandJoint = null;
            }
            return true;
        }
    };

    private void carryPick() {
        RevoluteJointDef jointDef = new RevoluteJointDef();
        jointDef.bodyA = pick.getBody();
        jointDef.bodyB = game.getRobot().getHand();
        jointDef.collideConnected = false;
        jointDef.localAnchorA.x = 1135 * pickSize / 1200;
        jointDef.localAnchorA.y = 591 * pickSize / 1200;
        jointDef.localAnchorB.set(game.getRobot().getHandPickPoint());
        pickHandJoint = game.getWorld().createJoint(jointDef);
    }

    private void putPickIntoBucket() {
        Vec2 offset = new Vec2(-0.47f * bucketSize, bucketSize * 1.1f);
        Transform transform = new Transform();
        transform.set(new Vec2(0, 0), bucket.getBody().getAngle());
        Vec2 pos = bucket.getBody().getPosition().add(Transform.mul(transform, offset));
        pick.getBody().setTransform(pos, bucket.getBody().getAngle() - (float)Math.PI * 0.4f);

        WeldJointDef jointDef = new WeldJointDef();
        jointDef.bodyA = pick.getBody();
        jointDef.bodyB = bucket.getBody();
        jointDef.collideConnected = false;
        jointDef.localAnchorB.set(jointDef.bodyB.getLocalCenter());
        jointDef.localAnchorA.x = 850 * pickSize / 1200;
        jointDef.localAnchorA.y = 591 * pickSize / 1200;
        jointDef.referenceAngle = (float)Math.PI / 180 * 80;
        game.getWorld().createJoint(jointDef);
    }

    public Game getGame() {
        return game;
    }

    private static class Environment extends GameObject {
        private Body body;

        public Environment(Game game) {
            super(game);
            Cave1Resources resources = game.loadResources(Cave1Resources.class);
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyType.STATIC;
            body = getWorld().createBody(bodyDef);
            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.filter.categoryBits = 0xFFF;
            fixtureDef.filter.maskBits = 0xFFF;
            fixtureDef.density = 1;
            fixtureDef.restitution = 0.1f;
            fixtureDef.friction = 0.4f;

            for (PolygonShape shape : resources.shape().create(13.333f / 2500)) {
                fixtureDef.shape = shape;
                body.createFixture(fixtureDef);
            }
        }

        @Override
        protected void destroy() {
            getWorld().destroyBody(body);
        }

        @Override
        protected void paint(Graphics graphics) {
            super.paint(graphics);
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
