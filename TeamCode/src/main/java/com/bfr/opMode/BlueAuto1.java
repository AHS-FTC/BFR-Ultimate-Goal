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

@Autonomous(name="Blue Auto 1", group="Linear OpMode")
//@Disabled
public class BlueAuto1 extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException{
        FTCUtilities.setOpMode(this);

        Robot robot = new Robot(new Position(0, 0,Math.toRadians(90)));
        WestCoast westCoast = robot.getWestCoast();
        WobbleArm wobbleArm = robot.getWobbleArm();
        Shooter shooter = robot.getShooter();
        Intake intake = robot.getIntake();
        westCoast.setTurnMode(WestCoast.MovementMode.FAST);


        waitForStart();



        wobbleArm.setState(WobbleArm.State.HOLDING);
        robot.driveStraight(0.7, 56, WestCoast.Direction.REVERSE);

        robot.turnGlobal(Math.toRadians(60));

        intake.setNineTailsState(Intake.NineTailsState.DEPLOYED);

        robot.driveStraight(0.7, 30, WestCoast.Direction.REVERSE);


        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        sleep(500);
        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        sleep(100);
        wobbleArm.setState(WobbleArm.State.RETRACTING);
        shooter.setState(Shooter.ShooterState.STANDARD);
        sleep(500);

        robot.driveStraight(0.7, 30, WestCoast.Direction.FORWARDS);

        robot.turnGlobal(Math.toRadians(-102));

        shooter.runIndexerServos();

        while (!shooter.areIndexerServosResting()){
            robot.update();
            //wait until indexing is finished
        }
        shooter.setState(Shooter.ShooterState.RESTING);

        robot.turnGlobal(Math.toRadians(-90));

        robot.driveStraight(0.7, 28, WestCoast.Direction.REVERSE);

        robot.turnGlobal(Math.toRadians(-45));

        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        robot.driveStraight(0.5, 18, WestCoast.Direction.REVERSE);

        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        sleep(500);
        wobbleArm.setState(WobbleArm.State.STORED);
        sleep(700);

        robot.turnGlobal(Math.toRadians(-110));

        intake.changeState(Intake.State.IN);
        robot.driveStraight(0.7, 38, WestCoast.Direction.FORWARDS);
        intake.changeState(Intake.State.STOPPED);
        wobbleArm.setState(WobbleArm.State.HOLDING);

        shooter.setState(Shooter.ShooterState.STANDARD);
        robot.turnGlobal(Math.toRadians(-85));

        shooter.runIndexerServos();

        while (!shooter.areIndexerServosResting()){
            robot.update();
            //wait until indexing is finished
        }
        shooter.setState(Shooter.ShooterState.RESTING);

        robot.turnGlobal(Math.toRadians(90));

        robot.driveStraight(0.7, 27, WestCoast.Direction.REVERSE);


        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        sleep(500);
        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        sleep(100);
        wobbleArm.setState(WobbleArm.State.RETRACTING);
        sleep(500);
        wobbleArm.setState(WobbleArm.State.STORED);

        robot.turnGlobal(Math.toRadians(270));
    }
}