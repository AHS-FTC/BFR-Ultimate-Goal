package com.bfr.util.loggers;

import android.annotation.SuppressLint;

import com.bfr.control.path.Position;
import com.bfr.hardware.sensors.DifOdometry;

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

    public static void setTelemetry(Telemetry telemetry){
        ControlCenter.telemetry = telemetry;
    }

    @SuppressLint("DefaultLocale")
    public static void update(){
        telemetry.addLine(String.format("Heading Offset: %.2f", Math.toDegrees(offset)));
        telemetry.addLine(String.format("x: %1$.2f | y: %2$.2f | h: %3$.2f", position.x, position.y, Math.toDegrees(position.heading)));


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

        difOdometry.incrementHeading(increment);
    }

    public static void setPosition(Position position) {
        ControlCenter.position = position;
    }

    public static void addNotice(String string){
        notices.add(string);
    }
}
