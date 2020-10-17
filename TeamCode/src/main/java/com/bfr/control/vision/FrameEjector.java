package com.bfr.control.vision;

import org.opencv.core.Mat;
import org.openftc.easyopencv.OpenCvPipeline;

/**
 * EasyOpenCv only allows for continuous pipeline running on the webcam.
 * A FrameEjector pipeline does no processing, but captures and returns the next frame from
 * the continuous webcam feed.
 */
public class FrameEjector extends OpenCvPipeline {

    private Mat frame;

    @Override
    public synchronized Mat processFrame(Mat input) {

        frame = input.clone();

        return input;
    }

    public synchronized Mat getFrame(){
        return frame;
    }
}
