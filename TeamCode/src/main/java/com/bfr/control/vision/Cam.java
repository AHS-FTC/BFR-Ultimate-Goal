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

    public Cam(String deviceName) {
        HardwareMap hardwareMap = FTCUtilities.getHardwareMap();

        //int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        openCvCamera  = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, deviceName));

        openCvCamera.setPipeline(frameEjector);
    }

    /**
     * Initializes the camera and blocks until it starts running values into the frameEjector
     */
    public void start(){
        openCvCamera.openCameraDeviceAsync(() -> openCvCamera.startStreaming(1184, 656, OpenCvCameraRotation.UPRIGHT));

        //wait for the camera to initialize
        while (!frameEjector.isInitialized()){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Echos frameEjector.copyFrameTo()
     */
    public void copyFrameTo(Mat mat){
        frameEjector.copyFrameTo(mat);
    }
}
