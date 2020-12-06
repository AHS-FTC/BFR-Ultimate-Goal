package com.bfr.control.vision;

import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.opencv.core.Mat;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;

/**
 * Wrapper class for OpenCvCamera, frameEjector and all the nasty low level vision stuff
 * @see org.openftc.easyopencv.OpenCvCamera
 * @see FrameEjector
 */
public class Cam {
    private FrameEjector frameEjector = new FrameEjector();
    private OpenCvCamera openCvCamera;

    public static final int RES_WIDTH = 800;
    public static final int RES_HEIGHT = 600;
    public static final double MIDDLE_X = RES_WIDTH / 2.0;

    public static final double FOV_H = Math.toRadians(48.5);

    //todo make these gud
    public static final double FOV_V = Math.toRadians(31.6);
    public static final double FOCAL_LENGTH_PX = RES_WIDTH / (2 * Math.tan(FOV_H) / 2.0);
    public static final double FOCAL_LENGTH_PX_2 = RES_HEIGHT / (2 * Math.tan(FOV_V) / 2.0);

    public Cam(String deviceName) {
        HardwareMap hardwareMap = FTCUtilities.getHardwareMap();

        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        openCvCamera  = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, deviceName), cameraMonitorViewId);

        openCvCamera.setPipeline(frameEjector);
    }

    /**
     * Initializes the camera and blocks until it starts running values into the frameEjector
     */
    public void start(){
        openCvCamera.openCameraDeviceAsync(() -> {
            openCvCamera.startStreaming(RES_WIDTH, RES_HEIGHT, OpenCvCameraRotation.UPRIGHT);
        });

        //wait for the camera to initialize
        while (!frameEjector.isInitialized()){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public double findHorizontalAngle(){
        return 0;
    }

    //todo use the more accurate method to do this.
    public static double getAngleFromX(double pixelX){

        //negative is right, ccw; positive is left, cw
        double distanceFromCenter = pixelX - MIDDLE_X;

        double degreesPerPixel = Math.toDegrees(FOV_H) / RES_WIDTH;

        return distanceFromCenter * degreesPerPixel;
    }

    /**
     * Echos frameEjector.copyFrameTo()
     */
    public void copyFrameTo(Mat mat){
        frameEjector.copyFrameTo(mat);
    }

    /**
     * Echos frameEjector.setOutputMat()
     */
    public void setOutputMat(Mat mat){
        frameEjector.setOutputMat(mat);
    }
}
