package ru.geobot.game.caves;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import ru.geobot.Game;
import ru.geobot.GameObject;
import ru.geobot.game.GeobotGame;
import ru.geobot.graphics.Graphics;
import ru.geobot.graphics.ImageUtil;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class Cave2 {
    private static final float SCALE = 13.333f / 2500;
    private GeobotGame game;
    private Cave2Resources caveResources;

    public Cave2(GeobotGame game) {
        this.game = game;
        caveResources = game.loadResources(Cave2Resources.class);
        new Environment(game);
        game.setScale(1);
        game.resizeWorld(13.333f, 7.5f);
    }

    private class Environment extends GameObject {
        private Body body;

        public Environment(Game game) {
            super(game);
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyType.STATIC;
            body = getWorld().createBody(bodyDef);
            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.filter.categoryBits = 0xFFF;
            fixtureDef.filter.maskBits = 0xFFF;
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
            float alpha = -0.8f + Math.abs(game.getRobot().getPosition().x - 600 * SCALE) / 1.5f;
            alpha = Math.max(0f, Math.min(1f, alpha));
            patch.draw(graphics, 494, 1406 - 904 - 500, 278, 500, alpha);
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
    }
}
