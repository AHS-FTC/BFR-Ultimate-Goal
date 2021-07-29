package com.bfr.opMode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.bfr.control.vision.Cam;
import com.bfr.control.vision.MTIVisionBridge;
import com.bfr.control.vision.objects.MTIBackboardDetectionPipeline;
import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.openftc.easyopencv.OpenCvCameraRotation;

@TeleOp(name = "MTI Vision OpMode", group = "Iterative Opmode")
//@Disabled
public class MTIVisionOpMode extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        FTCUtilities.setOpMode(this);
        FTCUtilities.startVisionTuningMode();

        Cam cam = new Cam("shooter_cam", 640, 360, Math.toRadians(87.0));

        cam.startPipelineAsync(new MTIBackboardDetectionPipeline(), OpenCvCameraRotation.UPSIDE_DOWN);
        MTIVisionBridge.instance.setActiveCam(cam);

        waitForStart();

        while (opModeIsActive()){
            telemetry.addData("Is Goal Visible", MTIVisionBridge.instance.isGoalVisible());
            telemetry.addData("Angle To Goal", Math.toDegrees(MTIVisionBridge.instance.getAngleToGoal()));

            telemetry.update();
        }
    }
}

