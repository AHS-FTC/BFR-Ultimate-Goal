package com.bfr.opMode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;

@TeleOp(name = "Dif Odometry Logger", group = "Iterative Opmode")
//@Disabled
public class CameraOpMode extends OpMode {
    OpenCvCamera webcam;

    @Override
    public void init() {
        int cameraId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        webcam = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"), cameraId);

        webcam.setPipeline(null);
    }

    @Override
    public void init_loop() {
    }

    @Override
    public void start(){

    }

    @Override
    public void loop() {

    }

    @Override
    public void stop() {

    }

}

