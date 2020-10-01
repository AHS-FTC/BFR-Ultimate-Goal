package com.bfr.hardware.sensors;

import com.bfr.control.path.Position;


/**
 * Odometry implementation for a 'differential drive', in FTC this will generally be a 6wd.
 * @author Alex and Andrew, 16896
 *     ____  __    ___   ________ __    __________  ____  _________________   ____  ____  ____  ____  _____________________
 *    / __ )/ /   /   | / ____/ //_/   / ____/ __ \/ __ \/ ____/ ___/_  __/  / __ \/ __ \/ __ )/ __ \/_  __/  _/ ____/ ___/
 *   / __  / /   / /| |/ /   / ,<     / /_  / / / / /_/ / __/  \__ \ / /    / /_/ / / / / __  / / / / / /  / // /    \__ \
 *  / /_/ / /___/ ___ / /___/ /| |   / __/ / /_/ / _, _/ /___ ___/ // /    / _, _/ /_/ / /_/ / /_/ / / / _/ // /___ ___/ /
 * /_____/_____/_/  |_\____/_/ |_|  /_/    \____/_/ |_/_____//____//_/    /_/ |_|\____/_____/\____/ /_/ /___/\____//____/
 *
 */
public class DifOdometry extends Odometry{

    private Odometer left, right;
    private double lastLeft, lastRight;

    private boolean running = false;

    public DifOdometry(Odometer left, Odometer right, Position startingPosition, double trackWidth) {
        super(trackWidth, startingPosition);
        this.left = left;
        this.right = right;

        lastLeft = left.getDistance();
        lastRight = right.getDistance();
    }

    @Override
    public void update() {
        if (!running){
            return;
        }

        double leftReading = left.getDistance();
        double rightReading = right.getDistance();

        //find encoder deltas
        double dLeft = leftReading - lastLeft;
        double dRight = rightReading - lastRight;

        double travelDist = (dLeft + dRight) / 2.0;

        //set lasts
        lastLeft = leftReading;
        lastRight = rightReading;

        double dHeading = findDeltaHeading(dRight, dLeft);

        double dxLocal, dyLocal;
        if(dHeading != 0.0){
            //find the radius of the arc of travel
            double radius = travelDist / dHeading; // arc length - l = theta*r

            //Derive the local x and y components of our arc of travel:
            //https://docs.google.com/drawings/d/1huTcdb1gKbdQXPz-jUMN_eZfvonkwaVhh-DvI5_L7cY/edit?usp=sharing

            dxLocal = radius * Math.sin(dHeading);
            dyLocal = radius * (1.0 - Math.cos(dHeading));
        } else { //driving in a straight line
            dxLocal = travelDist;
            dyLocal = 0.0;
        }

        double globalX = findGlobalX(dxLocal, dyLocal);
        double globalY = findGlobalY(dxLocal, dyLocal);

        position.x += globalX;
        position.y += globalY;
        position.heading += dHeading;
    }


    @Override
    public void start() {
        running = true;
    }

    @Override
    public void stop() {
        running = false;
    }

}
