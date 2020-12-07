package com.bfr.control.vision;

import org.opencv.core.Mat;
import org.openftc.easyopencv.OpenCvPipeline;

/**
 * EasyOpenCv only allows for continuous pipeline running on the webcam.
 * A FrameEjector pipeline does no processing, but captures and returns the next frame from
 * the continuous webcam feed.
 *
 * As always, be careful about memory leaks on the native stack.
 */
public class FrameEjector extends OpenCvPipeline {

    private Mat lastFrame = new Mat();
    private Mat outputMat = new Mat();

    private boolean initialized = false;

    @Override
    public synchronized Mat processFrame(Mat input) {
        if(!initialized) initialized = true;
        input.copyTo(lastFrame);

        if (outputMat.empty()){
            input.copyTo(outputMat);
        }

        return outputMat;
    }

    /**
     * Fills a mat with the last frame from the camera.
     * Done in this manner to prevent a memory leak.
     * @param mat This mat will be populated by the camera frame.
     */
    public synchronized void copyFrameTo(Mat mat)
    {
        if(!initialized) throw new Error("Attempted to copy a frame before its camera was initialized");
        lastFrame.copyTo(mat);
    }

    /**
     * Set the mat that gets 'returned' by the pipeline and shows up on the phone screen.
     * @param mat
     */
    public synchronized void setOutputMat(Mat mat){
        mat.copyTo(outputMat);
    }


    public synchronized boolean isInitialized(){
        return initialized;
    }
}
