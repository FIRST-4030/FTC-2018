package org.firstinspires.ftc.teamcode.robot.lift;

import org.firstinspires.ftc.teamcode.actuators.Motor;
import org.firstinspires.ftc.teamcode.utils.Background;
import org.firstinspires.ftc.teamcode.driveto.PID;
import org.firstinspires.ftc.teamcode.driveto.PIDParams;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.sensors.switches.Switch;

public class IndexedMotor extends Background {

    public static final boolean DEBUG = false;

    public static int MAX;
    public static int MIN;
    

    PIDParams params;

    protected boolean initialized = false;
    private PID pid = new PID(params);
    private Motor motor;

    public void init(Motor motor, Switch button) {
        this.motor = motor;
        pid.setTarget(MIN);
    }

    @Override
    public void loop() {
        if (!isAvailable()) {
            notAvailable();
        } else {
            pid.input(motor.getEncoder());
            motor.setPower(pid.output());
            if (DEBUG) {
                Robot.robot.telemetry.log().add("A/T/P: " + motor.getEncoder() + "\t" + pid.target + "\t" + pid.output());
            }
        }
    }

    protected void notAvailable(){
        initialized = true;
    }

    public void set(int target) {
        if (!isAvailable()) return;
        target = Math.max(target, MIN);
        target = Math.min(target, MAX);
        pid.setTarget(target);
    }

    public boolean isAvailable() {
        return initialized;
    }
}
