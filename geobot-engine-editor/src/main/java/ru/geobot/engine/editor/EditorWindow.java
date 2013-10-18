package ru.geobot.engine.editor;

import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import ru.geobot.engine.editor.model.Polygon;
import ru.geobot.engine.editor.model.PolygonalObject;
import ru.geobot.engine.editor.model.Vertex;
import ru.geobot.engine.editor.poly.PolygonalObjectEditor;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class EditorWindow extends JFrame {
    private static final long serialVersionUID = -8196161427466717351L;
    private PolygonalObjectEditor editor = new PolygonalObjectEditor();

    public EditorWindow() {
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
        getContentPane().add(new JScrollPane(editor));
    }

    public static void main(String[] args) throws Exception {
        PolygonalObject object = new PolygonalObject();
        final EditorWindow window = new EditorWindow();
        try (InputStream input = EditorWindow.class.getClassLoader().getResourceAsStream("cave.png")) {
            object.setBackgroundImage(ImageIO.read(input));
        }
        Polygon polygon = new Polygon();
        Vertex v = new Vertex();
        v.setX(20);
        v.setY(20);
        polygon.getVertices().add(v);
        v = new Vertex();
        v.setX(600);
        v.setY(50);
        polygon.getVertices().add(v);
        v = new Vertex();
        v.setX(300);
        v.setY(500);
        polygon.getVertices().add(v);
        object.getPolygons().add(polygon);
        window.editor.setEditObject(object);
        window.editor.setScale(1);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                window.pack();
                window.setLocationRelativeTo(null);
            }
        });
    }
}
