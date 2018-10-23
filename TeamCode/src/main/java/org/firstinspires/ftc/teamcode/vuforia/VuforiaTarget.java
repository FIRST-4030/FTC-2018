package org.firstinspires.ftc.teamcode.vuforia;

import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;

public class VuforiaTarget {
    private static final int NUM_DIMENSIONS = 3;

    public final String name;
    public final float[] location;
    public final float[] rotation;
    public final AxesOrder axesOrder;

    public VuforiaTarget(String name, float[] location, float[] rotation, AxesOrder axesOrder) {
        if (location == null || location.length != NUM_DIMENSIONS) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + ": Invalid location");
        }
        if (rotation == null || rotation.length != NUM_DIMENSIONS) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + ": Invalid rotation");
        }

        this.name = name;
        this.location = location;
        this.rotation = rotation;
        if (axesOrder != null) {
            this.axesOrder = axesOrder;
        } else {
            this.axesOrder = AxesOrder.XZX;
        }
    }

    public VuforiaTarget(String name, float[] location, float[] rotation) {
        this(name, location, rotation, null);
    }
}