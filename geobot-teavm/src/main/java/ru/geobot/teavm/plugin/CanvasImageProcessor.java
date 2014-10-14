package ru.geobot.teavm.plugin;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import javax.imageio.ImageIO;
import org.teavm.javascript.RenderingContext;
import org.teavm.model.ClassReader;
import org.teavm.vm.BuildTarget;
import org.teavm.vm.spi.RendererListener;

/**
 *
 * @author Alexey Andreev
 */
public class CanvasImageProcessor implements RendererListener {
    private List<CanvasImageProcessing> imageProcessingList;

    public CanvasImageProcessor(List<CanvasImageProcessing> imageProcessingList) {
        this.imageProcessingList = imageProcessingList;
    }

    @Override
    public void begin(RenderingContext context, BuildTarget buildTarget) throws IOException {
        for (CanvasImageProcessing processing : imageProcessingList) {
            process(buildTarget, processing);
        }
    }

    private void process(BuildTarget buildTarget, CanvasImageProcessing processing) throws IOException {
        BufferedImage image;
        try (InputStream input = CanvasImageProcessor.class.getClassLoader().getResourceAsStream(processing.path)) {
            image = ImageIO.read(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int factor = 2;
        BufferedImage scaledImage = image;
        int suffix = processing.firstSuffix;
        for (int i = 0; i < processing.levels; ++i) {
            try (OutputStream out = buildTarget.createResource("res/" + suffix++ + ".png")) {
                ImageIO.write(scaledImage, "png", out);
            }
            int w = image.getWidth() / factor;
            int h = image.getHeight() / factor;
            if (w == 0 || h == 0) {
                break;
            }
            BufferedImage nextImage = new BufferedImage(w, h, scaledImage.getType());
            Graphics2D graphics = nextImage.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.drawRenderedImage(scaledImage, AffineTransform.getScaleInstance(
                    (float)w / scaledImage.getWidth(), (float)h / scaledImage.getHeight()));
            graphics.dispose();
            scaledImage = nextImage;
            factor *= 2;
        }
    }

    @Override
    public void beforeClass(ClassReader cls) throws IOException {
    }

    @Override
    public void afterClass(ClassReader cls) throws IOException {
    }

    @Override
    public void complete() throws IOException {
    }
}
