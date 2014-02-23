package ru.geobot;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
abstract class ClassPathScanner {
    public void scan() throws IOException {
        for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
            scanPath(entry);
        }
        Enumeration<URL> manifests = ClassPathScanner.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
        while (manifests.hasMoreElements()) {
            String uri = manifests.nextElement().toString();
            if (uri.startsWith("jar:")) {
                int index = uri.lastIndexOf('!');
                uri = uri.substring("jar:".length(), index);
            } else {
                uri = uri.substring(0, uri.length() - "META-INF/MANIFEST.MF".length());
            }
            if (!uri.startsWith("file:")) {
                continue;
            }
            uri = uri.substring("file:".length());
            System.out.println(uri);
            String entry = new File(uri).getPath();
            scanPath(entry);
        }
    }

    private void scanPath(String path) throws IOException {
        if (path.endsWith(".jar")) {
            scanJar(path);
        } else {
            scanDirectory(path);
        }
    }

    private void scanDirectory(String path) throws IOException {
        for (File file : new File(path).listFiles()) {
            if (file.isDirectory()) {
                scanDirectory(file.getPath());
            } else if (file.getName().endsWith(".class")) {
                try (FileInputStream input = new FileInputStream(file)) {
                    ClassReader classReader = new ClassReader(input);
                    classReader.accept(getVisitor(), 0);
                }
            }
        }
    }

    private void scanJar(String path) throws IOException {
        try (JarInputStream input = new JarInputStream(new FileInputStream(path))) {
            ZipEntry entry = input.getNextEntry();
            if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                ClassReader classReader = new ClassReader(input);
                classReader.accept(getVisitor(), 0);
            }
            input.closeEntry();
        }
    }

    protected abstract ClassVisitor getVisitor();
}
