package ru.geobot.engine.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
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

    public EditorWindow() {
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
        getContentPane().add(new JScrollPane(editor));
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
