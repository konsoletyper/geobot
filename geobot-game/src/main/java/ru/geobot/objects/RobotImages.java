package ru.geobot.objects;

import ru.geobot.graphics.Image;
import ru.geobot.graphics.ImagePath;
import ru.geobot.graphics.ResourceSet;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
@ResourceSet
public interface RobotImages {
    @ImagePath("robot/body.svg")
    Image body();

    @ImagePath("robot/wheel.svg")
    Image wheel();

    @ImagePath("robot/smallWheel.svg")
    Image smallWheel();

    @ImagePath("robot/damper.svg")
    Image damper();

    @ImagePath("robot/axle.svg")
    Image axle();

    @ImagePath("robot/smallDamper.svg")
    Image smallDamper();

    @ImagePath("robot/smallAxle.svg")
    Image smallAxle();

    @ImagePath("robot/antenna1.svg")
    Image antenna1();

    @ImagePath("robot/antenna2.svg")
    Image antenna2();

    @ImagePath("robot/antenna3.svg")
    Image antenna3();
}
