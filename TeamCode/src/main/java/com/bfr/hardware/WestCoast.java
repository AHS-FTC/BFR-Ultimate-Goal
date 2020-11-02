package com.bfr.hardware;

import com.bfr.control.path.Position;
import com.bfr.hardware.sensors.DifOdometry;
import com.bfr.hardware.sensors.Odometer;
import com.bfr.hardware.sensors.OdometerImpl;
import com.bfr.hardware.sensors.Odometry;
import com.bfr.util.math.Circle;
import com.bfr.util.math.Line;
import com.bfr.util.math.Point;

/**
 * West coast drive / 6 Wheel drive for Aokigahara
 */
public class WestCoast {
    private Motor leftMotor, rightMotor;
    private Odometer leftOdo, rightOdo;

    private Odometry odometry;
    private final double TRACK_WIDTH = 16.16;
    private final double HALF_WIDTH = TRACK_WIDTH / 2.0;

    public WestCoast() {
        leftMotor = new Motor("L", 0,true);
        rightMotor = new Motor("R", 0,true);

        leftOdo = new OdometerImpl("l_odo", 3.95, false, 1440.0);
        rightOdo = new OdometerImpl("r_odo", 3.95, true, 1440.0);

        odometry = new DifOdometry(leftOdo, rightOdo, Position.origin, TRACK_WIDTH);

        odometry.start();
    }

    /**
     * (prototype) drive protocol for comfy and powerful WCD control
     * @param forward A component that drives in a straight line
     * @param arc A component that arcs the robot
     * @param turn A component that point turns the robot.
     */
    public void gateauDrive(double forward,  double arc, double turn){
        double leftPower;
        double rightPower;

        if(arc == 0){
            leftPower = forward;
            rightPower = forward;
        } else {
            Point targetPoint = new Point(forward, arc);

            Circle driveArc = new Circle(targetPoint, Point.origin, Line.xAxis);

            double smallRadius = driveArc.radius - HALF_WIDTH;
            double largeRadius = driveArc.radius + HALF_WIDTH;

            double driveRatio = smallRadius / largeRadius;

            double largePower = forward;
            double smallPower = forward * driveRatio;

            //determine the direction of the turn by reading the sign of the 'arc' component
            boolean right = (Math.signum(arc) == 1);

            if(right){
                leftPower = largePower;
                rightPower = smallPower;
            } else {
                rightPower = largePower;
                leftPower = smallPower;
            }
        }

        leftPower += turn;
        rightPower -= turn;

        leftMotor.setPower(leftPower);
        rightMotor.setPower(rightPower);
    }

    public void setTankPower(double l, double r){
        leftMotor.setPower(l);
        rightMotor.setPower(r);
    }

    public void arcadeDrive(double forward, double turn){
        leftMotor.setPower(forward - turn);
        rightMotor.setPower(forward + turn);
    }

    /**
     * Not to be confused with brakeMotors()
     */
    public void kill(){
        odometry.stop();
        brakeMotors();
    }

    public void brakeMotors(){
        leftMotor.setPower(0);
        rightMotor.setPower(0);
    }

    public Position getPosition(){
        odometry.update();
        return odometry.getPosition();
    }
}
