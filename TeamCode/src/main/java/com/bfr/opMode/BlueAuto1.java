package com.bfr.opMode;

import com.bfr.control.path.Position;
import com.bfr.control.pidf.FastTurnConstants;
import com.bfr.hardware.Intake;
import com.bfr.hardware.Robot;
import com.bfr.hardware.SerialServo;
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

        Robot robot = new Robot(new Position(0, 0, -Math.toRadians(90)));
        WestCoast westCoast = robot.getWestCoast();
        WobbleArm wobbleArm = robot.getWobbleArm();
        Shooter shooter = robot.getShooter();
        Intake intake = robot.getIntake();
        westCoast.setTurnMode(WestCoast.MovementMode.ACCURATE);


        waitForStart();



        wobbleArm.setState(WobbleArm.State.HOLDING);
        robot.driveStraight(0.7, 58, WestCoast.Direction.REVERSE);

        robot.turnGlobal(Math.toRadians(65));

        robot.driveStraight(0.7, 26, WestCoast.Direction.REVERSE);


        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        sleep(500);
        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        sleep(100);
        wobbleArm.setState(WobbleArm.State.RETRACTING);

        sleep(500);

        robot.driveStraight(0.7, 26, WestCoast.Direction.FORWARDS);
        shooter.runShooter();
        robot.turnGlobal(Math.toRadians(-103));

        shooter.runIndexerServos();

        while (!shooter.isResting()){
            robot.update();
            //wait until indexing is finished
        }
        shooter.stopShooter();

        robot.turnGlobal(Math.toRadians(-90));

        robot.driveStraight(0.7, 28, WestCoast.Direction.REVERSE);

        robot.turnGlobal(Math.toRadians(-40));

        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        robot.driveStraight(0.5, 16, WestCoast.Direction.REVERSE);

        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        sleep(500);
        wobbleArm.setState(WobbleArm.State.HOLDING);
        sleep(500);

        robot.turnGlobal(Math.toRadians(-110));

        intake.changeState(Intake.State.IN);
        robot.driveStraight(0.7, 40, WestCoast.Direction.FORWARDS);
        intake.changeState(Intake.State.STOPPED);

        shooter.runShooter();
        robot.turnGlobal(Math.toRadians(-85));

        shooter.runIndexerServos();

        while (!shooter.isResting()){
            robot.update();
            //wait until indexing is finished
        }
        shooter.stopShooter();

        robot.turnGlobal(Math.toRadians(95));

        robot.driveStraight(0.7, 20, WestCoast.Direction.REVERSE);


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