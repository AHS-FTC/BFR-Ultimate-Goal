package com.bfr.control.vision;

import com.acmerobotics.dashboard.config.Config;
import static com.bfr.control.pidf.ThresholdConstants.*;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;

public class BackboardThresholdPipeline extends OpenCvPipeline {

    //private Mat hsv = new Mat();
    private Mat thresh = new Mat();
    private Mat eroded = new Mat();
    private Mat dilated = new Mat();

    private static final Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 1));

    private static final double HUE_RANGE = 10;

    @Override
    public Mat processFrame(Mat input) {

         Scalar min = new Scalar(min_h, min_s, min_v);
         Scalar max = new Scalar(max_h, max_s, max_v);

        //Imgproc.cvtColor(input, hsv, Imgproc.COLOR_BGR2HSV);
        Core.inRange(input, min, max, thresh);

        //note that hue wraps at 0
        if (min.val[0] < 0) {
            double differenceFromZero = -min.val[0];
            Mat extra = new Mat();
            Core.inRange(input, new Scalar(180 - differenceFromZero, 100, 35), new Scalar(180, 255, 255), extra);

            Core.bitwise_or(thresh, extra, thresh);
            extra.release();
        }

        Imgproc.erode(thresh, eroded, kernel);
        Imgproc.dilate(eroded, dilated, kernel);

        return dilated;
    }

    public void setHue(double middleHueVal){
        //min.set(new double[] {middleHueVal - HUE_RANGE, 50, 0});
        //max.set(new double[] {middleHueVal + HUE_RANGE, 255, 255});
    }
}
