package org.firstinspires.ftc.teamcode.actuators;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.utils.Background;
import org.firstinspires.ftc.teamcode.driveto.PID;
import org.firstinspires.ftc.teamcode.sensors.switches.Switch;

public class PIDMotor extends Motor {
    private static final boolean DEBUG = false;
    private static final int TIMEOUT = 3000;

    private Background background;
    private final PIDMotorConfig config;
    private boolean initialized;
    private final PID pid;

    public PIDMotor(HardwareMap map, Telemetry telemetry, PIDMotorConfig config) {
        super(map, telemetry, config);
        this.config = config;
        initialized = false;
        pid = new PID(config.pid);
    }

    // Generic implementation for a switch-based reset, can be overridden
    public void init(Switch button, float speed) {
        long end = System.currentTimeMillis() + TIMEOUT;

        // Until the button goes down
        while (!button.get()) {
            // Run as directed
            motor.setPower(speed);

            // Timeout for safety
            if (System.currentTimeMillis() > end) {
                stop();
                telemetry.log().add(this.getClass().getSimpleName() + ": Init timeout: " + config.name);
                return;
            }
        }

        // Stop and set our zero point
        resetEncoder();
        stop();

        // Finalized
        setInitialized();
        pid.setTarget(config.min);

        // Run in another thread
        start();
    }

    private void loop() {
        if (!isAvailable()) {
            telemetry.log().add(this.getClass().getSimpleName() + ": Cannot loop: " + config.name);
            return;
        }

        pid.input(getEncoder());
        motor.setPower(pid.output());
        if (DEBUG) {
            telemetry.log().add("A/T/P: " + getEncoder() + "\t" + pid.target + "\t" + pid.output());
        }
    }

    protected void setInitialized() {
        initialized = true;
    }

    public void set(int target) {
        if (!isAvailable()) return;

        target = Math.max(target, config.min);
        target = Math.min(target, config.max);
        pid.setTarget(target);
    }

    public boolean isAvailable() {
        return super.isAvailable() && initialized;
    }

    public void start() {
        if (!isAvailable()) {
            telemetry.log().add(this.getClass().getSimpleName() + ": Cannot start: " + config.name);
        }

        final PIDMotor me = this;
        background = new Background() {
            @Override
            public void loop() {
                me.loop();
            }
        };
        background.start();
    }

    public void stop() {
        if (background != null) {
            background.stop();
        }
    }
}
