package com.bfr.control.vision;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;

import static com.bfr.control.vision.PowershotDetectorConstants.*;

public class PowershotThresholdPipeline extends OpenCvPipeline {

    private Mat blurred = new Mat();
    private Mat thresh = new Mat();

    @Override
    public Mat processFrame(Mat mat) {

        Scalar min = new Scalar(minH, minS, minV);
        Scalar max = new Scalar(maxH, maxS, maxV);

        Imgproc.blur(mat, blurred, new Size(5,5));

        Core.inRange(blurred, min, max, thresh);

        return thresh;
    }
}
