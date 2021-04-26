package com.bfr.hardware;

import com.acmerobotics.dashboard.FtcDashboard;
import com.bfr.control.path.Position;
import com.bfr.control.pidf.FastRampdownConstants;
import com.bfr.control.pidf.FastTurnConstants;
import com.bfr.control.pidf.PIDFConfig;
import com.bfr.control.pidf.PIDFController;
import com.bfr.control.pidf.AccurateRampdownConstants;
import com.bfr.control.pidf.StraightConstants;
import com.bfr.control.pidf.AccurateTurnConstants;
import com.bfr.hardware.sensors.IMU;
import com.bfr.hardware.sensors.Odometry;
import com.bfr.util.FTCUtilities;
import com.bfr.util.math.Circle;
import com.bfr.util.math.FTCMath;
import com.bfr.util.math.Line;
import com.bfr.util.math.Point;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * West coast drive / 6 Wheel drive for Deep Temerity
 */
public class WestCoast {
    private Motor leftMotor, rightMotor;

    private static Telemetry dashboardTelemetry = FtcDashboard.getInstance().getTelemetry();

    private final double TRACK_WIDTH = 16.16;
    private final double HALF_WIDTH = TRACK_WIDTH / 2.0;

    public static final double INCHES_PER_ROTATION = 12.2;

    private State state = State.IDLE;
    private final Gamepad driverGamepad;

    private double driveStraightPower = 0.0;
    private double driveStraightDistance = 0.0;

    private double targetAngle = 0.0;
    private Position startDrivePos = null;

    private double initalDriveHeading = 0.0;

    private final IMU imu;
    private final Odometry odometry;

    private PIDFController rampdownController, turnController, straightController;

    private State defaultState = State.IDLE;
    private MovementMode rampdownMode = MovementMode.ACCURATE;
    private MovementMode turnMode = MovementMode.FAST;

    //adds a slight wait after a drive straight (and maybe turn???)
    private boolean waitingState = false;
    private long waitingStartTime = FTCUtilities.getCurrentTimeMillis();
    private static final long WAIT_TIME = 100;

    private Direction direction;

    public enum State {
        IDLE,
        DRIVER_CONTROL,
        DRIVE_STRAIGHT,
        POINT_TURN,
    }

    public enum MovementMode {
        ACCURATE,
        FAST
    }

    public enum Direction {
        FORWARDS(1),
        REVERSE(-1);

        public final int sign;

        Direction(int sign) {
            this.sign = sign;
        }
    }

    public WestCoast(IMU imu, Odometry odometry) {
        this.imu = imu;
        this.odometry = odometry;
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

        rampdownController.setStabilityThreshold(0.005);
    }

    public PIDFController getTurnController(){
        return turnController;
    }

    private double getDistance(){
        return startDrivePos.distanceTo(odometry.getPosition());

//        double avgRotations = (leftMotor.getRotations() + rightMotor.getRotations()) / 2.0;
//        return avgRotations * INCHES_PER_ROTATION;
    }

    public void resetEncoders(){
        leftMotor.zeroDistance();
        rightMotor.zeroDistance();
    }

    public boolean isInDefaultMode(){
        return state.equals(defaultState);
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
    
    public void setState(State state){
        if (state.equals(State.IDLE)){
            brakeMotors();
        }

        this.state = state;
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
        state = State.DRIVER_CONTROL;
    }

    public void startDriveStraight(double power, double targetDistance, Direction direction){
        this.direction = direction;

        driveStraightPower = power;
        driveStraightDistance = targetDistance;
        startDrivePos = new Position(odometry.getPosition());

        initalDriveHeading = startDrivePos.heading;
        straightController.reset(initalDriveHeading, initalDriveHeading);

        rampdownController.reset(getDistance(), driveStraightDistance);

        state = State.DRIVE_STRAIGHT;
    }

    public void startTurnGlobal(double globalAngle){
        targetAngle = FTCMath.ensureIdealAngle(globalAngle, odometry.getPosition().heading);

        turnController.reset(odometry.getPosition().heading, targetAngle);
        state = State.POINT_TURN;
    }

    public void startTurnLocal(double globalAngle){
        startTurnGlobal(odometry.getPosition().heading + globalAngle);
    }

    public State getState() {
        return state;
    }

    public void setDefaultState(State defaultState){
        this.defaultState = defaultState;
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
        switch (state){
            case IDLE:
                break;
            case POINT_TURN:
                double turnHeading = odometry.getPosition().heading;
                double angleError = targetAngle - turnHeading;

                if(FTCUtilities.isDashboardMode()){
                    dashboardTelemetry.addData("heading", Math.toDegrees(turnHeading));
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
                    state = defaultState;
                    brakeMotors();
                    break;
                }

                double turnPower = turnController.getOutput(turnHeading);
                double turnMinPower;
                double turnMaxPower;
                if (turnMode.equals(MovementMode.ACCURATE)){
                    turnMinPower = AccurateTurnConstants.minPower;
                    turnMaxPower = AccurateTurnConstants.maxPower;
                } else {
                    turnMinPower = FastTurnConstants.minPower;
                    turnMaxPower = FastTurnConstants.maxPower;
                }
                double finalMaxTurnPower = FTCMath.nearestToZero(turnPower, turnMaxPower * Math.signum(angleError));
                double turnFinalPower = FTCMath.furthestFromZero(finalMaxTurnPower, turnMinPower * Math.signum(angleError));

                setTankPower(-turnFinalPower, turnFinalPower);

                break;
            case DRIVE_STRAIGHT:
                if(waitingState){
                    if (FTCUtilities.getCurrentTimeMillis() - waitingStartTime > WAIT_TIME) {
                        state = defaultState;
                        waitingState = false;
                    }
                    break;
                }

                double distance = getDistance();
                double distanceError = driveStraightDistance - distance;
                double rampdownPower = rampdownController.getOutput(distance);

                double heading = odometry.getPosition().heading;
                double turnCorrection = straightController.getOutput(heading);

                if(FTCUtilities.isDashboardMode()){
                    dashboardTelemetry.addData("startX", startDrivePos.x);
                    dashboardTelemetry.addData("startY", startDrivePos.y);

                    dashboardTelemetry.addData("distance", distance);
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
                    dashboardTelemetry.addData("isStable", finishedCondition);

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

                setTankPower((finalPower * direction.sign) - turnCorrection, (finalPower * direction.sign) + turnCorrection);
                break;
            case DRIVER_CONTROL:
                arcadeDrive(-driverGamepad.left_stick_y, -driverGamepad.right_stick_x);
                break;

        }
    }
}
