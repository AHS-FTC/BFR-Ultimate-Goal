package com.bfr.hardware;


import com.acmerobotics.dashboard.FtcDashboard;
import com.bfr.control.path.Position;
import com.bfr.control.vision.BackboardDetector;
import com.bfr.control.vision.StackDetector;
import com.bfr.control.vision.VisionException;
import com.bfr.hardware.sensors.DifOdometry;
import com.bfr.hardware.sensors.IMU;
import com.bfr.hardware.sensors.MB1242System;
import com.bfr.hardware.sensors.OdometerImpl;
import com.bfr.hardware.sensors.Odometry;
import com.bfr.util.AllianceColor;
import com.bfr.util.FTCUtilities;
import com.bfr.util.OpModeType;
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
    private SerialServo brolafActuator;
    private IMU imu;
    private Telemetry dashboardTelemetry = FtcDashboard.getInstance().getTelemetry();

    private Odometry odometry;

    private Point shootingPoint = new Point(-42,66); //BLUE
    private Point intakingPoint = new Point(-42,20); //BLUE

    private Point cheesePoint = new Point(-50, 70); //BLUE

    private StackDetector stackDetector;
    private StackDetector.FieldConfiguration fieldConfiguration;
    private BackboardDetector backboardDetector;
    private long stackDetectorStartTime;
    private Position position;

    private boolean nextCycleState = false;

    private List<LynxModule> hubs;
    private State state = Robot.State.FREE;
    private CycleState cycleState = CycleState.TURNING_TO_INTAKE;
    private GoToHomeState homeState = GoToHomeState.TURNING_TO_HOME;
    private GoToCheeseState cheeseState = GoToCheeseState.TURNING_TO_CHEESE;
    private PowershotState powershotState = PowershotState.TURN_TO_SHOT_1;

    //store the state before powershot so we can return to it
    private State previousState = state;

    public enum State {
        FREE,
        AUTO_CYCLE,
        TURN_TO_SHOOT,
        GO_TO_HOME,
        DETECTING_STACK,
        SQUARE_UP,
        CHEESE,
        GO_TO_CHEESE,
        AUTO_POWERSHOT;
    }

    private enum GoToHomeState {
        TURNING_TO_HOME,
    }

    private enum GoToCheeseState {
        TURNING_TO_CHEESE,
        DRIVING_TO_CHEESE
    }

    private enum CycleState {
        TURNING_TO_INTAKE,
        INTAKING,
        WAITING_FOR_SENSORS,
        TURNING_BACK,
        DRIVING_BACK,
        AIMING,
        SHOOTING,
        TURNING_FORWARD,
        DRIVING_FORWARD,
    }

    private enum PowershotState {
        TURN_TO_SHOT_1,
        PSHOT_1,
        TURN_TO_SHOT_2,
        PSHOT_2,
        TURN_TO_SHOT_3,
        PSHOT_3,
    }

    public Robot(Position startingPosition) {
        hubs = FTCUtilities.getHardwareMap().getAll(LynxModule.class);
        position = startingPosition;

        if (FTCUtilities.getAllianceColor().equals(AllianceColor.RED)){
            shootingPoint = new Point(42,66);
            intakingPoint = new Point(42,20);
        }

        ControlCenter.setIntakingPoint(intakingPoint);

        brolafActuator = new SerialServo("brolaf", false);
        brolafActuator.mapPosition(.7, 1);
        brolafActuator.setPosition(0);
        imu = new IMU("imu", true, Math.PI/2);

        wobbleArm.setState(WobbleArm.State.STORED);

        odometry = new DifOdometry(
                new OdometerImpl("l_odo", 1.885, true, 1440.0),
                new OdometerImpl("r_odo", 1.89, false, 1440.0),
                startingPosition, 15.6
        );
        ControlCenter.setPosition(odometry.getPosition());

        mb1242System = new MB1242System(odometry);

        ControlCenter.setDifOdometry((DifOdometry) odometry);

        westCoast = new WestCoast(imu, odometry);

        odometry.start();

        if(FTCUtilities.getOpModeType().equals(OpModeType.AUTO)) {
            stackDetector = new StackDetector();
        } else {
            backboardDetector = new BackboardDetector();
            backboardDetector.start();
        }

        for (LynxModule hub : hubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }

//        //calinew Position(0,0, 0);brate vision
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

    public Odometry getOdometry(){return odometry;}

    public Intake getIntake(){return intake;}

    public Shooter getShooter(){return shooter;}

    public Point getIntakingPoint() {
        return intakingPoint;
    }

    public MB1242System getMb1242System(){
        return mb1242System;
    }

    public SerialServo getBrolafActuator(){return brolafActuator;}

    public StackDetector.FieldConfiguration getFieldConfiguration() {
        return fieldConfiguration;
    }

    public void setState(State state){
        switch(state){
            case FREE:
                return;
            case AUTO_CYCLE:
                westCoast.setRampdownMode(WestCoast.MovementMode.FAST);
                westCoast.startTurnGlobal(-Math.PI / 2);
                shooter.setState(Shooter.ShooterState.STANDARD);

                //if we're returning from a powershot state, don't restart the auto cycling
                if(!this.state.equals(State.AUTO_POWERSHOT)){
                    cycleState = CycleState.TURNING_TO_INTAKE;

                }
                previousState = this.state;
                break;
            case TURN_TO_SHOOT:
                try {
                    double angleToGoal = backboardDetector.getAngleToGoal();

                    westCoast.startTurnLocal(angleToGoal);

                    westCoast.setCheeseHeading(odometry.getPosition().heading + angleToGoal);
                } catch (VisionException e) {
                    ControlCenter.addNotice("Vision Exception: " + e.getMessage());
                    setState(State.FREE);
                }
                setState(previousState);
                break;
            case GO_TO_HOME:
                Position position = odometry.getPosition();
                double angle = FTCMath.ensureIdealAngle(position.getAsPoint().angleTo(intakingPoint), position.heading);
                westCoast.startTurnGlobal(angle);
                homeState = GoToHomeState.TURNING_TO_HOME;
                break;
            case DETECTING_STACK:
                fieldConfiguration = stackDetector.getFieldConfiguration();
                stackDetectorStartTime = FTCUtilities.getCurrentTimeMillis();
                break;
            case SQUARE_UP:
                double squareUpAngle = FTCMath.ensureIdealAngle(Math.toRadians(-90), odometry.getPosition().heading);
                westCoast.startTurnGlobal(squareUpAngle);
                brolafActuator.setPosition(1);
                intake.changeState(Intake.State.IN);
                shooter.setState(Shooter.ShooterState.STANDARD);
                cycleState = CycleState.INTAKING;
                break;
            case CHEESE:
                westCoast.setState(WestCoast.State.CHEESE);
                shooter.setState(Shooter.ShooterState.CHEESE);
                previousState = State.CHEESE;
                break;
            case GO_TO_CHEESE:
                Position currentPosition = odometry.getPosition();
                double angleToCheese = FTCMath.ensureIdealAngle(cheesePoint.angleTo(currentPosition.getAsPoint()), currentPosition.heading);
                westCoast.startTurnGlobal(angleToCheese);
                cheeseState = GoToCheeseState.TURNING_TO_CHEESE;
                break;
            case AUTO_POWERSHOT:
                double powershotAngle;
                if(FTCUtilities.getAllianceColor().equals(AllianceColor.BLUE)){
                    if (previousState.equals(State.CHEESE)){
                        powershotAngle = Math.toRadians(-92);
                    } else {
                        powershotAngle = Math.toRadians(-93);
                    }
                } else {
                    if (previousState.equals(State.CHEESE)){
                        powershotAngle = Math.toRadians(-93);
                    } else {
                        powershotAngle = Math.toRadians(-90);
                    }
                }
                westCoast.startTurnGlobal(powershotAngle);

                powershotState = PowershotState.TURN_TO_SHOT_1;
                break;
        }

        this.state = state;
        ControlCenter.setRobotState(state);
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

        while (westCoast.getState().equals(WestCoast.State.DRIVE_STRAIGHT) && FTCUtilities.opModeIsActive()){
            update();
        }
        System.out.println("ds: " + westCoast.getState().equals(WestCoast.State.DRIVE_STRAIGHT));
        System.out.println("active: " + FTCUtilities.opModeIsActive());
    }


    /**
     * Blocking global turn method for auto only
     * @param globalAngle
     */
    public void turnGlobal(double globalAngle){
        westCoast.startTurnGlobal(globalAngle);

        while (westCoast.getState() == WestCoast.State.POINT_TURN && FTCUtilities.opModeIsActive()){
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
        shooter.setState(Shooter.ShooterState.RESTING);
        intake.changeState(Intake.State.STOPPED);
        backboardDetector.stop();
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

        switch (state){
            case SQUARE_UP:
                state = State.AUTO_CYCLE;
            case AUTO_CYCLE:
                if (FTCUtilities.getController1().areSticksNonZero() && !cycleState.equals(CycleState.INTAKING)){
                    state = Robot.State.FREE;
                    westCoast.setState(WestCoast.State.DRIVER_CONTROL);
                }

                switch (cycleState){
                    case TURNING_TO_INTAKE:
                        if(westCoast.isInDefaultMode()){
                            intake.changeState(Intake.State.IN);

                            brolafActuator.setPosition(1);
                            cycleState = CycleState.INTAKING;
                        }
                        break;
                    case INTAKING:
                        if(checkNextCycleState()){

                            mb1242System.runSystem();

                            cycleState = CycleState.WAITING_FOR_SENSORS;
                        }
                        break;
                    case WAITING_FOR_SENSORS:
                        if (mb1242System.isResting()){

                            //update position, because the mb1242 system updates the odometry;
                            position = odometry.getPosition();

                            dashboardTelemetry.addData("x", position.x);
                            dashboardTelemetry.addData("y", position.y);

                            //calculate the angle and distance to our target point
                            //For the angle, keep in mind the robot is moving backwards

                            double angle = shootingPoint.angleTo(position.getAsPoint());
                            angle = FTCMath.ensureIdealAngle(angle, odometry.getPosition().heading);

                            westCoast.startTurnGlobal(angle);

                            brolafActuator.setPosition(0);

                            cycleState = CycleState.TURNING_BACK;
                        }
                        break;
                    case TURNING_BACK:
                        if (westCoast.isInDefaultMode()){
                            double distance = shootingPoint.distanceTo(position);

                            westCoast.startDriveStraight(.9, distance, WestCoast.Direction.REVERSE);
                            cycleState = CycleState.DRIVING_BACK;
                        }
                        break;
                    case DRIVING_BACK:
                        if (westCoast.isInDefaultMode()){
                            intake.changeState(Intake.State.STOPPED);
                            if(shooter.isState(Shooter.ShooterState.POWERSHOT)){
                                setState(State.AUTO_POWERSHOT);

                                //when we return to auto cycle from powershots, enter shooting state and skip AIMING
                                cycleState = CycleState.SHOOTING;
                            } else {
                                if (FTCUtilities.getAllianceColor().equals(AllianceColor.BLUE)){
                                    westCoast.startTurnGlobal(Math.toRadians(-82));
                                } else {
                                    westCoast.startTurnGlobal(Math.toRadians(-101));
                                }
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
                        if(shooter.areIndexerServosResting()){
                            westCoast.startTurnGlobal(-Math.PI / 2.0);
                            shooter.setState(Shooter.ShooterState.STANDARD);
                            cycleState = CycleState.TURNING_FORWARD;
                        }
                        break;
                    case TURNING_FORWARD:
                        if (westCoast.isInDefaultMode()){
                            intake.changeState(Intake.State.IN);

                            double distance = shootingPoint.distanceTo(intakingPoint);
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
                }
                break;
            case TURN_TO_SHOOT:
                if(westCoast.isInDefaultMode()){
                    setState(State.FREE);
                }
                break;
            case GO_TO_HOME:
                if (homeState.equals(GoToHomeState.TURNING_TO_HOME)) {
                    if (westCoast.isInDefaultMode()) {
                        intake.changeState(Intake.State.IN);

                        double distance = intakingPoint.distanceTo(odometry.getPosition());
                        westCoast.startDriveStraight(.9, distance, WestCoast.Direction.FORWARDS);

                        state = State.AUTO_CYCLE;
                        cycleState = CycleState.DRIVING_FORWARD;
                    }
                }
                break;
            case DETECTING_STACK:
                if (FTCUtilities.getCurrentTimeMillis() - stackDetectorStartTime > 250){
                    fieldConfiguration = stackDetector.getFieldConfiguration();
                    stackDetectorStartTime = FTCUtilities.getCurrentTimeMillis();
                }
                break;
            case GO_TO_CHEESE:
                switch (cheeseState){
                    case TURNING_TO_CHEESE:
                        if (westCoast.isInDefaultMode()){
                            double distance = cheesePoint.distanceTo(odometry.getPosition());
                            westCoast.startDriveStraight(.5, distance, WestCoast.Direction.REVERSE);

                            cheeseState = GoToCheeseState.DRIVING_TO_CHEESE;
                        }
                        break;
                    case DRIVING_TO_CHEESE:
                        if (westCoast.isInDefaultMode()){
                            setState(State.CHEESE);
                        }
                }
                break;
            case AUTO_POWERSHOT:
                switch (powershotState){
                    case TURN_TO_SHOT_1:
                        if(westCoast.isInDefaultMode()){
                            shooter.runIndexerServos();
                            powershotState = PowershotState.PSHOT_1;
                        }
                        break;
                    case PSHOT_1:
                        if(shooter.areIndexerServosResting()){
                            double angle;
                            if(FTCUtilities.getAllianceColor().equals(AllianceColor.BLUE)){
                                if (previousState.equals(State.CHEESE)){
                                    angle = Math.toRadians(-95);
                                } else {
                                    angle = Math.toRadians(-98);
                                }
                            } else {
                                if (previousState.equals(State.CHEESE)){
                                    angle = Math.toRadians(-88);
                                } else {
                                    angle = Math.toRadians(-84);
                                }
                            }
                            westCoast.startTurnGlobal(angle);
                            powershotState = PowershotState.TURN_TO_SHOT_2;
                        }
                        break;
                    case TURN_TO_SHOT_2:
                        if(westCoast.isInDefaultMode()){
                            shooter.runIndexerServos();
                            powershotState = PowershotState.PSHOT_2;
                        }
                        break;
                    case PSHOT_2:
                        if(shooter.areIndexerServosResting()){
                            double angle;
                            if(FTCUtilities.getAllianceColor().equals(AllianceColor.BLUE)){
                                if (previousState.equals(State.CHEESE)){
                                    angle = Math.toRadians(-103);
                                } else {
                                    angle = Math.toRadians(-103);
                                }
                            } else {
                                if (previousState.equals(State.CHEESE)){
                                    angle = Math.toRadians(-84);
                                } else {
                                    angle = Math.toRadians(-81);
                                }
                            }
                            westCoast.startTurnGlobal(angle);
                            powershotState = PowershotState.TURN_TO_SHOT_3;
                        }
                        break;
                    case TURN_TO_SHOT_3:
                        if(westCoast.isInDefaultMode()){
                            shooter.runIndexerServos();
                            shooter.setState(Shooter.ShooterState.POWERSHOT);
                            powershotState = PowershotState.PSHOT_3;
                        }
                        break;
                    case PSHOT_3:
                        if(shooter.areIndexerServosResting()) {
                            setState(previousState);
                            shooter.setState(Shooter.ShooterState.STANDARD);
                        }
                        break;
                }
                break;
            case CHEESE:
                if(shooter.isState(Shooter.ShooterState.POWERSHOT)){
                    setState(State.AUTO_POWERSHOT);
                }
                break;
        }

        westCoast.update();
        wobbleArm.update();
        mb1242System.update();

        if(FTCUtilities.isDashboardMode()){
            //todo remove
            dashboardTelemetry.addLine(previousState.toString());
            dashboardTelemetry.update();
        }
    }
}