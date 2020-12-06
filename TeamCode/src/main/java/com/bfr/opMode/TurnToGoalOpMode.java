package com.bfr.opMode;

import com.bfr.control.vision.Cam;
import com.bfr.control.vision.VisionException;
import com.bfr.control.vision.VisionSystem2;
import com.bfr.control.vision.objects.Backboard;
import com.bfr.hardware.Robot;
import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.R;
import org.opencv.core.Mat;

@TeleOp(name = "Turn to Goal OpMode", group = "Iterative Opmode")
//@Disabled
public class TurnToGoalOpMode extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        FTCUtilities.setOpMode(this);

        Cam cam = new Cam("Webcam 1");
        cam.start();

        Robot robot = new Robot();

        waitForStart();
        Backboard backboard = new Backboard();
        Mat mat = new Mat();

        while (opModeIsActive()){
            cam.copyFrameTo(mat);
            if(gamepad1.a){
                try {
                    backboard.make(mat);
                    double targetX = backboard.getMiddleX();
                    double angleToTarget = Cam.getAngleFromX(targetX);
                    robot.turnLocal(angleToTarget);
                } catch (VisionException e){
                    e.printStackTrace();
                    System.out.println("frick");
                    backboard.dump();
                }
            }
            cam.setOutputMat(mat);
        }
    }
}

