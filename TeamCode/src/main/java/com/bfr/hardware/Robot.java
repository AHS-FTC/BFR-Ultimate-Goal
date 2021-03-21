package com.bfr.hardware;


import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.roadrunner.drive.TankDrive;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.bfr.control.path.Position;
import com.bfr.control.vision.Cam;
import com.bfr.control.vision.VisionException;
import com.bfr.control.vision.objects.Backboard;
import com.bfr.hardware.sensors.DifOdometry;
import com.bfr.hardware.sensors.IMU;
import com.bfr.hardware.sensors.MB1242System;
import com.bfr.hardware.sensors.OdometerImpl;
import com.bfr.hardware.sensors.Odometry;
import com.bfr.util.FTCUtilities;
import com.bfr.util.loggers.ControlCenter;
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
    private WobbleArm wobbleArm = new WobbleArm();

    private MB1242System mb1242System;
    private IMU imu;
    private Telemetry dashboardTelemetry = FtcDashboard.getInstance().getTelemetry();

    private Odometry odometry;
    private static final Point shootingPosition = new Point(-42,64);
    private static final Point intakingPosition = new Point(-42,30);

    //vision stuff
    private Cam cam;
    private Backboard backboard = new Backboard();
    private Mat latestFrame = new Mat();

    private Point curPosMb1242 = new Point(0,0);

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
        SHOOTING,
        TURNING_FORWARD,
        DRIVING_FORWARD,

        //powershot modes
        TURN_TO_SHOT_1,
        PSHOT_1,
        TURN_TO_SHOT_2,
        PSHOT_2,
        TURN_TO_SHOT_3,
        PSHOT_3,
    }

    public Robot(Position startingPosition) {
        hubs = FTCUtilities.getHardwareMap().getAll(LynxModule.class);

        imu = new IMU("imu_ch", true, Math.PI/2);
//        cam = new Cam("Webcam 1");
//        cam.start();

        wobbleArm.setState(WobbleArm.State.STORED);

        mb1242System = new MB1242System();

        odometry = new DifOdometry(
                new OdometerImpl("l_odo", 1.885, true, 1440.0),
                new OdometerImpl("r_odo", 1.89, false, 1440.0),
                startingPosition, 15.6
        );

        ControlCenter.setDifOdometry((DifOdometry) odometry);

        westCoast = new WestCoast(imu, odometry);

        odometry.start();

        for (LynxModule hub : hubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }

//        //calibrate vision
//        cam.copyFrameTo(latestFrame);
////        FTCUtilities.saveImage(latestFrame, "yote.png");
//        double avgHue = VisionUtil.findAvgOfRegion(latestFrame, 350,190,10,17, VisionUtil.HSVChannel.HUE);
//        double sat = VisionUtil.findAvgOfRegion(latestFrame, 350,190,10,17, VisionUtil.HSVChannel.SATURATION);
//        double val = VisionUtil.findAvgOfRegion(latestFrame, 350,190,10,17, VisionUtil.HSVChannel.VALUE);
//
//        System.out.println("calibration hue: " + avgHue);
//        System.out.println("calibration sat: " + sat);
//        System.out.println("calibration val: " + val);
//
//        backboard.calibrate(avgHue, sat, val);

    }

    public Intake getIntake(){return intake;}

    public Shooter getShooter(){return shooter;}

    public void setState(State state){
        this.state = state;

        if(state.equals(State.AUTO_CYCLE)){
            westCoast.setRampdownMode(WestCoast.MovementMode.FAST);
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
     * @param targetDistance in inches
     */
    public void driveStraight(double power, double targetDistance, WestCoast.Direction direction){
        westCoast.startDriveStraight(power, targetDistance, direction);

        while (westCoast.getMode().equals(WestCoast.Mode.DRIVE_STRAIGHT) && FTCUtilities.opModeIsActive()){
            update();
        }
        System.out.println("ds: " + westCoast.getMode().equals(WestCoast.Mode.DRIVE_STRAIGHT));
        System.out.println("active: " + FTCUtilities.opModeIsActive());
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
        turnGlobal(odometry.getPosition().heading + angle);
    }

    public WestCoast getWestCoast() {
        return westCoast;
    }

    public WobbleArm getWobbleArm() {
        return wobbleArm;
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
     * Blocks for a fixed amount of time, while still updating the robot every control loop
     * @param ms how long to wait (in ms)
     */
    public void sleep(long ms){
        long startTime = FTCUtilities.getCurrentTimeMillis();

        while (FTCUtilities.getCurrentTimeMillis() - startTime < ms){
            update();
        }
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

        odometry.update();
        shooter.update(bulkReadTimestamp);

        ControlCenter.setPosition(odometry.getPosition());

        if(state.equals(State.AUTO_CYCLE)){
            switch (cycleState){
                case TURNING_TO_INTAKE:
                    if(westCoast.isInDefaultMode()){
                        intake.changeState(Intake.State.IN);

                        cycleState = CycleState.INTAKING;
                    }
                    break;
                case INTAKING:
                    if(checkNextCycleState()){

                        mb1242System.doPings();
                        FTCUtilities.sleep(80);
                        curPosMb1242 = mb1242System.doReads();

                        dashboardTelemetry.addData("x", curPosMb1242.x);
                        dashboardTelemetry.addData("y", curPosMb1242.y);

                        //calculate the angle and distance to our target point
                        //For the angle, keep in mind the robot is moving backwards

                        double angle = shootingPosition.angleTo(curPosMb1242);
                        angle = FTCMath.ensureIdealAngle(angle, odometry.getPosition().heading);

                        westCoast.startTurnGlobal(angle);
                        cycleState = CycleState.TURNING_BACK;
                    }
                    break;
                case TURNING_BACK:
                    if (westCoast.isInDefaultMode()){
                        double distance = shootingPosition.distanceTo(curPosMb1242);

                        westCoast.startDriveStraight(.9, distance, WestCoast.Direction.REVERSE);
                        cycleState = CycleState.DRIVING_BACK;
                    }
                    break;
                case DRIVING_BACK:
                    if (westCoast.isInDefaultMode()){
                        intake.changeState(Intake.State.STOPPED);
                        if(shooter.isPowershotMode()){
                            westCoast.startTurnGlobal(Math.toRadians(-93));
                            cycleState = CycleState.TURN_TO_SHOT_1;
                        } else {
                            //todo make work for other side
                            westCoast.startTurnGlobal(Math.toRadians(-81));
                            cycleState = CycleState.AIMING;
                        }
                    }
                    break;
                case AIMING:
                    if (westCoast.isInDefaultMode()){
                        shooter.runIndexerServos();
                        cycleState = CycleState.SHOOTING;
                    }
                    break;
                case SHOOTING:
                case PSHOT_3:
                    if(shooter.isResting()){
                        westCoast.startTurnGlobal(-Math.PI / 2.0);
                        cycleState = CycleState.TURNING_FORWARD;
                    }
                    break;
                case TURNING_FORWARD:
                    if (westCoast.isInDefaultMode()){
                        intake.changeState(Intake.State.IN);

                        double distance = shootingPosition.distanceTo(intakingPosition);
                        westCoast.startDriveStraight(.9, distance, WestCoast.Direction.FORWARDS);
                        cycleState = CycleState.DRIVING_FORWARD;
                    }
                    break;
                case DRIVING_FORWARD:
                    if(westCoast.isInDefaultMode()){
                        westCoast.startTurnGlobal(-Math.PI / 2.0);
                        cycleState = CycleState.TURNING_TO_INTAKE;
                    }
                    break;
                //powershot states
                case TURN_TO_SHOT_1:
                    if(westCoast.isInDefaultMode()){
                        shooter.runIndexerServos();
                        cycleState = CycleState.PSHOT_1;
                    }
                    break;
                case PSHOT_1:
                    if(shooter.isResting()){
                        westCoast.startTurnGlobal(Math.toRadians(-98));
                        cycleState = CycleState.TURN_TO_SHOT_2;
                    }
                    break;
                case TURN_TO_SHOT_2:
                    if(westCoast.isInDefaultMode()){
                        shooter.runIndexerServos();
                        cycleState = CycleState.PSHOT_2;
                    }
                    break;
                case PSHOT_2:
                    if(shooter.isResting()){
                        westCoast.startTurnGlobal(Math.toRadians(-103));
                        cycleState = CycleState.TURN_TO_SHOT_3;
                    }
                    break;
                case TURN_TO_SHOT_3:
                    if(westCoast.isInDefaultMode()){
                        shooter.runIndexerServos();
                        cycleState = CycleState.PSHOT_3;
                        shooter.setPowershotMode(false);
                    }
                    break;
                    //powershot 3 merged with SHOOTING state

            }
        }

        westCoast.update();
        wobbleArm.update();

        if(FTCUtilities.isDashboardMode()){
            dashboardTelemetry.update();
        }
        //todo track loop times
    }
}
