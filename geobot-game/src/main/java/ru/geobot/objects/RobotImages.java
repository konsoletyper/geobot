package ru.geobot.objects;

import ru.geobot.ResourceSet;
import ru.geobot.resources.Image;
import ru.geobot.resources.ResourcePath;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
@ResourceSet
public interface RobotImages {
    @ResourcePath("robot/body.svg")
    Image body();

    @ResourcePath("robot/wheel.svg")
    Image wheel();

    @ResourcePath("robot/smallWheel.svg")
    Image smallWheel();

    @ResourcePath("robot/damper.svg")
    Image damper();

    @ResourcePath("robot/axle.svg")
    Image axle();

    @ResourcePath("robot/smallDamper.svg")
    Image smallDamper();

    @ResourcePath("robot/smallAxle.svg")
    Image smallAxle();

    @ResourcePath("robot/antenna1.svg")
    Image antenna1();

    @ResourcePath("robot/antenna2.svg")
    Image antenna2();

    @ResourcePath("robot/antenna3.svg")
    Image antenna3();
}
