package org.firstinspires.ftc.teamcode.robot.auto;

import org.firstinspires.ftc.teamcode.field.Field;

public class Params {
    // Members for each non-state parameter we need in auto
    public int[] jewel_approach = {100, 100};
    public int[] jewel_center = {120, 120};

    Params(Field.AllianceColor color, Field.StartPosition position) {

        // Select this match's parameters based on driver input
        switch (color) {
            case RED:
                switch (position) {
                    case CRATER:
                        // Assign new values from scratch
                        jewel_approach = new int[]{-100, 100};
                        // Or mirror across the origin
                        jewel_center = mirrorX(jewel_center);
                        break;
                    case FLAG:
                        break;
                }
                break;
            case BLUE:
                switch (position) {
                    case CRATER:
                        break;
                    case FLAG:
                        break;
                }
                break;
        }
    }

    private int[] mirrorX(int[] location) {
        location[0] *= -1;
        return location;
    }
}
