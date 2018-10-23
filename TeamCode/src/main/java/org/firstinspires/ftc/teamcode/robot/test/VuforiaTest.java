package org.firstinspires.ftc.teamcode.robot.test;

import android.os.Environment;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.buttons.BUTTON_TYPE;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.buttons.PAD_BUTTON;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.vuforia.ImageFTC;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name = "Vuforia Test", group = "Test")
public class VuforiaTest extends OpMode {

    // Devices and subsystems
    private Robot robot;
    private ButtonHandler buttons;

    // Dynamic things we need to remember
    private long lastImageTS = 0;
    private int lastBearing = 0;
    private int lastDistance = 0;
    private String lastImage = "<None>";
    private String lastTarget = "<None>";
    private boolean saved = false;

    @Override
    public void init() {

        // Init the robot
        robot = new Robot(hardwareMap, telemetry);
        buttons = new ButtonHandler(robot);
        buttons.register("ENABLE", gamepad1, PAD_BUTTON.guide, BUTTON_TYPE.TOGGLE);
        buttons.register("CAPTURE", gamepad1, PAD_BUTTON.a);

        // Wait for the game to begin
        telemetry.addData(">", "Ready for game start");
        telemetry.update();
    }

    @Override
    public void start() {
        telemetry.clearAll();

        // Start Vuforia tracking
        robot.vuforia.start();
    }

    @Override
    public void loop() {
        buttons.update();

        // Update our location and target info
        robot.vuforia.track();

        // Capture
        robot.vuforia.enableCapture(buttons.get("ENABLE"));
        if (buttons.get("CAPTURE")) {
            robot.vuforia.capture();
        }
        ImageFTC image = robot.vuforia.getImage();
        if (image != null && image.getTimestamp() != lastImageTS) {
            lastImageTS = image.getTimestamp();
            lastImage = "(" + image.getWidth() + "," + image.getHeight() + ") " + image.getTimestamp();
            if (image.savePNG("vuforia-" + image.getTimestamp() + ".png")) {
                saved = true;
            } else {
                saved = false;
            }
        }

        // Collect data about the first visible target
        String target = null;
        int bearing = 0;
        int distance = 0;
        if (!robot.vuforia.isStale()) {
            for (String t : robot.vuforia.getVisible().keySet()) {
                if (robot.vuforia.getVisible(t)) {
                    target = t;
                    int index = robot.vuforia.getTargetIndex(t);
                    bearing = robot.vuforia.bearing(index);
                    distance = robot.vuforia.distance(index);
                    break;
                }
            }
            lastTarget = target;
            lastBearing = bearing;
            lastDistance = distance;
        }

        // Driver feedback
        robot.vuforia.display(telemetry);
        telemetry.addData("Image", lastImage);
        telemetry.addData("Capture", robot.vuforia.capturing());
        telemetry.addData("Target (" + lastTarget + ")", lastDistance + "mm @ " + lastBearing + "°");
        telemetry.addData("Saved", saved);
        telemetry.addData("Photo Dir", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
        telemetry.update();
    }
}