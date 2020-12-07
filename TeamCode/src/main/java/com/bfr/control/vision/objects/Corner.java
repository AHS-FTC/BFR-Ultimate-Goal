package com.bfr.control.vision.objects;

import com.bfr.control.vision.VisionUtil;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

public class Corner {
    private Mat mat = new Mat();

    private final Point estimatePoint;
    public Point point;

    public Corner(Point estimatePoint) {
        this.estimatePoint = estimatePoint;
    }

    public void make(Backboard backboard){
        Rect roi = VisionUtil.rectAroundPoint(estimatePoint, 10);
        Mat colorMat = backboard.colorCropped.submat(roi);
        mat.release();
        mat = VisionUtil.getHSVChannel(colorMat, VisionUtil.HSVChannel.VALUE);

        colorMat.release();

        MatOfPoint corners = new MatOfPoint();
        Imgproc.goodFeaturesToTrack(mat, corners, 1, 0.01, 10);
        point = corners.toArray()[0];
        //apply offsets going from image to backboard, then backboard to roi
        point.x += backboard.xOffset;
        point.y += backboard.yOffset;
        point.x += roi.x;
        point.y += roi.y;

        corners.release();
    }

    public Mat getMat(){
        return mat;
    }
}
