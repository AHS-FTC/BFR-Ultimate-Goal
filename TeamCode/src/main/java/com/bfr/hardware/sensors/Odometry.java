package com.bfr.hardware.sensors;

import com.bfr.control.path.Position;

/**
 * Abstract class representing any OdometrySystem
 * A newer, lightweight and single-threaded version of the ol' faithful OdometrySystem class
 * @author Alex Appleby
 */
public abstract class Odometry {
    protected Position position =  new Position(0,0,0);

    protected double trackWidth;

    protected Odometry(double trackWidth, Position startingPosition) {
        this.trackWidth = trackWidth;
        setPosition(startingPosition);
    }

    public Position getPosition() {
        return position;
    }

    public abstract void start();

    public abstract void stop();

    public abstract void update();

    public void setPosition(Position position){
        this.position.copyFrom(position);
    }

    /**
     * finds change in heading with the change of two parallel encoders
     * @param dRight change in right encoder
     * @param dLeft change in left encoder
     * @return the change in robot heading
     * math: https://www.desmos.com/calculator/1u5ynekr4d
     */
    protected double findDeltaHeading(double dRight, double dLeft) {
        return (dRight - dLeft) / trackWidth;//derived from double arcs
    }

    protected double findGlobalX(double localX, double localY){
        return (Math.cos(position.heading) * localX) - (Math.sin(position.heading) * localY);
    }

    protected double findGlobalY(double localX, double localY){
        return (Math.cos(position.heading) * localY) + (Math.sin(position.heading) * localX);
    }

}
