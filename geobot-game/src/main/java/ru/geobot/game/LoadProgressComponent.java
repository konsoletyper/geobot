package ru.geobot.game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.JComponent;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class LoadProgressComponent extends JComponent {
    private static final long serialVersionUID = -8998272086113068186L;
    private BufferedImage startScreen;
    private BufferedImage smallStartScreen;

    public LoadProgressComponent() {
        try (InputStream input = LoadProgressComponent.class.getResourceAsStream("main-screen.png")) {
            startScreen = ImageIO.read(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (InputStream input = LoadProgressComponent.class.getResourceAsStream("main-screen-small.png")) {
            smallStartScreen = ImageIO.read(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D gfx = (Graphics2D)g;
        gfx.setColor(Color.black);
        gfx.fillRect(0, 0, getWidth(), getHeight());
        BufferedImage image = getWidth() > 540 ? startScreen : smallStartScreen;
        AffineTransform transform = new AffineTransform();
        int width = image.getWidth() * getHeight() / image.getHeight();
        transform.translate((getWidth() - width) / 2, 0);
        transform.scale(getHeight() / (double)image.getHeight(), getHeight() / (double)image.getHeight());
        gfx.drawRenderedImage(image, transform);
    }
}
