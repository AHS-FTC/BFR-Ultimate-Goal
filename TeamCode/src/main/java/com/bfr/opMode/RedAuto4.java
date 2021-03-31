package com.bfr.opMode;

import com.bfr.control.path.Position;
import com.bfr.hardware.Intake;
import com.bfr.hardware.Robot;
import com.bfr.hardware.Shooter;
import com.bfr.hardware.WestCoast;
import com.bfr.hardware.WobbleArm;
import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

@Autonomous(name="Red Auto 4", group="Linear OpMode")
//@Disabled
public class RedAuto4 extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException{
        FTCUtilities.setOpMode(this);

        Robot robot = new Robot(new Position(-8, 123.5, Math.toRadians(90)));
        WobbleArm wobbleArm = robot.getWobbleArm();
        Shooter shooter = robot.getShooter();
        Intake intake = robot.getIntake();
        robot.getWestCoast().setTurnMode(WestCoast.MovementMode.FAST);


        waitForStart();


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

        robot.driveStraight(0.5, 5, WestCoast.Direction.FORWARDS);

        shooter.setState(Shooter.ShooterState.FAR);

        wobbleArm.setState(WobbleArm.State.HOLDING);

        intake.changeState(Intake.State.IN);
        robot.turnGlobal(Math.toRadians(-89));

        robot.sleep(200);
        shooter.repetitiveIndexing();

        robot.sleep(1800);

        robot.driveStraight(0.15, 6, WestCoast.Direction.FORWARDS);
        shooter.setState(Shooter.ShooterState.STANDARD);

        robot.driveStraight(.8,20, WestCoast.Direction.FORWARDS);

        robot.sleep(300);

        shooter.runIndexerServos();
        robot.sleep(300);

        shooter.setState(Shooter.ShooterState.RESTING);

        robot.driveStraight(0.8, 66, WestCoast.Direction.FORWARDS);


        robot.turnGlobal(Math.toRadians(50));

        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        robot.sleep(500);

        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        robot.sleep(100);

        wobbleArm.setState(WobbleArm.State.RETRACTING);

        robot.turnGlobal(Math.toRadians(90));

        robot.driveStraight(0.8, 42, WestCoast.Direction.FORWARDS);
    }
}