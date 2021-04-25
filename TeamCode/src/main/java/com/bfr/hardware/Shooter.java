package com.bfr.hardware;

import com.acmerobotics.dashboard.FtcDashboard;
import com.bfr.control.pidf.RingBuffer;
import com.bfr.control.pidf.ShooterConstants;
import com.bfr.control.pidf.PIDFConfig;
import com.bfr.control.pidf.PIDFController;
import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Shooter
 */
public class Shooter {
    private Motor shooterMotor1, shooterMotor2;

    private SerialServo indexerServo, holderServo;

    private double lastRotations;
    private static final double minsPerNano = 1.6666667E-11;
    private static Telemetry dashboardTelemetry = FtcDashboard.getInstance().getTelemetry();

    //private RunningAvg runningAvg = new RunningAvg(20);
    private RingBuffer<DataPoint> ringBuffer = new RingBuffer(10, new DataPoint(0, FTCUtilities.getCurrentTimeMillis()));
    private PIDFController controller;

    private IndexerState servoState = IndexerState.RESTING;
    private ShooterState shooterState = ShooterState.RESTING;
    private long startTime, elapsedTime, repeatStartTime;
    private final static long WAIT_TIME = 120; //175

    public Shooter() {
        shooterMotor1 = new Motor("s1", 41.0,true);
        shooterMotor2 = new Motor("s2", 41.0,true);

        indexerServo = new SerialServo("indexer", false);
        holderServo = new SerialServo("holder", false);

        indexerServo.mapPosition(-.05, .2);
        holderServo.mapPosition(.3, .5);
        holderServo.setPosition(1);
        indexerServo.setPosition(0);

        shooterMotor1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        shooterMotor2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        lastRotations = shooterMotor1.getRotations();

        PIDFConfig pidfConfig = new PIDFConfig() {
            @Override
            public double kP() {
                return ShooterConstants.kP;
            }

            @Override
            public double kI() {
                return 0;
            }

            @Override
            public double kD() {
                return ShooterConstants.kD;
            }

            //todo make more sophisticated feedforward model.
            @Override
            public double feedForward(double setPoint, double error) {
                if(setPoint == ShooterConstants.standardRPM){
                    return ShooterConstants.standardFeedforward;
                } else if(setPoint == ShooterConstants.powerShotRPM){
                    return ShooterConstants.powerShotFeedforward;
                } else if (setPoint == ShooterConstants.farRPM){
                    return ShooterConstants.farFeedForward;
                } else return 0.0;
            }
        };

        controller = new PIDFController(pidfConfig, ShooterConstants.standardRPM, lastRotations, 3);
    }

    public void setPower(double power){
        shooterMotor1.setPower(power);
        shooterMotor2.setPower(power);
    }

    private enum IndexerState {
        PUSHING1(WAIT_TIME),
        RETRACTING1(2*WAIT_TIME),
        PUSHING2(3*WAIT_TIME),
        RETRACTING2(4*WAIT_TIME),
        PUSHING3(5*WAIT_TIME),
        RESTING(6*WAIT_TIME+100),

        //repetitive indexing
        REPEAT_PUSH(0),
        REPEAT_RETRACT(0);

        public final long endTime;

        IndexerState(long endTime) {
            this.endTime = endTime;
        }
    }

    public enum ShooterState {
        RESTING,
        STANDARD,
        POWERSHOT,
        FAR,
        CHEESE
    }

    /**
     * In powershot mode, this only indexes one shot.
     */
    public void runIndexerServos(){
        startTime = FTCUtilities.getCurrentTimeMillis();
        indexerServo.setPosition(1);
        holderServo.setPosition(0);
        servoState = IndexerState.PUSHING1;
    }

    private void updateIndexerServos(){
        elapsedTime = FTCUtilities.getCurrentTimeMillis() - startTime;
        boolean nextState = (elapsedTime >= servoState.endTime);

        switch (servoState) {
            case RESTING:
                return;
            case PUSHING1:
                if (nextState){
                    indexerServo.setPosition(0);
                    servoState = IndexerState.RETRACTING1;
                }
                break;
            case RETRACTING1:
                if (nextState) {
                    if(shooterState.equals(ShooterState.POWERSHOT) || shooterState.equals((ShooterState.CHEESE))){
                        holderServo.setPosition(1);
                        servoState = IndexerState.RESTING;
                    } else {
                        indexerServo.setPosition(1);
                        servoState = IndexerState.PUSHING2;
                    }
                }
                break;
            case PUSHING2:
                if (nextState){
                    indexerServo.setPosition(0);
                    servoState = IndexerState.RETRACTING2;
                }
                break;
            case RETRACTING2:
                if (nextState) {
                    indexerServo.setPosition(1);
                    servoState = IndexerState.PUSHING3;
                }
                break;
            case PUSHING3:
                if (nextState){
                    indexerServo.setPosition(0);
                    holderServo.setPosition(1);
                    servoState = IndexerState.RESTING;
                }
                break;
            case REPEAT_PUSH:
                if(FTCUtilities.getCurrentTimeMillis() - startTime > WAIT_TIME){
                    servoState = IndexerState.REPEAT_RETRACT;
                    indexerServo.setPosition(0);
                    startTime = FTCUtilities.getCurrentTimeMillis();
                }
                break;
            case REPEAT_RETRACT:
                if(FTCUtilities.getCurrentTimeMillis() - startTime > WAIT_TIME) {
                    servoState = IndexerState.REPEAT_PUSH;
                    indexerServo.setPosition(1);
                    startTime = FTCUtilities.getCurrentTimeMillis();
                }
                break;
        }

    }

    public void repetitiveIndexing(){
        holderServo.setPosition(0);

        indexerServo.setPosition(1);
        servoState = IndexerState.REPEAT_PUSH;
    }

    public void setState(ShooterState state){
        shooterState = state;
        switch (state){
            case RESTING:
                servoState = IndexerState.RESTING;
                holderServo.setPosition(1);
                indexerServo.setPosition(0);
                setPower(0);
                break;
            case STANDARD:
                controller.setSetPoint(ShooterConstants.standardRPM);
                break;
            case POWERSHOT:
                controller.setSetPoint(ShooterConstants.powerShotRPM);
                break;
            case FAR:
                controller.setSetPoint(ShooterConstants.farRPM);
                break;
        }
    }

    public boolean isState(ShooterState state){
        return shooterState.equals(state);
    }

    public boolean areIndexerServosResting(){
        return servoState.equals(IndexerState.RESTING);
    }


    /**
     * Unifies encoder readings with their timestamps
     */
    private class DataPoint{
        final double rotations;
        final long time;

        public DataPoint(double rotations, long time) {
            this.rotations = rotations;
            this.time = time;
        }
    }

    public void update(long bulkReadTimestamp){

        DataPoint current = new DataPoint(shooterMotor1.getRotations(), bulkReadTimestamp);
        DataPoint last = ringBuffer.insert(current);

        double deltaRotations = current.rotations - last.rotations;

        //note conversions to minutes
        double deltaTime = (current.time - last.time) * minsPerNano;


        double rpm = deltaRotations / deltaTime;

        dashboardTelemetry.addData("Shooter RPM", rpm);

        lastRotations = shooterMotor1.getRotations();

        updateIndexerServos();

        if(!isState(ShooterState.RESTING)){
            setPower(controller.getOutput(rpm));
        }
    }
}
