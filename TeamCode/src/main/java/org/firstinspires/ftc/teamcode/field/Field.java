package org.firstinspires.ftc.teamcode.field;

public class Field {
    public static final float MM_PER_INCH = 25.4f;
    private static final int FIELD_WIDTH_INCHES = (12 * 12) - 3; // Fields are 11' 9"
    public static final int FIELD_WIDTH = (int) (FIELD_WIDTH_INCHES * MM_PER_INCH);
    public static final int HALF_FIELD_WIDTH = (int) ((FIELD_WIDTH_INCHES * MM_PER_INCH) / 2.0f);
    public static final float TARGET_HEIGHT = (6) * MM_PER_INCH;

    public enum StartPosition {CRATER, FLAG}

    public enum AllianceColor {
        RED, BLUE;

        public static AllianceColor opposite(AllianceColor color) {
            switch (color) {
                case RED:
                    color = AllianceColor.BLUE;
                    break;
                case BLUE:
                    color = AllianceColor.RED;
                    break;
            }
            return color;
        }
    }
}
