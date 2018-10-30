package org.firstinspires.ftc.teamcode.robot.common;

import org.firstinspires.ftc.teamcode.driveto.AutoDriver;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.utils.OrderedEnum;
import org.firstinspires.ftc.teamcode.utils.OrderedEnumHelper;

public class Lift implements CommonTask {
    private static final boolean DEBUG = false;

    // Intake speed
    public final static float INTAKE_SPEED_IN = 1.0f;
    public final static float INTAKE_SPEED_OUT = -0.75f * INTAKE_SPEED_IN;

    // Lift speed -- Up is motor positive, ticks increasing
    public final static float LIFT_SPEED_UP = 1.0f;
    public final static float LIFT_SPEED_DOWN = -LIFT_SPEED_UP;

    // Eject constants
    private final static float EJECT_DELAY = 0.75f;
    private final static int REVERSE_MM = 250;

    private final static int liftEncoderLowered = 100; // TODO: get a real value

    // Runtime
    private final Robot robot;
    private LIFT_STATE dismountState = LIFT_STATE.INIT;
    private LOWER_STATE lowerState = LOWER_STATE.INIT;

    public Lift(Robot robot) {
        this.robot = robot;
    }

    public AutoDriver dismount(AutoDriver driver) {
        if (DEBUG) {
            robot.telemetry.log().add("Lift state: " + liftEncoderLowered);
        }

        switch (dismountState) {
            case INIT:
                driver.done = false;
                dismountState = dismountState.next();
                break;
            case BACK_OFF:
                driver.drive = robot.common.drive.distance(-90);
                dismountState = dismountState.next();
                break;
            case ROTATE_TO_SEE:
                driver.drive = robot.common.drive.degrees(45); // TODO: depends on where the camera is
                dismountState = dismountState.next();
                break;
            case DONE:
                driver.done = true;
                break;
        }
        return driver;
    }

    enum LIFT_STATE implements OrderedEnum {
        INIT,
        BACK_OFF, // TODO: Need a translate in here?
        ROTATE_TO_SEE,
        DONE;

        public LIFT_STATE prev() {
            return OrderedEnumHelper.prev(this);
        }

        public LIFT_STATE next() {
            return OrderedEnumHelper.next(this);
        }
    }

    public AutoDriver lowerLift(AutoDriver driver) {

        switch (lowerState) {
            case INIT:
                driver.done = false;
                robot.lift.setPower(1);
                lowerState = lowerState.next();
                break;
            case WAIT:
                if(robot.lift.getEncoder() >= liftEncoderLowered) {
                    lowerState = lowerState.next();
                }
                break;
            case DONE:
                robot.lift.stop();
                driver.done = true;
                break;
        }
        return driver;

    }

    enum LOWER_STATE implements OrderedEnum {
        INIT,
        WAIT,
        DONE;

        public LOWER_STATE prev() {
            return OrderedEnumHelper.prev(this);
        }

        public LOWER_STATE next() {
            return OrderedEnumHelper.next(this);
        }
    }
}
