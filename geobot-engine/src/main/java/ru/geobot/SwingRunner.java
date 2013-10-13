package ru.geobot;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JComponent;
import ru.geobot.graphics.ImageLoader;
import ru.geobot.graphics.ImageSource;
import ru.geobot.graphics.Rectangle;

/**
 *
 * @author Alexey Andreev
 */
public class SwingRunner extends JComponent {
    private static final long serialVersionUID = 4009968313752182106L;
    private int paintRatio = 1000 / 30;
    private Execution currentExecution;
    private BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>();
    private volatile BufferedImage frameBuffer;

    public SwingRunner() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (currentExecution != null) {
                    eventQueue.add(new MouseMotionEvent(e.getX(), e.getY()));
                }
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (currentExecution != null) {
                    eventQueue.add(new MouseDownEvent());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (currentExecution != null) {
                    eventQueue.add(new MouseUpEvent());
                }
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (currentExecution != null) {
                    Key key = getKey(e.getKeyCode());
                    if (key != null) {
                        eventQueue.add(new KeyDownEvent(key));
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (currentExecution != null) {
                    Key key = getKey(e.getKeyCode());
                    if (key != null) {
                        eventQueue.add(new KeyUpEvent(key));
                    }
                }
            }

            private Key getKey(int code) {
                switch (code) {
                    case KeyEvent.VK_LEFT:
                        return Key.LEFT;
                    case KeyEvent.VK_RIGHT:
                        return Key.RIGHT;
                    default:
                        return null;
                }
            }
        });
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (currentExecution != null) {
                    currentExecution.setSize(e.getComponent().getWidth(),
                            e.getComponent().getHeight());
                }
                super.componentResized(e);
            }
        });
        setFocusable(true);
    }

    public void run(EntryPoint entryPoint) {
        currentExecution = new Execution(entryPoint);
        currentExecution.setSize(getWidth(), getHeight());
        new Thread(currentExecution).start();
    }

    public void interrupt() {
        if (currentExecution != null) {
            currentExecution.interrupt();
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D gfx = (Graphics2D)g;
        gfx.setBackground(Color.black);
        gfx.drawRenderedImage(frameBuffer, new AffineTransform());
    }

    private class Execution implements Runnable, EntryPointCallback, ImageSource {
        private EntryPoint entryPoint;
        private final Object monitor = new Object();
        private boolean stopped;
        private boolean interrupted;
        private volatile int width;
        private volatile int height;
        private AtomicBoolean sizeChanged = new AtomicBoolean(false);

        public Execution(EntryPoint entryPoint) {
            super();
            this.entryPoint = entryPoint;
        }

        @Override
        public void run() {
            entryPoint.setImageSource(this);
            entryPoint.start(this);
            entryPoint.resize(Math.max(1, getWidth()), Math.max(1, getHeight()));
            long nextPaintTime = System.currentTimeMillis();
            while (true) {
                synchronized (monitor) {
                    if (stopped) {
                        break;
                    }
                    boolean shouldRepaint = false;
                    if (System.currentTimeMillis() > nextPaintTime) {
                        shouldRepaint = true;
                        long count = (System.currentTimeMillis() - nextPaintTime) / paintRatio;
                        nextPaintTime += (1 + count) * paintRatio;
                    }
                    List<Event> events = new ArrayList<>();
                    eventQueue.drainTo(events);
                    for (Event event : events) {
                        event.process(entryPoint);
                    }
                    boolean shouldWait = entryPoint.idle();
                    boolean shouldChangeSize = sizeChanged.compareAndSet(true, false);
                    int currentWidth = Math.max(1, width);
                    int currentHeight = Math.max(1, height);
                    if (shouldChangeSize) {
                        entryPoint.resize(currentWidth, currentHeight);
                    }
                    if (shouldRepaint) {
                        paint(currentWidth, currentHeight);
                        shouldWait = false;
                    }
                    if (shouldWait) {
                        try {
                            monitor.wait(2);
                        } catch (InterruptedException e) {
                            interrupted = true;
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
            if (interrupted) {
                entryPoint.interrupt();
            }
        }

        private void paint(int currentWidth, int currentHeight) {
            BufferedImage image = new BufferedImage(currentWidth, currentHeight,
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D awtGraphics = image.createGraphics();
            awtGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            awtGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            entryPoint.paint(new AWTGraphics(awtGraphics, new Rectangle(0, 0,
                    currentWidth, currentHeight)));
            awtGraphics.dispose();
            frameBuffer = image;
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    SwingRunner.this.repaint();
                }
            });
        }

        public void setSize(int width, int height) {
            this.width = width;
            this.height = height;
            sizeChanged.set(true);
        }

        @Override
        public void stop() {
            synchronized (monitor) {
                stopped = true;
                monitor.notifyAll();
            }
        }

        public void interrupt() {
            synchronized (monitor) {
                stopped = true;
                interrupted = true;
                monitor.notifyAll();
            }
        }

        @Override
        public <T> T getImages(Class<T> imageSetType) {
            return ImageLoader.load(imageSetType);
        }
    };

    private interface Event {
        void process(EntryPoint entryPoint);
    }

    private static class MouseMotionEvent implements Event {
        private int x;
        private int y;

        public MouseMotionEvent(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void process(EntryPoint entryPoint) {
            entryPoint.mouseMove(x, y);
        }
    }

    private static class MouseUpEvent implements Event {
        @Override
        public void process(EntryPoint entryPoint) {
            entryPoint.mouseUp();
        }
    }

    private static class MouseDownEvent implements Event {
        @Override
        public void process(EntryPoint entryPoint) {
            entryPoint.mouseDown();
        }
    }

    private static class KeyUpEvent implements Event {
        private Key key;

        public KeyUpEvent(Key key) {
            this.key = key;
        }

        @Override
        public void process(EntryPoint entryPoint) {
            entryPoint.keyUp(key);
        }
    }

    private static class KeyDownEvent implements Event {
        private Key key;

        public KeyDownEvent(Key key) {
            this.key = key;
        }

        @Override
        public void process(EntryPoint entryPoint) {
            entryPoint.keyDown(key);
        }
    }
}
