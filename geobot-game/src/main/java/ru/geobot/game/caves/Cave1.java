package ru.geobot.game.caves;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.joints.DistanceJointDef;
import org.jbox2d.dynamics.joints.RevoluteJointDef;
import ru.geobot.Game;
import ru.geobot.GameAdapter;
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
    private GameObject selectedObject;

    public Cave1(GeobotGame game) {
        this.game = game;
        resources = game.loadResources(ObjectResources.class);
        env = new Environment(game);
        createRope();
        createBucket();
        createPick();
    }

    private void createRope() {
        RopeFactory ropeFactory = new RopeFactory();
        ropeFactory.setResistance(0.01f);
        ropeFactory.setWidth(0.05f);
        ropeFactory.setStartX(ropeConn.x);
        ropeFactory.setStartY(ropeConn.y);
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
        rope.addListener(new GameObjectAdapter() {
            @Override public boolean click() {
                if (selectedObject == bucket) {
                    bucket.dispose();
                    bucket = null;
                }
                return true;
            }
        });
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
        bucket.addListener(new GameObjectAdapter() {
            @Override public boolean click() {
                Vec2 pickPoint = bucket.getBody().getWorldPoint(new Vec2(0.2083f, 0.6815f));
                game.getRobot().pickAt(pickPoint.x, pickPoint.y, new Runnable() {
                    @Override public void run() {
                        pickBucket();
                    }
                });
                return true;
            }
        });
    }

    private void pickBucket() {
        RevoluteJointDef jointDef = new RevoluteJointDef();
        jointDef.bodyA = bucket.getBody();
        jointDef.bodyB = game.getRobot().getHand();
        jointDef.collideConnected = false;
        jointDef.localAnchorA.x = 0.2083f;
        jointDef.localAnchorA.y = 0.6815f;
        jointDef.localAnchorB.set(game.getRobot().getHandPickPoint());
        bucket.setImage(resources.bucketOnRopeImage());
        game.getWorld().createJoint(jointDef);
    }

    private GameAdapter gameAdapter = new GameAdapter() {
        @Override
        public void objectClicked(GameObject object) {
            if (selectedObject == object) {
                return;
            }
            selectedObject = null;
            game.removeListener(gameAdapter);
        }
    };


    private void createPick() {
        BodyObjectBuilder builder = new BodyObjectBuilder(game);
        builder.setImage(resources.pickImage());
        builder.setShape(resources.pickShape());
        builder.setRealHeight(pickSize);
        builder.getBodyDef().position = pickPos;
        builder.getBodyDef().type = BodyType.DYNAMIC;
        builder.getFixtureDef().density = 2.2f;
        builder.getFixtureDef().filter.categoryBits = 2;
        builder.getFixtureDef().filter.maskBits = 2;
        pick = builder.build();

        pick.addListener(new GameObjectAdapter() {
            @Override public boolean click() {
                return true;
            }
        });
    }

    private void movePickIntoBucket() {
        Vec2 offset = new Vec2(-0.47f * bucketSize, bucketSize * 1.1f);
        Transform transform = new Transform();
        transform.set(new Vec2(0, 0), bucket.getBody().getAngle());
        Vec2 pos = bucket.getBody().getPosition().add(Transform.mul(transform, offset));
        pick.getBody().setTransform(pos, bucket.getBody().getAngle() - (float)Math.PI * 0.4f);


        DistanceJointDef jointDef = new DistanceJointDef();
        jointDef.bodyA = pick.getBody();
        jointDef.bodyB = bucket.getBody();
        jointDef.collideConnected = true;
        jointDef.length = jointDef.bodyA.getWorldCenter().sub(jointDef.bodyB.getWorldCenter()).length();
        jointDef.localAnchorA.set(jointDef.bodyA.getLocalCenter());
        jointDef.localAnchorB.set(jointDef.bodyB.getLocalCenter());
        jointDef.dampingRatio = 0.08f;
        jointDef.frequencyHz = 10f;
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
