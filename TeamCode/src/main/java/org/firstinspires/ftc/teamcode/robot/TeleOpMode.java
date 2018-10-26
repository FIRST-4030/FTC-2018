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
        buttons.register("INTAKE_IN", gamepad1, PAD_BUTTON.a);
        buttons.register("INTAKE_OUT", gamepad1, PAD_BUTTON.y);
        buttons.register("SCOOP_IN", gamepad1, PAD_BUTTON.b);
        buttons.register("SCOOP_OUT", gamepad1, PAD_BUTTON.x);
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
        float intake = 0.0f;
        if (buttons.get("INTAKE_IN")) {
            intake = -1.0f;
        } else if (buttons.get("INTAKE_OUT")) {
            intake = 1.0f;
        }
        robot.intake.setPower(intake);

        // Scoop
        float scoop = 0.0f;
        if (buttons.get("SCOOP_IN")) {
            scoop = -1.0f;
        } else if (buttons.get("SCOOP_OUT")) {
            scoop = 1.0f;
        }
        robot.scoop.setPower(scoop);
    }
}
