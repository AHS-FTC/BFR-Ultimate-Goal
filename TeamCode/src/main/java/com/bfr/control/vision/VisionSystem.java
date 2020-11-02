package com.bfr.control.vision;

import com.bfr.util.FTCUtilities;
import com.bfr.util.Network;

import org.opencv.core.Mat;

import java.io.IOException;

/**
 * Vision system designed for Temerity in 2020-21 UG.
 */
public class VisionSystem {
    private Cam cam;
    private boolean streamMode;

    private Mat currentMat = new Mat();

    public VisionSystem(boolean streamMode) {
        cam = new Cam("Webcam 1");

        this.streamMode = streamMode;

        cam.start();

        if (streamMode) {
            try {
                Network.initTCP();

                Mat initMat = new Mat();
                cam.copyFrameTo(initMat);

                Network.startTCP(initMat);
            } catch (IOException e) {
                FTCUtilities.addLine("TCP Initialization failed, running in non-stream mode");
                FTCUtilities.updateTelemetry();
                e.printStackTrace(System.err);
                this.streamMode = false;
            }
        }

    }

    public void runVision(){
        cam.copyFrameTo(currentMat);
        if (streamMode){
            Network.updateTCPMat(currentMat);
        }
        FTCUtilities.saveImage(currentMat);
    }

}
