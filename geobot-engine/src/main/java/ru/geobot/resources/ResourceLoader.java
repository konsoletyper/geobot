package ru.geobot.resources;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import javax.imageio.ImageIO;
import org.apache.commons.lang3.StringUtils;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import ru.geobot.util.GeometryUtils;
import ru.geobot.util.Vertex;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class ResourceLoader {
    private static ConcurrentMap<Class<?>, CachedEntry> cache = new ConcurrentHashMap<>();

    private static class CachedEntry {
        public volatile CountDownLatch latch = new CountDownLatch(1);
        public volatile Object value;
    }

    public static <T> T load(Class<T> type) {
        CachedEntry entry = cache.get(type);
        if (entry == null) {
            entry = new CachedEntry();
            if (cache.putIfAbsent(type, entry) == null) {
                entry.value = create(type);
                entry.latch.countDown();
                entry.latch = null;
            } else {
                entry = cache.get(type);
                CountDownLatch latch = entry.latch;
                if (latch != null) {
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                }
            }
        }
        return type.cast(entry.value);
    }

    private static Object create(Class<?> type) {
        final Map<Method, Object> resourceMap = new HashMap<>();
        for (Method method : type.getMethods()) {
            if (method.getParameterTypes().length > 0) {
                throw new IllegalArgumentException("Method " + type.getName() + "." + method.getName() + " has " +
                        "non-empty argument list");
            }
            ResourcePath path = method.getAnnotation(ResourcePath.class);
            if (path == null) {
                throw new IllegalArgumentException("Method " + type.getName() + "." + method.getName() +
                        " does not have " + ResourcePath.class.getName() + " annotation");
            }
            if (method.getReturnType().equals(Image.class)) {
                if (method.isAnnotationPresent(Large.class)) {
                    resourceMap.put(method, createLargeImage(type, path.value()));
                } else {
                    resourceMap.put(method, createImage(type, path.value()));
                }
            } else if (method.getReturnType().equals(PolygonalBodyFactory.class)) {
                resourceMap.put(method, createPolygonalBody(type, path.value()));
            } else {
                throw new IllegalArgumentException("Method " +  type.getName() + "." + method.getName() +
                        " returns unexpected type " + method.getReturnType().getName());
            }
        }
        return Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type },
                new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return resourceMap.get(method);
            }
        });
    }

    private static DefaultPolygonalBodyFactory createPolygonalBody(Class<?> cls, String path) {
        if (cls.getResource(path) == null) {
            throw new RuntimeException("Resource not found: " + path);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(cls.getResourceAsStream(path)))) {
            List<PolygonShape> shapes = new ArrayList<>();
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = StringUtils.split(line, ' ');
                List<Vertex> polygon = new ArrayList<>();
                for (int i = 0; i < parts.length; i += 2) {
                    Vertex v = new Vertex(Integer.parseInt(parts[i].trim()), Integer.parseInt(parts[i + 1].trim()));
                    polygon.add(v);
                }
                if (polygon.size() >= 3) {
                    List<Vertex> triangles = GeometryUtils.triangulate(polygon);
                    for (int i = 0; i < triangles.size(); i += 3) {
                        Vertex a = triangles.get(i);
                        Vertex b = triangles.get(i + 1);
                        Vertex c = triangles.get(i + 2);
                        PolygonShape shapePrototype = new PolygonShape();
                        Vec2[] vertices;
                        if (GeometryUtils.getOrientation(Arrays.asList(a, b, c)) > 0) {
                            vertices = new Vec2[] { new Vec2(a.x, a.y), new Vec2(b.x, b.y), new Vec2(c.x, c.y) };
                        } else {
                            vertices = new Vec2[] { new Vec2(c.x, c.y), new Vec2(b.x, b.y), new Vec2(a.x, a.y) };
                        }
                        shapePrototype.set(vertices, vertices.length);
                        shapes.add(shapePrototype);
                    }
                }
            }
            return new DefaultPolygonalBodyFactory(shapes.toArray(new PolygonShape[shapes.size()]));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ImageImpl createImage(Class<?> cls, String path) {
        BufferedImage image;
        try (InputStream input = cls.getResourceAsStream(path)) {
            image = createRasterImage(input);
        } catch (IOException e) {
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
            image = createRasterImage(input);
        } catch (IOException e) {
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
        return new LargeImageScale(image.getWidth(), image.getHeight(), tileWidth, tileHeight, array, cols);
    }

    private static BufferedImage createRasterImage(InputStream input) throws IOException {
        return ImageIO.read(input);
    }
}
