package ru.geobot.game.objects;

import ru.geobot.ResourceSet;
import ru.geobot.resources.Image;
import ru.geobot.resources.ResourcePath;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
@ResourceSet
public interface RobotImages {
    @ResourcePath("robot/body.png")
    Image body();

    @ResourcePath("robot/head-left.png")
    Image headLeft();

    @ResourcePath("robot/head-right.png")
    Image headRight();

    @ResourcePath("robot/head-face.png")
    Image headFace();

    @ResourcePath("robot/big-left-wheel.png")
    Image bigLeftWheel();

    @ResourcePath("robot/big-right-wheel.png")
    Image bigRightWheel();

    @ResourcePath("robot/small-left-wheel.png")
    Image smallLeftWheel();

    @ResourcePath("robot/small-right-wheel.png")
    Image smallRightWheel();

    @ResourcePath("robot/damper.png")
    Image damper();

    @ResourcePath("robot/axle.png")
    Image axle();

    @ResourcePath("robot/antenna1.png")
    Image antenna1();

    @ResourcePath("robot/antenna2.png")
    Image antenna2();

    @ResourcePath("robot/antenna3.png")
    Image antenna3();

    @ResourcePath("robot/arm1.png")
    Image arm1();

    @ResourcePath("robot/arm2.png")
    Image arm2();

    @ResourcePath("robot/arm3.png")
    Image arm3();

    @ResourcePath("robot/arm4.png")
    Image arm4();

    @ResourcePath("robot/upper-claw.png")
    Image upperClaw();

    @ResourcePath("robot/lower-claw.png")
    Image lowerClaw();

    @ResourcePath("robot/claw-mount.png")
    Image clawMount();
}
