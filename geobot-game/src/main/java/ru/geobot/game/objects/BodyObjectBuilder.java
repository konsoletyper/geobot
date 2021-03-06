package ru.geobot.game.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import ru.geobot.Game;
import ru.geobot.resources.Image;
import ru.geobot.resources.PolygonalBodyFactory;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class BodyObjectBuilder {
    private Game game;
    private BodyDef bodyDef = new BodyDef();
    private FixtureDef fixtureDef = new FixtureDef();
    private Image image;
    private PolygonalBodyFactory shape;
    private PolygonalBodyFactory selectionShape;
    private float realHeight;

    public BodyObjectBuilder(Game game) {
        this.game = game;
    }

    public BodyDef getBodyDef() {
        return bodyDef;
    }

    public FixtureDef getFixtureDef() {
        return fixtureDef;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public void setShape(PolygonalBodyFactory shape) {
        this.shape = shape;
    }

    public void setRealHeight(float realHeight) {
        this.realHeight = realHeight;
    }

    public void setSelectionShape(PolygonalBodyFactory selectionShape) {
        this.selectionShape = selectionShape;
    }

    public BodyObject build() {
        BodyObject object = new BodyObject(game);
        object.scale = realHeight / image.getHeight();
        object.image = image;
        object.body = game.getWorld().createBody(bodyDef);
        for (PolygonShape part : shape.create(object.scale)) {
            fixtureDef.shape = part;
            object.body.createFixture(fixtureDef);
        }
        if (selectionShape != null) {
            List<Shape> shapes = new ArrayList<>();
            for (Shape shape : selectionShape.create(object.scale)) {
                shapes.add(shape);
            }
            object.selectionShapes = shapes;
        } else {
            object.selectionShapes = createSelectionShapes(object.body, 0.12f);
        }
        return object;
    }

    private List<Shape> createSelectionShapes(Body body, float distance) {
        List<Shape> shapes = new ArrayList<>();
        for (Fixture fixture = body.getFixtureList(); fixture != null; fixture = fixture.getNext()) {
            Shape shape = fixture.getShape();
            if (shape instanceof PolygonShape) {
                PolygonShape polygon = (PolygonShape)shape;
                Vec2[] points = Arrays.copyOf(polygon.getVertices(), polygon.getVertexCount());
                Vec2[] expandedPoints = expandPolygon(points, distance);
                for (int i = 2; i < expandedPoints.length; ++i) {
                    PolygonShape expandedPolygon = new PolygonShape();
                    Vec2[] expandedTriangle = new Vec2[] { expandedPoints[0], expandedPoints[i - 1],
                           expandedPoints[i] };
                    expandedPolygon.set(expandedTriangle, expandedTriangle.length);
                    shapes.add(expandedPolygon);
                }
            } else if (shape instanceof CircleShape) {
                CircleShape circle = new CircleShape();
                CircleShape expandedCircle = new CircleShape();
                expandedCircle.m_p.set(circle.m_p);
                expandedCircle.m_radius = circle.m_radius + distance;
                shapes.add(expandedCircle);
            }
        }
        return shapes;
    }

    private Vec2[] expandPolygon(Vec2[] polygon, float distance) {
        Vec2[] result = new Vec2[polygon.length * 3];
        for (int i = 0; i < polygon.length; ++i) {
            int nextIndex = (i + 1) % polygon.length;
            int previousIndex = i == 0 ? polygon.length - 1 : i - 1;
            Vec2 curr = polygon[i];
            Vec2 next = polygon[nextIndex];
            Vec2 prev = polygon[previousIndex];
            Vec2 dir = next.sub(curr);
            dir.normalize();
            Vec2 orthDir = new Vec2(dir.y, -dir.x).mul(distance);
            result[i * 3 + 1] = curr.add(orthDir);
            result[i * 3 + 2] = next.add(orthDir);
            Vec2 dir2 = prev.sub(curr);
            dir2.normalize();
            Vec2 bissect = dir2.add(dir);
            bissect.normalize();
            result[i * 3] = curr.sub(bissect.mul(distance));
        }
        return result;
    }
}
