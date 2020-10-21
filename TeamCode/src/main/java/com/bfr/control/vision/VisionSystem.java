package com.bfr.control.vision;

import com.bfr.util.FTCUtilities;
import com.bfr.util.Network;

import org.opencv.core.Mat;

import java.io.IOException;

/**
 * Vision system designed for Temerity in 2020-21 UG.
 */
public class VisionSystem {
    private Cam cam = new Cam("Webcam 1");
    private boolean streamMode;

    private Mat currentMat = new Mat();

    public VisionSystem(boolean streamMode){
        this.streamMode = streamMode;

        if (streamMode){
            try {
                Network.initTCP();
            } catch (IOException e) {
                FTCUtilities.addLine("TCP Initialization failed, running in non-stream mode");
                FTCUtilities.updateTelemetry();
                e.printStackTrace(System.err);
                this.streamMode = false;
            }
        }

        cam.start();
    }

    public void runVision(){
        cam.copyFrame(currentMat);
        if (streamMode){
            Network.sendTCP(currentMat);
        }
    }

}
