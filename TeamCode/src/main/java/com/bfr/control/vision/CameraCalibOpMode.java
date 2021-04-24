package com.bfr.control.vision;

import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point3;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvCameraRotation;

import java.util.ArrayList;
import java.util.List;

@TeleOp(name = "Camera Calibration OpMode", group = "Linear Opmode")
@Disabled
public class CameraCalibOpMode extends LinearOpMode {

    //todo tune these
    private static final int NUM_CORNERS_HORIZ = 9, NUM_CORNERS_VERT = 6;
    private static final Size size = new Size(NUM_CORNERS_HORIZ, NUM_CORNERS_VERT);
    private static final String deviceName = "Webcam 1";

    @Override
    public void runOpMode() throws InterruptedException {
        FTCUtilities.setOpMode(this);

        Cam cam = new Cam(deviceName, 1920, 1080, -1);
        cam.start(OpenCvCameraRotation.UPSIDE_DOWN);

        waitForStart();

        Mat current = new Mat();
        Mat gray = new Mat();
        MatOfPoint2f corners = new MatOfPoint2f();
        MatOfPoint3f obj = new MatOfPoint3f();

        Mat drawn = new Mat();

        List<MatOfPoint2f> calibMats = new ArrayList<>();

        while (opModeIsActive()){
            cam.copyFrameTo(current);

            Imgproc.cvtColor(current, gray, Imgproc.COLOR_RGB2GRAY);

            for (int j = 0; j < NUM_CORNERS_HORIZ * NUM_CORNERS_VERT; j++)
                obj.push_back(new MatOfPoint3f(new Point3(j / NUM_CORNERS_HORIZ, j % NUM_CORNERS_VERT, 0.0f)));

            boolean found = Calib3d.findChessboardCorners(gray, size, corners, Calib3d.CALIB_CB_ADAPTIVE_THRESH + Calib3d.CALIB_CB_NORMALIZE_IMAGE + Calib3d.CALIB_CB_FAST_CHECK + Calib3d.CALIB_CB_FILTER_QUADS);

            TermCriteria term = new TermCriteria(TermCriteria.EPS | TermCriteria.MAX_ITER, 30, 0.1);
            Imgproc.cornerSubPix(gray, corners, new Size(11, 11), new Size(-1, -1), term);

            gray.copyTo(drawn);

            Calib3d.drawChessboardCorners(drawn, size, corners, found);

            cam.setOutputMat(drawn);

            if(gamepad1.a && found){
                calibMats.add(corners);

                sleep(1000);
            }
        }
        //Calib3d.calibrateCamera();
    }
}

