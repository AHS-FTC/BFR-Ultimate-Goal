package com.bfr.opMode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.bfr.hardware.sensors.MB1242DistanceSensor;
import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "MB1242 OpMode", group = "Iterative Opmode")
//@Disabled
public class MB1242TestOp extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        FTCUtilities.setOpMode(this);

        telemetry = FtcDashboard.getInstance().getTelemetry();

        MB1242DistanceSensor mb = hardwareMap.get(MB1242DistanceSensor.class, "mb");

        telemetry.addLine(mb.getDeviceName());
        telemetry.update();

        waitForStart();

        while (opModeIsActive()){
            telemetry.addData("distance", mb.getDistance());
            telemetry.update();
        }
    }
}

