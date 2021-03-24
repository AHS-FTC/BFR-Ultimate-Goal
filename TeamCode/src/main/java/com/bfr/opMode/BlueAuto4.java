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

@Autonomous(name="Blue Auto 4", group="Linear OpMode")
//@Disabled
public class BlueAuto4 extends LinearOpMode {

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

        intake.setNineTailsState(Intake.NineTailsState.DEPLOYED);

        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        sleep(500);
        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        sleep(100);
        wobbleArm.setState(WobbleArm.State.RETRACTING);
        shooter.runShooter();
        sleep(500);

        robot.turnGlobal(Math.toRadians(90));
        robot.driveStraight(0.7, 56, WestCoast.Direction.FORWARDS);

        robot.turnGlobal(Math.toRadians(254));

        shooter.runIndexerServos();

        while (!shooter.isResting()){
            robot.update();
            //wait until indexing is finished
        }
        shooter.stopShooter();

        robot.turnGlobal(Math.toRadians(270));

        robot.driveStraight(0.7, 18, WestCoast.Direction.REVERSE);

        robot.turnGlobal(Math.toRadians(302));

        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        intake.setNineTailsState(Intake.NineTailsState.RING_4);

        robot.driveStraight(0.7, 20, WestCoast.Direction.REVERSE);

        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        sleep(500);
        wobbleArm.setState(WobbleArm.State.HOLDING);
        sleep(500);

        robot.turnGlobal(Math.toRadians(255));
        intake.changeState(Intake.State.STARTER_STACK);

        robot.driveStraight(0.5, 15, WestCoast.Direction.FORWARDS);

        intake.changeState(Intake.State.IN);
        shooter.runShooter();
        shooter.repetitiveIndexing();
        robot.turnGlobal(Math.toRadians(268));

        robot.driveStraight(0.2, 25, WestCoast.Direction.FORWARDS);

        robot.sleep(500);

        shooter.runIndexerServos();

        while (!shooter.isResting()){
            robot.update();
            //wait until indexing is finished
        }
        shooter.stopShooter();

        robot.turnGlobal(Math.toRadians(275));

        robot.driveStraight(0.8, 55, WestCoast.Direction.FORWARDS);

        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);

        robot.turnGlobal(Math.toRadians(90));

        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);

        robot.sleep(500);

        robot.driveStraight(0.8, 36, WestCoast.Direction.FORWARDS);
    }
}