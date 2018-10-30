package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.buttons.BUTTON_TYPE;
import org.firstinspires.ftc.teamcode.buttons.PAD_BUTTON;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name = "TeleOp")
public class TeleOpMode extends OpMode {

    // Drive speeds
    private final static float SCALE_FULL = 1.0f;
    private final static float SCALE_SLOW = SCALE_FULL * 0.5f;

    private final static int SCOOP_MAX = 100; //TODO: get a real number
    private final static int SCOOP_MIN = 0;
    private final static int INTAKE_MAX = 100;
    private final static int INTAKE_MIN = 0;

    private float servoAdjust = 0;

    private final static float SERVO_TIME_SCALAR = .1f;
    private long lastTS = System.currentTimeMillis();

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
        buttons.register("SLOW-MODE", gamepad2, PAD_BUTTON.a, BUTTON_TYPE.TOGGLE);

        // Wait for the game to begin
        telemetry.addData(">", "Ready for game start");
        telemetry.update();

    }

    @Override
    public void start() {
        robot.wheels.setTeleop(true);
    }

    @Override
    public void loop() {
        long now = System.currentTimeMillis();
        servoAdjust = SERVO_TIME_SCALAR * (now - lastTS);
        lastTS = now;


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

        // Lift
        float liftPower = 0;
        if (gamepad1.left_bumper && !gamepad1.right_bumper) liftPower = -1;
        if (!gamepad1.left_bumper && gamepad1.right_bumper) liftPower = 1;
        robot.lift.setPower(liftPower);

        // Arm
        robot.arm.setPower(gamepad1.right_trigger - gamepad1.left_trigger);

        // Intake
        float intake = -gamepad2.left_stick_y;
        if(robot.intake.getEncoder() >= INTAKE_MAX) intake = Math.min(intake, 0);
        if(robot.intake.getEncoder() <= INTAKE_MIN) intake = Math.max(intake, 0);
        robot.intake.setPower(intake);

        // Scoop
        float scoop = -gamepad2.right_stick_y;
        if(robot.scoop.getEncoder() >= SCOOP_MAX) scoop = Math.min(scoop, 0);
        if(robot.scoop.getEncoder() <= SCOOP_MIN) scoop = Math.max(scoop, 0);
        robot.scoop.setPower(scoop);
        // Example use as PIDMotor (compatible with setPower() while PID is stopped)
        //robot.scoop.set(100);

        // Intake Turn
        /*
         * This might be a good use for d-pad left/right with autokey, since there is no analog control
         * If the intake arm is converted to PIDMotor the d-pad up/down would make sense there too
         *
         * Autokey example:
         * buttons.register("INTAKE_LEFT", gamepad1, PAD_BUTTON.dpad_left);
         * buttons.register("INTAKE_RIGHT", gamepad1, PAD_BUTTON.dpad_right);
         * if (buttons.autokey("INTAKE_LEFT")) {
         *     robot.intakeTurn.setPosition(robot.intakeTurn.getPosition() + CONSTANT);
         * } else if (buttons.autokey("INTAKE_RIGHT")) {
         *     robot.intakeTurn.setPosition(robot.intakeTurn.getPosition() - CONSTANT);
         * }
         *
         * You can adjust delay between autokey presses for each individual button like this:
         *     buttons.getListener("INTAKE_LEFT").setAutokeyTimeout(500);
         * Or globally in buttons/Button.java::AUTOKEY_TIMEOUT
         */
        float turnStick = gamepad1.left_stick_x;
        if(Math.abs(turnStick) > 0.5) {
            turnStick = Math.copySign(servoAdjust, turnStick);
            robot.intakeTurn.setPosition(robot.intakeTurn.getPosition() + turnStick);
        }
        // getPosition() will never exceed the servo's configured limits, so this can't run too far

    }
}
