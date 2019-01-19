package org.firstinspires.ftc.teamcode.robot.common;

import org.firstinspires.ftc.teamcode.driveto.AutoDriver;
import org.firstinspires.ftc.teamcode.driveto.DriveTo;
import org.firstinspires.ftc.teamcode.driveto.DriveToListener;
import org.firstinspires.ftc.teamcode.driveto.DriveToParams;
import org.firstinspires.ftc.teamcode.driveto.PIDParams;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.utils.Heading;
import org.firstinspires.ftc.teamcode.utils.OrderedEnum;
import org.firstinspires.ftc.teamcode.utils.OrderedEnumHelper;
import org.firstinspires.ftc.teamcode.utils.Round;
import org.firstinspires.ftc.teamcode.wheels.MOTOR_SIDE;

public class Drive implements CommonTask, DriveToListener {
    private static final boolean DEBUG = true;

    // PID Turns
    private static final float TURN_TOLERANCE = 1.5f; // Permitted heading error in degrees
    private static final float TURN_TOLERANCE_CODE = 3f; // Permitted heading error in degrees
    private static final float TURN_DIFF_TOLERANCE = 0.001f; // Permitted error change rate
    private static final int TURN_TIMEOUT = (int) (DriveTo.TIMEOUT_DEFAULT * 1.5);
    public static final PIDParams TURN_PARAMS = new PIDParams(0.011f, 0.003f, 0.0f,
            null, true, true);
    public static final PIDParams TURN_PARAMS_CODE = new PIDParams(0.011f, 0.064f, 0.0f,
            null, true, true);

    // PID Drive
    private static final float DRIVE_TOLERANCE = 100.0f; // Permitted distance error in encoder ticks
    private static final float DRIVE_DIFF_TOLERANCE = 1.0f; // Permitted error change rate
    public static final PIDParams DRIVE_PARAMS = new PIDParams(0.0005f, 0.0002f, 0.0f,
            null, true, true);

    // Straight drive speed -- Forward is toward the claws, motor positive, ticks increasing
    public final static float SPEED_FORWARD = 1.0f;
    public final static float SPEED_FORWARD_SLOW = SPEED_FORWARD * 0.75f;
    public final static float SPEED_REVERSE = -SPEED_FORWARD;

    // Runtime
    private final Robot robot;

    private HEADING_DIST_STATE headingDistState = HEADING_DIST_STATE.INIT;
    private HEADING_DIST_HEADING_STATE headingDistHeadingState = HEADING_DIST_HEADING_STATE.INIT;


    public Drive(Robot robot) {
        this.robot = robot;
    }

    public AutoDriver loop(AutoDriver driver) {
        if (driver.drive != null) {
            driver.drive.drive();

            // Remember timeouts (until the next drive())
            driver.timeout = driver.drive.isTimeout();

            // Cancel AutoDrive when we're done
            if (driver.drive.isDone()) {
                driver.drive = null;
                robot.wheels.setTeleop(true);
            }
        }
        return driver;
    }

    // Sensor reference types for our DriveTo callbacks
    public enum SENSOR_TYPE {
        DRIVE_ENCODER,
        GYROSCOPE,
        TIME,
        TIME_TURN
    }

    public DriveTo time(int mills, float speed) {
        return time(mills, speed, false);
    }

    public DriveTo timeTurn(int mills, float speed) {
        return time(mills, speed, true);
    }

    private DriveTo time(int mills, float speed, boolean turn) {
        robot.wheels.setTeleop(false);

        SENSOR_TYPE type = turn ? SENSOR_TYPE.TIME_TURN : SENSOR_TYPE.TIME;
        DriveToParams param = new DriveToParams(this, type);
        param.greaterThan(1);
        param.limitRange = speed;
        param.timeout = Math.abs(mills);
        return new DriveTo(new DriveToParams[]{param});
    }

    public DriveTo translate(int millimeters) {
        robot.wheels.setTeleop(false);

        DriveToParams param = new DriveToParams(this, SENSOR_TYPE.DRIVE_ENCODER);
        int target = (int) ((float) millimeters * robot.wheels.getTicksPerMM()) + robot.wheels.getEncoder();
        param.translationPid(target, DRIVE_PARAMS, DRIVE_TOLERANCE, DRIVE_DIFF_TOLERANCE);
        return new DriveTo(new DriveToParams[]{param});
    }

    public DriveTo distance(int millimeters) {
        robot.wheels.setTeleop(false);

        DriveToParams param = new DriveToParams(this, SENSOR_TYPE.DRIVE_ENCODER);
        int target = (int) ((float) millimeters * robot.wheels.getTicksPerMM()) + robot.wheels.getEncoder();
        param.pid(target, DRIVE_PARAMS, DRIVE_TOLERANCE, DRIVE_DIFF_TOLERANCE);
        return new DriveTo(new DriveToParams[]{param});
    }

    public DriveTo heading(float heading) {
        robot.wheels.setTeleop(false);
        heading = Heading.normalize(heading);

        DriveToParams param = new DriveToParams(this, SENSOR_TYPE.GYROSCOPE);
        PIDParams params = TURN_PARAMS;
        float tolerance = TURN_TOLERANCE;
        float diffTolerance = TURN_DIFF_TOLERANCE;
        switch (robot.bot) {
            case PRODUCTION:
                params = TURN_PARAMS;
                tolerance = TURN_TOLERANCE;
                diffTolerance = TURN_DIFF_TOLERANCE;
                break;
            case CODE:
                params = TURN_PARAMS_CODE;
                tolerance = TURN_TOLERANCE_CODE;
                diffTolerance = TURN_DIFF_TOLERANCE;
                break;
        }
        param.rotationPid(heading, params, tolerance, diffTolerance);
        param.timeout = TURN_TIMEOUT; // Allow extra time for turns to settle (we expect them to overshoot)
        return new DriveTo(new DriveToParams[]{param});
    }

    public DriveTo degrees(float degrees) {
        return heading(degrees + robot.gyro.getHeading());
    }

    @Override
    public void driveToStop(DriveToParams param) {
        switch ((SENSOR_TYPE) param.reference) {
            case DRIVE_ENCODER:
            case GYROSCOPE:
            case TIME:
            case TIME_TURN:
                robot.wheels.stop();
                break;
            default:
                robot.wheels.stop();
                throw new IllegalStateException("Unhandled driveToStop: " +
                        param.reference + " ::" + param.comparator);
        }
    }

    @Override
    public float driveToSensor(DriveToParams param) {
        float value;
        switch ((SENSOR_TYPE) param.reference) {
            case DRIVE_ENCODER:
                value = robot.wheels.getEncoder();
                break;
            case GYROSCOPE:
                value = robot.gyro.getHeading();
                break;
            case TIME:
            case TIME_TURN:
                value = 0;
                break;
            default:
                throw new IllegalStateException("Unhandled driveToSensor: " +
                        param.reference + " ::" + param.comparator);
        }
        return value;
    }

    @Override
    public void driveToRun(DriveToParams param) {
        float speed;
        if (DEBUG && param.comparator.pid()) {
            robot.telemetry.log().add("T(" + Round.truncate(param.pid.target) +
                    ") E/A/D\t" + Round.truncate(param.pid.error) +
                    "\t" + Round.truncate(param.pid.accumulated) +
                    "\t" + Round.truncate(param.pid.differential) +
                    "\t(" + Round.truncate(param.pid.output()) + ")");
        }
        switch ((SENSOR_TYPE) param.reference) {
            case DRIVE_ENCODER:
                speed = param.pid.output();
                switch (param.comparator) {
                    case PID:
                        robot.wheels.setSpeed(speed);
                        break;
                    case TRANSLATION_PID:
                        robot.wheels.setSpeed(0, speed, 0);
                        break;
                    default:
                        throw new IllegalStateException("Unhandled driveToRun: " +
                                param.reference + "::" + param.comparator);
                }
                break;
            case GYROSCOPE:
                switch (param.comparator) {
                    case ROTATION_PID:
                        speed = param.pid.output();
                        // Left spins forward when heading is increasing
                        robot.wheels.setSpeed(speed, MOTOR_SIDE.LEFT);
                        robot.wheels.setSpeed(-speed, MOTOR_SIDE.RIGHT);
                        break;
                    default:
                        throw new IllegalStateException("Unhandled driveToRun: " +
                                param.reference + "::" + param.comparator);
                }
                break;
            case TIME:
                robot.wheels.setSpeed(param.limitRange);
                break;
            case TIME_TURN:
                robot.wheels.setSpeed(param.limitRange, MOTOR_SIDE.LEFT);
                robot.wheels.setSpeed(-param.limitRange, MOTOR_SIDE.RIGHT);
                break;
            default:
                throw new IllegalStateException("Unhandled driveToRun: " +
                        param.reference + " ::" + param.comparator);
        }
    }

    public AutoDriver headingDistance(AutoDriver driver, float heading, int distance) {

        switch (headingDistState) {
            case INIT:
                driver.done = false;
                headingDistState = headingDistState.next();
                break;
            case HEADING:
                driver.drive = heading(heading);
                headingDistState = headingDistState.next();
                break;
            case DISTANCE:
                driver.drive = distance(distance);
                headingDistState = headingDistState.next();
                break;
            case DONE:
                driver.done = true;
                headingDistState = HEADING_DIST_STATE.INIT;
                break;
        }
        return driver;
    }

    // I could probably delegate 75% of this method to the one above but what the hell, copy paste is a thing
    public AutoDriver headingDistanceHeading(AutoDriver driver, float heading, int distance, float heading2) {

        switch (headingDistHeadingState) {
            case INIT:
                driver.done = false;
                headingDistHeadingState = headingDistHeadingState.next();
                break;
            case HEADING:
                driver.drive = heading(heading);
                headingDistHeadingState = headingDistHeadingState.next();
                break;
            case DISTANCE:
                driver.drive = distance(distance);
                headingDistHeadingState = headingDistHeadingState.next();
                break;
            case HEADING2:
                driver.drive = heading(heading2);
                headingDistHeadingState = headingDistHeadingState.next();
            case DONE:
                driver.done = true;
                headingDistHeadingState = HEADING_DIST_HEADING_STATE.INIT;
                break;
        }
        return driver;
    }

    enum HEADING_DIST_STATE implements OrderedEnum {
        INIT,
        HEADING,
        DISTANCE,
        DONE;

        public HEADING_DIST_STATE prev() {
            return OrderedEnumHelper.prev(this);
        }

        public HEADING_DIST_STATE next() {
            return OrderedEnumHelper.next(this);
        }

    }

    enum HEADING_DIST_HEADING_STATE implements OrderedEnum {
        INIT,
        HEADING,
        DISTANCE,
        HEADING2,
        DONE;

        public HEADING_DIST_HEADING_STATE prev() {
            return OrderedEnumHelper.prev(this);
        }

        public HEADING_DIST_HEADING_STATE next() {
            return OrderedEnumHelper.next(this);
        }

    }
}
