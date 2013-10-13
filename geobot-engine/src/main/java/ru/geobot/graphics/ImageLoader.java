package ru.geobot.graphics;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import javax.imageio.ImageIO;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class ImageLoader {
    private static ConcurrentMap<Class<?>, CachedEntry> cache = new ConcurrentHashMap<>();

    private static class CachedEntry {
        public final CountDownLatch latch = new CountDownLatch(1);
        public volatile Object value;
    }

    public static <T> T load(Class<T> type) {
        CachedEntry entry = cache.get(type);
        if (entry == null) {
            entry = new CachedEntry();
            if (cache.putIfAbsent(type, entry) == null) {
                entry.value = create(type);
                entry.latch.countDown();
            } else {
                entry = cache.get(type);
                try {
                    entry.latch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        return type.cast(entry.value);
    }

    private static Object create(Class<?> type) {
        final Map<Method, Image> imageMap = new HashMap<>();
        for (Method method : type.getMethods()) {
            if (method.getParameterTypes().length > 0) {
                throw new IllegalArgumentException("Method " +
                        type.getName() + "." + method.getName() + " has " +
                        "arguments");
            }
            if (!method.getReturnType().equals(Image.class)) {
                throw new IllegalArgumentException("Method " +
                        type.getName() + "." + method.getName() + " does not return a " +
                        Image.class.getName());
            }
            ImagePath path = method.getAnnotation(ImagePath.class);
            if (path == null) {
                throw new IllegalArgumentException("Method " +
                        type.getName() + "." + method.getName() + " does not have " +
                        ImagePath.class.getName() + " annotation");
            }
            if (method.isAnnotationPresent(Large.class)) {
                imageMap.put(method, createLargeImage(type, path.value()));
            } else {
                imageMap.put(method, createImage(type, path.value()));
            }
        }
        return Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type },
                new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return imageMap.get(method);
            }
        });
    }

    private static ImageImpl createImage(Class<?> cls, String path) {
        BufferedImage image;
        try (InputStream input = cls.getResourceAsStream(path)) {
            if (path.endsWith(".svg")) {
                image = createVectorImage(input);
            } else {
                image = createRasterImage(input);
            }
        } catch (IOException | TranscoderException e) {
            throw new RuntimeException(e);
        }
        int factor = 2;
        List<BufferedImage> images = new ArrayList<>();
        images.add(image);
        BufferedImage scaledImage = image;
        while (true) {
            int w = image.getWidth() / factor;
            int h = image.getHeight() / factor;
            if (w == 0 || h == 0) {
                break;
            }
            BufferedImage nextImage = new BufferedImage(w, h, scaledImage.getType());
            Graphics2D graphics = nextImage.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.drawRenderedImage(scaledImage, AffineTransform.getScaleInstance(
                    (float)w / scaledImage.getWidth(), (float)h / scaledImage.getHeight()));
            graphics.dispose();
            images.add(nextImage);
            scaledImage = nextImage;
            factor *= 2;
        }
        return new ImageImpl(images.toArray(new BufferedImage[images.size()]));
    }

    private static LargeImageImpl createLargeImage(Class<?> cls, String path) {
        BufferedImage image;
        try (InputStream input = cls.getResourceAsStream(path)) {
            if (path.endsWith(".svg")) {
                image = createVectorImage(input);
            } else {
                image = createRasterImage(input);
            }
        } catch (IOException | TranscoderException e) {
            throw new RuntimeException(e);
        }
        int factor = 2;
        List<LargeImageScale> images = new ArrayList<>();
        images.add(splitImage(image));
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage scaledImage = image;
        while (true) {
            int w = image.getWidth() / factor;
            int h = image.getHeight() / factor;
            if (w == 0 || h == 0) {
                break;
            }
            BufferedImage nextImage = new BufferedImage(w, h, scaledImage.getType());
            Graphics2D graphics = nextImage.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.drawRenderedImage(scaledImage, AffineTransform.getScaleInstance(
                    (float)w / scaledImage.getWidth(), (float)h / scaledImage.getHeight()));
            graphics.dispose();
            images.add(splitImage(nextImage));
            scaledImage = nextImage;
            factor *= 2;
        }
        return new LargeImageImpl(images.toArray(new LargeImageScale[images.size()]),
                width, height);
    }

    private static LargeImageScale splitImage(BufferedImage image) {
        int tileWidth = 256;
        int tileHeight = 256;
        int rows = ((image.getHeight() - 1) / tileHeight) + 1;
        int cols = ((image.getWidth() - 1) / tileWidth) + 1;
        BufferedImage[] array = new BufferedImage[rows * cols];
        int index = 0;
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                int w = Math.min(tileWidth + 1, image.getWidth() - j * tileWidth);
                int h = Math.min(tileHeight + 1, image.getHeight() - i * tileHeight);
                BufferedImage tile = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = tile.createGraphics();
                graphics.drawRenderedImage(image, AffineTransform.getTranslateInstance(
                        -j * tileWidth, -i * tileHeight));
                graphics.dispose();
                array[index++] = tile;
            }
        }
        return new LargeImageScale(image.getWidth(), image.getHeight(), tileWidth, tileHeight,
                array, cols);
    }

    private static BufferedImage createVectorImage(InputStream input) throws TranscoderException {
        TranscoderInput ti = new TranscoderInput(input);
        BufferedTranscoder transcoder = new BufferedTranscoder();
        transcoder.transcode(ti, null);
        return transcoder.image;
    }

    private static BufferedImage createRasterImage(InputStream input) throws IOException {
        return ImageIO.read(input);
    }

    private static class BufferedTranscoder extends ImageTranscoder {
        public BufferedImage image;

        @Override
        public BufferedImage createImage(int width, int height) {
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            return image;
        }

        @Override
        public void writeImage(BufferedImage bi, TranscoderOutput to) throws TranscoderException {
        }
    }
}
