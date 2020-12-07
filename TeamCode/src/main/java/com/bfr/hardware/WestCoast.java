package com.bfr.hardware;

import com.acmerobotics.dashboard.FtcDashboard;
import com.bfr.control.pidf.PIDFConfig;
import com.bfr.control.pidf.PIDFController;
import com.bfr.control.pidf.RampdownConstants;
import com.bfr.util.FTCUtilities;
import com.bfr.util.math.Circle;
import com.bfr.util.math.FTCMath;
import com.bfr.util.math.Line;
import com.bfr.util.math.Point;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * West coast drive / 6 Wheel drive for Aokigahara
 */
public class WestCoast {
    private Motor leftMotor, rightMotor;

    private static Telemetry dashboardTelemetry = FtcDashboard.getInstance().getTelemetry();

    private final double TRACK_WIDTH = 16.16;
    private final double HALF_WIDTH = TRACK_WIDTH / 2.0;

    private Mode mode = Mode.IDLE;
    private final Gamepad driverGamepad;

    private double driveStraightPower = 0.0;
    private double driveStraightRotations = 0.0;

    private PIDFController rampdownController;

    public enum Mode {
        IDLE,
        DRIVER_CONTROL,
        DRIVE_STRAIGHT,
        POINT_TURN,
    }

    public WestCoast() {
        driverGamepad = FTCUtilities.getOpMode().gamepad1;

        leftMotor = new Motor("L", 1440.0,false);
        rightMotor = new Motor("R", 1440.0,false);

        leftMotor.flipEncoder();

        //initial values sorta don't matter here. we update them in driveStraight().
        rampdownController = new PIDFController(new PIDFConfig() {
            @Override
            public double kP() {
                return RampdownConstants.kP;
            }

            @Override
            public double kI() {
                return RampdownConstants.kI;
            }

            @Override
            public double kD() {
                return RampdownConstants.kD;
            }

            @Override
            public double feedForward(double setPoint, double error) {
                return 0;
            }
        }, 0,0,3);

        //todo find stability threshold
        rampdownController.setStabilityThreshold(0.001);
    }

    private double getAvgRotations(){
        return (leftMotor.getRotations() + rightMotor.getRotations()) / 2.0;
    }

    public void resetEncoders(){
        leftMotor.zeroDistance();
        rightMotor.zeroDistance();
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

    public void brakeMotors(){
        leftMotor.setPower(0.0);
        rightMotor.setPower(0.0);
    }

    public void startDriveStraight(double power, double targetRotations){
        driveStraightPower = power;
        driveStraightRotations = targetRotations;
        resetEncoders();

        rampdownController.reset(getAvgRotations());
        rampdownController.setSetPoint(driveStraightRotations);

        mode = Mode.DRIVE_STRAIGHT;
    }

    public Mode getMode() {
        return mode;
    }

    public void update(){
        switch (mode){
            case IDLE:
                break;
            case POINT_TURN:
                //todo
                break;
            case DRIVE_STRAIGHT:
                double avgRotations = getAvgRotations();
                double error = driveStraightRotations - avgRotations;
                double rampdownPower = rampdownController.getOutput(avgRotations);

                if(FTCUtilities.isDebugMode()){
                    dashboardTelemetry.addData("rotations", avgRotations);
                    dashboardTelemetry.addData("drive straight power", driveStraightPower);
                }

                if(rampdownController.isStable() && Math.abs(error) < RampdownConstants.finishedThreshold){
                    mode = Mode.IDLE;
                    brakeMotors();
                    break;
                }

                //take closest to zero between the default driving power and the PID rampdown power
                double motorPower = FTCMath.nearestToZero(driveStraightPower, rampdownPower);
                setTankPower(motorPower, motorPower);
                break;
            case DRIVER_CONTROL:
                arcadeDrive(driverGamepad.left_stick_y, driverGamepad.left_stick_x);
                break;
        }
    }
}
