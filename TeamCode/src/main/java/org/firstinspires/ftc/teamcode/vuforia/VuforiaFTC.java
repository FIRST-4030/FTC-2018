/*
Copyright (c) 2016 Robert Atkinson

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Robert Atkinson nor the names of his contributors may be used to
endorse or promote products derived from this software without specific prior
written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.firstinspires.ftc.teamcode.vuforia;

import android.graphics.Bitmap;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.ThreadPool;
import com.vuforia.Frame;
import com.vuforia.HINT;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.function.Consumer;
import org.firstinspires.ftc.robotcore.external.function.Continuation;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;
import org.firstinspires.ftc.teamcode.config.BOT;
import org.firstinspires.ftc.teamcode.field.VuforiaConfigs;
import org.firstinspires.ftc.teamcode.utils.Heading;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VuforiaFTC {
    private static final boolean DEBUG = true;

    /**
     * TODO: If you downloaded this file from another team you need to get your own Vuforia key
     * See https://library.vuforia.com/articles/Solution/How-To-Create-an-App-License for instructions
     */
    // Team-specific Vuforia key
    private static final String VUFORIA_KEY = "AV9rwXT/////AAABma+8TAirNkVYosxu9qv0Uz051FVEjKU+nkH+MaIvGuHMijrdgoZYBZwCW2aG8P3+eZecZZPq9UKsZiTHAg73h09NT48122Ui10c8DsPe0Tx5Af6VaBklR898w8xCTdOUa7AlBEOa4KfWX6zDngegeZT5hBLfJKE1tiDmYhJezVDlITIh7SHBv0xBvoQuXhemlzL/OmjrnLuWoKVVW0kLanImI7yra+L8eOCLLp1BBD/Iaq2irZCdvgziZPnMLeTUEO9XUbuW8txq9i51anvlwY8yvMXLvIenNC1xg4KFhMmFzZ8xnpx4nWZZtyRBxaDU99aXm7cQgkVP0VD/eBIDYN4AcB0/Pa7V376m6tRJ5UZh";

    // Short names for external constants
    private static final AxesReference AXES_REFERENCE = AxesReference.EXTRINSIC;
    private static final AngleUnit ANGLE_UNIT = AngleUnit.DEGREES;

    // Cartesian heading constants
    private static final int HEADING_OFFSET = -Heading.FULL_CIRCLE / 4;

    // Tracking config
    private final VuforiaTarget[] CONFIG_TARGETS;
    private final VuforiaTarget CONFIG_PHONE;

    // Dynamic things we need to remember
    private boolean ready = false;
    private boolean capture = false;
    private final Telemetry telemetry;
    private final VuforiaLocalizer.Parameters parameters;
    public VuforiaLocalizer vuforia = null;
    private int trackingTimeout = 100;
    private VuforiaTrackables targetsRaw;
    private final List<VuforiaTrackable> targets = new ArrayList<>();

    // The actual data we care about
    private long timestamp = 0;
    private final int[] location = new int[3];
    private final int[] orientation = new int[3];
    private final HashMap<String, Boolean> targetVisible = new HashMap<>();
    private final HashMap<String, Integer> targetAngle = new HashMap<>();
    private final HashMap<String, Integer> targetIndex = new HashMap<>();
    private ImageFTC image = null;

    private VuforiaFTC(HardwareMap map, Telemetry telemetry, BOT bot, String name, VuforiaLocalizer.CameraDirection direction) {
        CONFIG_TARGETS = VuforiaConfigs.Field();
        CONFIG_PHONE = VuforiaConfigs.Bot(bot);
        this.telemetry = telemetry;

        // Optionally display the live monitor
        if (DEBUG) {
            int cameraMonitorViewId = map.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", map.appContext.getPackageName());
            parameters = new VuforiaLocalizer.Parameters(cameraMonitorViewId);
        } else {
            parameters = new VuforiaLocalizer.Parameters();
        }

        // License
        parameters.vuforiaLicenseKey = VUFORIA_KEY;

        // Choose a camera
        if (map != null && name != null) {
            try {
                parameters.cameraName = map.get(WebcamName.class, name);
            } catch (Exception e) {
                telemetry.log().add(this.getClass().getSimpleName() + "No such camera: " + name + ". Using default camera.");
                parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;
            }
        } else if (direction != null) {
            parameters.cameraDirection = direction;
        } else {
            parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;
        }
        ready = true;
    }

    public VuforiaFTC(HardwareMap map, Telemetry telemetry, BOT bot, String name) {
        this(map, telemetry, bot, name, null);
    }

    public VuforiaFTC(HardwareMap map, Telemetry telemetry, BOT bot, VuforiaLocalizer.CameraDirection direction) {
        this(map, telemetry, bot, null, direction);
    }

    public VuforiaFTC(HardwareMap map, Telemetry telemetry, BOT bot) {
        this(map, telemetry, bot, null, null);
    }

    public void start() {
        if (isRunning() || !ready) {
            return;
        }

        // Init Vuforia
        try {
            vuforia = ClassFactory.getInstance().createVuforia(parameters);
        } catch (Exception e) {
            telemetry.log().add("Unable to start Vuforia: " + e.toString());
            stop();
            ready = false;
            return;
        }

        /*
         * Pre-processed target images from the Vuforia target manager:
         * https://developer.vuforia.com/target-manager.
         */
        targetsRaw = vuforia.loadTrackablesFromAsset(VuforiaConfigs.AssetName);
        com.vuforia.Vuforia.setHint(HINT.HINT_MAX_SIMULTANEOUS_IMAGE_TARGETS, VuforiaConfigs.TargetCount);
        targets.addAll(targetsRaw);

        // Configure target names, locations, rotations and hashmaps
        for (int i = 0; i < VuforiaConfigs.TargetCount; i++) {
            initTrackable(targetsRaw, i);
        }

        // Location and rotation of the image sensor plane relative to the robot
        OpenGLMatrix phoneLocation = positionRotationMatrix(CONFIG_PHONE.location, CONFIG_PHONE.rotation, CONFIG_PHONE.axesOrder);
        for (VuforiaTrackable trackable : targets) {
            ((VuforiaTrackableDefaultListener) trackable.getListener()).setPhoneInformation(phoneLocation, parameters.cameraDirection);
        }

        // Start tracking
        targetsRaw.activate();
    }

    // This doesn't completely disable tracking, but it's a start
    public void stop() {
        if (!isRunning()) {
            return;
        }
        if (targetsRaw != null) {
            targetsRaw.deactivate();
            targetsRaw = null;
        }
        targets.clear();
        vuforia = null;
    }

    public boolean isRunning() {
        return (vuforia != null);
    }

    public void track() {
        if (!isRunning()) {
            return;
        }

        for (VuforiaTrackable trackable : targets) {
            // Per-target visibility (somewhat imaginary but still useful)
            targetVisible.put(trackable.getName(), ((VuforiaTrackableDefaultListener) trackable.getListener()).isVisible());

            // Angle to target, if available
            OpenGLMatrix newPose = ((VuforiaTrackableDefaultListener) trackable.getListener()).getPose();
            if (newPose != null) {
                Orientation poseOrientation = Orientation.getOrientation(newPose, AXES_REFERENCE, AxesOrder.XYZ, ANGLE_UNIT);
                targetAngle.put(trackable.getName(), (int) poseOrientation.secondAngle);
            }

            /*
             * Update the location and orientation track
             *
             * We poll for each trackable so this happens in the loop, but the overall tracking
             * is aggregated among all targets with a defined pose and location. The current
             * field of view will dictate the quality of the track and if one or more targets
             * are present they will be the primary basis for tracking but tracking persists
             * even when the view does not include a target, and is self-consistent when the
             * view includes multiple targets
             */
            OpenGLMatrix newLocation = ((VuforiaTrackableDefaultListener) trackable.getListener()).getUpdatedRobotLocation();
            if (newLocation != null) {
                // Extract our location from the matrix
                for (int i = 0; i < location.length; i++) {
                    location[i] = (int) newLocation.get(i, 3);
                }

                // Calculate the orientation of our view
                Orientation newOrientation = Orientation.getOrientation(newLocation, AXES_REFERENCE, AxesOrder.XYZ, ANGLE_UNIT);
                orientation[0] = (int) newOrientation.firstAngle;
                orientation[1] = (int) newOrientation.secondAngle;
                orientation[2] = (int) newOrientation.thirdAngle;

                // Timestamp the update
                timestamp = System.currentTimeMillis();
            }
        }
    }

    public void display(Telemetry telemetry) {
        if (!isRunning()) {
            return;
        }

        // Is the location track valid?
        telemetry.addData("Valid", isStale() ? "No" : "Yes");

        // List of visible targets (if any)
        StringBuilder visibleStr = new StringBuilder();
        for (String target : targetVisible.keySet()) {
            if (getVisible(target)) {
                if (visibleStr.length() > 0) {
                    visibleStr.append(", ");
                }
                visibleStr.append(target);
            }
        }
        if (visibleStr.length() == 0) {
            visibleStr = new StringBuilder("<None>");
        }
        telemetry.addData("Visible", visibleStr.toString());

        // Angle to each visible target (if any)
        for (String target : targetVisible.keySet()) {
            if (getVisible(target)) {
                telemetry.addData(target + " ∠", getTargetAngle(target) + "°");
            }
        }

        // Raw data from the last location and orientation fix
        telemetry.addData("X/Y Heading", getX() + "/" + getY() + " " + getHeading() + "°");
    }

    /**
     * @return True if frame capture is enabled
     */
    public boolean capturing() {
        return isRunning() && capture;
    }

    /**
     * Enable frame capture -- not done by default because it consumes resources
     */
    public void enableCapture() {
        if (!isRunning()) {
            return;
        }
        vuforia.enableConvertFrameToBitmap();
        capture = true;
    }

    /*
     * Grab the next available frame, if capture is enabled
     */
    public void capture() {
        this.capture(null);
    }

    /*
     * Grab the next available frame, if capture is enabled, optionally saving to disk
     */
    public void capture(final String filename) {
        if (!capturing()) {
            return;
        }

        // Clear the buffer first so upper layers can monitor image != null to ensure freshness
        clearImage();

        vuforia.getFrameOnce(Continuation.create(ThreadPool.getDefault(), new Consumer<Frame>() {
            @Override
            public void accept(Frame frame) {
                Bitmap bitmap = vuforia.convertFrameToBitmap(frame);
                if (bitmap == null) {
                    telemetry.log().add(this.getClass().getSimpleName() + ": No frame captured");
                    return;
                }
                image = new ImageFTC(bitmap);
                if (filename != null) {
                    if (!image.savePNG(filename)) {
                        telemetry.log().add(this.getClass().getSimpleName() + ": Unable to save file: " + filename);
                    }
                }
            }
        }));
    }

    /**
     * @return The most recent available frame, if any
     */
    public ImageFTC getImage() {
        return image;
    }

    /**
     * Clear the captured frame buffer
     */
    public void clearImage() {
        image = null;
    }

    /**
     * Getters
     */
    public HashMap<String, Boolean> getVisible() {
        return targetVisible;
    }

    /**
     * @param target Name of the target of interest.
     * @return True if the target was actively tracked in the last round of VuforiaFTC processing
     */
    public boolean getVisible(String target) {
        return targetVisible.get(target);
    }

    public HashMap<String, Integer> getTargetAngle() {
        return targetAngle;
    }

    /**
     * @param target Name of the target of interest. Valid targets will also be visible per
     *               {@link #getVisible(String)} getVisible(target)}
     * @return The angle to the target's plane relative to the plane of the phone's image sensor
     * (i.e. 0° is dead-on, negative sign denotes right-of-center)
     */
    public int getTargetAngle(String target) {
        return targetAngle.get(target);
    }

    /**
     * @param target Name of the target of interest.
     * @return The Vuforia targetable index for the named target.
     */
    public int getTargetIndex(String target) {
        return targetIndex.get(target);
    }

    /**
     * @param index CONFIG_TARGETS index.
     * @return Live VuforiaTrackable for the indexed target.
     */
    public VuforiaTrackable getTrackable(int index) {
        return targets.get(index);
    }

    /**
     * @param name CONFIG_TARGETS name.
     * @return Live VuforiaTrackable for the named target.
     */
    public VuforiaTrackable getTrackable(String name) {
        return targets.get(getTargetIndex(name));
    }

    /**
     * @return System.currentTimeMillis() as reported at the time of the last location update
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @return True when the last location update was more than trackingTimeout milliseconds ago
     */
    public boolean isStale() {
        return (timestamp + trackingTimeout < System.currentTimeMillis());
    }

    public int[] getLocation() {
        return location;
    }

    public int[] getOrientation() {
        return orientation;
    }

    /**
     * @return The X component of the robot's last known location relative to the field center.
     * Negative values denote blue alliance side of field.
     * <p>
     * This value may be out-of-date. Most uses should include an evaluation of validity based on
     * {@link #isStale() isStale()} or {@link #getTimestamp() getTimestamp()}
     */
    public int getX() {
        return location[0];
    }

    /**
     * @return The Y component of the robot's last known location relative to the field center.
     * Negative sign denotes audience side of field.
     * <p>
     * This value may be out-of-date. Most uses should include an evaluation of validity based on
     * {@link #isStale() isStale()} or {@link #getTimestamp() getTimestamp()}
     */
    public int getY() {
        return location[1];
    }

    /**
     * @return The robot's last known heading relative to the field.
     * <p>
     * This value may be out-of-date. Most uses should include an evaluation of validity based on
     * {@link #isStale() isStale()} or {@link #getTimestamp() getTimestamp()}
     */
    public int getHeading() {
        int heading = orientation[2];
        if (orientation[0] < 0) {
            heading -= Heading.FULL_CIRCLE / 2;
        }
        return Heading.normalize(cartesianToCardinal(heading));
    }

    /**
     * @param x X component of destination in the field plane
     * @param y Y component of destination in the field plane
     * @return Bearing from the current location to {x,y} with respect to field north
     * <p>
     * This value may be out-of-date. Most uses should include an evaluation of validity based on
     * {@link #isStale() isStale()} or {@link #getTimestamp() getTimestamp()}
     */
    public int bearing(float x, float y) {
        return bearing(new int[]{getX(), getY()}, new int[]{(int) x, (int) y});
    }

    /**
     * @param dest X,Y array of destination in the field plane
     * @return Bearing from the current location to {x,y} with respect to field north
     * <p>
     * This value may be out-of-date. Most uses should include an evaluation of validity based on
     * {@link #isStale() isStale()} or {@link #getTimestamp() getTimestamp()}
     */
    public int bearing(int[] dest) {
        return bearing(dest[0], dest[1]);
    }

    /**
     * @param index CONFIG_TARGETS index. Syntax helper for {@link #bearing(float, float)} bearing(int, int)}
     * @return Bearing from the current location to {x,y} with respect to field north
     * <p>
     * This value may be out-of-date. Most uses should include an evaluation of validity based on
     * {@link #isStale() isStale()} or {@link #getTimestamp() getTimestamp()}
     */
    public int bearing(int index) {
        return bearing(CONFIG_TARGETS[index].location[0], CONFIG_TARGETS[index].location[1]);
    }

    /**
     * @param x X component of destination in the field plane
     * @param y Y component of destination in the field plane
     * @return Distance from the current location to {x,y} with respect to field units (millimeters)
     * <p>
     * This value may be out-of-date. Most uses should include an evaluation of validity based on
     * {@link #isStale() isStale()} or {@link #getTimestamp() getTimestamp()}
     */
    public int distance(float x, float y) {
        return distance(new int[]{getX(), getY()}, new int[]{(int) x, (int) y});
    }

    /**
     * @param index CONFIG_TARGETS index. Syntax helper for {@link #distance(float, float)} distance(int, int)}
     * @return Distance from the current location to {x,y} with respect to field units (millimeters)
     * <p>
     * This value may be out-of-date. Most uses should include an evaluation of validity based on
     * {@link #isStale() isStale()} or {@link #getTimestamp() getTimestamp()}
     */
    public int distance(int index) {
        return distance(CONFIG_TARGETS[index].location[0], CONFIG_TARGETS[index].location[1]);
    }

    /**
     * @param dest X,Y array of destination in the field plane
     * @return Distance from the current location to {x,y} with respect to field units (millimeters)
     * <p>
     * This value may be out-of-date. Most uses should include an evaluation of validity based on
     * {@link #isStale() isStale()} or {@link #getTimestamp() getTimestamp()}
     */
    public int distance(int[] dest) {
        return distance(dest[0], dest[1]);
    }

    public void setTrackingTimeout(int timeout) {
        trackingTimeout = timeout;
    }

    public int getTrackingTimeout() {
        return trackingTimeout;
    }

    /**
     * Helpers
     */

    // Bearing from x1,y1 to x2,y2 in degrees
    // Motion from south to north is correlated with increasing Y components in field locations
    private int bearing(int[] src, int[] dest) {
        double bearing = Math.atan2(dest[1] - src[1], dest[0] - src[0]);
        bearing = Math.toDegrees(bearing);
        return Heading.normalize(cartesianToCardinal((int) bearing));
    }

    // Distance from x1,y1 to x2,y2 in field location units (millimeters)
    private int distance(int[] src, int[] dest) {
        return (int) Math.hypot((dest[1] - src[1]), (dest[0] - src[0]));
    }

    // It's like a macro, but for Java
    private OpenGLMatrix positionRotationMatrix(float[] position, float[] rotation, AxesOrder order) {
        return OpenGLMatrix
                .translation(position[0], position[1], position[2])
                .multiplied(Orientation.getRotationMatrix(
                        AXES_REFERENCE, order, ANGLE_UNIT,
                        rotation[0], rotation[1], rotation[2]));
    }

    // More Java blasphemy
    private void initTrackable(VuforiaTrackables trackables, int index) {
        if (index >= trackables.size() || index < 0) {
            RobotLog.a("Invalid VuforiaFTC trackable index: %d", index);
            return;
        }

        // Per-target hashmaps, by name
        targetIndex.put(CONFIG_TARGETS[index].name, index);
        targetVisible.put(CONFIG_TARGETS[index].name, false);
        targetAngle.put(CONFIG_TARGETS[index].name, 0);

        // Location model parameters
        VuforiaTrackable trackable = trackables.get(index);
        trackable.setName(CONFIG_TARGETS[index].name);
        OpenGLMatrix location = positionRotationMatrix(CONFIG_TARGETS[index].location,
                CONFIG_TARGETS[index].rotation, CONFIG_TARGETS[index].axesOrder);
        trackable.setLocation(location);
    }

    private int cartesianToCardinal(int heading) {
        return Heading.FULL_CIRCLE - (heading + HEADING_OFFSET);
    }
}
