package com.bfr.control.vision;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

/**
 * Threadsafe data bridge between MTI Backboard Detector running on a seperate thread and the robot which utilizes its data.
 */
public class MTIVisionBridge {
    public static final MTIVisionBridge instance = new MTIVisionBridge();

    private boolean goalVisible = false;

    private Cam activeCam = null;

    private double angleToGoal = Double.NaN;

    public synchronized void setGoalVisible(boolean goalVisible) {
        this.goalVisible = goalVisible;
    }

    public synchronized boolean isGoalVisible() {
        return goalVisible;
    }

    public synchronized double getAngleToGoal(){
        return angleToGoal;
    }

    public synchronized void setActiveCam(Cam activeCam) {
        this.activeCam = activeCam;
    }

    public void setAngleToGoal(MatOfPoint goalContour){
        Rect boundingRect = Imgproc.boundingRect(goalContour);

        double middleX = (boundingRect.tl().x + boundingRect.br().x) / 2.0;

        double angleToGoal = activeCam.getAngleFromX(middleX);

        synchronized (this) {
            this.angleToGoal = angleToGoal;
        }
    }


    private MTIVisionBridge(){}
}
