package com.bfr.hardware;

import com.acmerobotics.dashboard.FtcDashboard;
import com.bfr.control.pidf.FastRampdownConstants;
import com.bfr.control.pidf.FastTurnConstants;
import com.bfr.control.pidf.PIDFConfig;
import com.bfr.control.pidf.PIDFController;
import com.bfr.control.pidf.AccurateRampdownConstants;
import com.bfr.control.pidf.StraightConstants;
import com.bfr.control.pidf.AccurateTurnConstants;
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

    private PIDFController rampdownController, turnController, straightController;

    private Mode defaultMode = Mode.IDLE;
    private MovementMode rampdownMode = MovementMode.ACCURATE;
    private MovementMode turnMode = MovementMode.ACCURATE;

    //adds a slight wait after a drive straight (and maybe turn???)
    private boolean waitingState = false;
    private long waitingStartTime = FTCUtilities.getCurrentTimeMillis();
    private static final long WAIT_TIME = 100;

    public enum Mode {
        IDLE,
        DRIVER_CONTROL,
        DRIVE_STRAIGHT,
        POINT_TURN,
    }

    public enum MovementMode {
        ACCURATE,
        FAST;
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

        //initial values don't matter
        rampdownController = new PIDFController(new PIDFConfig() {
            @Override
            public double kP() {
                return AccurateRampdownConstants.kP;
            }

            @Override
            public double kI() {
                return AccurateRampdownConstants.kI;
            }

            @Override
            public double kD() {
                return AccurateRampdownConstants.kD;
            }

            @Override
            public double feedForward(double setPoint, double error) {
                return 0;
            }
        }, 0,0,3);

        //turn PID
        PIDFConfig pidfConfig = new PIDFConfig() {
            @Override
            public double kP() {
                if (turnMode.equals(MovementMode.ACCURATE)){
                    return AccurateTurnConstants.kP;
                }
                return FastTurnConstants.kP;
            }

            @Override
            public double kI() {
                if (turnMode.equals(MovementMode.ACCURATE)) {
                    return AccurateTurnConstants.kI;
                }
                return FastTurnConstants.kI;
            }

            @Override
            public double kD() {
                if (turnMode.equals(MovementMode.ACCURATE)) {
                    return AccurateTurnConstants.kD;
                }
                return FastTurnConstants.kD;
            }

            @Override
            public double feedForward(double setPoint, double error) {
                return 0;
            }
        };

        //initial val doesnt matter
        turnController = new PIDFController(pidfConfig, 0, 0,3);
        turnController.setStabilityThreshold(0.00008);
    }

    private double getAvgDistance(){
        double avgRotations = (leftMotor.getRotations() + rightMotor.getRotations()) / 2.0;
        return avgRotations * INCHES_PER_ROTATION;
    }

    public void resetEncoders(){
        leftMotor.zeroDistance();
        rightMotor.zeroDistance();
    }

    public boolean isInDefaultMode(){
        return mode.equals(defaultMode);
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

    public void setDefaultMode(Mode defaultMode){
        this.defaultMode = defaultMode;
    }

    /**
     * The rampdown controller is tuned to be fast in TeleOp and accurate in Auto. This method switches controllers.
     */
    public void setRampdownMode(MovementMode rampdownMode){
        this.rampdownMode = rampdownMode;
    }

    public void setTurnMode(MovementMode turnMode){
        this.turnMode = turnMode;
    }

    public void update(){
        switch (mode){
            case IDLE:
                break;
            case POINT_TURN:
                double imuHeading = imu.getHeading();
                double angleError = targetAngle - imuHeading;

                if(FTCUtilities.isDashboardMode()){
                    dashboardTelemetry.addData("heading", imuHeading);
                    dashboardTelemetry.addData("Target Heading", targetAngle);
                    dashboardTelemetry.addData("degrees error", Math.toDegrees(angleError));

                    dashboardTelemetry.addData("isStable: ", turnController.isStable());

                    FTCUtilities.updateTelemetry();
                }

                double turnFinishedThreshold;
                if (turnMode.equals(MovementMode.ACCURATE)){
                    turnFinishedThreshold = AccurateTurnConstants.finishedThreshold;
                } else {
                    turnFinishedThreshold = FastTurnConstants.finishedThreshold;
                }

                if(turnController.isStable() && Math.abs(angleError) < turnFinishedThreshold){
                    mode = defaultMode;
                    brakeMotors();
                    break;
                }

                double turnPower = turnController.getOutput(imuHeading);
                double turnMinPower;
                double turnMaxPower;
                if (turnMode.equals(MovementMode.ACCURATE)){
                    turnMinPower = AccurateTurnConstants.minPower;
                    turnMaxPower = AccurateTurnConstants.maxPower;
                } else {
                    turnMinPower = FastTurnConstants.minPower;
                    turnMaxPower = FastTurnConstants.maxPower;
                }
                double finalMaxTurnPower = FTCMath.nearestToZero(turnPower, turnMaxPower);
                double turnFinalPower = FTCMath.furthestFromZero(finalMaxTurnPower, turnMinPower * Math.signum(angleError));

                setTankPower(-turnFinalPower, turnFinalPower);

                break;
            case DRIVE_STRAIGHT:

                if(waitingState){
                    if (FTCUtilities.getCurrentTimeMillis() - waitingStartTime > WAIT_TIME) {
                        mode = defaultMode;
                        waitingState = false;
                    }
                    break;
                }

                double avgDistance = getAvgDistance();
                double distanceError = driveStraightDistance - avgDistance;
                double rampdownPower = rampdownController.getOutput(avgDistance);

                double heading = imu.getHeading();
                double turnCorrection = straightController.getOutput(heading);

                if(FTCUtilities.isDashboardMode()){
                    dashboardTelemetry.addData("distance", avgDistance);
                    dashboardTelemetry.addData("Heading", heading);
                }

                //determine the motor power, finished threshold, etc. depending on the control mode
                double finalPower;
                double finishedThreshold;
                boolean finishedCondition;

                if(rampdownMode.equals(MovementMode.ACCURATE)){
                    //take closest to zero between the default driving power and the PID rampdown power
                    double controlPower = FTCMath.nearestToZero(driveStraightPower, rampdownPower);

                    //implement a minimum power by taking the furthest from zero between the minPower and the controlPower
                    //account for direction with distanceError
                    finalPower = FTCMath.furthestFromZero(AccurateRampdownConstants.minPower * Math.signum(distanceError), controlPower);

                    finishedThreshold = AccurateRampdownConstants.finishedThreshold;
                    finishedCondition = rampdownController.isStable();
                } else { //RampDownMode.FAST
                    if(Math.abs(distanceError) < FastRampdownConstants.distanceThreshold){
                        finalPower = FastRampdownConstants.brakePower * Math.signum(distanceError);
                    } else {
                        finalPower = driveStraightPower;
                    }

                    finishedThreshold = FastRampdownConstants.finishedThreshold;
                    finishedCondition = true;
                }

                if(finishedCondition && Math.abs(distanceError) < finishedThreshold){
                    waitingState = true;
                    waitingStartTime = FTCUtilities.getCurrentTimeMillis();
                    brakeMotors();
                    break;
                }

                setTankPower(finalPower - turnCorrection, finalPower + turnCorrection);
                break;
            case DRIVER_CONTROL:
                arcadeDrive(-driverGamepad.left_stick_y, -driverGamepad.right_stick_x);
                break;
        }
    }
}
