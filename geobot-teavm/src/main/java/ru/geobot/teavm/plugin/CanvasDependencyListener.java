package ru.geobot.teavm.plugin;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import javax.imageio.ImageIO;
import org.apache.commons.lang3.StringUtils;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.teavm.dependency.*;
import org.teavm.dom.html.HTMLImageElement;
import org.teavm.jso.JS;
import org.teavm.jso.JSObject;
import org.teavm.model.*;
import org.teavm.model.instructions.*;
import ru.geobot.ResourceSet;
import ru.geobot.resources.Image;
import ru.geobot.resources.PolygonalBodyFactory;
import ru.geobot.resources.ResourcePath;
import ru.geobot.teavm.CanvasImage;
import ru.geobot.teavm.CanvasResourceLoader;
import ru.geobot.teavm.HtmlPolygonalBodyFactory;
import ru.geobot.teavm.js.CanvasGlobal;
import ru.geobot.util.GeometryUtils;
import ru.geobot.util.Vertex;

/**
 *
 * @author Alexey Andreev
 */
public class CanvasDependencyListener implements DependencyListener {
    private static final MethodReference LOAD_RESOURCE_METHOD = new MethodReference(
            CanvasResourceLoader.class, "getResourceSet", Class.class, Object.class);
    private static final MethodReference ADD_RES_METHOD = new MethodReference(CanvasResourceLoader.class,
            "addResource", Class.class, Object.class, void.class);
    private static final MethodReference ADD_IMAGE_METHOD = new MethodReference(CanvasResourceLoader.class,
            "addImage", HTMLImageElement.class, String.class, void.class);
    private DependencyNode allClassesNode;
    private int lastPictureIndex;
    private Map<String, String> proxyMap = new HashMap<>();
    private List<CanvasImageProcessing> imageProcessingList = new ArrayList<>();

    public Map<String, String> getProxyMap() {
        return proxyMap;
    }

    List<CanvasImageProcessing> getImageProcessingList() {
        return imageProcessingList;
    }

    @Override
    public void started(DependencyAgent agent) {
        allClassesNode = agent.createNode();
    }

    @Override
    public void classAchieved(DependencyAgent agent, String className) {
        ClassReader cls = agent.getClassSource().get(className);
        if (cls != null) {
            AnnotationReader annot = cls.getAnnotations().get(ResourceSet.class.getName());
            if (annot != null) {
                ClassHolder proxyCls = generateProxy(agent, cls);
                allClassesNode.propagate(agent.getType(proxyCls.getName()));
                agent.linkMethod(proxyCls.getMethod(new MethodDescriptor("<init>", ValueType.VOID)).getReference(),
                        DependencyStack.ROOT).use();
                proxyMap.put(className, proxyCls.getName());
                agent.linkMethod(ADD_IMAGE_METHOD, DependencyStack.ROOT);
            }
        }
    }

    @Override
    public void methodAchieved(DependencyAgent agent, MethodDependency method) {
        if (method.getMethod().getReference().equals(LOAD_RESOURCE_METHOD)) {
            allClassesNode.connect(method.getResult());
            agent.linkMethod(ADD_RES_METHOD, method.getStack()).use();
        }
    }

    @Override
    public void fieldAchieved(DependencyAgent agent, FieldDependency field) {
    }

    private ClassHolder generateProxy(DependencyAgent agent, ClassReader cls) {
        ClassHolder proxy = new ClassHolder(agent.generateClassName());
        proxy.setLevel(AccessLevel.PUBLIC);
        proxy.getInterfaces().add(cls.getName());
        Map<MethodDescriptor, FieldHolder> fieldMap = new HashMap<>();

        int fieldNameSuffix = 0;
        for (MethodReader method : cls.getMethods()) {
            FieldHolder proxyField = new FieldHolder("fld_" + fieldNameSuffix++);
            proxyField.setType(method.getResultType());
            proxyField.setLevel(AccessLevel.PRIVATE);
            proxy.addField(proxyField);
            fieldMap.put(method.getDescriptor(), proxyField);
        }
        createProxyConstructor(agent, cls, proxy, fieldMap);
        for (MethodReader method : cls.getMethods()) {
            FieldHolder field =  fieldMap.get(method.getDescriptor());
            MethodHolder proxyMethod = new MethodHolder(method.getDescriptor());
            proxyMethod.setLevel(AccessLevel.PUBLIC);
            proxy.addMethod(proxyMethod);
            Program program = new Program();
            Variable insntance = program.createVariable();
            Variable tmp = program.createVariable();
            BasicBlock block = program.createBasicBlock();
            List<Instruction> insnList = block.getInstructions();
            GetFieldInstruction getFieldInsn = new GetFieldInstruction();
            getFieldInsn.setField(field.getReference());
            getFieldInsn.setInstance(insntance);
            getFieldInsn.setReceiver(tmp);
            getFieldInsn.setFieldType(field.getType());
            insnList.add(getFieldInsn);
            ExitInstruction exitInsn = new ExitInstruction();
            exitInsn.setValueToReturn(tmp);
            insnList.add(exitInsn);
            proxyMethod.setProgram(program);
        }

        agent.submitClass(proxy);
        return proxy;
    }

    private MethodHolder createProxyConstructor(DependencyAgent agent, ClassReader cls, ClassHolder proxy,
            Map<MethodDescriptor, FieldHolder> fieldMap) {
        MethodHolder cons = new MethodHolder(new MethodDescriptor("<init>", ValueType.VOID));
        cons.setLevel(AccessLevel.PUBLIC);
        proxy.addMethod(cons);
        Program program = new Program();
        BasicBlock block = program.createBasicBlock();
        List<Instruction> insnList = block.getInstructions();
        Variable instanceVar = program.createVariable();

        Variable globalVar = program.createVariable();
        InvokeInstruction getGlobalInsn = new InvokeInstruction();
        getGlobalInsn.setType(InvocationType.SPECIAL);
        getGlobalInsn.setMethod(new MethodReference(JS.class.getName(), "getGlobal",
                ValueType.object(JSObject.class.getName())));
        getGlobalInsn.setReceiver(globalVar);
        insnList.add(getGlobalInsn);
        CastInstruction castGlobalInsn = new CastInstruction();
        castGlobalInsn.setValue(globalVar);
        globalVar = program.createVariable();
        castGlobalInsn.setReceiver(globalVar);
        castGlobalInsn.setTargetType(ValueType.object(CanvasGlobal.class.getName()));
        insnList.add(castGlobalInsn);

        for (MethodReader method : cls.getMethods()) {
            ValueType type = method.getResultType();
            Variable fieldVar = program.createVariable();
            if (type.isObject(Image.class.getName())) {
                createImageProxy(agent, method, block, fieldVar, globalVar);
            } else if (type.isObject(PolygonalBodyFactory.class.getName())) {
                createShapeProxy(agent, method, block, fieldVar);
            } else {
                NullConstantInstruction nullInsn = new NullConstantInstruction();
                nullInsn.setReceiver(fieldVar);
                insnList.add(nullInsn);
            }

            FieldHolder field = fieldMap.get(method.getDescriptor());
            PutFieldInstruction storeInsn = new PutFieldInstruction();
            storeInsn.setField(field.getReference());
            storeInsn.setValue(fieldVar);
            storeInsn.setInstance(instanceVar);
            insnList.add(storeInsn);
        }
        insnList.add(new ExitInstruction());
        cons.setProgram(program);
        return cons;
    }

    private void createImageProxy(DependencyAgent agent, MethodReader method, BasicBlock block, Variable fieldVar,
            Variable globalVar) {
        Program program = block.getProgram();
        String path = getImagePath(method);
        BufferedImage image = readImage(agent, method, path);
        int widthLog = 32 - Integer.numberOfLeadingZeros(image.getWidth());
        int heightLog = 32 - Integer.numberOfLeadingZeros(image.getHeight());
        int arraySize = Math.min(widthLog, heightLog);
        List<Instruction> insnList = block.getInstructions();
        Variable arraySizeVar = program.createVariable();
        Variable arrayVar = program.createVariable();
        Variable arrayDataVar = program.createVariable();
        insnList.add(intConstant(arraySizeVar, arraySize));
        insnList.add(createArray(arrayVar, ValueType.object(HTMLImageElement.class.getName()),
                arraySizeVar));
        insnList.add(unwrapArray(arrayDataVar, ArrayElementType.OBJECT, arrayVar));
        CanvasImageProcessing processing = new CanvasImageProcessing();
        processing.levels = arraySize;
        processing.firstSuffix = lastPictureIndex;
        processing.path = path;
        imageProcessingList.add(processing);
        for (int i = 0; i < arraySize; ++i) {
            Variable arrayIndexVar = program.createVariable();
            Variable imageVar = program.createVariable();
            Variable imagePathVar = program.createVariable();
            IntegerConstantInstruction arrayIndexInsn = new IntegerConstantInstruction();
            arrayIndexInsn.setConstant(i);
            arrayIndexInsn.setReceiver(arrayIndexVar);
            insnList.add(arrayIndexInsn);
            StringConstantInstruction imagePathInsn = new StringConstantInstruction();
            imagePathInsn.setConstant("res/" + lastPictureIndex++ + ".png");
            imagePathInsn.setReceiver(imagePathVar);
            insnList.add(imagePathInsn);
            InvokeInstruction createImageInsn = new InvokeInstruction();
            createImageInsn.setInstance(globalVar);
            createImageInsn.setType(InvocationType.VIRTUAL);
            createImageInsn.setMethod(new MethodReference(CanvasGlobal.class, "newImage", HTMLImageElement.class));
            createImageInsn.setReceiver(imageVar);
            insnList.add(createImageInsn);
            InvokeInstruction addImageInsn = new InvokeInstruction();
            addImageInsn.setMethod(ADD_IMAGE_METHOD);
            addImageInsn.setType(InvocationType.SPECIAL);
            addImageInsn.getArguments().add(imageVar);
            addImageInsn.getArguments().add(imagePathVar);
            insnList.add(addImageInsn);
            PutElementInstruction putImageInsn = new PutElementInstruction();
            putImageInsn.setArray(arrayDataVar);
            putImageInsn.setIndex(arrayIndexVar);
            putImageInsn.setValue(imageVar);
            insnList.add(putImageInsn);
        }

        Variable widthVar = program.createVariable();
        Variable heightVar = program.createVariable();
        insnList.add(intConstant(widthVar, image.getWidth()));
        insnList.add(intConstant(heightVar, image.getHeight()));
        ConstructInstruction createCanvasImageInsn = new ConstructInstruction();
        createCanvasImageInsn.setReceiver(fieldVar);
        createCanvasImageInsn.setType(CanvasImage.class.getName());
        insnList.add(createCanvasImageInsn);
        InvokeInstruction initCanvasImageInsn = new InvokeInstruction();
        initCanvasImageInsn.setInstance(fieldVar);
        initCanvasImageInsn.setType(InvocationType.SPECIAL);
        initCanvasImageInsn.setMethod(new MethodReference(CanvasImage.class, "<init>",
                HTMLImageElement[].class, int.class, int.class, void.class));
        initCanvasImageInsn.getArguments().add(arrayVar);
        initCanvasImageInsn.getArguments().add(widthVar);
        initCanvasImageInsn.getArguments().add(heightVar);
        insnList.add(initCanvasImageInsn);
    }

    private void createShapeProxy(DependencyAgent agent, MethodReader method, BasicBlock block, Variable fieldVar) {
        String path = getImagePath(method);
        List<PolygonShape> shapes;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                agent.getClassLoader().getResourceAsStream(path)))) {
            shapes = getShapes(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Program program = block.getProgram();
        List<Instruction> insnList = block.getInstructions();
        Variable shapeCountVar = program.createVariable();
        Variable shapeArrayVar = program.createVariable();
        Variable shapeArrayDataVar = program.createVariable();
        insnList.add(intConstant(shapeCountVar, shapes.size()));
        insnList.add(createArray(shapeArrayVar, ValueType.arrayOf(ValueType.FLOAT), shapeCountVar));
        insnList.add(unwrapArray(shapeArrayDataVar, ArrayElementType.OBJECT, shapeArrayVar));
        for (int i = 0; i < shapes.size(); ++i) {
            PolygonShape shape = shapes.get(i);
            Variable shapeIndexVar = program.createVariable();
            Variable coordCountVar = program.createVariable();
            Variable coordArrayVar = program.createVariable();
            Variable coordArrayDataVar = program.createVariable();
            insnList.add(intConstant(coordCountVar, shape.getVertexCount() * 2));
            insnList.add(createArray(coordArrayVar, ValueType.FLOAT, coordCountVar));
            insnList.add(unwrapArray(coordArrayDataVar, ArrayElementType.FLOAT, coordArrayVar));
            for (int j = 0; j < shape.getVertexCount(); ++j) {
                Vec2 pt = shape.getVertex(j);
                Variable coordIndexVar = program.createVariable();
                Variable coordVar = program.createVariable();
                insnList.add(intConstant(coordIndexVar, j * 2));
                insnList.add(floatConstant(coordVar, pt.x));
                insnList.add(putArray(coordArrayDataVar, coordIndexVar, coordVar));
                coordIndexVar = program.createVariable();
                coordVar = program.createVariable();
                insnList.add(intConstant(coordIndexVar, j * 2 + 1));
                insnList.add(floatConstant(coordVar, pt.y));
                insnList.add(putArray(coordArrayDataVar, coordIndexVar, coordVar));
            }
            insnList.add(intConstant(shapeIndexVar, i));
            insnList.add(putArray(shapeArrayDataVar, shapeIndexVar, coordArrayVar));
        }
        ConstructInstruction constructInsn = new ConstructInstruction();
        constructInsn.setType(HtmlPolygonalBodyFactory.class.getName());
        constructInsn.setReceiver(fieldVar);
        insnList.add(constructInsn);
        InvokeInstruction initInsn = new InvokeInstruction();
        initInsn.setInstance(fieldVar);
        initInsn.setMethod(new MethodReference(HtmlPolygonalBodyFactory.class.getName(), "<init>",
                ValueType.arrayOf(ValueType.arrayOf(ValueType.FLOAT)), ValueType.VOID));
        initInsn.setType(InvocationType.SPECIAL);
        initInsn.getArguments().add(shapeArrayVar);
        insnList.add(initInsn);
    }

    private IntegerConstantInstruction intConstant(Variable receiver, int constant) {
        IntegerConstantInstruction insn = new IntegerConstantInstruction();
        insn.setConstant(constant);
        insn.setReceiver(receiver);
        return insn;
    }

    private FloatConstantInstruction floatConstant(Variable receiver, float constant) {
        FloatConstantInstruction insn = new FloatConstantInstruction();
        insn.setConstant(constant);
        insn.setReceiver(receiver);
        return insn;
    }

    private ConstructArrayInstruction createArray(Variable receiver, ValueType elementType, Variable size) {
        ConstructArrayInstruction insn = new ConstructArrayInstruction();
        insn.setItemType(elementType);
        insn.setReceiver(receiver);
        insn.setSize(size);
        return insn;
    }

    private UnwrapArrayInstruction unwrapArray(Variable receiver, ArrayElementType type, Variable array) {
        UnwrapArrayInstruction insn = new UnwrapArrayInstruction(type);
        insn.setArray(array);
        insn.setReceiver(receiver);
        return insn;
    }

    private PutElementInstruction putArray(Variable array, Variable index, Variable data) {
        PutElementInstruction insn = new PutElementInstruction();
        insn.setArray(array);
        insn.setIndex(index);
        insn.setValue(data);
        return insn;
    }

    private List<PolygonShape> getShapes(BufferedReader reader) throws IOException {
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
                List<List<Vertex>> triangles = GeometryUtils.triangulate(polygon);
                for (List<Vertex> poly : triangles) {
                    PolygonShape shapePrototype = new PolygonShape();
                    Vec2[] vertices = new Vec2[poly.size()];
                    if (GeometryUtils.getOrientation(poly) < 0) {
                        Collections.reverse(poly);
                    }
                    for (int i = 0; i < vertices.length; ++i) {
                        vertices[i] = new Vec2(poly.get(i).x, poly.get(i).y);
                    }
                    shapePrototype.set(vertices, vertices.length);
                    shapes.add(shapePrototype);
                }
            }
        }
        return shapes;
    }

    private String getImagePath(MethodReader method) {
        AnnotationReader annot = method.getAnnotations().get(ResourcePath.class.getName());
        String path = annot.getValue("value").getString();
        String packageName = method.getOwnerName().substring(0, method.getOwnerName().lastIndexOf('.') + 1);
        path = packageName.replace('.', '/') + path;
        return path;
    }

    private BufferedImage readImage(DependencyAgent agent, MethodReader method, String path) {
        try (InputStream input = agent.getClassLoader().getResourceAsStream(path)) {
            return ImageIO.read(input);
        } catch (IOException e) {
            throw new RuntimeException("IO error occured while reading image " + path + " in order to " +
                    "generate proxy for " + method.getReference(), e);
        }
    }
}
