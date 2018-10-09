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

    // Runtime
    private final Robot robot;
    private LIFT_STATE liftState = LIFT_STATE.INIT;
    private EJECT_STATE ejectState = EJECT_STATE.INIT;

    public Lift(Robot robot) {
        this.robot = robot;
    }

    public AutoDriver autoStart(AutoDriver driver) {
        if (DEBUG) {
            robot.telemetry.log().add("Lift state: " + liftState);
        }

        switch (liftState) {
            case INIT:
                driver.done = false;
                liftState = liftState.next();
                break;
            case START:
                liftState = liftState.next();
                break;
            case DONE:
                driver.done = true;
                break;
        }
        return driver;
    }

    enum LIFT_STATE implements OrderedEnum {
        INIT,
        START,
        DONE;

        public LIFT_STATE prev() {
            return OrderedEnumHelper.prev(this);
        }

        public LIFT_STATE next() {
            return OrderedEnumHelper.next(this);
        }
    }

    enum EJECT_STATE implements OrderedEnum {
        INIT,
        EJECT,
        REVERSE,
        STOP,
        DONE;

        public EJECT_STATE prev() {
            return OrderedEnumHelper.prev(this);
        }

        public EJECT_STATE next() {
            return OrderedEnumHelper.next(this);
        }
    }
}
