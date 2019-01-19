package org.firstinspires.ftc.teamcode.robot.config;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.actuators.MotorConfig;
import org.firstinspires.ftc.teamcode.actuators.PIDMotor;
import org.firstinspires.ftc.teamcode.actuators.PIDMotorConfig;
import org.firstinspires.ftc.teamcode.config.BOT;
import org.firstinspires.ftc.teamcode.config.Configs;
import org.firstinspires.ftc.teamcode.driveto.PIDParams;
import org.firstinspires.ftc.teamcode.robot.MOTORS;

public class PIDMotorConfigs extends MotorConfigs {
    public PIDMotorConfigs(HardwareMap map, Telemetry telemetry, BOT bot) {
        super(map, telemetry, bot);

    }

    public PIDMotor init(MOTORS name) {
        PIDMotorConfig config = config(name);
        super.checkConfig(config, name);
        PIDMotor motor = new PIDMotor(map, telemetry, config);
        super.checkAvailable(motor, name);
        return motor;
    }

    public PIDMotorConfig config(MOTORS motor) {
        super.checkBOT();
        checkNull(motor, MOTORS.class.getName());

        PIDMotorConfig config = null;
        MotorConfig m = super.config(motor);
        switch (bot) {

            // IMPORTANT: If you need to change the *names* of the motors here, change them in MotorConfigs too

            case PRODUCTION: case CODE:
                switch (motor) {
                    case SCOOP:
                        config = new PIDMotorConfig(m, new PIDParams(0.003f, 0.0001f, 0f), -8000, 8000);
                        break;
                    case LIFT:
                        config = new PIDMotorConfig(m, new PIDParams(.003f, 0.00001f, 0f), -8000, 8000);
                        break;
                    case ARM:
                        config = new PIDMotorConfig(m, new PIDParams(.003f, 0.00001f, 0f), 0, 8000);
                        break;
                    case INTAKE:
                        config = new PIDMotorConfig(m, new PIDParams(.003f, 0.00001f, 0f), -8000, 8000);
                        break;
                }
                break;
        }
        return config;
    }
}
