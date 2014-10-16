package ru.geobot.teavm.js;

import org.teavm.dom.html.HTMLImageElement;
import org.teavm.jso.JSConstructor;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 *
 * @author Alexey Andreev
 */
public interface CanvasGlobal extends JSObject {
    @JSConstructor
    HTMLImageElement newImage();

    @JSProperty
    Screen getScreen();
}
