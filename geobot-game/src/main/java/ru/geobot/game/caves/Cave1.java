package ru.geobot.game.caves;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.joints.RevoluteJointDef;
import ru.geobot.*;
import ru.geobot.game.objects.*;
import ru.geobot.graphics.Graphics;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class Cave1 {
    private static final Vec2 ropeConn = new Vec2(8.5f, 4.8f);
    private static final Vec2 bucketPos = new Vec2(7.8f, 1.25f);
    private static final float bucketSize = 0.45f;
    private Game game;
    private Environment env;
    private Rope rope;
    private BodyObject bucket;
    private ObjectResources resources;
    private boolean bucketPicked;

    public Cave1(Game game) {
        this.game = game;
        resources = game.loadResources(ObjectResources.class);
        env = new Environment(game);
        createRope();
        createBucket();
    }

    private void createRope() {
        RopeFactory ropeFactory = new RopeFactory();
        ropeFactory.setResistance(0.01f);
        ropeFactory.setWidth(0.05f);
        ropeFactory.setStartX(ropeConn.x);
        ropeFactory.setStartY(ropeConn.y);
        ropeFactory.setDensity(0.9f);
        for (int i = 0; i < 21; ++i) {
            ropeFactory.addChunk((float)Math.PI);
        }
        rope = ropeFactory.create(game);
        RevoluteJointDef ropeJointDef = new RevoluteJointDef();
        ropeJointDef.bodyA = env.getBody();
        ropeJointDef.bodyB = rope.part(0);
        ropeJointDef.localAnchorA = ropeConn;
        ropeJointDef.localAnchorB = new Vec2(0, 0);
        game.getWorld().createJoint(ropeJointDef);
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
        bucket = builder.build();
        bucket.addListener(new GameObjectAdapter() {
            @Override public void click() {
                bucketPicked = true;
                game.addListener(bucketGameAdapter);
            }
        });
    }

    private GameAdapter bucketGameAdapter = new GameAdapter() {
        @Override
        public void objectClicked(GameObject object) {
            if (object == bucket) {
                return;
            }
            if (object == rope && bucketPicked) {
                bucket.dispose();
                bucket = null;
                createBucketOnRope();
            }
            bucketPicked = false;
            game.removeListener(bucketGameAdapter);
        }
    };

    private void createBucketOnRope() {
        BodyObjectBuilder builder = new BodyObjectBuilder(game);
        builder.setImage(resources.bucketOnRopeImage());
        builder.setShape(resources.bucketOnRopeShape());
        float bucketHeight = bucketSize * resources.bucketOnRopeImage().getHeight() /
                resources.bucketImage().getHeight();
        builder.setRealHeight(bucketHeight);
        Body ropePart = rope.part(rope.partCount() - 1);
        Vec2 pos = ropePart.getPosition().add(new Vec2(0, -rope.getChunkLength()));
        float bucketWidth = resources.bucketOnRopeImage().getWidth() * bucketHeight /
                resources.bucketOnRopeImage().getHeight();
        pos = pos.add(new Vec2(-bucketWidth / 2, -bucketHeight));
        builder.getBodyDef().position = pos;
        builder.getBodyDef().type = BodyType.DYNAMIC;
        builder.getFixtureDef().filter.categoryBits = 1;
        builder.getFixtureDef().filter.maskBits = 1;
        builder.getFixtureDef().density = 0.3f;
        bucket = builder.build();

        RevoluteJointDef jointDef = new RevoluteJointDef();
        jointDef.collideConnected = false;
        jointDef.bodyA = ropePart;
        jointDef.bodyB = bucket.getBody();
        jointDef.localAnchorA = new Vec2(0, rope.getChunkLength());
        jointDef.localAnchorB = new Vec2(new Vec2(bucketWidth / 2, bucketHeight));
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
