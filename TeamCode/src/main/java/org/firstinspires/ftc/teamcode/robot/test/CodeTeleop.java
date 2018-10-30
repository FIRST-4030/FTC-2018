package org.firstinspires.ftc.teamcode.robot.test;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.actuators.Motor;
import org.firstinspires.ftc.teamcode.buttons.BUTTON_TYPE;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.buttons.PAD_BUTTON;
import org.firstinspires.ftc.teamcode.robot.common.Common;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.wheels.MOTOR_END;
import org.firstinspires.ftc.teamcode.wheels.MOTOR_SIDE;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name = "Code Teleop", group = "Test")
public class CodeTeleop extends OpMode {

    private static final float SERVO_INCREMENT = 0.05f;

    // Devices and subsystems
    private Robot robot = null;
    private ButtonHandler buttons = null;

    @Override
    public void init() {
        telemetry.addData(">", "Init…");
        telemetry.update();

        // Common init
        robot = new Robot(hardwareMap, telemetry);

        // Buttons
        buttons = new ButtonHandler(robot);
        buttons.register("CHARMY_OUT", gamepad1, PAD_BUTTON.dpad_up);
        buttons.register("CHARMY_IN", gamepad1, PAD_BUTTON.dpad_down);
        buttons.register("SMARMY_OUT", gamepad1, PAD_BUTTON.dpad_left);
        buttons.register("SMARMY_IN", gamepad1, PAD_BUTTON.dpad_right);
        buttons.register("DUMPY_OUT", gamepad1, PAD_BUTTON.y);
        buttons.register("DUMPY_IN", gamepad1, PAD_BUTTON.a);
        buttons.register("DOC_OUT", gamepad1, PAD_BUTTON.x);
        buttons.register("DOC_IN", gamepad1, PAD_BUTTON.b);
        buttons.register("CHARMY_LEFT", gamepad1, PAD_BUTTON.left_bumper, BUTTON_TYPE.SINGLE_PRESS);
        buttons.register("CHARMY_RIGHT", gamepad1, PAD_BUTTON.right_bumper, BUTTON_TYPE.SINGLE_PRESS);
    }

    @Override
    public void init_loop() {
        telemetry.addData("Gyro", robot.gyro.isReady() ? "Ready" : "Calibrating…");
        if (robot.gyro.isReady()) {
            telemetry.addData(">", "Ready for game start");
        }
        telemetry.update();
    }

    @Override
    public void start() {
        telemetry.clearAll();
    }

    @Override
    public void loop() {
        buttons.update();

        telemetry.addData("Charmy", robot.intake.getEncoder());
        doButton("CHARMY", robot.intake);

        telemetry.addData("Smarmy", robot.arm.getEncoder());
        doButton("SMARMY", robot.arm);

        telemetry.addData("Dumpy", robot.scoop.getEncoder());
        doButton("DUMPY", robot.scoop);

        telemetry.addData("Gimili::Glick", robot.wheels.getEncoder(MOTOR_SIDE.LEFT, MOTOR_END.BACK));
        telemetry.addData("Gimili::Glaen", robot.wheels.getEncoder(MOTOR_SIDE.LEFT, MOTOR_END.FRONT));
        telemetry.addData("Gimili::Dad", robot.wheels.getEncoder(MOTOR_SIDE.RIGHT, MOTOR_END.BACK));
        telemetry.addData("Gimili::Gloin", robot.wheels.getEncoder(MOTOR_SIDE.RIGHT, MOTOR_END.FRONT));
        robot.wheels.loop(gamepad1);

        telemetry.addData("Doc", robot.lift.getEncoder());
        doButton("DOC", robot.lift);

        telemetry.addData("Charmy Turn", robot.armTurn.getPostion());
        if (buttons.get("CHARMY_LEFT")) {
            robot.armTurn.setPositionRaw(robot.armTurn.getPostion() - SERVO_INCREMENT);
        } else if (buttons.get("CHARMY_RIGHT")) {
            robot.armTurn.setPositionRaw(robot.armTurn.getPostion() + SERVO_INCREMENT);
        }

        telemetry.update();
    }

    @Override
    public void stop() {
    }

    private void doButton(String name, Motor motor) {
        float speed = 0;
        if (buttons.get(name + "_IN")) {
            speed = -1;
        } else if (buttons.get(name + "_OUT")) {
            speed = 1;
        }
        motor.setPower(speed);
    }
}