package com.bfr.opMode;

import com.bfr.control.vision.VisionSystem;
import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;

@TeleOp(name = "Camera OpMode", group = "Linear Opmode")
//@Disabled
public class CameraOpMode extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        FTCUtilities.setOpMode(this);
        VisionSystem visionSystem = new VisionSystem(true);

        waitForStart();

        while (opModeIsActive()){
            try {
                visionSystem.runVision();
            } catch (Throwable t){
                t.printStackTrace(System.err);
            }
        }
    }
}

