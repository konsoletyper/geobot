package ru.geobot.teavm;

import org.teavm.dom.browser.TimerHandler;
import org.teavm.dom.browser.Window;
import org.teavm.dom.canvas.CanvasRenderingContext2D;
import org.teavm.dom.core.Element;
import org.teavm.dom.events.Event;
import org.teavm.dom.events.EventListener;
import org.teavm.dom.events.EventTarget;
import org.teavm.dom.events.MouseEvent;
import org.teavm.dom.html.HTMLCanvasElement;
import org.teavm.dom.html.HTMLDocument;
import org.teavm.dom.html.HTMLElement;
import org.teavm.dom.html.HTMLImageElement;
import org.teavm.jso.JS;
import ru.geobot.EntryPoint;
import ru.geobot.EntryPointCallback;
import ru.geobot.Key;
import ru.geobot.game.GeobotMainScreen;
import ru.geobot.graphics.Color;
import ru.geobot.resources.ResourceReader;
import ru.geobot.teavm.js.CanvasGlobal;
import ru.geobot.teavm.js.KeyEvent;
import ru.geobot.teavm.js.LocatedElement;

/**
 *
 * @author Alexey Andreev
 */
public class WebStart {
    private EntryPoint entryPoint;
    private Window window = (Window)JS.getGlobal();
    private HTMLDocument document = window.getDocument();
    private HTMLCanvasElement canvas = (HTMLCanvasElement)document.getElementById("geobot-canvas");
    private HTMLElement progressBar = document.getElementById("progress-bar");
    private HTMLElement progressBarContent = document.getElementById("progress-bar-content");
    private long startTime;
    private boolean stopped;

    public WebStart(EntryPoint entryPoint) {
        this.entryPoint = entryPoint;
        CanvasRenderingContext2D context = (CanvasRenderingContext2D)canvas.getContext("2d");
        context.setFillStyle("black");
        context.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        CanvasGlobal canvasGlobal = (CanvasGlobal)window;
        final HTMLImageElement image = canvasGlobal.newImage();
        image.setSrc("main-screen.png");
        image.addEventListener("load", new EventListener() {
            @Override public void handleEvent(Event evt) {
                CanvasRenderingContext2D context = (CanvasRenderingContext2D)canvas.getContext("2d");
                context.drawImage(image, (canvas.getWidth() - image.getWidth()) / 2, 0);
            }
        });
    }

    public void run() {
        installInputListeners();
        startTime = System.currentTimeMillis();
        ResourceReader resourceLoader = CanvasResourceLoader.getInstance();
        CanvasResourceLoader.init();
        CanvasResourceLoader.addProgressListener(new ProgressListener() {
            @Override public void progressChanged(int current, int total) {
                progressBarContent.getStyle().setProperty("width", String.valueOf(100 * current / (float)total) + "%");
            }
        });
        CanvasResourceLoader.startLoadingImages();
        entryPoint.setResourceReader(resourceLoader);
        CanvasResourceLoader.addCompleteHandler(new Runnable() {
            @Override public void run() {
                progressBar.getStyle().setProperty("display", "none");
                resourcesLoaded();
            }
        });
    }

    private void resourcesLoaded() {
        entryPoint.start(new EntryPointCallback() {
            @Override public void stop() {
                stopped = true;
                Element parent = (Element)canvas.getParentNode();
                parent.removeChild(canvas);
            }
        });
        entryPoint.resize(canvas.getWidth(), canvas.getHeight());
        step();
    }

    private void installInputListeners() {
        canvas.addEventListener("mousemove", new EventListener() {
            @Override public void handleEvent(Event evt) {
                MouseEvent mouseEvent = (MouseEvent)evt;
                float x = mouseEvent.getClientX();
                float y = mouseEvent.getClientY();
                LocatedElement element = (LocatedElement)canvas;
                while (element != null && !JS.isUndefined(element)) {
                    x -= element.getOffsetLeft();
                    y -= element.getOffsetTop();
                    element = element.getOffsetElement();
                }
                entryPoint.mouseMove((int)x, (int)y);
            }
        }, false);
        canvas.addEventListener("mousedown", new EventListener() {
            @Override public void handleEvent(Event evt) {
                entryPoint.mouseDown();
            }
        }, false);
        canvas.addEventListener("mouseup", new EventListener() {
            @Override public void handleEvent(Event evt) {
                entryPoint.mouseUp();
            }
        }, false);
        EventTarget documentEventTarget = (EventTarget)document;
        documentEventTarget.addEventListener("keydown", new EventListener() {
            @Override public void handleEvent(Event evt) {
                Key key = getKeyCode((KeyEvent)evt);
                if (key != null) {
                    entryPoint.keyDown(key);
                }
            }
        }, false);
        documentEventTarget.addEventListener("keyup", new EventListener() {
            @Override public void handleEvent(Event evt) {
                Key key = getKeyCode((KeyEvent)evt);
                if (key != null) {
                    entryPoint.keyUp(key);
                }
            }
        }, false);
    }

    private static Key getKeyCode(KeyEvent event) {
        int code = JS.isUndefined(event.getWhich()) ? JS.unwrapInt(event.getWhich()) : event.getKeyCode();
        switch (code) {
            case 37:
                return Key.LEFT;
            case 38:
                return Key.UP;
            case 39:
                return Key.RIGHT;
            case 40:
                return Key.DOWN;
            case 32:
                return Key.SPACE;
            default:
                return null;
        }
    }

    private void step() {
        long timeToCalculate = System.currentTimeMillis() - startTime;
        entryPoint.idle(timeToCalculate);
        CanvasRenderingContext2D context = (CanvasRenderingContext2D)canvas.getContext("2d");
        CanvasGraphics graphics = new CanvasGraphics(context);
        graphics.setColor(Color.black());
        graphics.fillRectangle(0, 0, 800, 600);
        entryPoint.paint(graphics);
        if (!stopped) {
            window.setTimeout(new TimerHandler() {
                @Override public void onTimer() {
                    step();
                }
            }, 10);
        }
    }

    public static void main(String[] args) {
        WebStart container = new WebStart(new GeobotMainScreen());
        container.run();
    }
}
