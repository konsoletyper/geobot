package ru.geobot.teavm.js;

import org.teavm.dom.core.Element;
import org.teavm.dom.events.EventTarget;
import org.teavm.jso.JSProperty;

/**
 *
 * @author Alexey Andreev
 */
public interface LocatedElement extends Element, EventTarget {
    @JSProperty
    float getOffsetLeft();

    @JSProperty
    float getOffsetTop();

    @JSProperty
    float getScrollLeft();

    @JSProperty
    float getScrollTop();

    @JSProperty
    LocatedElement getOffsetElement();
}
