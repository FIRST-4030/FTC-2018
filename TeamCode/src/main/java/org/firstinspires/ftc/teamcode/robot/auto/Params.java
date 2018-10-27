package org.firstinspires.ftc.teamcode.robot.auto;

import org.firstinspires.ftc.teamcode.field.Field;

public class Params {
    // Members for each non-state parameter we need in auto
    public int[] jewel_approach = {534, 534};
    public int[] jewel_center = {120, 120};

    public float centerSampleAngle = 45;
    public int[] depotPos = new int[]{};
    public int[] parkSamePos = new int[]{};
    public int[] parkDifferentPos = new int[]{};

    Params(Field.AllianceColor color, Field.StartPosition position) {

        // Select this match's parameters based on driver input
        switch (color) {
            case RED:
                switch (position) {
                    case CRATER:
                        // Assign new values from scratch
                        jewel_approach = new int[]{-534, 534};
                        // Or mirror across the origin
                        jewel_center = mirrorX(jewel_center);

                        centerSampleAngle = 45 + 270;
                        break;
                    case FLAG:
                        centerSampleAngle = 45 + 180;
                        break;
                }
                break;
            case BLUE:
                switch (position) {
                    case CRATER:
                        centerSampleAngle = 45 + 90;
                        break;
                    case FLAG: // Uses the default values above
                        break;
                }
                break;
        }
    }

    private int[] mirrorX(int[] location) {
        location[0] *= -1;
        return location;
    }

    private int[] mirrorY(int[] location) {
        location[1] *= -1;
        return location;
    }
}
