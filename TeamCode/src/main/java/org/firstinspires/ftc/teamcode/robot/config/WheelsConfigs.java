package org.firstinspires.ftc.teamcode.robot.config;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.config.BOT;
import org.firstinspires.ftc.teamcode.config.Configs;
import org.firstinspires.ftc.teamcode.driveto.PIDParams;
import org.firstinspires.ftc.teamcode.wheels.DRIVE_TYPE;
import org.firstinspires.ftc.teamcode.wheels.MOTOR_END;
import org.firstinspires.ftc.teamcode.wheels.MOTOR_SIDE;
import org.firstinspires.ftc.teamcode.wheels.MecanumDrive;
import org.firstinspires.ftc.teamcode.wheels.WheelsConfig;
import org.firstinspires.ftc.teamcode.wheels.TankDrive;
import org.firstinspires.ftc.teamcode.wheels.WheelMotor;
import org.firstinspires.ftc.teamcode.wheels.Wheels;

public class WheelsConfigs extends Configs {
    private final static float DERATE = 1.0f;
    private final static float WC_MAX_RATE = 2.50f * DERATE;
    private final static float WC_TICKS_PER_MM = 1.605f;

    private final static float M_MAX_RATE = WC_MAX_RATE;
    private final static float M_TICKS_PER_MM = WC_TICKS_PER_MM;

    public WheelsConfigs(HardwareMap map, Telemetry telemetry, BOT bot) {
        super(map, telemetry, bot);
    }

    public Wheels init() {
        WheelsConfig config = config();
        super.checkConfig(config);
        Wheels wheels = null;
        switch (config.type) {
            case TANK:
                wheels = new TankDrive(map, telemetry, config);
                break;
            case MECANUM:
                wheels = new MecanumDrive(map, telemetry, config);
                break;
        }
        super.checkAvailable(wheels);
        return wheels;
    }

    public WheelsConfig config() {
        super.checkBOT();

        WheelMotor[] motors;
        WheelsConfig config = null;
        switch (bot) {
            case PRODUCTION:
                motors = new WheelMotor[4];
                motors[0] = new WheelMotor("FL", MOTOR_SIDE.LEFT, MOTOR_END.FRONT, false,
                        new PIDParams(), M_TICKS_PER_MM, M_MAX_RATE); // Gimli::Glaen
                motors[1] = new WheelMotor("BL", MOTOR_SIDE.LEFT, MOTOR_END.BACK, false,
                        new PIDParams(), M_TICKS_PER_MM, M_MAX_RATE); // Gimli::Glick
                motors[2] = new WheelMotor("FR", MOTOR_SIDE.RIGHT, MOTOR_END.FRONT, true,
                        new PIDParams(), M_TICKS_PER_MM, M_MAX_RATE); // Gimli::Gloin
                motors[3] = new WheelMotor("BR", MOTOR_SIDE.RIGHT, MOTOR_END.BACK, true,
                        new PIDParams(), M_TICKS_PER_MM, M_MAX_RATE); //Gimli::Dad
                config = new WheelsConfig(DRIVE_TYPE.MECANUM, motors, true, DcMotor.RunMode.RUN_USING_ENCODER);
                break;
            case CODE:
                motors = new WheelMotor[4];
                motors[0] = new WheelMotor("FL_Code", MOTOR_SIDE.LEFT, MOTOR_END.FRONT, false,
                        new PIDParams(), M_TICKS_PER_MM, M_MAX_RATE); // Gimli::Glaen
                motors[1] = new WheelMotor("BL", MOTOR_SIDE.LEFT, MOTOR_END.BACK, false,
                        new PIDParams(), M_TICKS_PER_MM, M_MAX_RATE); // Gimli::Glick
                motors[2] = new WheelMotor("FR", MOTOR_SIDE.RIGHT, MOTOR_END.FRONT, true,
                        new PIDParams(), M_TICKS_PER_MM, M_MAX_RATE); // Gimli::Gloin
                motors[3] = new WheelMotor("BR", MOTOR_SIDE.RIGHT, MOTOR_END.BACK, true,
                        new PIDParams(), M_TICKS_PER_MM, M_MAX_RATE); //Gimli::Dad
                config = new WheelsConfig(DRIVE_TYPE.MECANUM, motors, true, DcMotor.RunMode.RUN_USING_ENCODER);
                break;
        }
        return config;
    }
}
