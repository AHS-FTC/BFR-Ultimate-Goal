package com.bfr.opMode;

import com.bfr.control.vision.Cam;
import com.bfr.control.vision.VisionException;
import com.bfr.control.vision.VisionSystem2;
import com.bfr.control.vision.objects.Backboard;
import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "Camera OpMode", group = "Linear Opmode")
//@Disabled
public class TurnToGoalOpMode extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        FTCUtilities.setOpMode(this);

        Cam cam = new Cam("Webcam 1");
        cam.start();



        waitForStart();

        while (opModeIsActive()){
            if(gamepad1.a){
                Backboard backboard = new Backboard();


            }
        }
    }
}

