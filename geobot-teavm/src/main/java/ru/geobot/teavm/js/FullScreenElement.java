package ru.geobot.teavm.js;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 *
 * @author konsoletyper
 */
public interface FullScreenElement extends JSObject {
    @JSProperty
    JSObject getRequestFullscreen();

    @JSProperty
    JSObject getMozRequestFullScreen();

    @JSProperty
    JSObject getWebkitRequestFullscreen();

    void requestFullscreen();

    void mozRequestFullScreen();

    void webkitRequestFullscreen();
}
