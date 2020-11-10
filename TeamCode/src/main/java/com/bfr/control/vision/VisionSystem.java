package com.bfr.control.vision;

import com.bfr.util.FTCUtilities;
import com.bfr.util.Network;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Vision system designed for Temerity in 2020-21 UG.
 */
public class VisionSystem {
    private Cam cam;
    private boolean streamMode;

    private Mat currentMat = new Mat();

    private BackboardThresholdPipeline pipeline = new BackboardThresholdPipeline();

    public VisionSystem(boolean streamMode) {
        cam = new Cam("Webcam 1");

        this.streamMode = streamMode;

        cam.start();

        Mat initMat = new Mat();
        cam.copyFrameTo(initMat);

    }

    public void runVision(){
        cam.copyFrameTo(currentMat);

        Mat thresholdMat = pipeline.processFrame(currentMat);

        cam.setOutputMat(thresholdMat);

        thresholdMat.release();
        //FTCUtilities.saveImage(currentMat);
    }

    public void calibrate(){
        cam.copyFrameTo(currentMat);

        Rect cropRect = new Rect(0,0,100,100);

    }

}
