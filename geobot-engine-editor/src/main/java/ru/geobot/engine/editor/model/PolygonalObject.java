package ru.geobot.engine.editor.model;

import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class PolygonalObject {
    private RenderedImage backgroundImage;
    private List<Polygon> polygons = new ArrayList<>();

    public RenderedImage getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(RenderedImage backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    public List<Polygon> getPolygons() {
        return polygons;
    }
}
