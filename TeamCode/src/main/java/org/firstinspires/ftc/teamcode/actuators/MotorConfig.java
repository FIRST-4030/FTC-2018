package org.firstinspires.ftc.teamcode.actuators;

import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.config.Config;

public class MotorConfig implements Config {
    public final String name;
    public final boolean reverse;
    public boolean brake;
    public DcMotor.RunMode mode;

    public MotorConfig(String name, boolean reverse, boolean brake, DcMotor.RunMode mode) {
        if (mode == null) {
            mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER;
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
