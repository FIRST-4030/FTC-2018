package org.firstinspires.ftc.teamcode.robot.common;

import org.firstinspires.ftc.teamcode.actuators.PIDMotor;
import org.firstinspires.ftc.teamcode.actuators.PIDMotorConfig;
import org.firstinspires.ftc.teamcode.robot.Robot;

public class Scoop extends PIDMotor {
    public Scoop(Robot robot, PIDMotorConfig config) {
        super(robot.map, robot.telemetry, config);

        // Do nothing to initialize, other than assume we're at 0
        resetEncoder();
        setInitialized();

        // Background
        start();
    }
}
