package ru.geobot.teavm.plugin;

import org.teavm.model.MethodReference;
import org.teavm.model.ValueType;
import org.teavm.vm.spi.TeaVMHost;
import org.teavm.vm.spi.TeaVMPlugin;
import ru.geobot.teavm.CanvasResourceLoader;

/**
 *
 * @author Alexey Andreev
 */
public class CanvasPlugin implements TeaVMPlugin {
    @Override
    public void install(TeaVMHost host) {
        CanvasDependencyListener dependencyListener = new CanvasDependencyListener();
        host.add(dependencyListener);
        MethodReference fillCacheMethod = new MethodReference(CanvasResourceLoader.class.getName(), "fillCache",
                ValueType.VOID);
        host.add(fillCacheMethod, new ResourceReaderGenerator(dependencyListener.getProxyMap()));
        host.add(new CanvasImageProcessor(dependencyListener.getImageProcessingList()));
    }
}
