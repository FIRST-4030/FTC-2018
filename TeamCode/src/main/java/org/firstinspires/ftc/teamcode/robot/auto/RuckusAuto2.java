package org.firstinspires.ftc.teamcode.robot.auto;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

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

@com.qualcomm.robotcore.eventloop.opmode.Autonomous(name = "Only Lower Lift", group = "Test")
public class RuckusAuto2 extends OpMode {

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
    private GOLD_POS gold = GOLD_POS.CENTER;
    private boolean gameReady = false;

    // Init-time config
    private ButtonHandler buttons;
    private Field.AllianceColor alliance = Field.AllianceColor.RED;
    private boolean wallLeft = true;
    private boolean claim = true;
    private boolean returnLeft = true;
    private boolean startCrater = true;
    private boolean dismountNeeded = true;

    private int[] aproachPos;
    private float centerSampleAngle;
    private int[] depotPos;
    private int[] parkSamePos; //pos for parking in the same color crater
    private int[] parkDifferentPos;

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
        vuforia.enableCapture();

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

        buttons.register("DISMOUNT-YES", gamepad1, PAD_BUTTON.right_trigger);
        buttons.register("DISMOUNT-NO", gamepad1, PAD_BUTTON.left_trigger);
    }

    @Override
    public void init_loop() {

        // Process driver input
        userSettings();

        // Driver setup
        telemetry.addData("Alliance", alliance);
        telemetry.addData("Start", (startCrater) ? "Crater" : "Depot");
        telemetry.addData("Dismount Needed", (dismountNeeded) ? "Yes" : "No");
        telemetry.addData("Claiming", claim);
        if(claim) telemetry.addData("Return Direction", returnLeft ? "Left" : "Right");
        if(!(claim && startCrater)) telemetry.addData("Drive to Wall Direction", wallLeft ? "Left" : "Right");

        // Overall ready status
        gameReady = (robot.gyro.isReady());
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

        robot.vuforia.start();
        robot.vuforia.enableCapture();

        if(alliance == Field.AllianceColor.BLUE && !startCrater) robot.gyro.setOffset(45 - 90);
        if(alliance == Field.AllianceColor.BLUE && startCrater) robot.gyro.setOffset(45);
        if(alliance == Field.AllianceColor.RED && !startCrater) robot.gyro.setOffset(-45 - 180);
        if(alliance == Field.AllianceColor.RED && startCrater) robot.gyro.setOffset(-45 - 90);

        aproachPos = new int[]{534, 534}; // something
        centerSampleAngle = 45;
        depotPos = new int[]{0, 0};
        parkSamePos = new int[]{-457, 1626};
        parkDifferentPos = new int[]{1626, -457};

        if(startCrater) {
            int temp = aproachPos[1];
            aproachPos[1] = aproachPos[0];
            aproachPos[0] = -temp;
            centerSampleAngle += 90;
        }

        if(alliance == Field.AllianceColor.RED) {
            aproachPos[0] *= -1;
            aproachPos[1] *= -1;
            centerSampleAngle += 180;
            depotPos[0] *= -1;
            depotPos[1] *= -1;
            parkSamePos[0] *= -1;
            parkSamePos[1] *= -1;
            parkDifferentPos[0] *= -1;
            parkDifferentPos[1] *= -1;
        }

        dismountNeeded = true;

    }

    @Override
    public void loop() {

        // Handle AutoDriver driving
        driver = common.drive.loop(driver);

        // Update our location and target info
        robot.vuforia.track();

        // Debug feedback
        telemetry.addData("State", state);
        telemetry.addData("Running", driver.isRunning(time));
        telemetry.addData("Pivot CCW", common.sampling.getImage() != null ? common.sampling.pivotCCW(alliance) : "<No Image>");
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
        switch (state) { // TODO: fix all delegate driver things that then advance the state seperately
            case INIT:
                driver.done = false;
                state = state.next();
                break;
            case LOWER_LIFT:
                if(dismountNeeded){
                    driver = delegateDriver(common.lift.lowerLift(driver));
                } else {
                    state = state.next();
                }
                break;
            case PARSE_SAMPLE: // TODO: hey fix the insides of this method
                //driver = delegateDriver(common.sampling.parse(driver));
                //gold = common.sampling dot something
                state = AUTO_STATE.DONE;
                break;
            case DISMOUNT:
                if(dismountNeeded) {
                    driver = delegateDriver(common.lift.dismount(driver));
                } else {
                    state = state.next();
                }
                break;
            case TURN_TO_TARGET:
                state = AUTO_STATE.DONE; //Hijacking this state to just end it all
                //driver.drive = common.drive.heading(45);
                //state = state.next();
                break;
            case GO_TO_APPROACH: //TODO: fix me (depends on dismounting)
                float heading = 0;
                int distance = 0;
                if(!robot.vuforia.isStale()) {
                    //Need to figure out what dismouting looks like
                } else {
                    heading = robot.vuforia.bearing(aproachPos);
                    distance = robot.vuforia.distance(aproachPos);
                }
                driver = delegateDriver(common.drive.headingDistance(driver, heading, distance));
                state = state.next();
                break;
            case SAMPLE:
                driver = delegateDriver(common.drive.headingDistance(driver, centerSampleAngle + gold.offset, gold.dist));
                state = state.next();
                break;
            case REVERSE_SAMPLE:
                driver.drive = common.drive.distance(-gold.dist);
                state = state.next();
                break;
            case GO_TO_WALL_APROACH:
                float heading1 = centerSampleAngle + (70 * (wallLeft ? 1 : -1));
                float heading2 = centerSampleAngle + (135 * (wallLeft ? 1 : -1));
                driver = delegateDriver(common.drive.headingDistanceHeading(driver, heading1, 940, heading2));
                state = state.next();
                break;
            case TRANSLATE_TO_WALL: // TODO: translate in auto
                state = state.next();
                break;
            case CLAIM_SKIP:
                if(!claim) {
                    state = AUTO_STATE.PARK;
                } else {
                    state = state.next();
                }
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
        LOWER_LIFT,         //lower the lift

        PARSE_SAMPLE,       //parse the sample and make a decision

        DISMOUNT,           //unhook the hook, by translating or rotating

        TURN_TO_TARGET,     //turn and look at the orientation target
        GO_TO_APPROACH,     //drive to the approach location, hopefully using a target
        SAMPLE,             //move to bump (hopefully) the gold
        REVERSE_SAMPLE,     //reverse the previous move to back in front of the metals

        GO_TO_WALL_APROACH,         //Go either right or left to one of the walls
        TRANSLATE_TO_WALL,

        //// Claiming (optional) ////

        CLAIM_SKIP,         //Skip claiming if selected OR in the invalid position
        MOVE_TO_BOX,        //Drive to the box
        DROP_FLAG,          //Drop the flag in the box

        //// Parking ////

        PARK,               //Drive to the correct parking location
                            //if claimed, the user gets a choice here

        DONE;               // Finish

        public RuckusAuto2.AUTO_STATE prev() {
            return OrderedEnumHelper.prev(this);
        }

        public RuckusAuto2.AUTO_STATE next() {
            return OrderedEnumHelper.next(this);
        }
    }

    enum GOLD_POS {
        CENTER (0, 380),
        LEFT (-45, 530),
        RIGHT (45, 530);

        public int offset;
        public int dist;

        GOLD_POS(int offset, int dist) {
            this.offset = offset;
            this.dist = dist;
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

    private void userSettings(){
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
    }

}