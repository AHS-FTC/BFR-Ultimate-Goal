package com.bfr.control.vision;

import com.bfr.control.pidf.ThresholdConstants;
import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.ExposureControl;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvPipeline;
import org.openftc.easyopencv.OpenCvWebcam;

import java.util.concurrent.TimeUnit;

/**
 * Wrapper class for OpenCvCamera, frameEjector and all the nasty low level vision stuff
 * @see org.openftc.easyopencv.OpenCvCamera
 * @see FrameEjector
 */
public class Cam {
    private FrameEjector frameEjector = new FrameEjector();
    private OpenCvWebcam openCvCamera;

    private boolean streaming = false;

    public final int width;
    public final int height;
    public final double middleX;

    public final double fovH; //= Math.toRadians(87.0);

    public Cam(String deviceName, int width, int height, double fovH) {
        this.width = width;
        this.height = height;
        this.fovH = fovH;

        middleX = width / 2.0;

        HardwareMap hardwareMap = FTCUtilities.getHardwareMap();

        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        openCvCamera  = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, deviceName), cameraMonitorViewId);

        openCvCamera.setPipeline(frameEjector);

    }

    /**
     * Initializes the camera and blocks until it starts running values into the frameEjector
     */
    public void start(OpenCvCameraRotation rot){
        openCvCamera.openCameraDeviceAsync(() -> {
            openCvCamera.startStreaming(width, height, rot);
        });

        //wait for the camera to initialize
        while (!frameEjector.isInitialized()){
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        streaming = true;
    }

    public void startPipelineAsync(OpenCvPipeline pipeline, OpenCvCameraRotation rot) {
        openCvCamera.setPipeline(pipeline);
        openCvCamera.openCameraDeviceAsync(() -> {
            openCvCamera.startStreaming(width, height, rot);
        });
    }

    //todo use the more accurate method to do this.
    public double getAngleFromX(double pixelX){

        //positive is left, ccw; negative is right, cw
        double distanceFromCenter = middleX - pixelX;

        double radiansPerPixel = fovH / width;

        return distanceFromCenter * radiansPerPixel;
    }

    public boolean isStreaming(){
        return streaming;
    }

    public void stop(){
        openCvCamera.stopStreaming();
        streaming = false;
    }

    /**
     * Echos frameEjector.copyFrameTo()
     * Returns HSV Mat.
     */
    public void copyFrameTo(Mat mat){
        Mat hsv = new Mat();
        frameEjector.copyFrameTo(hsv);
        Imgproc.cvtColor(hsv, hsv, Imgproc.COLOR_RGB2HSV);
        hsv.copyTo(mat);
        hsv.release();
    }

    /**
     * Echos frameEjector.setOutputMat() and converts to HSV
     */
    public void setOutputMat(Mat mat){
        frameEjector.setOutputMat(mat);
    }
}
