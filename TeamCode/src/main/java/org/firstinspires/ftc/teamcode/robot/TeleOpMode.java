package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
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
    private final static float SCOOP_SPEED = 0.5f;
    private final static float WHEELY_SPEED = 0.0f;
    private final static int INTAKE_SPEED = 1;

    // Limits
    private final static int INTAKE_MAX = 4570;
    private final static int INTAKE_MIN = 100;
    private final static int ARM_MAX = 8000;
    private final static int ARM_MIN = 100;
    private final static int LIFT_MAX = 11222;
    private final static int LIFT_MIN = 0;
    private final static int SCOOP_MIN = 0;
    private final static int SCOOP_MAX = 1300;


    // Potentially Unused
    private final static int SCOOP_UP = 50;
    private final static int SCOOP_DOWN = 1150;
    private final static int SCOOP_EXTEND = 1200;
    private final static int SCOOP_INCREMENT = 10;
    private final static int INTAKE_FINE_MOTOR_CONTROL = 300;
    private final static float SERVO_TIME_SCALAR = .004375f;

    // Fine motor control system
    private float last_goal;

    // Devices and subsystems
    private Robot robot = null;
    private ButtonHandler buttons;

    @Override
    public void init() {

        // Placate drivers
        telemetry.addData(">", "NOT READY");
        telemetry.update();

        // Init the common tasks elements
        robot = new Robot(hardwareMap, telemetry);

        // Register buttons
        buttons = new ButtonHandler(robot);
        buttons.register("INTAKE_IN", gamepad2, PAD_BUTTON.dpad_down);
        buttons.register("INTAKE_OUT", gamepad2, PAD_BUTTON.dpad_up);
        buttons.register("SLOW_MODE", gamepad1, PAD_BUTTON.a, BUTTON_TYPE.TOGGLE);
        buttons.register("REVERSE_COLLECTOR", gamepad2, PAD_BUTTON.b);
        buttons.register("STOP_COLLECTOR", gamepad2, PAD_BUTTON.y, BUTTON_TYPE.TOGGLE);
        buttons.register("SCOOP_RETURN", gamepad2, PAD_BUTTON.left_stick_button);
        buttons.register("SCOOP_EXTEND", gamepad2, PAD_BUTTON.right_stick_button);
        buttons.register("SCOOP_DOWN", gamepad2, PAD_BUTTON.dpad_right);
        buttons.register("SCOOP_UP", gamepad2, PAD_BUTTON.dpad_left);
        buttons.register("DROP_FLAG", gamepad1, PAD_BUTTON.b, BUTTON_TYPE.TOGGLE);

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
        telemetry.addData("Slow Mode", buttons.get("SLOW_MODE"));
        telemetry.addData("Lift Height", robot.lift.getEncoder());

        if (DEBUG) telemetry.addData("Arm Encoder", robot.arm.getEncoder());
        if (DEBUG) telemetry.addData("Intake Encoder", robot.intake.getEncoder());
        if (DEBUG) telemetry.addData("Scoop Encoder", robot.scoop.getEncoder());

        telemetry.update();
    }

    public void driveBase() {
        if (buttons.get("SLOW_MODE")) {
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
        float armPower = gamepad1.right_trigger - gamepad1.left_trigger;
        if (!HASHTAG_NO_LIMITS) {
            if (robot.arm.getEncoder() >= ARM_MAX) armPower = Math.min(armPower, 0);
            if (robot.arm.getEncoder() <= ARM_MIN) armPower = Math.max(armPower, 0);
        }
        robot.arm.setPower(armPower);

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
        float scoopPower = -gamepad2.right_stick_y * SCOOP_SPEED;
        if (!HASHTAG_NO_LIMITS) {
            if (robot.scoop.getEncoder() >= SCOOP_MAX) scoopPower = Math.min(scoopPower, 0);
            if (robot.scoop.getEncoder() <= SCOOP_MIN) scoopPower = Math.max(scoopPower, 0);
        }
        robot.scoop.setPower(scoopPower);

        // +----------------------+
        // | Continuous Collector |
        // +----------------------+
        if (buttons.held("STOP_COLLECTOR")) {
            robot.wheelCollector.setPosition(0.5f);
        } else if (buttons.held("REVERSE_COLLECTOR")) {
            robot.wheelCollector.setPosition(1 - WHEELY_SPEED);
        } else {
            robot.wheelCollector.setPosition(WHEELY_SPEED);
        }

        // emergency flag drop
        if (buttons.get("DROP_FLAG")) {
            robot.flagDropper.max();
        } else {
            robot.flagDropper.min();
        }

    }

    public void stop() {
        robot.scoop.stop();
        robot.intake.stop();
        robot.arm.stop();
    }
}
