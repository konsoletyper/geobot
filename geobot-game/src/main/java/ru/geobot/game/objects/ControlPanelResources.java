package ru.geobot.game.objects;

import ru.geobot.ResourceSet;
import ru.geobot.resources.Image;
import ru.geobot.resources.ResourcePath;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
@ResourceSet
public interface ControlPanelResources {
    @ResourcePath("control-panel.png")
    Image panel();

    @ResourcePath("control-handle.png")
    Image handle();
}
