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

@Autonomous(name="Blue Auto", group="Linear OpMode")
//@Disabled
public class BlueAuto extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException{
        FTCUtilities.setOpMode(this);
        FTCUtilities.setAllianceColor(AllianceColor.BLUE);
        Robot robot = new Robot(new Position(-8,124, Math.toRadians(90)));

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

        wobbleArm.setState(WobbleArm.State.HOLDING);
        robot.driveStraight(0.7, 64, WestCoast.Direction.REVERSE);


        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        robot.sleep(500);
        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        robot.sleep(100);
        wobbleArm.setState(WobbleArm.State.RETRACTING);
        shooter.setState(Shooter.ShooterState.STANDARD);
        robot.sleep(500);

        robot.driveStraight(0.7, 12, WestCoast.Direction.FORWARDS);

        robot.turnGlobal(Math.toRadians(254.5));

        shooter.runIndexerServos();

        while (!shooter.areIndexerServosResting()){
            robot.update();
            //wait until indexing is finished
        }
        shooter.setState(Shooter.ShooterState.RESTING);

        robot.turnGlobal(Math.toRadians(292));

        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        robot.driveStraight(0.5, 32, WestCoast.Direction.REVERSE);

        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        robot.sleep(500);
        wobbleArm.setState(WobbleArm.State.HOLDING);
        robot.sleep(500);

        robot.turnGlobal(Math.toRadians(463));
        robot.driveStraight(0.7, 36, WestCoast.Direction.REVERSE);

        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        robot.sleep(500);
        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        robot.sleep(100);
        wobbleArm.setState(WobbleArm.State.RETRACTING);
        robot.sleep(500);

        robot.turnGlobal(Math.toRadians(400));
        robot.driveStraight(0.5, 24, WestCoast.Direction.REVERSE);

        robot.turnGlobal(Math.toRadians(270));
    }

    public static void  path1(Robot robot){
        WestCoast westCoast = robot.getWestCoast();
        WobbleArm wobbleArm = robot.getWobbleArm();
        Shooter shooter = robot.getShooter();
        Intake intake = robot.getIntake();


        wobbleArm.setState(WobbleArm.State.HOLDING);
        robot.driveStraight(0.7, 52, WestCoast.Direction.REVERSE);

        robot.turnGlobal(Math.toRadians(60));

        robot.driveStraight(0.7, 34, WestCoast.Direction.REVERSE);


        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        robot.sleep(500);
        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        robot.sleep(100);
        wobbleArm.setState(WobbleArm.State.RETRACTING);
        shooter.setState(Shooter.ShooterState.STANDARD);
        robot.sleep(500);

        robot.driveStraight(0.7, 34, WestCoast.Direction.FORWARDS);

        robot.turnGlobal(Math.toRadians(-102));

        shooter.runIndexerServos();

        while (!shooter.areIndexerServosResting()){
            robot.update();
            //wait until indexing is finished
        }
        shooter.setState(Shooter.ShooterState.RESTING);

        robot.turnGlobal(Math.toRadians(-90));

        robot.driveStraight(0.7, 28, WestCoast.Direction.REVERSE);

        robot.turnGlobal(Math.toRadians(-44));

        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        robot.driveStraight(0.5, 18, WestCoast.Direction.REVERSE);

        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        robot.sleep(500);
        wobbleArm.setState(WobbleArm.State.STORED);
        robot.sleep(700);

        robot.turnGlobal(Math.toRadians(-110));

        intake.changeState(Intake.State.IN);
        shooter.setState(Shooter.ShooterState.STANDARD);

        robot.driveStraight(0.7, 38, WestCoast.Direction.FORWARDS);
        intake.changeState(Intake.State.STOPPED);
        wobbleArm.setState(WobbleArm.State.HOLDING);

        robot.turnGlobal(Math.toRadians(-84));

        robot.driveStraight(.5,6, WestCoast.Direction.FORWARDS);

        shooter.runIndexerServos();

        while (!shooter.areIndexerServosResting()){
            robot.update();
            //wait until indexing is finished
        }
        shooter.setState(Shooter.ShooterState.RESTING);

        robot.turnGlobal(Math.toRadians(90));

        robot.driveStraight(0.7, 24, WestCoast.Direction.REVERSE);


        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        robot.sleep(500);
        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        robot.sleep(100);
        wobbleArm.setState(WobbleArm.State.RETRACTING);
        robot.sleep(1000);
        wobbleArm.setState(WobbleArm.State.STORED);



        robot.driveStraight(.5, 10, WestCoast.Direction.FORWARDS);

        robot.turnGlobal(Math.toRadians(270));
    }

    public static void path4(Robot robot){
        WobbleArm wobbleArm = robot.getWobbleArm();
        Shooter shooter = robot.getShooter();
        Intake intake = robot.getIntake();

        wobbleArm.setState(WobbleArm.State.HOLDING);
        robot.driveStraight(0.9, 109, WestCoast.Direction.REVERSE);

        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        robot.sleep(500);
        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        robot.sleep(100);
        wobbleArm.setState(WobbleArm.State.RETRACTING);
        shooter.setState(Shooter.ShooterState.STANDARD);
        robot.sleep(500);

        robot.turnGlobal(Math.toRadians(90));
        robot.driveStraight(0.9, 56, WestCoast.Direction.FORWARDS);

        robot.turnGlobal(Math.toRadians(254));

        shooter.runIndexerServos();

        while (!shooter.areIndexerServosResting()){
            robot.update();
            //wait until indexing is finished
        }
        shooter.setState(Shooter.ShooterState.RESTING);

        robot.turnGlobal(Math.toRadians(270));

        robot.driveStraight(0.9, 18, WestCoast.Direction.REVERSE);

        robot.turnGlobal(Math.toRadians(300));

        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
//        intake.setNineTailsState(Intake.NineTailsState.RING_4);

        robot.driveStraight(0.9, 20, WestCoast.Direction.REVERSE);

        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        robot.sleep(500);
        wobbleArm.setState(WobbleArm.State.STORED);
        robot.sleep(1000);

        robot.turnGlobal(Math.toRadians(222)); //225

        shooter.setState(Shooter.ShooterState.FAR);

        robot.driveStraight(0.9, 6, WestCoast.Direction.FORWARDS);
        wobbleArm.setState(WobbleArm.State.HOLDING);

        intake.changeState(Intake.State.IN);
        robot.turnGlobal(Math.toRadians(266));

        robot.sleep(1600);
        intake.changeState(Intake.State.STOPPED);
        shooter.runIndexerServos();
        shooter.runIndexerServos();

        robot.sleep(400);

        intake.changeState(Intake.State.IN);

        robot.driveStraight(0.15, 6, WestCoast.Direction.FORWARDS);
        shooter.setState(Shooter.ShooterState.STANDARD);

        robot.driveStraight(.8,20, WestCoast.Direction.FORWARDS);

        intake.changeState(Intake.State.STOPPED);
        shooter.runIndexerServos();

        robot.sleep(300);
        shooter.setState(Shooter.ShooterState.RESTING);

        robot.driveStraight(0.9, 66, WestCoast.Direction.FORWARDS);


        robot.turnGlobal(Math.toRadians(130));

        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        robot.sleep(500);

        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        robot.sleep(100);

        wobbleArm.setState(WobbleArm.State.RETRACTING);
        robot.sleep(400);

        robot.turnGlobal(Math.toRadians(90));

        robot.driveStraight(0.9, 40, WestCoast.Direction.FORWARDS);
    }
}