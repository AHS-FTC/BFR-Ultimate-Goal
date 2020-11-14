package com.bfr.hardware;

import com.acmerobotics.dashboard.FtcDashboard;
import com.bfr.hardware.sensors.PDFController;
import com.bfr.util.FTCUtilities;
import com.bfr.util.math.RunningAvg;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Shooter
 */
public class Shooter {
    private Motor shooterMotor1, shooterMotor2;
    private double rpm = 0;

    SerialServo indexerServo, holderServo;

    private long lastBulkReadTimeStamp = System.nanoTime();
    private double lastRotations;
    private static final double minsPerNano = 1.6666667E-11;
    private static Telemetry telemetry = FtcDashboard.getInstance().getTelemetry();

    private RunningAvg runningAvg = new RunningAvg(20);
    private PDFController controller;

    private State servoState = State.RESTING;
    private long startTime, elapsedTime;
    private final static long WAIT_TIME = 120;

    public Shooter() {
        shooterMotor1 = new Motor("s1", 41.0,true);
        shooterMotor2 = new Motor("s2", 41.0,true);

        indexerServo = new SerialServo("indexer", false);
        holderServo = new SerialServo("holder", false);

        indexerServo.mapPosition(-.05, .2);
        holderServo.mapPosition(.3, .5);
        holderServo.setPosition(1);

        shooterMotor1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        shooterMotor2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        lastRotations = shooterMotor1.getRotations();
        controller = new PDFController(0.002, 0,0.8, 3000, lastRotations);
    }

    public void brakeMotors(){
        shooterMotor1.setPower(0);
        shooterMotor2.setPower(0);
    }

    public void setPower(double power){
        shooterMotor1.setPower(power);
        shooterMotor2.setPower(power);
    }

    private enum State {
        PUSHING1(WAIT_TIME),
        RETRACTING1(2*WAIT_TIME),
        PUSHING2(3*WAIT_TIME),
        RETRACTING2(4*WAIT_TIME),
        PUSHING3(5*WAIT_TIME),
        RESTING(6*WAIT_TIME+100);

        public final long endTime;

        State(long endTime) {
            this.endTime = endTime;
        }
    }

    public void runIndexerServos(){
        startTime = FTCUtilities.getCurrentTimeMillis();
        indexerServo.setPosition(1);
        holderServo.setPosition(0);
        servoState = State.PUSHING1;
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
                    servoState = State.RETRACTING1;
                }
                break;
            case RETRACTING1:
                if (nextState) {
                    indexerServo.setPosition(1);
                    servoState = State.PUSHING2;
                }
                break;
            case PUSHING2:
                if (nextState){
                    indexerServo.setPosition(0);
                    servoState = State.RETRACTING2;
                }
                break;
            case RETRACTING2:
                if (nextState) {
                    indexerServo.setPosition(1);
                    servoState = State.PUSHING3;
                }
                break;
            case PUSHING3:
                if (nextState){
                    indexerServo.setPosition(0);
                    holderServo.setPosition(1);
                    servoState = State.RESTING;
                }
                break;
        }

    }

    public void update(long bulkReadTimestamp){
        double deltaRotations = lastRotations - shooterMotor1.getRotations();

        //note conversions to minutes
        double deltaTime = (bulkReadTimestamp - lastBulkReadTimeStamp) * minsPerNano;

        rpm = -runningAvg.calc(deltaRotations / deltaTime);

        //todo fix negative permanently
        telemetry.addData("rpm", rpm);
        telemetry.update();

        setPower(controller.getOutput(rpm));

        lastRotations = shooterMotor1.getRotations();
        lastBulkReadTimeStamp = bulkReadTimestamp;

        updateIndexerServos();
    }
}
