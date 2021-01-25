package com.bfr.hardware;

import com.acmerobotics.dashboard.FtcDashboard;
import com.bfr.control.pidf.PIDFConfig;
import com.bfr.control.pidf.PIDFController;
import com.bfr.control.pidf.RampdownConstants;
import com.bfr.control.pidf.StraightConstants;
import com.bfr.control.pidf.TurnConstants;
import com.bfr.hardware.sensors.IMU;
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

    public static final double INCHES_PER_ROTATION = 12.2;

    private Mode mode = Mode.IDLE;
    private final Gamepad driverGamepad;

    private double driveStraightPower = 0.0;
    private double driveStraightDistance = 0.0;

    private double targetAngle = 0.0;

    private double initalDriveHeading = 0.0;

    private final IMU imu;

    private PIDFController rampdownController, turnController, straightController ;

    private static Mode defaultMode = Mode.IDLE;

    public enum Mode {
        IDLE,
        DRIVER_CONTROL,
        DRIVE_STRAIGHT,
        POINT_TURN,
    }

    public WestCoast(IMU imu) {
        this.imu = imu;
        driverGamepad = FTCUtilities.getOpMode().gamepad1;

        leftMotor = new Motor("L", 1440.0,false);
        rightMotor = new Motor("R", 1440.0,false);

        leftMotor.flipEncoder();

        straightController = new PIDFController(new PIDFConfig() {
            @Override
            public double kP() {
                return StraightConstants.kP;
            }

            @Override
            public double kI() {
                return StraightConstants.kI;
            }

            @Override
            public double kD() {
                return StraightConstants.kD;
            }

            @Override
            public double feedForward(double setPoint, double error) {
                return 0;
            }
        },0,0,3);


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
        rampdownController.setStabilityThreshold(0.01);

        //turn PID
        PIDFConfig pidfConfig = new PIDFConfig() {
            @Override
            public double kP() {
                return TurnConstants.kP;
            }

            @Override
            public double kI() {
                return TurnConstants.kI;
            }

            @Override
            public double kD() {
                return TurnConstants.kD;
            }

            @Override
            public double feedForward(double setPoint, double error) {
                if(Math.abs(error) < TurnConstants.finishedThreshold || Math.abs(error) > 20){
                    return 0;
                }
                return TurnConstants.minPower * Math.signum(error);
            }
        };

        //constants dont matter
        turnController = new PIDFController(pidfConfig, 0, 0,3);
        turnController.setStabilityThreshold(.005);
    }

    private double getAvgDistance(){
        double avgRotations = (leftMotor.getRotations() + rightMotor.getRotations()) / 2.0;
        return avgRotations * INCHES_PER_ROTATION;
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

    public void startDriverControl(){
        mode = Mode.DRIVER_CONTROL;
    }

    public void startDriveStraight(double power, double targetDistance){
        driveStraightPower = power;
        driveStraightDistance = targetDistance;
        resetEncoders();
        initalDriveHeading = imu.getHeading();
        straightController.reset(initalDriveHeading, initalDriveHeading);

        rampdownController.reset(getAvgDistance(), driveStraightDistance);

        mode = Mode.DRIVE_STRAIGHT;
    }

    public void startTurnGlobal(double globalAngle){
        targetAngle = globalAngle;

        turnController.reset(imu.getHeading(), globalAngle);
        mode = Mode.POINT_TURN;

    }

    public void startTurnLocal(double globalAngle){
        startTurnGlobal(imu.getHeading() + globalAngle);
    }

    public Mode getMode() {
        return mode;
    }

    public static void setDefaultMode(Mode defaultMode){
        WestCoast.defaultMode = defaultMode;
    }

    public void update(){
        switch (mode){
            case IDLE:
                break;
            case POINT_TURN:
                //todo implement a better system for minPower (see drive straight)
                double imuHeading = imu.getHeading();
                double angleError = targetAngle - imuHeading;

                if(FTCUtilities.isDashboardMode()){
                    FTCUtilities.addData("heading", imuHeading);
                    FTCUtilities.addData("Target Heading", targetAngle);
                    FTCUtilities.updateTelemetry();
                }

                if(turnController.isStable() && Math.abs(angleError) < TurnConstants.finishedThreshold){
                    mode = defaultMode;
                    brakeMotors();
                    break;
                }

                double turnPower = turnController.getOutput(imuHeading);

                setTankPower(-turnPower, turnPower);

                break;
            case DRIVE_STRAIGHT:
                double avgDistance = getAvgDistance();
                double distanceError = driveStraightDistance - avgDistance;
                double rampdownPower = rampdownController.getOutput(avgDistance);

                double heading = imu.getHeading();
                double turnCorrection = straightController.getOutput(heading);

                if(FTCUtilities.isDashboardMode()){
                    dashboardTelemetry.addData("distance", avgDistance);
                    dashboardTelemetry.addData("drive straight power", driveStraightPower);
                    dashboardTelemetry.addData("Heading", heading);
                }

                if(rampdownController.isStable() && Math.abs(distanceError) < RampdownConstants.finishedThreshold){
                    mode = defaultMode;
                    brakeMotors();
                    break;
                }

                //take closest to zero between the default driving power and the PID rampdown power
                double controlPower = FTCMath.nearestToZero(driveStraightPower, rampdownPower);

                //implement a minimum power by taking the furthest from zero between the minPower and the controlPower
                //account for direction with distanceError
                double finalPower = FTCMath.furthestFromZero(RampdownConstants.minPower * Math.signum(distanceError), controlPower);

                setTankPower(finalPower - turnCorrection, finalPower + turnCorrection);
                break;
            case DRIVER_CONTROL:
                arcadeDrive(-driverGamepad.left_stick_y, -driverGamepad.right_stick_x);
                break;
        }
    }
}
