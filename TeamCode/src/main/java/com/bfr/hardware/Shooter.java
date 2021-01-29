package com.bfr.hardware;

import com.acmerobotics.dashboard.FtcDashboard;
import com.bfr.control.pidf.RingBuffer;
import com.bfr.control.pidf.ShooterConstants;
import com.bfr.control.pidf.PIDFConfig;
import com.bfr.control.pidf.PIDFController;
import com.bfr.util.FTCUtilities;
import com.bfr.util.Toggle;
import com.bfr.util.math.RunningAvg;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Shooter
 */
public class Shooter {
    private Motor shooterMotor1, shooterMotor2;
    private double rpm = 0;

    private SerialServo indexerServo, holderServo;

    private long lastBulkReadTimeStamp = System.nanoTime();
    private double lastRotations;
    private static final double minsPerNano = 1.6666667E-11;
    private static Telemetry dashboardTelemetry = FtcDashboard.getInstance().getTelemetry();

    //private RunningAvg runningAvg = new RunningAvg(20);
    private RingBuffer<DataPoint> ringBuffer = new RingBuffer(10, new DataPoint(0, FTCUtilities.getCurrentTimeMillis()));
    private PIDFController controller;

    private IndexerState servoState = IndexerState.RESTING;
    private ShooterState shooterState = ShooterState.RESTING;
    private long startTime, elapsedTime;
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
                return 0.8;

            }
        };

        controller = new PIDFController(pidfConfig, 3000, lastRotations, 3);
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
        RESTING(6*WAIT_TIME+100);

        public final long endTime;

        IndexerState(long endTime) {
            this.endTime = endTime;
        }
    }

    private enum ShooterState {
        RESTING,
        RUNNING
    }

    public void runIndexerServos(){
        startTime = FTCUtilities.getCurrentTimeMillis();
        indexerServo.setPosition(1);
        holderServo.setPosition(0);
        servoState = IndexerState.PUSHING1;
        updateIndexerServos();
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
                    indexerServo.setPosition(1);
                    servoState = IndexerState.PUSHING2;
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
        }

    }

    public void runShooter(){
        shooterState = ShooterState.RUNNING;
        updateShooterState();
    }

    public void stopShooter(){
        shooterState = ShooterState.RESTING;
        setPower(0);
        updateShooterState();
    }

    public boolean isResting(){
        return servoState.equals(IndexerState.RESTING);
    }

//    public void shootPowerShots(){
//        powerShotToggle.canFlip();
//        updateShooterSpeed();
//        updateShooterState();
//    }

//    private void updateShooterSpeed(){
//        if (powerShotToggle.isEnabled()){
//            controller.setKf(.6);
//            controller.setSetPoint(2600);
//        } else {
//            controller.setKf(.8);
//            controller.setSetPoint(3000);
//        }
//    }

    private void updateShooterState(){
        switch (shooterState){
            case RESTING:
                return;
            case RUNNING:
                setPower(controller.getOutput(rpm));
                break;
        }
    }

    private class DataPoint{
        final double rotations;
        final long time;

        public DataPoint(double rotations, long time) {
            this.rotations = rotations;
            this.time = time;
        }
    }

    public boolean isRunning(){
        if(shooterState == ShooterState.RUNNING){
            return true;
        }
        return false;
    }

    public void update(long bulkReadTimestamp){

        DataPoint current = new DataPoint(shooterMotor1.getRotations(), bulkReadTimestamp);
        DataPoint last = ringBuffer.insert(current);

        double deltaRotations = current.rotations - last.rotations;

        //note conversions to minutes
        double deltaTime = (current.time - last.time) * minsPerNano;


        rpm = deltaRotations / deltaTime;

        dashboardTelemetry.addData("Shooter RPM", rpm);

//        setPower(controller.getOutput(rpm));

        lastRotations = shooterMotor1.getRotations();
        lastBulkReadTimeStamp = bulkReadTimestamp;

        updateIndexerServos();
        updateShooterState();
    }
}
