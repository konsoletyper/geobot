package ru.geobot.caves;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.RevoluteJointDef;
import ru.geobot.Game;
import ru.geobot.GameAdapter;
import ru.geobot.GameObject;
import ru.geobot.GameObjectAdapter;
import ru.geobot.graphics.AffineTransform;
import ru.geobot.graphics.Color;
import ru.geobot.graphics.Graphics;
import ru.geobot.graphics.Rectangle;
import ru.geobot.objects.Rope;
import ru.geobot.objects.RopeFactory;
import ru.geobot.objects.Stone;

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
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyType.STATIC;
            body = getWorld().createBody(bodyDef);
            int[] coords = { 231, 16, 112, 129, 75, 302, 73, 559, 50, 644, 63, 735,
                    32, 874, 49, 959, 113, 1019, 241, 1095, 389, 1149, 585, 1185, 819, 1166,
                    937, 1179, 1069, 1148, 1125, 1177, 1235, 1157, 1282, 1175, 1499, 1193,
                    1589, 1185, 1640, 1202, 1697, 1192, 1731, 1189, 1764, 1172, 1824, 1197,
                    1844, 1197, 1838, 1170, 1872, 1128, 1861, 1107, 1889, 1052,
                    1900, 1006, 1935, 968, 1916, 947, 1947, 921, 1943, 892, 1967, 859,
                    1967, 832, 1948, 816, 1949, 753, 1974, 677 };
            PolygonShape shape = new PolygonShape();
            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = shape;
            fixtureDef.density = 1;
            fixtureDef.restitution = 0.1f;
            fixtureDef.friction = 0.4f;

            for (int i = 2; i < coords.length; i += 2) {
                Vec2 a = map(coords[i - 2], coords[i - 1]);
                Vec2 b = map(coords[i], coords[i + 1]);
                Vec2 dir = a.sub(b);
                float len = dir.normalize();
                dir = dir.mul(0.01f / len);
                Vec2 c = new Vec2(b.x + dir.y, b.y - dir.x);
                Vec2 d = new Vec2(a.x + dir.y, a.y - dir.x);
                Vec2[] line = { a, b, c, d };
                shape.set(line, line.length);
                body.createFixture(fixtureDef);
            }
        }

        private Vec2 map(int x, int y) {
            return new Vec2(x * 13.333f / 2500, (1406 - y) * 7.5f / 1406);
        }

        @Override
        protected void destroy() {
            getWorld().destroyBody(body);
        }

        @Override
        protected void paint(Graphics graphics) {
            /*graphics.setColor(Color.red());
            graphics.setStrokeWidth(3);
            for (Fixture fixture = body.getFixtureList(); fixture != null;
                    fixture = fixture.getNext()) {
                PolygonShape shape = (PolygonShape)fixture.getShape();
                boolean first = true;
                for (Vec2 v : shape.getVertices()) {
                    if (first) {
                        graphics.moveTo(v.x, v.y);
                        first = false;
                    } else {
                        graphics.lineTo(v.x, v.y);
                    }
                }
                graphics.fill();
            }*/

            //graphics.fillRectangle(0, 0, 15, 0.2f);
            //graphics.fillRectangle(0, 0, 0.2f, 5);
            //graphics.fillRectangle(14.8f, 0, 0.2f, 5);
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
    }

    private static class Ball extends GameObject {
        private Body body;
        private Cave1Images images;

        public Ball(Game game) {
            super(game);
            images = game.loadImages(Cave1Images.class);
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyType.DYNAMIC;
            bodyDef.position.set(4, 3);
            body = getWorld().createBody(bodyDef);
            FixtureDef fixtureDef = new FixtureDef();
            PolygonShape shape = new PolygonShape();
            Vec2[] points = new Vec2[20];
            for (int i = 0; i < points.length; ++i) {
                points[i] = new Vec2(0.25f * (float)Math.cos(i * 2 * Math.PI / points.length),
                        0.25f * (float)Math.sin(i * 2 * Math.PI / points.length));
            }
            shape.set(points, points.length);
            fixtureDef.shape = shape;
            fixtureDef.restitution = 0.5f;
            fixtureDef.density = 1f;
            body.createFixture(fixtureDef);
        }

        @Override
        protected void destroy() {
            getWorld().destroyBody(body);
        }

        @Override
        protected void paint(Graphics graphics) {
            graphics.setColor(isUnderMouse() ? Color.blue() : Color.green());
            Vec2 center = body.getPosition();
            AffineTransform transform = AffineTransform.identity();
            transform.translate(center.x, center.y);
            transform.rotate(body.getAngle());
            transform.translate(-0.25f, 0.25f);
            transform.scale(0.5f / images.ball().getWidth(), -0.5f / images.ball().getHeight());
            AffineTransform oldTransform = graphics.getTransform();
            graphics.transform(transform);
            images.ball().draw(graphics);
            graphics.setTransform(oldTransform);
            super.paint(graphics);
        }

        @Override
        protected boolean hasPoint(float x, float y) {
            return body.getFixtureList().testPoint(new Vec2(x, y));
        }

        @Override
        protected void mouseEnter() {
            body.applyForce(new Vec2(20f, 85f), body.getPosition().addLocal(0.03f, 0.0f));
            super.mouseEnter();
        }
    }

    private static class RopePendant extends GameObject {
        private Body pendant;
        private Rope rope;
        private Joint joint;

        public RopePendant(Game game, float x, float y) {
            super(game);
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(0.05f, 0.05f);
            BodyDef pendantDef = new BodyDef();
            pendantDef.type = BodyType.STATIC;
            pendantDef.position = new Vec2(x, y);
            pendant = getWorld().createBody(pendantDef);
            FixtureDef pendantFixtureDef = new FixtureDef();
            pendantFixtureDef.filter.groupIndex = -1;
            pendantFixtureDef.shape = shape;
            pendant.createFixture(pendantFixtureDef);
        }

        @Override
        protected boolean hasPoint(float x, float y) {
            Vec2 pt = pendant.getPosition();
            return new Rectangle(pt.x - 0.05f, pt.y - 0.05f, 0.1f, 0.1f).contains(x, y);
        }

        @Override
        protected void paint(Graphics graphics) {
            graphics.setColor(!isUnderMouse() ? Color.magenta() : Color.blue());
            Vec2 pt = pendant.getPosition();
            graphics.fillRectangle(pt.x - 0.05f, pt.y - 0.05f, 0.1f, 0.1f);
        }

        public void connectRope(Rope rope) {
            if (this.rope != null) {
                disconnectRope();
            }
            RevoluteJointDef pendantJointDef = new RevoluteJointDef();
            pendantJointDef.bodyA = pendant;
            pendantJointDef.localAnchorA.y = -0.15f;
            pendantJointDef.bodyB = rope.part(0);
            joint = getWorld().createJoint(pendantJointDef);
            this.rope = rope;
        }

        public void disconnectRope() {
            if (this.rope != null) {
                getWorld().destroyJoint(joint);
                joint = null;
                this.rope = null;
            }
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
