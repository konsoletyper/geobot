package ru.geobot.game;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import org.jbox2d.common.Settings;
import ru.geobot.Game;
import ru.geobot.ResourcePreloader;
import ru.geobot.SwingRunner;
import ru.geobot.game.caves.Cave1Game;

/**
 *
 * @author Alexey Andreev
 */
public class Starter {
    private static boolean debugMode;
    private static SwingRunner component;

    public static void main(String[] args) {
        Settings.maxPolygonVertices = 30;
        if (args.length > 0) {
            if (args[0].equals("debug")) {
                debugMode = true;
            }
        }

        final SwingRunner component = new SwingRunner();
        new ResourcePreloader(component.getResourceReader()).preloadResources();
        GeobotEntryPoint entryPoint = new GeobotEntryPoint();
        entryPoint.setGame(new Cave1Game(entryPoint));
        component.run(entryPoint);
        if (debugMode) {
            startInWindow(component);
        } else {
            startInFullScreen(component);
        }
    }

    private static void startInFullScreen(final SwingRunner component) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice graphicsDevice = ge.getDefaultScreenDevice();
        final JFrame window = new JFrame();
        window.setSize(600, 400);
        window.setUndecorated(true);
        window.setFocusable(true);
        window.add(component);
        window.setLayout(new BoxLayout(window.getContentPane(), BoxLayout.PAGE_AXIS));
        graphicsDevice.setFullScreenWindow(window);
        window.setVisible(true);
        component.resume();
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                window.requestFocusInWindow();
                component.requestFocus();
            }
        });
    }

    private static void startInWindow(final SwingRunner component) {
        final JFrame frame = new JFrame("Geobot");
        frame.setSize(600, 400);
        frame.setFocusable(true);
        frame.add(component);
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.PAGE_AXIS));
        frame.setVisible(true);
        if (!debugMode) {
            component.resume();
        } else {
            JMenuBar menu = new JMenuBar();
            initMenu(menu);
            frame.setJMenuBar(menu);
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                component.requestFocusInWindow();
                frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                frame.addWindowListener(new WindowAdapter() {
                    @Override public void windowClosing(WindowEvent e) {
                        component.interrupt();
                        frame.dispose();
                    }
                });
            }
        });
    }

    private static void initMenu(JMenuBar menu) {
        JMenu gameMenu = new JMenu("Игра");
        menu.add(gameMenu);

        final JMenuItem resumeSuspendItem = new JMenuItem("Продолжить");
        resumeSuspendItem.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if (component.isSuspended()) {
                    component.resume();
                    resumeSuspendItem.setText("Приостановить");
                } else {
                    component.suspend();
                    resumeSuspendItem.setText("Продолжить");
                }
            }
        });
        gameMenu.add(resumeSuspendItem);

        final JMenuItem outlinePaintedItem = new JMenuItem("Обводить границы объектов");
        outlinePaintedItem.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                Game.setOutlinePainted(!Game.isOutlinePainted());
                outlinePaintedItem.setSelected(Game.isOutlinePainted());
            }
        });
        gameMenu.add(outlinePaintedItem);
    }
}
