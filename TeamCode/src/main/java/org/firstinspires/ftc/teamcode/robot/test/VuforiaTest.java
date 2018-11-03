package org.firstinspires.ftc.teamcode.robot.test;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.buttons.PAD_BUTTON;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.vuforia.ImageFTC;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name = "Vuforia Test", group = "Test")
@Disabled
public class VuforiaTest extends OpMode {

    // Devices and subsystems
    private Robot robot;
    private ButtonHandler buttons;

    // Dynamic things we need to remember
    private int lastBearing = 0;
    private int lastDistance = 0;
    private String lastImage = "<None>";
    private String lastTarget = "<None>";

    @Override
    public void init() {

        // Init the robot
        robot = new Robot(hardwareMap, telemetry);
        buttons = new ButtonHandler(robot);
        buttons.register("CAPTURE", gamepad1, PAD_BUTTON.a);

        // Wait for the game to begin
        telemetry.addData(">", "Ready for game start");
        telemetry.update();
    }

    @Override
    public void start() {
        telemetry.clearAll();

        // Start Vuforia tracking and enable capture
        robot.vuforia.start();
        robot.vuforia.enableCapture();
    }

    @Override
    public void loop() {
        buttons.update();

        // Update our location and target info
        robot.vuforia.track();

        // Capture
        if (buttons.get("CAPTURE")) {
            robot.vuforia.capture();
        }
        if (robot.vuforia.getImage() != null) {
            ImageFTC image = robot.vuforia.getImage();
            lastImage = "(" + image.getWidth() + "," + image.getHeight() + ") " + image.getTimestamp();
            String filename = "vuforia-" + image.getTimestamp() + ".png";
            if (!image.savePNG(filename)) {
                telemetry.log().add(this.getClass().getSimpleName() + ": Unable to save file: " + filename);
            }
            robot.vuforia.clearImage();
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
        telemetry.addData("Target (" + lastTarget + ")", lastDistance + "mm @ " + lastBearing + "°");
        telemetry.update();
    }
}