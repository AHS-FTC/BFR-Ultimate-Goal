package com.bfr.hardware;

import com.acmerobotics.dashboard.FtcDashboard;
import com.bfr.control.vision.Cam;
import com.bfr.control.vision.VisionException;
import com.bfr.control.vision.VisionUtil;
import com.bfr.control.vision.objects.Backboard;
import com.bfr.hardware.sensors.IMU;
import com.bfr.util.FTCUtilities;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.sony.SonyGamepadPS4;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.opencv.core.Mat;

import java.util.List;

public class Robot {
    private WestCoast westCoast;
    private Shooter shooter = new Shooter();
    private Intake intake = new Intake();
    private IMU imu;
    private Telemetry dashboardTelemetry = FtcDashboard.getInstance().getTelemetry();

    //vision stuff
    private Cam cam;
    private Backboard backboard = new Backboard();
    private Mat latestFrame = new Mat();

    private List<LynxModule> hubs;

    public Robot() {
        hubs = FTCUtilities.getHardwareMap().getAll(LynxModule.class);

        imu = new IMU("imu", true);
        westCoast = new WestCoast(imu);
        cam = new Cam("Webcam 1");
        cam.start();


        for (LynxModule hub : hubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }

        //calibrate vision
        cam.copyFrameTo(latestFrame);
//        FTCUtilities.saveImage(latestFrame, "yote.png");
        double avgHue = VisionUtil.findAvgOfRegion(latestFrame, 350,190,10,17, VisionUtil.HSVChannel.HUE);
        double sat = VisionUtil.findAvgOfRegion(latestFrame, 350,190,10,17, VisionUtil.HSVChannel.SATURATION);
        double val = VisionUtil.findAvgOfRegion(latestFrame, 350,190,10,17, VisionUtil.HSVChannel.VALUE);

        System.out.println("calibration hue: " + avgHue);
        System.out.println("calibration sat: " + sat);
        System.out.println("calibration val: " + val);

        backboard.calibrate(avgHue, sat, val);
    }

    public Intake getIntake(){return intake;}

    public Shooter getShooter(){return shooter;}

    public void drive(double forward, double turn){
        westCoast.arcadeDrive(forward, turn);
    }

    public void driveStraight(double power, double targetRotations){
        westCoast.startDriveStraight(power, targetRotations);

        while (westCoast.getMode() == WestCoast.Mode.DRIVE_STRAIGHT && FTCUtilities.opModeIsActive()){
            update();
        }
    }

    public void autoAim(){
        cam.copyFrameTo(latestFrame);

        try {
            backboard.make(latestFrame);
            double targetX = backboard.getMiddleX();
            double angleToTarget = Cam.getAngleFromX(targetX);
            westCoast.startTurnLocal(angleToTarget);
            backboard.dump();
        } catch (VisionException e){
            e.printStackTrace();
            System.out.println("frick");
            backboard.dump();
        }
        cam.setOutputMat(backboard.binaryCropped);
    }

    public void turnGlobal(double globalAngle){
        westCoast.startTurnGlobal(globalAngle);

        while (westCoast.getMode() == WestCoast.Mode.POINT_TURN && FTCUtilities.opModeIsActive()){
            update();
        }
    }

    public void turnLocal(double angle){
        turnGlobal(imu.getHeading() + angle);
    }

    public WestCoast getWestCoast() {
        return westCoast;
    }

    public void stopAll(){
        westCoast.brakeMotors();
        shooter.stopShooter();
        intake.changeState(Intake.State.STOPPED);
    }

    /**
     * The update() method contains maintenance stuff
     * it should be called every iteration in any blocking method.
     */
    public void update(){
        long nanosBefore = System.nanoTime();

        //clear sensor cache
        for(LynxModule hub : hubs) {
            hub.clearBulkCache();
        }

        long nanosAfter = System.nanoTime();
        long bulkReadTimestamp = (nanosBefore + nanosAfter) / 2;

        shooter.update(bulkReadTimestamp);
        westCoast.update();

        if(FTCUtilities.isDashboardMode()){
            dashboardTelemetry.update();
        }
        //todo track loop times
    }
}
