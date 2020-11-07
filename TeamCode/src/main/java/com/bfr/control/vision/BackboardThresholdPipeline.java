package com.bfr.control.vision;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;

public class BackboardThresholdPipeline extends OpenCvPipeline {

    private Mat hsv = new Mat();
    private Mat thresh = new Mat();
    private Mat eroded = new Mat();
    private Mat dilated = new Mat();

    private Scalar min = new Scalar(0, 50, 0);
    private Scalar max = new Scalar(30, 255, 255);

    private static final double HUE_RANGE = 20;

    @Override
    public Mat processFrame(Mat input) {

        Imgproc.cvtColor(input, hsv, Imgproc.COLOR_BGR2HSV);
        Core.inRange(hsv, min, max, thresh);

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(10, 5));

        Imgproc.erode(thresh, eroded, kernel);
        Imgproc.dilate(eroded, dilated, kernel);

        return null;
    }

    public void setHue(double middleHueVal){
        min.set(new double[] {middleHueVal - HUE_RANGE, 50, 0});
        max.set(new double[] {middleHueVal + HUE_RANGE, 255, 255});
    }
}
