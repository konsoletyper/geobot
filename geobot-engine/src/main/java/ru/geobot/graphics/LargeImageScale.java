package ru.geobot.graphics;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 *
 * @author Alexey Andreev
 */
class LargeImageScale {
    public final int width;
    public final int height;
    public final int tileWidth;
    public final int tileHeight;
    public final BufferedImage[] array;
    public final int columns;
    public final int rows;

    public LargeImageScale(int width, int height, int tileWidth, int tileHeight,
            BufferedImage[] array, int rowSize) {
        this.width = width;
        this.height = height;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.array = array;
        this.columns = rowSize;
        this.rows = array.length / rowSize;
    }

    public void draw(Graphics2D graphics) {
        draw(graphics, 0, 0, rows, columns);
    }

    private void draw(Graphics2D graphics, int firstRow, int firstCol, int lastRow, int lastCol) {
        if (firstRow == lastRow || firstCol == lastCol) {
            return;
        }
        float left = firstCol * tileWidth;
        float top = firstRow * tileHeight;
        float right = lastCol * tileWidth;
        float bottom = lastRow * tileHeight;
        float[] points = { left, top, right, top, right, bottom, left, bottom };
        float[] mappedPoints = new float[points.length];
        graphics.getTransform().transform(points, 0, mappedPoints, 0, 4);
        left = mappedPoints[0];
        right = mappedPoints[0];
        top = mappedPoints[1];
        bottom = mappedPoints[1];
        for (int i = 2; i < mappedPoints.length;) {
            left = Math.min(left, mappedPoints[i]);
            right = Math.max(right, mappedPoints[i++]);
            top = Math.min(top, mappedPoints[i]);
            bottom = Math.max(bottom, mappedPoints[i++]);
        }
        java.awt.Rectangle clipRect = graphics.getClipBounds();
        int rowCount = lastRow - firstRow;
        int columnCount = lastCol - firstCol;
        if (left >= clipRect.x && right < clipRect.x + clipRect.width &&
                top >= clipRect.y && bottom <= clipRect.y + clipRect.height) {
            render(graphics, firstRow, firstCol, lastRow, lastCol);
        } else if (left < clipRect.x + clipRect.width && right > clipRect.x &&
                top < clipRect.y + clipRect.height && bottom > clipRect.y) {
            if (rowCount == 1 && columnCount == 1) {
                render(graphics, firstRow, firstCol, lastRow, lastCol);
            } else {
                int midRow = (firstRow + lastRow) / 2;
                int midCol = (firstCol + lastCol) / 2;
                draw(graphics, firstRow, firstCol, midRow, midCol);
                draw(graphics, firstRow, midCol, midRow, lastCol);
                draw(graphics, midRow, firstCol, lastRow, midCol);
                draw(graphics, midRow, midCol, lastRow, lastCol);
            }
        }
    }

    private void render(Graphics2D graphics, int firstRow, int firstCol, int lastRow, int lastCol) {
        for (int i = firstRow; i < lastRow; ++i) {
            for (int j = firstCol; j < lastCol; ++j) {
                graphics.drawRenderedImage(array[columns * i + j],
                        AffineTransform.getTranslateInstance(j * tileWidth,
                        i * tileHeight));
            }
        }
    }
}
