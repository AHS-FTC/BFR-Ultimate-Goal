package com.bfr.opMode;

import com.bfr.control.vision.StackDetector;
import com.bfr.control.vision.VisionException;
import com.bfr.control.vision.VisionSystem2;
import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "Stack Detecting OpMode", group = "Linear Opmode")
//@Disabled
public class StackDetectingOpMode extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        FTCUtilities.setOpMode(this);

        StackDetector sd = new StackDetector();

        waitForStart();

        while (opModeIsActive()){
            System.out.println(sd.getFieldConfiguration());
        }

        sd.stop();
    }
}

