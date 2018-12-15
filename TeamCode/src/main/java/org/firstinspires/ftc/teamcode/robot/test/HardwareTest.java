package org.firstinspires.ftc.teamcode.robot.test;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.actuators.Motor;
import org.firstinspires.ftc.teamcode.actuators.ServoFTC;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.buttons.PAD_BUTTON;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.sensors.switches.Switch;
import org.firstinspires.ftc.teamcode.wheels.MOTOR_END;
import org.firstinspires.ftc.teamcode.wheels.MOTOR_SIDE;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name = "Hardware Test", group = "Test")
@Disabled
public class HardwareTest extends OpMode {

    private static final float SERVO_INCREMENT = 0.05f;
    private static final String MOTOR_FWD = "_M_FWD";
    private static final String MOTOR_BACK = "_M_BACK";
    private static final String SERVO_FWD = "_S_FWD";
    private static final String SERVO_BACK = "_S_BACK";

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
        buttons.register("ENCODER_RESET", gamepad1, PAD_BUTTON.guide);
        buttons.register("CHARMY" + MOTOR_FWD, gamepad1, PAD_BUTTON.dpad_up);
        buttons.register("CHARMY" + MOTOR_BACK, gamepad1, PAD_BUTTON.dpad_down);
        buttons.register("CHARMY" + SERVO_FWD, gamepad1, PAD_BUTTON.left_bumper);
        buttons.register("CHARMY" + SERVO_BACK, gamepad1, PAD_BUTTON.right_bumper);
        buttons.register("FLAG_DROPPER" + SERVO_FWD, gamepad1, PAD_BUTTON.left_stick_button);
        buttons.register("FLAG_DROPPER" + SERVO_BACK, gamepad1, PAD_BUTTON.right_stick_button);
        buttons.register("SMARMY" + MOTOR_FWD, gamepad1, PAD_BUTTON.dpad_left);
        buttons.register("SMARMY" + MOTOR_BACK, gamepad1, PAD_BUTTON.dpad_right);
        buttons.register("DUMPY" + MOTOR_FWD, gamepad1, PAD_BUTTON.y);
        buttons.register("DUMPY" + MOTOR_BACK, gamepad1, PAD_BUTTON.a);
        buttons.register("DOC" + MOTOR_FWD, gamepad1, PAD_BUTTON.x);
        buttons.register("DOC" + MOTOR_BACK, gamepad1, PAD_BUTTON.b);
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

        updateMotor("CHARMY", robot.intake);
        updateServo("CHARMY", robot.intakeTurn);
        updateServo("FLAG_DROPPER", robot.flagDropper);
        updateSwitch("CHARMY", robot.intakeSwitch);
        updateMotor("SMARMY", robot.arm);
        updateSwitch("SMARMY", robot.armSwitch);
        updateMotor("DUMPY", robot.scoop);
        updateMotor("DOC", robot.lift);

        telemetry.addData("Gimli::Glick", robot.wheels.getEncoder(MOTOR_SIDE.LEFT, MOTOR_END.BACK));
        telemetry.addData("Gimli::Glaen", robot.wheels.getEncoder(MOTOR_SIDE.LEFT, MOTOR_END.FRONT));
        telemetry.addData("Gimli::Dad", robot.wheels.getEncoder(MOTOR_SIDE.RIGHT, MOTOR_END.BACK));
        telemetry.addData("Gimli::Gloin", robot.wheels.getEncoder(MOTOR_SIDE.RIGHT, MOTOR_END.FRONT));
        robot.wheels.loop(gamepad1);

        if (buttons.get("ENCODER_RESET")) {
            robot.intake.resetEncoder();
            robot.arm.resetEncoder();
            robot.scoop.resetEncoder();
            robot.lift.resetEncoder();
            robot.wheels.resetEncoder(MOTOR_SIDE.LEFT, MOTOR_END.BACK);
            robot.wheels.resetEncoder(MOTOR_SIDE.RIGHT, MOTOR_END.BACK);
            robot.wheels.resetEncoder(MOTOR_SIDE.LEFT, MOTOR_END.FRONT);
            robot.wheels.resetEncoder(MOTOR_SIDE.RIGHT, MOTOR_END.FRONT);
        }

        telemetry.update();
    }

    @Override
    public void stop() {
    }

    private void updateSwitch(String name, Switch d) {
        telemetry.addData(name + " (D)", d.get());
    }

    private void updateMotor(String name, Motor motor) {
        float speed = 0;
        if (buttons.held(name + MOTOR_BACK)) {
            speed = -1;
        } else if (buttons.held(name + MOTOR_FWD)) {
            speed = 1;
        }
        motor.setPower(speed);
        telemetry.addData(name + " (M)", motor.getEncoder());
    }

    private void updateServo(String name, ServoFTC servo) {
        float pos = servo.getPosition();
        if (buttons.get(name + SERVO_BACK)) {
            pos -= SERVO_INCREMENT;
        } else if (buttons.get(name + SERVO_FWD)) {
            pos += SERVO_INCREMENT;
        }
        servo.setPositionRaw(pos);
        telemetry.addData(name + " (S)", servo.getPosition());
    }
}