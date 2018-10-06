package org.firstinspires.ftc.teamcode.field;

import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.teamcode.config.BOT;
import org.firstinspires.ftc.teamcode.vuforia.VuforiaTarget;

/*
 * This config depends on the game layout but not on the robot itself
 * (unless you customize the camera position, which we haven't done to date)
 *
 * FYI: For some games the ftc_app SDK provides a default location for the Vuforia targets
 */
public class VuforiaConfigs {
    public static final String AssetName = "RoverRuckus";
    public static final String[] TargetNames = {"Space", "Dirt", "Rover", "Footprint"};
    public static final int TargetCount = TargetNames.length;

    static public VuforiaTarget Bot(BOT bot) {
        // TODO: This location and rotation is imaginary, but should at least be close.
        return new VuforiaTarget(
                "Phone",
                new float[]{(18 * Field.MM_PER_INCH) / 2, 0, 0},
                new float[]{-90, 0, 0},
                AxesOrder.YZY
        );
    }

    static public VuforiaTarget[] Field() {
        // TODO: These targets, locations and rotations are imaginary.
        float[] ADJUST = {0, 0, 0};

        return new VuforiaTarget[]{
                new VuforiaTarget(
                        TargetNames[0],
                        new float[]{0, Field.FIELD_WIDTH / 2, 0},
                        ADJUST, new float[]{90, 0, 0}
                ),
                new VuforiaTarget(
                        TargetNames[1],
                        new float[]{Field.FIELD_WIDTH / 2, 0, 0},
                        ADJUST, new float[]{90, 90, 0}
                ),
                new VuforiaTarget(
                        TargetNames[2],
                        new float[]{0, -Field.FIELD_WIDTH / 2, 0},
                        ADJUST, new float[]{90, 180, 0}
                ),
                new VuforiaTarget(
                        TargetNames[3],
                        new float[]{-Field.FIELD_WIDTH / 2, 0, 0},
                        ADJUST, new float[]{90, 270, 0}
                )};
    }
}
