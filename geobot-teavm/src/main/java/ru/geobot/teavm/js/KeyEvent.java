package ru.geobot.teavm.js;

import org.teavm.dom.events.Event;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 *
 * @author Alexey Andreev
 */
public interface KeyEvent extends Event {
    @JSProperty
    JSObject getWhich();

    @JSProperty
    int getKeyCode();
}
