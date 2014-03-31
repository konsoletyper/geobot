package ru.geobot;

import java.io.IOException;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import ru.geobot.resources.ResourceReader;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class ResourcePreloader {
    private ResourceReader loader;

    public ResourcePreloader(ResourceReader loader) {
        this.loader = loader;
    }

    public void preloadResources() {
        try {
            scanner.scan();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void preload(String className) {
        ClassLoader classLoader = ResourcePreloader.class.getClassLoader();
        try {
            Class<?> cls = Class.forName(className, true, classLoader);
            System.out.println("Preloading resource " + cls.getName());
            loader.getResourceSet(cls);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private ClassPathScanner scanner = new ClassPathScanner() {
        @Override
        protected ClassVisitor getVisitor() {
            return classVisitor;
        }
    };

    private ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM4) {
        private String className;

        @Override
        public void visit(int version, int access, String name, String signature, String superName,
                String[] interfaces) {
            this.className = name;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            if (desc.equals(Type.getDescriptor(ResourceSet.class))) {
                preload(className.replace('/', '.'));
            }
            return null;
        };
    };
}
