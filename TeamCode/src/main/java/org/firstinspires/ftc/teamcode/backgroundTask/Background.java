package org.firstinspires.ftc.teamcode.backgroundTask;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

public abstract class Background implements Runnable {

    private boolean running = false;
    private Thread thread;

    public void start() {
        if (thread != null) return;
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public void stop() {
        if (thread == null) return;
        running = false;
        thread.interrupt();
    }

    @Override
    public void run() {
        while (running) {
            loop();
        }
    }

    protected abstract void loop();

}
