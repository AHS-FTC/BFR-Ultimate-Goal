package com.bfr.opMode;

import com.bfr.hardware.Intake;
import com.bfr.hardware.Robot;
import com.bfr.hardware.Shooter;
import com.bfr.hardware.WobbleArm;
import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

@Autonomous(name="Blue Auto 0", group="Linear OpMode")
//@Disabled
public class BlueAuto0 extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException{
        FTCUtilities.setOpMode(this);

        Robot robot = new Robot();
        WobbleArm wobbleArm = robot.getWobbleArm();
        Shooter shooter = robot.getShooter();
        Intake intake = robot.getIntake();
        //westCoast.setTurnMode(WestCoast.MovementMode.ACCURATE);

        waitForStart();

        wobbleArm.setState(WobbleArm.State.HOLDING);
        robot.driveStraight(-0.7, -58);

        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        sleep(500);
        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        sleep(100);
        wobbleArm.setState(WobbleArm.State.RETRACTING);
        shooter.runShooter();
        sleep(500);

        robot.driveStraight(0.7, 5);

        robot.turnGlobal(Math.toRadians(256));

        shooter.runIndexerServos();

        while (!shooter.isResting()){
            robot.update();
            //wait until indexing is finished
        }
        shooter.stopShooter();

        robot.turnGlobal(Math.toRadians(300));

        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        robot.driveStraight(-0.5, -32);

        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        sleep(500);
        wobbleArm.setState(WobbleArm.State.HOLDING);
        sleep(500);

        robot.turnGlobal(Math.toRadians(470));
        robot.driveStraight(-0.7, -32);

        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        sleep(500);
        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        sleep(100);
        wobbleArm.setState(WobbleArm.State.RETRACTING);
        sleep(500);

        robot.turnGlobal(Math.toRadians(400));
        robot.driveStraight(-0.5, -24);

        robot.turnGlobal(Math.toRadians(270));

    }
}