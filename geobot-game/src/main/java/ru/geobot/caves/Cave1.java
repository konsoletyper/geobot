package ru.geobot.caves;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.RevoluteJointDef;
import ru.geobot.Game;
import ru.geobot.GameAdapter;
import ru.geobot.GameObject;
import ru.geobot.graphics.Color;
import ru.geobot.graphics.Graphics;
import ru.geobot.graphics.Rectangle;
import ru.geobot.objects.Rope;
import ru.geobot.objects.RopeFactory;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class Cave1 {
    private Game game;
    private RopePendant firstPendant;
    private boolean connectingRope;
    private RopePendant connectingPendant;

    public Cave1(Game game) {
        this.game = game;
        new Environment(game);
        //new Ball(game);
        RopeFactory ropeFactory = new RopeFactory();
        ropeFactory.setResistance(0.01f);
        ropeFactory.setChunkLength(0.2f);
        ropeFactory.setWidth(0.05f);
        ropeFactory.setStartX(4);
        ropeFactory.setStartY(4.8f);
        ropeFactory.setDensity(0.9f);
        for (int i = 0; i < 19; ++i) {
            ropeFactory.addChunk((float)Math.PI);
        }
        /*final Rope rope = ropeFactory.create(game);
        firstPendant = new RopePendant(game, 4, 4.9f);
        new RopePendant(game, 6, 4f);
        firstPendant.connectRope(rope);
        connectingPendant = firstPendant;
        for (int i = 0; i < 20; ++i) {
            new Stone(game, 1 + 0.3f * i, 0.24f, 0.022f);
        }
        new HeavyAnchor(game);

        rope.addListener(new GameObjectAdapter() {
            @Override
            public void click() {
                connectingRope = true;
                rope.setColor(Color.red());
            }

            @Override
            public void mouseEnter() {
                rope.setColor(Color.blue());
            }

            @Override
            public void mouseLeave() {
                if (!connectingRope) {
                    rope.setColor(Color.yellow());
                }
            }
        });
        game.addListener(new GameAdapter() {
            @Override
            public void objectClicked(GameObject object) {
                if (connectingRope) {
                    if (object instanceof RopePendant) {
                        connectingPendant.disconnectRope();
                        connectingPendant = (RopePendant)object;
                        connectingPendant.connectRope(rope);
                    }
                    if (object != rope) {
                        connectingRope = false;
                        rope.setColor(Color.yellow());
                    }
                }
            }
        });*/
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

            for (Fixture fixture = body.getFixtureList(); fixture != null; fixture = fixture.getNext()) {
                PolygonShape shape = (PolygonShape)fixture.getShape();
                Vec2[] vertices = shape.getVertices();
                Vec2 v = vertices[shape.getVertexCount() - 1];
                graphics.setColor(Color.red());
                graphics.moveTo(v.x, v.y);
                for (int i = 0; i < shape.getVertexCount(); ++i) {
                    graphics.lineTo(vertices[i].x, vertices[i].y);
                }
                graphics.stroke();
            }
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
    }

    private static class RopePendant extends GameObject {
        public RopePendant(Game game) {
            super(game);
        }

        @Override
        protected boolean hasPoint(float x, float y) {
            return false;
        }

        @Override
        protected void paint(Graphics graphics) {
        }
    }

    private static class HeavyAnchor extends GameObject {
        private Body body;
        private Joint joint;
        private boolean connecting;

        public HeavyAnchor(Game game) {
            super(game);
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(0.2f, 0.3f);
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyType.DYNAMIC;
            bodyDef.position = new Vec2(8, 0.7f);
            body = getWorld().createBody(bodyDef);
            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = shape;
            fixtureDef.density = 1;
            fixtureDef.restitution = 0.3f;
            body.createFixture(fixtureDef);
            getGame().addListener(new GameAdapter() {
                @Override
                public void objectClicked(GameObject object) {
                    if (connecting && object instanceof Rope) {
                        if (joint != null) {
                            getWorld().destroyJoint(joint);
                        }
                        Rope rope = (Rope)object;
                        Body ropePart = rope.part(rope.partCount() - 1);
                        RevoluteJointDef jointDef = new RevoluteJointDef();
                        jointDef.bodyA = body;
                        jointDef.bodyB = ropePart;
                        jointDef.localAnchorA = new Vec2(0, 0.21f);
                        jointDef.localAnchorB = new Vec2(0, rope.getChunkLength());
                        body.setTransform(ropePart.getWorldPoint(new Vec2(0, 0.31f)), 0);
                        joint = getWorld().createJoint(jointDef);
                    }
                    if (object != HeavyAnchor.this) {
                        connecting = false;
                    }
                }
            });
        }

        @Override
        protected void click() {
            connecting = true;
        }

        @Override
        protected boolean hasPoint(float x, float y) {
            Vec2 pt = body.getLocalPoint(new Vec2(x, y));
            return new Rectangle(-0.2f, - 0.3f, 0.4f, 0.6f).contains(pt.x, pt.y);
        }

        @Override
        protected void paint(Graphics graphics) {
            graphics.setColor(!connecting ? (!isUnderMouse() ? Color.pink() : Color.blue()) :
                    Color.red());
            Vec2 pt = body.getPosition();
            graphics.translate(pt.x, pt.y);
            graphics.rotate(body.getAngle());
            graphics.fillRectangle(-0.2f, -0.3f, 0.4f, 0.6f);
        }
    }
}
