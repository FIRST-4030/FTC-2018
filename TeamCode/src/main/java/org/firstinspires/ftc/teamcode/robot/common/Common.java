package org.firstinspires.ftc.teamcode.robot.common;

import org.firstinspires.ftc.teamcode.robot.Robot;

/*
 * These are robot-specific helper methods
 * They exist to encourage code re-use across classes
 *
 * They are a reasonable template for future robots, but are unlikely to work as-is
 */
public class Common {

    // Jewel arm post-start retracted position
    public static final float JEWEL_ARM_RETRACT = 0.40f;

    // Runtime
    public final Lift lift;
    public final Sampling sampling;
    public final Drive drive;
    private final Robot robot;

    public Common(Robot r) {
        if (r == null) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + ": Null robot");
        }
        this.robot = r;

        this.lift = new Lift(robot);
        this.sampling = new Sampling(robot);
        this.drive = new Drive(robot);
    }
}
