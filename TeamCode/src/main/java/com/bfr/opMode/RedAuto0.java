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

@Autonomous(name="Red Auto 0", group="Linear OpMode")
//@Disabled
public class RedAuto0 extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException{
        FTCUtilities.setOpMode(this);

        Robot robot = new Robot(new Position(0, 0, Math.toRadians(90)));
        WobbleArm wobbleArm = robot.getWobbleArm();
        Shooter shooter = robot.getShooter();
        Intake intake = robot.getIntake();
        robot.getWestCoast().setTurnMode(WestCoast.MovementMode.FAST);

        waitForStart();

        wobbleArm.setState(WobbleArm.State.HOLDING);
        robot.driveStraight(0.7, 64, WestCoast.Direction.REVERSE);

//        intake.setNineTailsState(Intake.NineTailsState.DEPLOYED);

        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        sleep(500);
        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        sleep(100);
        wobbleArm.setState(WobbleArm.State.RETRACTING);
        shooter.setState(Shooter.ShooterState.STANDARD);
        sleep(500);

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
        sleep(500);
        wobbleArm.setState(WobbleArm.State.HOLDING);
        sleep(500);

        robot.turnGlobal(Math.toRadians(-283));
        robot.driveStraight(0.7, 36, WestCoast.Direction.REVERSE);

        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        sleep(500);
        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        sleep(100);
        wobbleArm.setState(WobbleArm.State.RETRACTING);
        sleep(500);

        robot.turnGlobal(Math.toRadians(-220));
        robot.driveStraight(0.5, 24, WestCoast.Direction.REVERSE);

        robot.turnGlobal(Math.toRadians(-90));

    }
}