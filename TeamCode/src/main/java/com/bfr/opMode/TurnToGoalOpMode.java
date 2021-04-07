package com.bfr.opMode;

import com.acmerobotics.dashboard.FtcDashboard;
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

@TeleOp(name = "Turn to Goal OpMode", group = "Iterative Opmode")
@Disabled
public class TurnToGoalOpMode extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        FTCUtilities.setOpMode(this);

        Cam cam = new Cam("Webcam 1");
        cam.start(OpenCvCameraRotation.UPSIDE_DOWN);

        waitForStart();

        Backboard backboard = new Backboard();
        Mat mat = new Mat();

        while (opModeIsActive()){
            cam.copyFrameTo(mat);
            try {
                backboard.make(mat);
                double targetX = backboard.getMiddleX();
                double angleToTarget = Cam.getAngleFromX(targetX);

                FtcDashboard.getInstance().getTelemetry().addData("target x", targetX);
                FtcDashboard.getInstance().getTelemetry().addData("angle to target", angleToTarget);
                FtcDashboard.getInstance().getTelemetry().update();

                backboard.dump();

            } catch (VisionException e){
                System.out.println("frick");
                e.printStackTrace();
            }

            cam.setOutputMat(backboard.binaryCropped);
        }
    }
}

