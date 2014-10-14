package ru.geobot.teavm;

import java.util.*;
import org.teavm.dom.events.Event;
import org.teavm.dom.events.EventListener;
import org.teavm.dom.html.HTMLImageElement;
import ru.geobot.resources.ResourceReader;

/**
 *
 * @author Alexey Andreev
 */
public class CanvasResourceLoader implements ResourceReader {
    private static ResourceReader instance;
    private static Map<Class<?>, Object> resourceCache;
    private static List<Runnable> completeHandlers = new ArrayList<>();
    private static List<ProgressListener> progressListeners = new ArrayList<>();
    private static int imagesToAwait;
    private static Queue<ImageLoadTask> imageQueue = new ArrayDeque<>();
    private static int imagesLoaded = 0;
    private static final int MAX_THREAD_COUNT = 8;
    private static boolean completeInvoked;

    private static class ImageLoadTask {
        HTMLImageElement image;
        String src;
    }

    private CanvasResourceLoader() {
    }

    public static ResourceReader getInstance() {
        if (instance == null) {
            instance = new CanvasResourceLoader();
        }
        return instance;
    }

    @Override
    public <T> T getResourceSet(Class<T> resourceSetType) {
        ensureCache();
        return resourceSetType.cast(resourceCache.get(resourceSetType));
    }

    public static void init() {
        ensureCache();
    }

    public static void startLoadingImages() {
        for (int i = 0; i < MAX_THREAD_COUNT; ++i) {
            ImageLoadTask task = imageQueue.poll();
            if (task == null) {
                break;
            }
            loadImage(task);
        }
    }

    private static void loadImage(ImageLoadTask task) {
        task.image.addEventListener("load", new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                imagesLoaded++;
                for (ProgressListener listener : progressListeners) {
                    listener.progressChanged(imagesLoaded, imagesToAwait);
                }
                ImageLoadTask task = imageQueue.poll();
                if (task != null) {
                    loadImage(task);
                } else {
                    if (!completeInvoked) {
                        completeInvoked = true;
                        for (Runnable completeHandler : completeHandlers) {
                            completeHandler.run();
                        }
                    }
                }
            }
        }, false);
        task.image.setSrc(task.src);
    }

    private static void ensureCache() {
        if (resourceCache == null) {
            resourceCache = new HashMap<>();
            fillCache();
        }
    }

    private static native void fillCache();

    private static void addResource(Class<?> type, Object instance) {
        resourceCache.put(type, instance);
    }

    private static void addImage(final HTMLImageElement image, String src) {
        ImageLoadTask task = new ImageLoadTask();
        task.image = image;
        task.src = src;
        imageQueue.add(task);
        imagesToAwait++;
    }

    public static void addCompleteHandler(Runnable handler) {
        completeHandlers.add(handler);
    }

    public static void addProgressListener(ProgressListener progressListener) {
        progressListeners.add(progressListener);
    }
}
