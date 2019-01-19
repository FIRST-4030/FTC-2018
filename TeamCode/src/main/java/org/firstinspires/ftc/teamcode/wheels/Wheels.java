package org.firstinspires.ftc.teamcode.wheels;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.utils.Available;

public interface Wheels extends Available {
    void stop();

    void setSpeed(float speed);

    void setSpeed(float x, float y, float rotation);

    void setSpeed(float speed, MOTOR_SIDE side);

    float getTicksPerMM();

    float getTicksPerMM(MOTOR_SIDE side);

    float getTicksPerMM(MOTOR_SIDE side, MOTOR_END end);

    int getEncoder();

    int getEncoder(MOTOR_SIDE side);

    int getEncoder(MOTOR_SIDE side, MOTOR_END end);

    void resetEncoder();

    void resetEncoder(MOTOR_SIDE side);

    void resetEncoder(MOTOR_SIDE side, MOTOR_END end);

    void loop(Gamepad pad);

    boolean isTeleop();

    void setTeleop(boolean enabled);

    void setSpeedScale(float scale);
}
