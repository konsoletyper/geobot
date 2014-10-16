package ru.geobot.teavm.js;

import org.teavm.dom.html.HTMLElement;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 *
 * @author konsoletyper
 */
public interface FullScreenDocument extends JSObject {
    @JSProperty
    HTMLElement getFullscreenElement();

    @JSProperty
    HTMLElement getMozFullscreenElement();

    @JSProperty
    HTMLElement getWebkitFullscreenElement();
}
