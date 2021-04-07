package com.bfr.opMode;

import com.bfr.control.vision.StackDetector;
import com.bfr.control.vision.VisionException;
import com.bfr.control.vision.VisionSystem2;
import com.bfr.util.AllianceColor;
import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "Stack Detecting OpMode", group = "Linear Opmode")
@Disabled
public class StackDetectingOpMode extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        FTCUtilities.setOpMode(this);

        if(gamepad1.b){
            FTCUtilities.setAllianceColor(AllianceColor.RED);
        } else if (gamepad1.x){
            FTCUtilities.setAllianceColor(AllianceColor.BLUE);
        }

        StackDetector sd = new StackDetector();

        telemetry.addLine(FTCUtilities.getAllianceColor().toString());
        telemetry.update();

        waitForStart();

        while (opModeIsActive()){
            StackDetector.FieldConfiguration fc = sd.getFieldConfiguration();

            System.out.println(fc);

            telemetry.addLine(FTCUtilities.getAllianceColor().toString());
            telemetry.addLine(fc.toString());
            telemetry.update();
        }

        sd.stop();
    }
}

