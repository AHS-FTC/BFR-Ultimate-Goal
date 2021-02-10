package com.bfr.opMode;

import com.bfr.hardware.Intake;
import com.bfr.hardware.Robot;
import com.bfr.hardware.Shooter;
import com.bfr.hardware.WestCoast;
import com.bfr.hardware.WobbleArm;
import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.opencv.core.Mat;

@Autonomous(name="Blue Auto 4", group="Linear OpMode")
//@Disabled
public class BlueAuto4 extends LinearOpMode {

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
        robot.driveStraight(-0.7, -109);

        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        sleep(500);
        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        sleep(100);
        wobbleArm.setState(WobbleArm.State.RETRACTING);
        shooter.runShooter();
        sleep(500);

        robot.turnGlobal(Math.toRadians(90));
        robot.driveStraight(0.7, 50);

        robot.turnGlobal(Math.toRadians(256));

        shooter.runIndexerServos();

        while (!shooter.isResting()){
            robot.update();
            //wait until indexing is finished
        }
        shooter.stopShooter();

        robot.turnGlobal(Math.toRadians(270));

        robot.driveStraight(-0.7, -24);

        robot.turnGlobal(Math.toRadians(310));

        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);

        robot.driveStraight(-0.7, -18);

        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        sleep(500);
        wobbleArm.setState(WobbleArm.State.HOLDING);
        sleep(500);

        robot.turnGlobal(Math.toRadians(270));
        intake.changeState(Intake.State.STARTER_STACK);

        robot.driveStraight(0.3, 12);
    }
}