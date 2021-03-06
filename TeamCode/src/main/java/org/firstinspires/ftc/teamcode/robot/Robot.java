package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.actuators.Motor;
import org.firstinspires.ftc.teamcode.actuators.PIDMotor;
import org.firstinspires.ftc.teamcode.actuators.ServoFTC;
import org.firstinspires.ftc.teamcode.config.BOT;
import org.firstinspires.ftc.teamcode.robot.common.Common;
import org.firstinspires.ftc.teamcode.robot.config.GyroConfigs;
import org.firstinspires.ftc.teamcode.robot.config.MotorConfigs;
import org.firstinspires.ftc.teamcode.robot.config.PIDMotorConfigs;
import org.firstinspires.ftc.teamcode.robot.config.ServoConfigs;
import org.firstinspires.ftc.teamcode.robot.config.SwitchConfigs;
import org.firstinspires.ftc.teamcode.robot.config.WheelsConfigs;
import org.firstinspires.ftc.teamcode.sensors.gyro.Gyro;
import org.firstinspires.ftc.teamcode.sensors.switches.Switch;
import org.firstinspires.ftc.teamcode.vuforia.VuforiaFTC;
import org.firstinspires.ftc.teamcode.wheels.Wheels;

public class Robot {
    public static Robot robot = null;
    public final Common common;

    public final BOT bot;
    public final Wheels wheels;
    public final Motor lift;
    public final PIDMotor arm;
    public final ServoFTC intakeTurn;
    public final ServoFTC flagDropper;
    public ServoFTC wheelCollector = null;
    public final Switch armSwitch;
    public final PIDMotor intake;
    public final Switch intakeSwitch;
    public final PIDMotor scoop;
    public final Gyro gyro;
    public final VuforiaFTC vuforia;

    public final HardwareMap map;
    public final Telemetry telemetry;

    public Robot(HardwareMap map, Telemetry telemetry) {
        this(map, telemetry, null);
    }

    public Robot(HardwareMap map, Telemetry telemetry, BOT bot) {
        robot = this;
        this.map = map;
        this.telemetry = telemetry;
        if (bot == null) {
            bot = detectBot();
        }
        this.bot = bot;

        GyroConfigs gyros = new GyroConfigs(map, telemetry, bot);
        WheelsConfigs wheels = new WheelsConfigs(map, telemetry, bot);
        MotorConfigs motors = new MotorConfigs(map, telemetry, bot);
        PIDMotorConfigs pids = new PIDMotorConfigs(map, telemetry, bot);
        ServoConfigs servos = new ServoConfigs(map, telemetry, bot);
        SwitchConfigs switches = new SwitchConfigs(map, telemetry, bot);

        this.wheels = wheels.init();
        this.wheels.stop();

        lift = motors.init(MOTORS.LIFT);
        lift.stop();

        // Init Arm
        armSwitch = switches.init(SWITCHES.ARM);
        arm = pids.init(MOTORS.ARM);
        arm.stop();

        // Init Intake
        intakeSwitch = switches.init(SWITCHES.INTAKE);
        intake = pids.init(MOTORS.INTAKE);
        intake.init();
        intake.stop();
        intakeTurn = servos.init(SERVOS.ARM_TURN);

        scoop = pids.init(MOTORS.SCOOP);
        scoop.init();
        scoop.stop();

        flagDropper = servos.init(SERVOS.FLAG_DROPPER);
        wheelCollector = servos.init(SERVOS.WHEEL_COLLECTOR);

        gyro = gyros.init();
        vuforia = new VuforiaFTC(map, telemetry, bot, "Webcam");

        this.common = new Common(this);
    }

    public BOT detectBot() {
        // Try WheelsConfigs from each bot until something succeeds
        BOT bot = null;
        for (BOT b : BOT.values()) {
            WheelsConfigs wheels = new WheelsConfigs(map, telemetry, b);
            Wheels w = wheels.init();
            if (w != null && w.isAvailable()) {
                bot = b;
                break;
            }
        }
        if (bot == null) {
            bot = BOT.values()[0];
            telemetry.log().add("BOT detection failed. Default: " + bot);
        }
        if (bot.ordinal() != 0) {
            telemetry.log().add("Using BOT: " + bot);
        }
        return bot;
    }
}
