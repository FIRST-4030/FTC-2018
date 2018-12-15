package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.buttons.BUTTON_TYPE;
import org.firstinspires.ftc.teamcode.buttons.PAD_BUTTON;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name = "TeleOp")
public class TeleOpMode extends OpMode {

    // Configuration
    private boolean DEBUG = true;
    private boolean HASHTAG_NO_LIMITS = true;

    // Drive speeds
    private final static float SCALE_FULL = 1.0f;
    private final static float SCALE_SLOW = SCALE_FULL * 0.5f;
    private final static int SCOOP_SPEED = 40;
    private final static float WHEELY_SPEED = 0.0f;
    private final static int INTAKE_SPEED = 1;

    // Limits
    private final static int INTAKE_MAX = 4570;
    private final static int INTAKE_MIN = 100;
    private final static int ARM_MAX = 8000;
    private final static int ARM_MIN = 100;
    private final static int LIFT_MAX = 11222;
    private final static int LIFT_MIN = 0;
    private final static int SCOOP_UP = 50;
    private final static int SCOOP_DOWN = 1150;

    // Potentially Unused
    private final static int SCOOP_MIN = 0;
    private final static int SCOOP_EXTEND = 1200;
    private final static int SCOOP_MAX = SCOOP_EXTEND + 300;
    private final static int SCOOP_INCREMENT = 10;
    private final static int INTAKE_FINE_MOTOR_CONTROL = 300;
    private final static float SERVO_TIME_SCALAR = .004375f;

    // Fine motor control system
    private float last_goal;

    // Devices and subsystems
    private Robot robot = null;
    private ButtonHandler buttons;
    private int scoopTarget;

    @Override
    public void init() {

        // Placate drivers
        telemetry.addData(">", "NOT READY");
        telemetry.update();

        // Init the common tasks elements
        robot = new Robot(hardwareMap, telemetry);

        // Register buttons
        buttons = new ButtonHandler(robot);
        buttons.register("INTAKE-IN", gamepad2, PAD_BUTTON.dpad_down);
        buttons.register("INTAKE-OUT", gamepad2, PAD_BUTTON.dpad_up);
        buttons.register("SLOW-MODE", gamepad1, PAD_BUTTON.a, BUTTON_TYPE.TOGGLE);
        buttons.register("REVERSE-COLLECTOR", gamepad2, PAD_BUTTON.b);
        buttons.register("STOP-COLLECTOR", gamepad2, PAD_BUTTON.y, BUTTON_TYPE.TOGGLE);
        buttons.register("SCOOP_RETURN", gamepad2, PAD_BUTTON.left_stick_button);
        buttons.register("SCOOP_EXTEND", gamepad2, PAD_BUTTON.right_stick_button);
        buttons.register("SCOOP_DOWN", gamepad2, PAD_BUTTON.dpad_right);
        buttons.register("SCOOP_UP", gamepad2, PAD_BUTTON.dpad_left);

        // Wait for the game to begin
        telemetry.addData(">", "Ready for game start");
        telemetry.update();

    }

    @Override
    public void init_loop() {
    }

    @Override
    public void start() {
        robot.wheels.setTeleop(true);
    }

    @Override
    public void loop() {

        // Update buttons
        buttons.update();

        // Move the robot
        driveBase();
        liftSystem();

        // Driver Feedback
        telemetry.addData("Wheels", robot.wheels.isAvailable());
        telemetry.addData("Teleop", robot.wheels.isTeleop());
        telemetry.addData("Slow Mode", buttons.get("SLOW-MODE"));
        telemetry.addData("Lift Height", robot.lift.getEncoder());

        if (DEBUG) telemetry.addData("Arm Encoder", robot.arm.getEncoder());
        if (DEBUG) telemetry.addData("Intake Encoder", robot.intake.getEncoder());
        if (DEBUG) telemetry.addData("Scoop Encoder", robot.scoop.getEncoder());
        if (DEBUG) telemetry.addData("Scoop Target", scoopTarget);

        telemetry.update();
    }

    public void driveBase() {
        if (buttons.get("SLOW-MODE")) {
            robot.wheels.setSpeedScale(SCALE_SLOW);
        } else {
            robot.wheels.setSpeedScale(SCALE_FULL);
        }
        robot.wheels.loop(gamepad1);
    }

    public void liftSystem() {

        // +------+
        // | Lift |
        // +------+
        float liftPower = 0;
        if (gamepad1.left_bumper && !gamepad1.right_bumper) liftPower = -1;
        if (!gamepad1.left_bumper && gamepad1.right_bumper) liftPower = 1;
        if (!HASHTAG_NO_LIMITS) {
            if (robot.lift.getEncoder() >= LIFT_MAX) liftPower = Math.min(liftPower, 0);
            if (robot.lift.getEncoder() <= LIFT_MIN) liftPower = Math.max(liftPower, 0);
        }
        robot.lift.setPower(liftPower);

        // +-----+
        // | Arm |
        // +-----+
        float arm = gamepad1.right_trigger - gamepad1.left_trigger;
        if (!HASHTAG_NO_LIMITS) {
            if (robot.arm.getEncoder() >= ARM_MAX) arm = Math.min(arm, 0);
            if (robot.arm.getEncoder() <= ARM_MIN) arm = Math.max(arm, 0);
        }
        robot.arm.setPower(arm);

        // +--------+
        // | Intake |
        // +--------+

        // Left Stick Y Controls
        /*
        float intake = -gamepad2.left_stick_y;
        if (!HASHTAG_NO_LIMITS) {
            if (robot.intake.getEncoder() >= INTAKE_MAX) intake = Math.min(intake, 0);
            if (robot.intake.getEncoder() <= INTAKE_MIN) intake = Math.max(intake, 0);
        }
        robot.intake.setPower(intake);
        */

        // Dpad controls
        float intake;
        if (gamepad2.dpad_down) {
            intake = -INTAKE_SPEED;
            if (!HASHTAG_NO_LIMITS && robot.intake.getEncoder() <= INTAKE_MIN) intake = 0;
        } else if (gamepad2.dpad_up) {
            intake = INTAKE_SPEED;
            if (!HASHTAG_NO_LIMITS && robot.intake.getEncoder() >= INTAKE_MAX) intake = 0;
        } else {
            intake = 0;
        }
        robot.intake.setPower(intake);

        // +-------+
        // | Scoop |
        // +-------+
        /*
        if (buttons.get("SCOOP_RETURN")) {
            robot.scoop.set(SCOOP_MIN);
        } else if (buttons.get("SCOOP_EXTEND")) {
            robot.scoop.set(SCOOP_EXTEND);
        } else {
            int current = robot.scoop.getEncoder();
            int target = current;
            if (buttons.get("SCOOP_DOWN")) {
                target += SCOOP_INCREMENT;
                target = Math.max(target, SCOOP_MAX);
            } else if (buttons.get("SCOOP_UP")) {
                target -= SCOOP_INCREMENT;
                target = Math.min(target, SCOOP_MIN);
            }
            if (target != current) {
                robot.scoop.set(target);
            }
        }*/

        scoopTarget = (int) (robot.scoop.pid.target + (-gamepad2.right_stick_y * SCOOP_SPEED));
        robot.scoop.set(scoopTarget);
        if (buttons.get("SCOOP_UP"))
            robot.scoop.set(SCOOP_UP);
        if (buttons.get("SCOOP_DOWN"))
            robot.scoop.set(SCOOP_DOWN);

        // +----------------------+
        // | Continuous Collector |
        // +----------------------+
        if (buttons.held("STOP-COLLECTOR")) {
            robot.wheelCollector.setPosition(0.5f);
        } else if (buttons.held("REVERSE-COLLECTOR")) {
            robot.wheelCollector.setPosition(1 - WHEELY_SPEED);
        } else {
            robot.wheelCollector.setPosition(WHEELY_SPEED);
        }

    }

    public void stop() {
        robot.scoop.stop();
        robot.intake.stop();
        robot.arm.stop();
    }
}
