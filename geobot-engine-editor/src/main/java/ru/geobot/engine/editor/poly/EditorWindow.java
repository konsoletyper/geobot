package ru.geobot.engine.editor.poly;

import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public class EditorWindow extends JFrame {
    private static final long serialVersionUID = -8196161427466717351L;
    private PolyEditorComponent editor = new PolyEditorComponent();

    public EditorWindow() {
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
        getContentPane().add(new JScrollPane(editor));
    }

    public static void main(String[] args) throws Exception {
        final EditorWindow window = new EditorWindow();
        try (InputStream input = EditorWindow.class.getClassLoader().getResourceAsStream("cave.png")) {
            window.editor.setBackgroundImage(ImageIO.read(input));
        }
        Polygon polygon = new Polygon();
        polygon.setClosed(true);
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
        window.editor.setPolygon(polygon);
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
