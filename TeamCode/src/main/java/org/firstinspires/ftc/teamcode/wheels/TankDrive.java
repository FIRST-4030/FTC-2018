package org.firstinspires.ftc.teamcode.wheels;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.actuators.Motor;

public class TankDrive implements Wheels {
    private static final boolean DEBUG = false;
    private static final float JOYSTICK_DEADZONE = 0.1f;
    private static final float SPEED_DEADZONE = JOYSTICK_DEADZONE * 0.85f;
    private static final int JOYSTICK_EXPONENT = 3;

    protected WheelsConfig config = null;
    protected final Telemetry telemetry;
    protected float speedScale = 1.0f;
    private boolean teleop = false;

    public TankDrive(HardwareMap map, Telemetry telemetry, WheelsConfig config) {
        this.telemetry = telemetry;
        for (WheelMotor wheelMotor : config.motors) {
            if (wheelMotor == null) {
                telemetry.log().add(this.getClass().getSimpleName() + ": Null motor");
                break;
            }
            wheelMotor.motor = new Motor(map, telemetry, wheelMotor);
            if (!wheelMotor.motor.isAvailable()) {
                return;
            }
        }
        this.config = config;
        resetEncoder();
    }

    public boolean isAvailable() {
        return config != null;
    }

    private int getIndex(MOTOR_SIDE side, MOTOR_END end){
        for (int i = 0; i < config.motors.length; i++) {
            if ((end == null || config.motors[i].end == end) &&
                    (side == null || config.motors[i].side == side)) {
                return i;
            }
        }
        throw new IllegalArgumentException("no motor of given side and end: " + side + " and " + end);
    }

    public void resetEncoder() {
        resetEncoder(null, null);
    }

    public void resetEncoder(MOTOR_SIDE side) {
        resetEncoder(side, null);
    }

    public void resetEncoder(MOTOR_SIDE side, MOTOR_END end) {
        if (!isAvailable()) {
            return;
        }
        config.motors[getIndex(side, end)].motor.resetEncoder();
    }

    public float getTicksPerMM() {
        return getTicksPerMM(null, null);
    }

    public float getTicksPerMM(MOTOR_SIDE side) {
        return getTicksPerMM(side, null);
    }

    public float getTicksPerMM(MOTOR_SIDE side, MOTOR_END end) {
        if (!isAvailable()) {
            return 0.0f;
        }
        return config.motors[getIndex(side, end)].ticksPerMM;
    }

    public int getEncoder() {
        return getEncoder(null, null);
    }

    public int getEncoder(MOTOR_SIDE side) {
        return getEncoder(side, null);
    }

    public int getEncoder(MOTOR_SIDE side, MOTOR_END end) {
        return config.motors[getIndex(side, end)].motor.getEncoder();
    }

    public void setSpeed(float x, float y, float rotation) {
        this.setSpeed(x);
    }

    public void setSpeed(float speed) {
        setSpeed(speed, null);
    }

    public void setSpeed(float speed, MOTOR_SIDE side) {
        if (!isAvailable()) {
            return;
        }
        for (WheelMotor motor : config.motors) {
            if (side == null || motor.side == side) {
                motor.motor.setPower(limit(speed));
            }
        }
    }

    public void stop() {
        if (!isAvailable()) {
            return;
        }
        for (int i = 0; i < config.motors.length; i++) {
            config.motors[i].motor.setPower(0.0f);
        }
    }

    public boolean isTeleop() {
        return this.teleop;
    }

    public void setTeleop(boolean enabled) {
        if (this.teleop != enabled) {
            stop();
        }
        this.teleop = enabled;
    }

    public void setSpeedScale(float scale) {
        this.speedScale = limit(scale);
    }

    public void loop(Gamepad pad) {
        if (!isAvailable() || !isTeleop() || pad == null) {
            return;
        }

        // Negative is forward; this is typically the opposite of native motor config
        float left = cleanJoystick(-pad.left_stick_y);
        this.setSpeed(left, MOTOR_SIDE.LEFT);

        float right = cleanJoystick(-pad.right_stick_y);
        this.setSpeed(right, MOTOR_SIDE.RIGHT);
    }

    protected float limit(float input) {
        return com.qualcomm.robotcore.util.Range.clip(input, -1.0f, 1.0f);
    }

    protected float cleanJoystick(float stick) {
        if (Math.abs(stick) < JOYSTICK_DEADZONE) {
            return 0.0f;
        }

        float power = limit(stick);
        power = (float)Math.pow(power, JOYSTICK_EXPONENT);
        power = Math.copySign(power, stick);
        return power;
    }
}
