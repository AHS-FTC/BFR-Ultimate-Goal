package com.bfr.opMode;

import com.bfr.control.vision.VisionException;
import com.bfr.control.vision.VisionSystem2;
import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "Camera OpMode", group = "Linear Opmode")
//@Disabled
public class CameraOpMode extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        FTCUtilities.setOpMode(this);

        VisionSystem2 visionSystem2 = new VisionSystem2(true);

        //visionSystem.saveCurrentFrame();
        visionSystem2.calibrate();

        waitForStart();

        while (opModeIsActive()){
            try {
                visionSystem2.runVision();
            } catch (VisionException e) {
                System.out.println("Vision failed. " + e.getMessage());
                FTCUtilities.addLine("Vision failed."  + e.getMessage());
                FTCUtilities.updateTelemetry();
                visionSystem2.dump();
                e.printStackTrace();
                requestOpModeStop();
            }
        }
    }
}

