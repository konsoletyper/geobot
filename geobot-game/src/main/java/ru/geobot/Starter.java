package ru.geobot;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.jbox2d.common.Settings;

/**
 *
 * @author Alexey Andreev
 */
public class Starter {
    public static void main(String[] args) {
        Settings.maxPolygonVertices = 20;
        final JFrame frame = new JFrame("Geobot");
        frame.setSize(600, 400);
        frame.setFocusable(true);
        final SwingRunner component = new SwingRunner();
        frame.add(component);
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.PAGE_AXIS));
        frame.setVisible(true);
        GeobotGame game = new GeobotGame();
        component.run(game);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                component.requestFocusInWindow();
                frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        component.interrupt();
                        frame.dispose();
                    }
                });
            }
        });
    }
}
