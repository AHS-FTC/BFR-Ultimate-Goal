package com.bfr.control.vision.objects;

import com.bfr.control.vision.VisionException;
import com.bfr.control.vision.VisionUtil;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class PotentialBackboard extends VisionObject{
    public final int numSides;
    private MatOfPoint2f approx, contour2f;
    public final MatOfPoint contour;
    public final int index;
    public List<MatOfPoint> subContours = new ArrayList<>();
    public final MatOfPoint approxContour;

    public PotentialBackboard(MatOfPoint contour, int index) {
        this.contour = contour;
        approx = new MatOfPoint2f();
        contour2f = new MatOfPoint2f(contour.toArray());

        double epsilon = 0.017 * Imgproc.arcLength(contour2f, true);

        Imgproc.approxPolyDP(contour2f, approx, epsilon, true);

        numSides = approx.toArray().length;
        approxContour = new MatOfPoint(approx.toArray());
        this.index = index;
    }

    public void release() {
        approx.release();
        contour2f.release();
    }

    @Override
    public void dump() {

    }
}
