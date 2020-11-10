package com.bfr.control.vision;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.List;

public class VisionUtil {

    public enum  HSVChannel{
        HUE(0),
        SATURATION(1),
        VALUE(2);

        public final int index;

        HSVChannel(int index) {
            this.index = index;
        }
    }

    /**
     * Finds the average value of a channel in a particular region of a Mat.
     * Input mat must be in HSV form.
     * @param mat input
     * @param x the x value of the top right corner
     * @param y the x value of the top right corner
     * @param width region width
     * @param height region height
     * @param hsvChannel the channel being averaged.
     */
    public static double findAvgOfRegion(Mat mat, int x, int y, int width, int height, HSVChannel hsvChannel){
        Rect cropRect = new Rect(x, y, width, height);

        Mat roi = mat.submat(cropRect);

        List<Mat> channels = new ArrayList<>();

        Core.split(roi, channels);

        Mat channel = channels.get(hsvChannel.index);
        Scalar meanScalar = Core.mean(channel);

        roi.release();
        channel.release();

        return meanScalar.val[0];
    }


    private VisionUtil() {}
}
