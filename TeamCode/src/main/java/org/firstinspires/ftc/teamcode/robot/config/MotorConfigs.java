package org.firstinspires.ftc.teamcode.robot.config;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.actuators.Motor;
import org.firstinspires.ftc.teamcode.actuators.MotorConfig;
import org.firstinspires.ftc.teamcode.robot.MOTORS;
import org.firstinspires.ftc.teamcode.config.BOT;
import org.firstinspires.ftc.teamcode.config.Configs;

public class MotorConfigs extends Configs {
    public MotorConfigs(HardwareMap map, Telemetry telemetry, BOT bot) {
        super(map, telemetry, bot);

    }

    public Motor init(MOTORS name) {
        MotorConfig config = config(name);
        super.checkConfig(config, name);
        Motor motor = new Motor(map, telemetry, config);
        super.checkAvailable(motor, name);
        return motor;
    }

    public MotorConfig config(MOTORS motor) {
        super.checkBOT();
        checkNull(motor, MOTORS.class.getName());

        MotorConfig config = null;
        switch (bot) {

            // IMPORTANT: If you need to change the *names* of the motors here, change them in MotorConfigs too

            case PRODUCTION: case CODE:
                switch (motor) {
                    case SCOOP:
                        config = new MotorConfig("Scoop", true, true);
                        break;
                    case LIFT:
                        config = new MotorConfig("Lift", false);
                        break;
                    case ARM:
                        config = new MotorConfig("Arm", true);
                        break;
                    case INTAKE:
                        config = new MotorConfig("Intake", true);
                        break;
                }
                break;
        }
        return config;
    }
}
