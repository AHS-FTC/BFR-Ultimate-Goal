package com.bfr.opMode;

import com.bfr.control.vision.VisionSystem;
import com.bfr.util.FTCUtilities;
import com.bfr.util.Network;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;

import java.io.IOException;

@TeleOp(name = "Camera OpMode", group = "Linear Opmode")
//@Disabled
public class CameraOpMode extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        FTCUtilities.setOpMode(this);

        VisionSystem visionSystem = new VisionSystem(true);

        visionSystem.saveCurrentFrame();
        //visionSystem.calibrate();

        waitForStart();

        while (opModeIsActive()){
            visionSystem.runVision();
            //sleep(50);
        }
    }
}

