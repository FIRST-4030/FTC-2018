package org.firstinspires.ftc.teamcode.actuators;

import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.config.Config;

import java.security.InvalidParameterException;

public class MotorConfig implements Config {
    public final String name;
    public final boolean reverse;
    public final boolean brake;
    public final DcMotor.RunMode mode;

    public MotorConfig(MotorConfig config) {
        if (config == null) {
            throw new InvalidParameterException(this.getClass().getSimpleName() + ": Null config");
        }

        this.name = config.name;
        this.reverse = config.reverse;
        this.brake = config.brake;
        this.mode = config.mode;
    }

    public MotorConfig(String name, boolean reverse, boolean brake, DcMotor.RunMode mode) {
        if (name == null) {
            throw new InvalidParameterException(this.getClass().getSimpleName() + ": Null name");
        }

        this.name = name;
        this.reverse = reverse;
        this.brake = brake;
        this.mode = mode;
    }

    public MotorConfig(String name, boolean reverse, boolean brake) {
        this(name, reverse, brake, null);
    }

    public MotorConfig(String name, boolean reverse) {
        this(name, reverse, false, null);
    }
}
