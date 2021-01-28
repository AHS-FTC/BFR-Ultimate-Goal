package com.bfr.hardware;

import com.acmerobotics.dashboard.FtcDashboard;
import com.bfr.control.vision.Cam;
import com.bfr.control.vision.VisionException;
import com.bfr.control.vision.VisionUtil;
import com.bfr.control.vision.objects.Backboard;
import com.bfr.hardware.sensors.IMU;
import com.bfr.hardware.sensors.MB1242System;
import com.bfr.util.FTCUtilities;
import com.bfr.util.math.FTCMath;
import com.bfr.util.math.Point;
import com.qualcomm.hardware.lynx.LynxModule;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.opencv.core.Mat;

import java.util.List;

public class Robot {
    private WestCoast westCoast;
    private Shooter shooter = new Shooter();
    private Intake intake = new Intake();
    private MB1242System mb1242System;
    private IMU imu;
    private Telemetry dashboardTelemetry = FtcDashboard.getInstance().getTelemetry();

    private static final Point shootingPosition = new Point(-39,64);
    private static final Point intakingPosition = new Point(-39,20);

    //vision stuff
    private Cam cam;
    private Backboard backboard = new Backboard();
    private Mat latestFrame = new Mat();

    private Point currentPosition = new Point(0,0);

    private boolean nextCycleState = false;

    private List<LynxModule> hubs;
    private State state = State.FREE;
    private CycleState cycleState = CycleState.TURNING_TO_INTAKE;

    public enum State {
        FREE,
        AUTO_CYCLE
    }

    private enum CycleState {
        TURNING_TO_INTAKE,
        INTAKING,
        TURNING_BACK,
        DRIVING_BACK,
        AIMING,
        TURNING_FORWARD,
        DRIVING_FORWARD;
    }

    public Robot() {
        hubs = FTCUtilities.getHardwareMap().getAll(LynxModule.class);

        imu = new IMU("imu", true, -Math.PI/2);
        westCoast = new WestCoast(imu);
        cam = new Cam("Webcam 1");
        cam.start();

        mb1242System = new MB1242System();

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

    public void setState(State state){
        this.state = state;

        if(state.equals(State.AUTO_CYCLE)){
            westCoast.startTurnGlobal(-Math.PI / 2);
            cycleState = CycleState.TURNING_TO_INTAKE;
        }
    }

    public void drive(double forward, double turn){
        westCoast.arcadeDrive(forward, turn);
    }

    /**
     * Blocking drive straight method for auto only
     * @param power
     * @param targetRotations
     */
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

    /**
     * Blocking global turn method for auto only
     * @param globalAngle
     */
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
     * Handles manual state changes in state machines.
     * State machines may only advance when nextCycleState was set to true in the current control loop.
     * Otherwise, nextCycleState is reset back to false.
     */
    private boolean checkNextCycleState(){
        if(nextCycleState){
            nextCycleState = false;
            return true;
        }
        return false;
    }

    public void nextCycleState(){
        nextCycleState = true;
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

        if(state.equals(State.AUTO_CYCLE)){
            switch (cycleState){
                case TURNING_TO_INTAKE:
                    if(westCoast.isInDefaultMode()){
                        mb1242System.doPings();

                        try {
                            Thread.sleep(80);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        intake.changeState(Intake.State.IN);

                        currentPosition = mb1242System.doReads();
                        dashboardTelemetry.addData("x", currentPosition.x);
                        dashboardTelemetry.addData("y", currentPosition.y);

                        cycleState = CycleState.INTAKING;
                    }
                    break;
                case INTAKING:
                    if(checkNextCycleState()){
                        intake.changeState(Intake.State.STOPPED);

                        //calculate the angle and distance to our target point
                        //For the angle, keep in mind the robot is moving backwards

                        double angle = shootingPosition.angleTo(currentPosition);
                        angle = FTCMath.ensureIdealAngle(angle, imu.getHeading());

                        westCoast.startTurnGlobal(angle);
                        cycleState = CycleState.TURNING_BACK;
                    }
                    break;
                case TURNING_BACK:
                    if (westCoast.isInDefaultMode()){
                        double distance = shootingPosition.distanceTo(currentPosition);

                        westCoast.startDriveStraight(-.7, -distance);
                        cycleState = CycleState.DRIVING_BACK;
                    }
                    break;
                case DRIVING_BACK:
                    if (westCoast.isInDefaultMode()){

                        //todo make work for other side
                        westCoast.startTurnGlobal(Math.toRadians(-83));
                        cycleState = CycleState.AIMING;
                    }
                    break;
                case AIMING:
                    if (checkNextCycleState() && westCoast.isInDefaultMode()){
                        westCoast.startTurnGlobal(-Math.PI / 2.0);
                        cycleState = CycleState.TURNING_FORWARD;
                    }
                    break;
                case TURNING_FORWARD:
                    if (westCoast.isInDefaultMode()){
                        double distance = shootingPosition.distanceTo(intakingPosition);
                        westCoast.startDriveStraight(0.7, distance);
                        cycleState = CycleState.DRIVING_FORWARD;
                    }
                    break;
                case DRIVING_FORWARD:
                    if(westCoast.isInDefaultMode()){
                        westCoast.startTurnGlobal(-Math.PI / 2.0);
                        cycleState = CycleState.TURNING_TO_INTAKE;
                    }
                    break;
            }
        }

        shooter.update(bulkReadTimestamp);
        westCoast.update();

        if(FTCUtilities.isDashboardMode()){
            dashboardTelemetry.update();
        }
        //todo track loop times
    }
}
