package com.bfr.opMode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.bfr.control.vision.BackboardDetector;
import com.bfr.control.vision.Cam;
import com.bfr.control.vision.VisionException;
import com.bfr.control.vision.VisionSystem2;
import com.bfr.control.vision.objects.Backboard;
import com.bfr.hardware.Robot;
import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.R;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvCameraRotation;

@TeleOp(name = "Backboard OpMode", group = "Iterative Opmode")
//@Disabled
public class BackboardDetectorOpMode extends LinearOpMode {
    @Override
    public void runOpMode() {
        FTCUtilities.setOpMode(this);

        BackboardDetector b = new BackboardDetector();

        waitForStart();
        b.start();

        while (opModeIsActive()){
            try {
                double ang = b.getAngleToGoal();
                System.out.println(ang);
                FtcDashboard.getInstance().getTelemetry().addData("angle to goal", ang);
            } catch (VisionException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}

