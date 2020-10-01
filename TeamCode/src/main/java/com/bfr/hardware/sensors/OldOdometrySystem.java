package com.bfr.hardware.sensors;

import com.bfr.control.path.Position;
import com.bfr.util.loggers.DataLogger;
import com.bfr.util.loggers.Logger;
import com.bfr.util.loggers.MockDataLogger;

import org.firstinspires.ftc.robotcore.internal.android.dx.util.Warning;

/**
 * A collection of Odometers used to monitor robot position.
 *
 * @author Alex Appleby
 */
public class OldOdometrySystem{ //todo redo all of this grossness
    private Position position;

    private OdometerImpl xR, xL, y;

    private static final int DISTANCE_TIME_BUFFER_SIZE = 10;
    //private RingBuffer<Double> distanceBuffer;

    //private double distance = 0.0;

    private double dySum = 0;

    private double xRLast, xLLast, yLast;
    private double yInchesPerDegree;
    private double distanceBetweenYWheels;
    private Position lastPosition;

    private OdometerThread odometerThread;

    private DataLogger logger;

    /**
     * @param xR               The 'first' odometer measuring in the X direction. Should be on the right side of the robot.
     * @param xL               The 'second' odometer measuring in the X direction. Should be on the left side of the robot.
     * @param y                The odometer measuring in the Y direction.
     * @param yInchesPerDegree The amount of displacement given to the y odometer induced by a degree rotation to the robot. Tunable in OdometryCalibrationOpMode
     * //@see edu.ahs.robotics.com.bfr.util.opmodes.OdometryCalibration
     */
    public OldOdometrySystem(OdometerImpl xR, OdometerImpl xL, OdometerImpl y, double yInchesPerDegree, double distanceBetweenYWheels) {
        this.xR = xR;
        this.xL = xL;
        this.y = y;

        position = new Position(0, 0, 0);
        lastPosition = new Position(0, 0, 0);

        this.yInchesPerDegree = yInchesPerDegree;
        this.distanceBetweenYWheels = distanceBetweenYWheels;

        odometerThread = new OdometerThread();

        logger = (DataLogger) Logger.getLogger("odometrySystem");
        if(logger == null){ //create a mock/empty logger if one hasn't been created higher up, so as to not have null pointers. this way we don't log unless specified.
            logger = new MockDataLogger("odometrySystem");
        }

//        distanceBuffer = new RingBuffer<>(DISTANCE_TIME_BUFFER_SIZE, 0.0);
//        velocityTimeBuffer = new RingBuffer<>(DISTANCE_TIME_BUFFER_SIZE, 0L);//type is long
//
//        speedBuffer = new RingBuffer<>(SPEED_TIME_BUFFER_SIZE, 0.0);
//        accelerationTimeBuffer = new RingBuffer<>(SPEED_TIME_BUFFER_SIZE, 0L);
    }

    /**
     * starts thread continuously monitoring position
     */
    public void start() {
        logger.startWriting();
        resetEncoders();
        odometerThread.start();
        logger.startWriting();
    }

    public synchronized void stop() {
        odometerThread.end();
        logger.stopWriting();
    }

    public void setPosition(double x, double y, double heading) {
        position.setPosition(x, y, heading);
        lastPosition.copyFrom(position);
    }

    /**
     * Resets encoders and sets lasts. Package Protected for testing access.
     */
    void resetEncoders() {
        xR.reset();
        xL.reset();
        y.reset();
        xRLast = xR.getDistance();
        xLLast = xL.getDistance();
        yLast = y.getDistance();
    }

    /**
     * Runs central odom math, called continuously by thread and accessible in package for unit testing
     */
    synchronized void updatePosition() {
        double xRReading, xLReading, yReading;
        double dxR, dxL, dyBeforeFactorOut, dyExpected, dy, dx;
        double dxLocal, dyLocal, dyGlobal, dxGlobal;
        double dHeading;


        //set readings from odom
        xRReading = xR.getDistance();
        xLReading = xL.getDistance();
        yReading = y.getDistance();

        //find deltas
        dxR = xRReading - xRLast;
        dxL = xLReading - xLLast;
        dx = (dxR + dxL) / 2.0; //find the average
        dyBeforeFactorOut = yReading - yLast;

        //set lasts
        xRLast = xRReading;
        xLLast = xLReading;
        yLast = yReading;

        //find change in heading
        dHeading = findDeltaHeading(dxR, dxL);

        //factor out the dy expected from rotation of robot
        dyExpected = Math.toDegrees(dHeading) * yInchesPerDegree;

        //find real dy
        dySum += (dyBeforeFactorOut - dyExpected);
        dy = dyBeforeFactorOut - dyExpected;


        // https://docs.google.com/drawings/d/1huTcdb1gKbdQXPz-jUMN_eZfvonkwaVhh-DvI5_L7cY/edit?usp=sharing
        if (dHeading != 0) {//courtesy of 11115, thanks gluten free
            double xRadius = dx / dHeading; // arc length - l = theta*r
            double yRadius = dy / dHeading;

            //find the x and y components of each arc
            dxLocal = (xRadius * Math.sin(dHeading)) - (yRadius * (1 - Math.cos(dHeading)));
            dyLocal = (xRadius * (1 - Math.cos(dHeading))) + (yRadius * Math.sin(dHeading));


        } else { //curve with infinite radius, aka robot moves in a straight line
            dxLocal = dx;
            dyLocal = dy;
        }

        dxGlobal = -Math.sin(position.heading) * dyLocal + Math.cos(position.heading) * dxLocal; //convert to global coords. Recall that 0 rads is in direction of y axis
        dyGlobal = Math.cos(position.heading) * dyLocal + Math.sin(position.heading) * dxLocal;

        position.heading += dHeading;//apply our heading change.

        position.x += dxGlobal;
        position.y += dyGlobal;

        FTCUtilities.addData("dySum", dySum);
        FTCUtilities.addData("heading", position.heading);
        FTCUtilities.addData("YWheelRaw", yReading);
        FTCUtilities.addData("XWheelLRaw", xLReading);
        FTCUtilities.addData("XWheelRRaw", xRReading);


        FTCUtilities.updateTelemetry();


        logger.append("xR", String.valueOf(xRReading));
        logger.append("xL", String.valueOf(xLReading));
        logger.append("yReading", String.valueOf(yReading));
        logger.append("dHeading", String.valueOf(dHeading));
        logger.append("dyBeforeFactorOut", String.valueOf(dyBeforeFactorOut));
        logger.append("dyExpected", String.valueOf(dyExpected));
        logger.append("dy", String.valueOf(dy));

        //logger.append("direction of travel", String.valueOf(velocity.direction()));
        logger.append("x" , String.valueOf(position.x));
        logger.append("y" , String.valueOf(position.y));
        logger.writeLine();
    }

    public synchronized Position getPosition() {
        return position;
    }

    public boolean isRunning() {
        return odometerThread.running;
    }


    /**
     * finds delta heading with two perpendicular odometers
     *
     * @param xR one arc distance
     * @param xL the other arc distance
     * @return the theta of the arcs in radians
     * math: https://www.desmos.com/calculator/1u5ynekr4d
     */
    private double findDeltaHeading(double xR, double xL) {
        return (xR - xL) / distanceBetweenYWheels;//derived from double arcs
    }

    private class OdometerThread extends Thread {
        private final int SLEEP_TIME = 20;

        private volatile boolean running;

        @Override
        public void run() {
            long lastTime = FTCUtilities.getCurrentTimeMillis();

            running = true;
            while (running) {
                updatePosition();

                long deltaTime = FTCUtilities.getCurrentTimeMillis() - lastTime;

                if (deltaTime < SLEEP_TIME) {
                    try {
                        synchronized (this) {
                            this.wait(SLEEP_TIME - deltaTime);
                        }
                    } catch (InterruptedException e) {
                        throw new Warning(e.getMessage()); //transpose error
                    }
                }
                lastTime = FTCUtilities.getCurrentTimeMillis();
            }
        }

        private synchronized void end() {
            running = false;
            this.notifyAll();
        }
    }

}
