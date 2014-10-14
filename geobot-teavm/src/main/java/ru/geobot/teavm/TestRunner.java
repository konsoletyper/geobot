package ru.geobot.teavm;

import java.util.Properties;
import org.teavm.dependency.DependencyListener;
import org.teavm.javascript.ni.Generator;
import org.teavm.javascript.ni.Injector;
import org.teavm.jso.plugin.JSObjectBuilderPlugin;
import org.teavm.model.*;
import org.teavm.model.util.ListingBuilder;
import org.teavm.parsing.ClasspathClassHolderSource;
import org.teavm.vm.spi.RendererListener;
import org.teavm.vm.spi.TeaVMHost;

/**
 *
 * @author Alexey Andreev
 */
public class TestRunner {
    public static void main(String[] args) {
        ClassLoader classLoader = TestRunner.class.getClassLoader();
        ClasspathClassHolderSource classSource = new ClasspathClassHolderSource(classLoader);
        ClassHolder cls = classSource.get("ru.geobot.teavm.WebStart$2");
        MethodHolder method = cls.getMethod(new MethodDescriptor("handleEvent",
                ValueType.object("org.teavm.dom.events.Event"), ValueType.VOID));
        FakeVM vm = new FakeVM();
        new JSObjectBuilderPlugin().install(vm);
        vm.transformer.transformClass(cls, classSource);
        System.out.println(new ListingBuilder().buildListing(method.getProgram(), ""));
    }

    private static class FakeVM implements TeaVMHost {
        ClassHolderTransformer transformer;

        @Override
        public void add(DependencyListener dependencyListener) {
        }

        @Override
        public void add(ClassHolderTransformer classTransformer) {
            this.transformer = classTransformer;
        }

        @Override
        public void add(MethodReference methodRef, Generator generator) {
        }

        @Override
        public void add(RendererListener listener) {
        }

        @Override
        public ClassLoader getClassLoader() {
            return null;
        }

        @Override
        public Properties getProperties() {
            return null;
        }

        @Override
        public void add(MethodReference methodRef, Injector injector) {
        }

        @Override
        public <T> void registerService(Class<T> type, T instance) {
        }
    }
}
