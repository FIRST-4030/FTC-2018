package org.firstinspires.ftc.teamcode.robot.auto;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;
import org.firstinspires.ftc.teamcode.buttons.ButtonHandler;
import org.firstinspires.ftc.teamcode.buttons.PAD_BUTTON;
import org.firstinspires.ftc.teamcode.driveto.AutoDriver;
import org.firstinspires.ftc.teamcode.field.Field;
import org.firstinspires.ftc.teamcode.field.VuforiaConfigs;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.robot.common.Common;
import org.firstinspires.ftc.teamcode.utils.OrderedEnum;
import org.firstinspires.ftc.teamcode.utils.OrderedEnumHelper;
import org.firstinspires.ftc.teamcode.utils.Round;
import org.firstinspires.ftc.teamcode.vuforia.VuforiaFTC;

@com.qualcomm.robotcore.eventloop.opmode.Autonomous(name = "Jewel + Block", group = "Auto")
public class RuckusAuto extends OpMode {

    // Auto constants
    private static final String TARGET = VuforiaConfigs.TargetNames[0];
    private static final int START_ANGLE = -4;
    private static final int START_DISTANCE = 1120;

    // Devices and subsystems
    private Robot robot = null;
    private Common common = null;
    private VuforiaFTC vuforia = null;

    // Runtime state
    private AutoDriver driver = new AutoDriver();
    private AUTO_STATE state = AUTO_STATE.INIT;
    private boolean liftReady = false;
    private boolean targetReady = false;
    private boolean gameReady = false;

    // Init-time config
    private ButtonHandler buttons;
    private Field.AllianceColor alliance = Field.AllianceColor.RED;
    private boolean wallLeft = true;
    private boolean claim = true;
    private boolean returnLeft = true;
    private boolean startCrater = true;

    @Override
    public void init() {
        telemetry.addData(">", "Init…");
        telemetry.update();

        // Init the common tasks elements
        robot = new Robot(hardwareMap, telemetry);
        common = robot.common;
        vuforia = robot.vuforia;

        // Init the camera system
        vuforia.start();
        vuforia.enableCapture(true);

        // Register buttons
        buttons = new ButtonHandler(robot);
        buttons.register("WALL-RIGHT", gamepad1, PAD_BUTTON.dpad_right);
        buttons.register("WALL-LEFT", gamepad1, PAD_BUTTON.dpad_left);
        buttons.register("START-CRATER", gamepad1, PAD_BUTTON.y);
        buttons.register("START-DEPOT", gamepad1, PAD_BUTTON.a);
        buttons.register("ALLIANCE-RED", gamepad1, PAD_BUTTON.b);
        buttons.register("ALLIANCE-BLUE", gamepad1, PAD_BUTTON.x);

        buttons.register("CLAIM-YES", gamepad1, PAD_BUTTON.dpad_up);
        buttons.register("CLAIM-NO", gamepad1, PAD_BUTTON.dpad_down);

        buttons.register("RETURN-LEFT", gamepad1, PAD_BUTTON.left_bumper);
        buttons.register("RETURN-RIGHT", gamepad1, PAD_BUTTON.right_bumper);
    }

    @Override
    public void init_loop() {

        // Process driver input
        buttons.update();
        if (buttons.get("ALLIANCE-RED")) {
            alliance = Field.AllianceColor.RED;
        } else if (buttons.get("ALLIANCE-BLUE")) {
            alliance = Field.AllianceColor.BLUE;
        }

        if (buttons.get("START-CRATER")) {
            startCrater = true;
            if(claim){
                wallLeft = true;
            }
        } else if (buttons.get("START-DEPOT")) {
            startCrater = false;
        }

        if (buttons.get("CLAIM-YES")) {
            claim = true;
            if(startCrater){
                wallLeft = true;
            }
        } else if (buttons.get("CLAIM-NO")) {
            claim = false;
        }

        if(!(startCrater && claim)) {
            if (buttons.get("WALL-LEFT")) {
                wallLeft = true;
            } else if (buttons.get("WALL-RIGHT")) {
                wallLeft = false;
            }
        }

        if(claim) {
            if (buttons.get("RETURN-LEFT")) {
                returnLeft = true;
            } else if (buttons.get("RETURN-RIGHT")) {
                returnLeft = false;
            }
        }

        // Driver setup
        telemetry.addData("Alliance", alliance);
        telemetry.addData("Start", (startCrater) ? "Crater" : "Depot");
        telemetry.addData("Claiming", claim);
        if(claim) telemetry.addData("Return Direction", returnLeft ? "Left" : "Right");
        if(!(claim && startCrater)) telemetry.addData("Drive to Wall Direction", wallLeft ? "Left" : "Right");

        // Overall ready status
        gameReady = (robot.gyro.isReady() && targetReady && liftReady);
        telemetry.addData("\t\t\t", "");
        telemetry.addData(">", gameReady ? "Ready for game start" : "NOT READY");

        // Detailed feedback
        telemetry.addData("\t\t\t", "");
        telemetry.addData("Gyro", robot.gyro.isReady() ? "Ready" : "Calibrating…");

        // Update
        telemetry.update();
    }

    @Override
    public void start() {
        telemetry.clearAll();

        // Log if we didn't exit init as expected
        if (!gameReady) {
            telemetry.log().add("Started before ready");
        }

        // Steady…
        state = AUTO_STATE.values()[0];
    }

    @Override
    public void loop() {

        // Handle AutoDriver driving
        driver = common.drive.loop(driver);


        // Debug feedback
        telemetry.addData("State", state);
        telemetry.addData("Running", driver.isRunning(time));
        telemetry.addData("Pivot CCW", common.jewel.getImage() != null ? common.jewel.pivotCCW(alliance) : "<No Image>");
        telemetry.addData("Gyro", Round.truncate(robot.gyro.getHeading()));
        telemetry.addData("Encoder", robot.wheels.getEncoder());
        telemetry.update();

        /*
         * Cut the loop short when we are AutoDriver'ing
         * This keeps us out of the state machine until the preceding command is complete
         */
        if (driver.isRunning(time)) {
            return;
        }

        // Main state machine, see enum for description of each state
        switch (state) {
            case INIT:
                driver.done = false;
                state = state.next();
                break;
            case DISMOUNT:
                state = state.next();
                break;
            case GET_SAMPLE:
                state = state.next();
                break;
            case PARSE_SAMPLE:
                state = state.next();
                break;
            case TURN_TO_TARGET:
                state = state.next();
                break;
            case GET_ORIENTATION:
                state = state.next();
                break;
            case TURN_TO_SAMPLE:
                state = state.next();
                break;
            case SAMPLE:
                state = state.next();
                break;
            case REVERSE_SAMPLE:
                state = state.next();
                break;
            case GO_TO_WALL:
                state = state.next();
                break;
            case CLAIM_SKIP:
                state = state.next();
                break;
            case MOVE_TO_BOX:
                state = state.next();
                break;
            case DROP_FLAG:
                state = state.next();
                break;
            case PARK:
                state = state.next();
                break;
            case DONE:
                driver.done = true;
                break;
        }
    }

    // Define the order of auto routine components
    enum AUTO_STATE implements OrderedEnum { // this list is not complete or likely detailed enough
        INIT,               // Initiate stuff

        //// Dismounting and sampling ////
        DISMOUNT,           //lower the lift and drive sideways a little

        //this might be better after the getting orientation part
        GET_SAMPLE,         //get the image of the metals
        PARSE_SAMPLE,       //parse the sample and make a decision

        TURN_TO_TARGET,     //turn and look at the orientation target
        GET_ORIENTATION,    //get the image
        TURN_TO_SAMPLE,     //turn back to face the samples

        // could be either a turn and drive or a translate and drive
        SAMPLE,             //move to bump (hopefully) the gold
        REVERSE_SAMPLE,     //reverse the previous move to back in front of the metals

        GO_TO_WALL,         //Go either right or left to one of the walls

        //// Claiming (optional) ////

        CLAIM_SKIP,         //Skip claiming if selected OR in the invalid position
        MOVE_TO_BOX,        //Drive to the box
        DROP_FLAG,          //Drop the flag in the box

        //// Parking ////

        PARK,               //Drive to the correct parking location
                            //if claimed, the user gets a choice here

        DONE;               // Finish

        public RuckusAuto.AUTO_STATE prev() {
            return OrderedEnumHelper.prev(this);
        }

        public RuckusAuto.AUTO_STATE next() {
            return OrderedEnumHelper.next(this);
        }
    }

    // Utility function to delegate our AutoDriver to an external provider
    // Driver is proxied back up to caller, state is advanced when delegate sets ::done
    private AutoDriver delegateDriver(AutoDriver autoDriver) {
        if (autoDriver.isDone()) {
            autoDriver.done = false;
            state = state.next();
        }
        return autoDriver;
    }

    // Process up/down buttons pairs for ordered enums
    private OrderedEnum updateEnum(String name, OrderedEnum e) {
        OrderedEnum retval = e;
        if (buttons.get(name + "-UP")) {
            retval = e.next();
        } else if (buttons.get(name + "-DOWN")) {
            retval = e.prev();
        }
        return retval;
    }
}