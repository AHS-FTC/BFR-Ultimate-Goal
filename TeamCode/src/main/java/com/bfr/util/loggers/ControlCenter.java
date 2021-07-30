package com.bfr.util.loggers;

import android.annotation.SuppressLint;

import com.bfr.control.path.Position;
import com.bfr.control.vision.MTIVisionBridge;
import com.bfr.hardware.Robot;
import com.bfr.hardware.sensors.DifOdometry;
import com.bfr.util.FTCUtilities;
import com.bfr.util.math.Point;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.ArrayList;
import java.util.List;

/**
 * Control boi thats on the driver station phone. manages telemetry to phone.
 * @author Alex and Andeeeerue
 */
public class ControlCenter {
    private static Telemetry telemetry;
    private static double offset = 0;
    private static DifOdometry difOdometry;
    private static Position position = new Position(0,0,0);
    private static List<String> notices = new ArrayList<>();
    private static int susSensorReads = 0;
    private static Point intakingPoint;
    private static Robot.State robotState;

    private static long lastTime = FTCUtilities.getCurrentTimeMillis();

    public static void setTelemetry(Telemetry telemetry){
        ControlCenter.telemetry = telemetry;
    }

    public static void setIntakingPoint(Point intakingPoint){ControlCenter.intakingPoint = intakingPoint;
    }

    @SuppressLint("DefaultLocale")
    public static void update(){
        long currentTime = FTCUtilities.getCurrentTimeMillis();
        long loopTime = currentTime - lastTime;
        lastTime = currentTime;

        telemetry.addLine(String.format("Heading Offset: %.2f", Math.toDegrees(offset)));
        telemetry.addLine(String.format("Intaking Depth: %.2fin", intakingPoint.y));
        telemetry.addLine(String.format("x: %1$.2f | y: %2$.2f | h: %3$.2f", position.x, position.y, Math.toDegrees(position.heading)));
        telemetry.addLine(String.format("Loop Time: %oms", loopTime));
        telemetry.addLine(String.format("Sus Sensor Reads: %o", susSensorReads));
        telemetry.addLine("Alliance Color: " + FTCUtilities.getAllianceColor());
        telemetry.addLine("Robot State: " + robotState);
        telemetry.addLine(String.format("Angle To Goal: %f degs", MTIVisionBridge.instance.getAngleToGoal()));
        telemetry.addLine("Is goal visible? " + MTIVisionBridge.instance.isGoalVisible());

        for (String notice : notices) {
            telemetry.addLine(notice);
        }

        telemetry.update();
    }

    public static void setDifOdometry(DifOdometry difOdometry) {
        ControlCenter.difOdometry = difOdometry;
    }

    public static void incrementOffset(double increment){
        offset += increment;
        System.out.println(increment);
        difOdometry.incrementHeading(increment);
    }

    public static void incrementSusSensorReads(double increment){
        susSensorReads += increment;
    }

    public static void setPosition(Position position) {
        ControlCenter.position = position;
    }

    public static void addNotice(String string){
        notices.add(string);
    }

    public static void setRobotState(Robot.State state){robotState = state;}

    public static void clearNotices(){
        notices.clear();
    }
}
