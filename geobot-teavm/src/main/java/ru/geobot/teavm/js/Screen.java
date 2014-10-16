package ru.geobot.teavm.js;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 *
 * @author Alexey Andreev
 */
public interface Screen extends JSObject {
    @JSProperty
    int getWidth();

    @JSProperty
    int getHeight();
}
