package com.bfr.opMode;

import com.bfr.control.path.Position;
import com.bfr.control.vision.StackDetector;
import com.bfr.hardware.Intake;
import com.bfr.hardware.Robot;
import com.bfr.hardware.Shooter;
import com.bfr.hardware.WestCoast;
import com.bfr.hardware.WobbleArm;
import com.bfr.util.AllianceColor;
import com.bfr.util.AutoTransitioner;
import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import java.io.IOException;

@Autonomous(name="Red Auto", group="Linear OpMode")
//@Disabled
public class RedAuto extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException{
        FTCUtilities.setOpMode(this);
        FTCUtilities.setAllianceColor(AllianceColor.RED);
        Robot robot = new Robot(new Position(8,124, Math.toRadians(90)));

        robot.setState(Robot.State.DETECTING_STACK);
        while(!isStarted()){
            robot.update();
            telemetry.addData("Field Configuration: ", robot.getFieldConfiguration().toString());
            telemetry.update();
        }

        waitForStart();

        robot.setState(Robot.State.FREE);

        StackDetector.FieldConfiguration fieldConfiguration = robot.getFieldConfiguration();

        switch (fieldConfiguration){
            case ZERO:
                path0(robot);
                break;
            case ONE:
                path1(robot);
                break;
            case FOUR:
                path4(robot);
                break;
        }

        try {
            AutoTransitioner.writeJSON(robot.getOdometry().getPosition());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void path0(Robot robot){
        WobbleArm wobbleArm = robot.getWobbleArm();
        Shooter shooter = robot.getShooter();
        Intake intake = robot.getIntake();
        robot.getWestCoast().setTurnMode(WestCoast.MovementMode.FAST);


        wobbleArm.setState(WobbleArm.State.HOLDING);
        robot.driveStraight(0.7, 64, WestCoast.Direction.REVERSE);

//        intake.setNineTailsState(Intake.NineTailsState.DEPLOYED);

        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        robot.sleep(500);
        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        robot.sleep(100);
        wobbleArm.setState(WobbleArm.State.RETRACTING);
        shooter.setState(Shooter.ShooterState.STANDARD);
        robot.sleep(500);

        robot.driveStraight(0.7, 12, WestCoast.Direction.FORWARDS);

        robot.turnGlobal(Math.toRadians(-76));

        shooter.runIndexerServos();

        while (!shooter.areIndexerServosResting()){
            robot.update();
            //wait until indexing is finished
        }
        shooter.setState(Shooter.ShooterState.RESTING);

        robot.turnGlobal(Math.toRadians(-112.5));

        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        robot.driveStraight(0.5, 32, WestCoast.Direction.REVERSE);

        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        robot.sleep(500);
        wobbleArm.setState(WobbleArm.State.HOLDING);
        robot.sleep(500);

        robot.turnGlobal(Math.toRadians(-283));
        robot.driveStraight(0.7, 36, WestCoast.Direction.REVERSE);

        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        robot.sleep(500);
        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        robot.sleep(100);
        wobbleArm.setState(WobbleArm.State.RETRACTING);
        robot.sleep(500);

        robot.turnGlobal(Math.toRadians(-220));
        robot.driveStraight(0.5, 20, WestCoast.Direction.REVERSE);

        robot.turnGlobal(Math.toRadians(-90));
    }

    public static void  path1(Robot robot){
        WestCoast westCoast = robot.getWestCoast();
        WobbleArm wobbleArm = robot.getWobbleArm();
        Shooter shooter = robot.getShooter();
        Intake intake = robot.getIntake();
        westCoast.setTurnMode(WestCoast.MovementMode.FAST);


        wobbleArm.setState(WobbleArm.State.HOLDING);
        robot.driveStraight(0.7, 56, WestCoast.Direction.REVERSE);

        robot.turnGlobal(Math.toRadians(120));

        robot.driveStraight(0.7, 32, WestCoast.Direction.REVERSE);


        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        robot.sleep(500);
        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        robot.sleep(100);
        wobbleArm.setState(WobbleArm.State.RETRACTING);
        shooter.setState(Shooter.ShooterState.STANDARD);
        robot.sleep(500);

        robot.driveStraight(0.7, 32, WestCoast.Direction.FORWARDS);

        robot.turnGlobal(Math.toRadians(279));

        shooter.runIndexerServos();

        while (!shooter.areIndexerServosResting()){
            robot.update();
            //wait until indexing is finished
        }
        shooter.setState(Shooter.ShooterState.RESTING);

        robot.turnGlobal(Math.toRadians(270));

        robot.driveStraight(0.7, 28, WestCoast.Direction.REVERSE);

        robot.turnGlobal(Math.toRadians(223));

        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        robot.driveStraight(0.5, 18, WestCoast.Direction.REVERSE);

        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        robot.sleep(500);
        wobbleArm.setState(WobbleArm.State.STORED);
        robot.sleep(700);

        robot.turnGlobal(Math.toRadians(290));


        shooter.setState(Shooter.ShooterState.STANDARD);

        intake.changeState(Intake.State.IN);
        robot.driveStraight(0.7, 38, WestCoast.Direction.FORWARDS);
        intake.changeState(Intake.State.STOPPED);
        wobbleArm.setState(WobbleArm.State.HOLDING);

        robot.turnGlobal(Math.toRadians(262));

        shooter.runIndexerServos();

        while (!shooter.areIndexerServosResting()){
            robot.update();
            //wait until indexing is finished
        }
        shooter.setState(Shooter.ShooterState.RESTING);

        robot.turnGlobal(Math.toRadians(90));

        robot.driveStraight(0.7, 27, WestCoast.Direction.REVERSE);


        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        robot.sleep(500);
        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        robot.sleep(100);
        wobbleArm.setState(WobbleArm.State.RETRACTING);
        robot.sleep(1000);
        wobbleArm.setState(WobbleArm.State.STORED);

        robot.driveStraight(.5, 10, WestCoast.Direction.FORWARDS);
        robot.turnGlobal(Math.toRadians(-90));


    }

    public static void path4(Robot robot){
        WobbleArm wobbleArm = robot.getWobbleArm();
        Shooter shooter = robot.getShooter();
        Intake intake = robot.getIntake();
        robot.getWestCoast().setTurnMode(WestCoast.MovementMode.FAST);


        wobbleArm.setState(WobbleArm.State.HOLDING);
        robot.driveStraight(0.7, 109, WestCoast.Direction.REVERSE);

//        intake.setNineTailsState(Intake.NineTailsState.DEPLOYED);

        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        robot.sleep(500);
        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        robot.sleep(100);
        wobbleArm.setState(WobbleArm.State.RETRACTING);
        shooter.setState(Shooter.ShooterState.STANDARD);
        robot.sleep(500);

        robot.turnGlobal(Math.toRadians(90));
        robot.driveStraight(0.7, 56, WestCoast.Direction.FORWARDS);

        robot.turnGlobal(Math.toRadians(-76.5));

        shooter.runIndexerServos();

        while (!shooter.areIndexerServosResting()){
            robot.update();
            //wait until indexing is finished
        }
        shooter.setState(Shooter.ShooterState.RESTING);

        robot.turnGlobal(Math.toRadians(-90));

        robot.driveStraight(0.7, 18, WestCoast.Direction.REVERSE);

        robot.turnGlobal(Math.toRadians(-122));

        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
//        intake.setNineTailsState(Intake.NineTailsState.RING_4);

        robot.driveStraight(0.7, 20, WestCoast.Direction.REVERSE);

        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        robot.sleep(500);
        wobbleArm.setState(WobbleArm.State.STORED);
        robot.sleep(1000);

        robot.turnGlobal(Math.toRadians(-42)); //225

        robot.driveStraight(0.5, 6, WestCoast.Direction.FORWARDS);

        shooter.setState(Shooter.ShooterState.FAR);

        wobbleArm.setState(WobbleArm.State.HOLDING);

        intake.changeState(Intake.State.IN);
        robot.turnGlobal(Math.toRadians(-88));

        robot.sleep(1600);
        intake.changeState(Intake.State.STOPPED);
        shooter.runIndexerServos();
        shooter.runIndexerServos();

        robot.sleep(400);

        intake.changeState(Intake.State.IN);

        robot.driveStraight(0.15, 6, WestCoast.Direction.FORWARDS);
        shooter.setState(Shooter.ShooterState.STANDARD);

        robot.driveStraight(.8,20, WestCoast.Direction.FORWARDS);

        robot.sleep(300);
        intake.changeState(Intake.State.STOPPED);
        shooter.runIndexerServos();

        robot.sleep(300);

        shooter.setState(Shooter.ShooterState.RESTING);
        intake.changeState(Intake.State.IN);

        robot.driveStraight(0.8, 66, WestCoast.Direction.FORWARDS);


        robot.turnGlobal(Math.toRadians(50));

        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        robot.sleep(500);

        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        robot.sleep(100);

        wobbleArm.setState(WobbleArm.State.RETRACTING);
        robot.sleep(400);

        robot.turnGlobal(Math.toRadians(90));

        robot.driveStraight(0.8, 42, WestCoast.Direction.FORWARDS);
    }
}