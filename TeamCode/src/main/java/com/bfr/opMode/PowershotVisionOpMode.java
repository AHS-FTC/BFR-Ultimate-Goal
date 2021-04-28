package com.bfr.opMode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.bfr.control.vision.BackboardDetector;
import com.bfr.control.vision.VisionException;
import com.bfr.control.vision.objects.Powershots;
import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import java.util.Map;

@TeleOp(name = "Powershot Vision OpMode", group = "Iterative Opmode")
//@Disabled
public class PowershotVisionOpMode extends LinearOpMode {
    @Override
    public void runOpMode() {
        FTCUtilities.setOpMode(this);

        BackboardDetector b = new BackboardDetector();

        waitForStart();
        b.start();

        while (opModeIsActive()){
            try {
                Map<Powershots.Position, Double> anglesToPowershots = b.getAnglesToPowershots();

                telemetry.addData("Left", Math.toDegrees(anglesToPowershots.get(Powershots.Position.LEFT)));
                telemetry.addData("Mid", Math.toDegrees(anglesToPowershots.get(Powershots.Position.MID)));
                telemetry.addData("Right", Math.toDegrees(anglesToPowershots.get(Powershots.Position.RIGHT)));
            } catch (VisionException e) {
                System.out.println(e.getMessage());
            }
            telemetry.update();
        }
    }
}

