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
//@Disabled
public class MB1242TestOp extends LinearOpMode {
    //@Override
    public void runOpMode() throws InterruptedException {
        FTCUtilities.setOpMode(this);

        telemetry = FtcDashboard.getInstance().getTelemetry();

        waitForStart();

        while (opModeIsActive()){
            MB1242DistanceSensor lateral1 = FTCUtilities.getHardwareMap().get(MB1242DistanceSensor.class, "dist_left_1");
            MB1242DistanceSensor lateral2 = FTCUtilities.getHardwareMap().get(MB1242DistanceSensor.class, "dist_left_2");
            lateral1.pingDistance();
            sleep(50);
            telemetry.addData("left 1", lateral1.readDistance());

            lateral2.pingDistance();
            sleep(50);
            telemetry.addData("left 2", lateral2.readDistance());
            telemetry.update();


        }
    }
}

