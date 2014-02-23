package ru.geobot.game.caves;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.joints.RevoluteJoint;
import org.jbox2d.dynamics.joints.RevoluteJointDef;
import ru.geobot.GameObjectAdapter;
import ru.geobot.game.GeobotGame;
import ru.geobot.game.objects.BodyObject;
import ru.geobot.game.objects.BodyObjectBuilder;
import ru.geobot.resources.Image;
import ru.geobot.resources.PolygonalBodyFactory;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class StoneExplosion {
    private StoneWallResources resources;
    private Image[] images;
    private PolygonalBodyFactory[] shapes;
    private int[] offsets = { 145, 293, 149, 238, 156, 174, 219, 180, 140, 149, 191, 141, 255, 152, 262, 216,
            125, 69, 206, 109, 80, 76, 232, 81, 256, 74, 12, 21, -1, 3, 51, 2, 204, 36, 293, 35, 212, 262,
            157, 321, 291, 307, 345, 334, 139, 408, 116, 479, 169, 458, 94, 533, 77, 599, 35, 683,
            319, 396, 280, 409, 223, 436, 360, 439, 167, 498, 299, 486, 234, 509, 369, 533, 450, 535, 134, 534,
            265, 554, 340, 548, 406, 575, 207, 604, 139, 641, 471, 576, 233, 669, 329, 604, 287, 623, 396, 539,
            481, 603, 479, 627, 166, 733, 215, 701, 242, 735, 363, 646, 264, 709, 354, 706, 407, 683, 351, 719,
            432, 674, 504, 671, 567, 679 };
    private BodyObject stoneInHand;
    private RevoluteJoint stoneJoint;
    private GeobotGame game;

    public StoneExplosion(GeobotGame game) {
        this.game = game;
        resources = game.loadResources(StoneWallResources.class);
        images = new Image[] { resources.stoneImage1(), resources.stoneImage2(), resources.stoneImage3(),
                resources.stoneImage4(), resources.stoneImage5(), resources.stoneImage6(), resources.stoneImage7(),
                resources.stoneImage8(), resources.stoneImage9(), resources.stoneImage10(),
                resources.stoneImage11(), resources.stoneImage12(), resources.stoneImage13(),
                resources.stoneImage14(), resources.stoneImage15(), resources.stoneImage16(),
                resources.stoneImage17(), resources.stoneImage18(), resources.stoneImage19(),
                resources.stoneImage20(), resources.stoneImage21(), resources.stoneImage22(),
                resources.stoneImage23(), resources.stoneImage24(), resources.stoneImage25(),
                resources.stoneImage26(), resources.stoneImage27(), resources.stoneImage28(),
                resources.stoneImage29(), resources.stoneImage30(), resources.stoneImage31(),
                resources.stoneImage32(), resources.stoneImage33(), resources.stoneImage34(),
                resources.stoneImage35(), resources.stoneImage36(), resources.stoneImage37(),
                resources.stoneImage38(), resources.stoneImage39(), resources.stoneImage40(),
                resources.stoneImage41(), resources.stoneImage42(), resources.stoneImage43(),
                resources.stoneImage44(), resources.stoneImage45(), resources.stoneImage46(),
                resources.stoneImage47(), resources.stoneImage48(), resources.stoneImage49(),
                resources.stoneImage50(), resources.stoneImage51(), resources.stoneImage52(),
                resources.stoneImage53(), resources.stoneImage54(), resources.stoneImage55(),
                resources.stoneImage56(), resources.stoneImage57(), resources.stoneImage58(),
                resources.stoneImage59(), resources.stoneImage60(), resources.stoneImage61() };
        shapes = new PolygonalBodyFactory[] { resources.stoneShape1(), resources.stoneShape2(),
                resources.stoneShape3(), resources.stoneShape4(), resources.stoneShape5(), resources.stoneShape6(),
                resources.stoneShape7(), resources.stoneShape8(), resources.stoneShape9(), resources.stoneShape10(),
                resources.stoneShape11(), resources.stoneShape12(), resources.stoneShape13(),
                resources.stoneShape14(), resources.stoneShape15(), resources.stoneShape16(),
                resources.stoneShape17(), resources.stoneShape18(), resources.stoneShape19(),
                resources.stoneShape20(), resources.stoneShape21(), resources.stoneShape22(),
                resources.stoneShape23(), resources.stoneShape24(), resources.stoneShape25(),
                resources.stoneShape26(), resources.stoneShape27(), resources.stoneShape28(),
                resources.stoneShape29(), resources.stoneShape30(), resources.stoneShape31(),
                resources.stoneShape32(), resources.stoneShape33(), resources.stoneShape34(),
                resources.stoneShape35(), resources.stoneShape36(), resources.stoneShape37(),
                resources.stoneShape38(), resources.stoneShape39(), resources.stoneShape40(),
                resources.stoneShape41(), resources.stoneShape42(), resources.stoneShape43(),
                resources.stoneShape44(), resources.stoneShape45(), resources.stoneShape46(),
                resources.stoneShape47(), resources.stoneShape48(), resources.stoneShape49(),
                resources.stoneShape50(), resources.stoneShape51(), resources.stoneShape52(),
                resources.stoneShape53(), resources.stoneShape54(), resources.stoneShape55(),
                resources.stoneShape56(), resources.stoneShape57(), resources.stoneShape58(),
                resources.stoneShape59(), resources.stoneShape60(), resources.stoneShape61() };
        BodyObjectBuilder stoneBuilder = new BodyObjectBuilder(game);
        stoneBuilder.getFixtureDef().density = 0.3f;
        stoneBuilder.getFixtureDef().restitution = 0.2f;
        stoneBuilder.getFixtureDef().friction = 0.97f;
        for (int i = 0; i < 61; ++i) {
            int offsetX = 1795 + offsets[i * 2];
            int offsetY = (1406 - 437) - offsets[i * 2 + 1];
            stoneBuilder.getFixtureDef().filter.maskBits = offsets[i * 2 + 1] > 700 ? 0x10F0 : 0x1000;
            stoneBuilder.getFixtureDef().filter.categoryBits = stoneBuilder.getFixtureDef().filter.maskBits;
            PolygonShape[] fixtures = shapes[i].create(13.333f / 2500);
            float height = height(fixtures);
            stoneBuilder.getBodyDef().position.x = offsetX * (13.333f / 2500);
            stoneBuilder.getBodyDef().position.y = (offsetY - height) * (13.333f / 2500);
            stoneBuilder.setImage(images[i]);
            stoneBuilder.setRealHeight(images[i].getHeight() * 13.333f / 2500);
            stoneBuilder.setShape(shapes[i]);
            stoneBuilder.getBodyDef().type = BodyType.DYNAMIC;
            final BodyObject stone = stoneBuilder.build();
            stone.addListener(new GameObjectAdapter() {
                @Override public boolean click() {
                    stoneClicked(stone);
                    return true;
                }
            });
        }
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.STATIC;
        bodyDef.position.x = 0;
        bodyDef.position.y = -3f;
        final Body body = game.getWorld().createBody(bodyDef);
        FixtureDef fixtureDef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(15, 0.1f);
        fixtureDef.shape = shape;
        fixtureDef.filter.maskBits = 0x1000;
        fixtureDef.filter.categoryBits = 0x1000;
        body.createFixture(fixtureDef);
    }

    private float height(PolygonShape[] shapes) {
        float height = 0;
        for (PolygonShape shape : shapes) {
            for (Vec2 pt : shape.getVertices()) {
                height = Math.max(height, pt.y);
            }
        }
        return height;
    }

    private void stoneClicked(BodyObject stone) {
        if (stoneInHand == stone) {
            game.getWorld().destroyJoint(stoneJoint);
            stoneJoint = null;
            game.getRobot().setCarriesObject(false);
            stoneInHand = null;
        } else if (!game.getRobot().isCarriesObject()) {
            pickStone(stone);
        }
    }

    private void pickStone(final BodyObject stone) {
        Vec2 worldCenter = stone.getBody().getWorldCenter();
        game.getRobot().pickAt(worldCenter.x, worldCenter.y, new Runnable() {
            @Override public void run() {
                stoneInHand = stone;
                game.getRobot().setCarriesObject(true);
                RevoluteJointDef jointDef = new RevoluteJointDef();
                jointDef.bodyA = game.getRobot().getHand();
                jointDef.localAnchorA.set(game.getRobot().getHandPickPoint());
                jointDef.bodyB = stone.getBody();
                jointDef.localAnchorB.set(stone.getBody().getLocalCenter());
                stoneJoint = (RevoluteJoint)game.getWorld().createJoint(jointDef);
            }
        });
    }
}
