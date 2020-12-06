package com.bfr.hardware;

import com.acmerobotics.dashboard.FtcDashboard;
import com.bfr.hardware.sensors.PDFController;
import com.bfr.util.math.RunningAvg;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Shooter
 */
public class Shooter {
    private Motor shooterMotor1, shooterMotor2;
    private double rpm = 0;

    private long lastBulkReadTimeStamp = System.nanoTime();
    private double lastRotations;
    private static final double minsPerNano = 1.6666667E-11;
    private static Telemetry telemetry = FtcDashboard.getInstance().getTelemetry();

    private RunningAvg runningAvg = new RunningAvg(20);
    private PDFController controller;

    public Shooter() {
        shooterMotor1 = new Motor("s1", 41.0,true);
        shooterMotor2 = new Motor("s2", 41.0,true);

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
    }
}
