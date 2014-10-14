package ru.geobot.teavm.plugin;

import java.io.IOException;
import java.util.Map;
import org.teavm.codegen.SourceWriter;
import org.teavm.javascript.ni.Generator;
import org.teavm.javascript.ni.GeneratorContext;
import org.teavm.model.MethodReference;
import org.teavm.model.ValueType;
import ru.geobot.teavm.CanvasResourceLoader;

/**
 *
 * @author Alexey Andreev
 */
public class ResourceReaderGenerator implements Generator {
    private Map<String, String> proxyMap;

    public ResourceReaderGenerator(Map<String, String> proxyMap) {
        this.proxyMap = proxyMap;
    }

    @Override
    public void generate(GeneratorContext context, SourceWriter writer, MethodReference methodRef) throws IOException {
        MethodReference addResMethod = new MethodReference(CanvasResourceLoader.class.getName(), "addResource",
                ValueType.object("java.lang.Class"), ValueType.object("java.lang.Object"), ValueType.VOID);
        for (Map.Entry<String, String> entry : proxyMap.entrySet()) {
            MethodReference cons = new MethodReference(entry.getValue(), "<init>", ValueType.VOID);
            writer.appendMethodBody(addResMethod).append("($rt_cls(").appendClass(entry.getKey()).append("), ")
                    .appendClass(entry.getValue()).append('.').appendMethod(cons).append("());").softNewLine();
        }
    }
}
