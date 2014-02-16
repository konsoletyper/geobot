package ru.geobot.engine.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.apache.commons.lang3.StringUtils;
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
    private JMenuBar menuBar = new JMenuBar();
    private JMenu fileMenu = new JMenu("Файл");
    private JMenuItem chooseImageMenuItem = new JMenuItem("Выбрать изображение...");
    private JMenuItem openFileMenuItem = new JMenuItem("Открыть...");
    private JMenuItem saveFileMenuItem = new JMenuItem("Сохранить");
    private JMenuItem saveFileAsMenuItem = new JMenuItem("Сохранить как...");
    private JMenuItem exitMenuItem = new JMenuItem("Выход");
    private File currentFile;

    public EditorWindow() {
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
        getContentPane().add(new JScrollPane(editor));
        editor.setScale(2);
        setJMenuBar(menuBar);
        menuBar.add(fileMenu);
        fileMenu.add(openFileMenuItem);
        fileMenu.add(saveFileMenuItem);
        fileMenu.add(saveFileAsMenuItem);
        fileMenu.add(chooseImageMenuItem);
        fileMenu.add(exitMenuItem);
        bindEvents();
        setTitle("Geobot - игровой редактор");
    }

    private void bindEvents() {
        chooseImageMenuItem.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                chooseImage();
            }
        });
        saveFileMenuItem.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                saveFile();
            }
        });
        saveFileAsMenuItem.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                saveFileAs();
            }
        });
        openFileMenuItem.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                openFile();
            }
        });
    }

    private void chooseImage() {
        JFileChooser fileChooser = new JFileChooser();
        int state = fileChooser.showOpenDialog(this);
        if (state == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                editor.setBackgroundImage(ImageIO.read(file));
            } catch (IOException e) {
                System.out.println(e);
                JOptionPane.showMessageDialog(this, "При открытии файла произошла ошибка", "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveFile() {
        if (currentFile == null) {
            saveFileAs();
        } else {
            saveFile(currentFile);
        }
    }

    private void saveFileAs() {
        JFileChooser fileChooser = new JFileChooser();
        int state = fileChooser.showSaveDialog(this);
        if (state == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (saveFile(file)) {
                currentFile = file;
            }
        }
    }

    private boolean saveFile(File file) {
        try (Writer writer = new FileWriter(file)) {
            PolygonalObject obj = editor.getEditObject();
            for (Polygon polygon : obj.getPolygons()) {
                List<Vertex> vertices = polygon.getVertices();
                for (int i = 0; i < vertices.size(); ++i) {
                    if (i > 0) {
                        writer.append(' ');
                    }
                    Vertex vertex = vertices.get(i);
                    writer.append(Integer.toString(vertex.getX())).append(' ')
                            .append(Integer.toString(vertex.getY()));
                }
                writer.append("\n");
            }
        } catch (IOException e) {
            System.out.println(e);
            JOptionPane.showMessageDialog(this, "При сохранении файла произошла ошибка", "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        int state = fileChooser.showSaveDialog(this);
        if (state == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (openFile(file)) {
                currentFile = file;
            }
        }
    }

    private boolean openFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            PolygonalObject obj = new PolygonalObject();
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
                if (parts.length % 2 != 0) {
                    JOptionPane.showMessageDialog(this, "Файл повреждён или имеет неверный формат", "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                Polygon polygon = new Polygon();
                for (int i = 0; i < parts.length; i += 2) {
                    Vertex vertex = new Vertex();
                    vertex.setX(Integer.parseInt(parts[i]));
                    vertex.setY(Integer.parseInt(parts[i + 1]));
                    polygon.getVertices().add(vertex);
                }
                obj.getPolygons().add(polygon);
            }
            obj.setBackgroundImage(editor.getBackgroundImage());
            editor.setEditObject(obj);
        } catch (IOException e) {
            System.out.println(e);
            JOptionPane.showMessageDialog(this, "При открытии файла произошла ошибка", "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    public static void main(String[] args) throws Exception {
        final EditorWindow window = new EditorWindow();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                window.setSize(800, 480);
                window.setLocationRelativeTo(null);
                window.setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
        });
    }
}
