package com.bfr.opMode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.bfr.hardware.sensors.MB1242DistanceSensor;
import com.bfr.hardware.sensors.MB1242System;
import com.bfr.util.FTCUtilities;
import com.bfr.util.math.Point;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "MB1242 OpMode", group = "Iterative Opmode")
@Disabled
public class MB1242TestOp extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        FTCUtilities.setOpMode(this);

        telemetry = FtcDashboard.getInstance().getTelemetry();

//        MB1242System mb1242System = new MB1242System();
//
//        waitForStart();
//
//        while (opModeIsActive()){
//            mb1242System.doPings();
//            sleep(80);
//
//            Point p = mb1242System.doReads();
//            telemetry.addData("x", p.x);
//            telemetry.addData("y", p.y);
//            telemetry.update();
//        }
    }
}

