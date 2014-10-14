package ru.geobot.game;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import ru.geobot.Game;
import ru.geobot.ResourcePreloader;
import ru.geobot.SwingRunner;

/**
 *
 * @author Alexey Andreev
 */
public class Starter {
    private static boolean debugMode;
    private static SwingRunner component;
    private static ResourcePreloader preloader;

    public static void main(String[] args) {
        if (args.length > 0) {
            if (args[0].equals("debug")) {
                debugMode = true;
            }
        }

        component = new SwingRunner();
        preloader = new ResourcePreloader(component.getResourceReader());
        component.run(new GeobotMainScreen());
        if (debugMode) {
            startInWindow(component);
            component.suspend();
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
        window.setLayout(new BoxLayout(window.getContentPane(), BoxLayout.PAGE_AXIS));
        window.add(new LoadProgressComponent());
        graphicsDevice.setFullScreenWindow(window);
        window.setVisible(true);
        new Thread(new Runnable() {
            @Override public void run() {
                preloader.preloadResources();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        window.getContentPane().removeAll();
                        window.invalidate();
                        window.setLayout(new BoxLayout(window.getContentPane(), BoxLayout.PAGE_AXIS));
                        window.add(component);
                        window.revalidate();
                        window.repaint();
                        component.revalidate();
                        component.resume();
                        component.repaint();
                        window.requestFocusInWindow();
                        component.requestFocus();
                    }
                });
            }
        }).start();
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                window.requestFocusInWindow();
                component.requestFocus();
                window.addWindowListener(new WindowAdapter() {
                    @Override public void windowClosing(WindowEvent e) {
                        component.interrupt();
                    }
                });
            }
        });
        component.addStopHandler(new Runnable() {
            @Override public void run() {
                window.dispose();
            }
        });
    }

    private static void startInWindow(final SwingRunner component) {
        preloader.preloadResources();
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
