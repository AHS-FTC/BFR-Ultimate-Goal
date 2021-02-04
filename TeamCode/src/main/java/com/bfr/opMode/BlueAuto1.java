package com.bfr.opMode;

import com.bfr.hardware.Robot;
import com.bfr.hardware.WestCoast;
import com.bfr.hardware.WobbleArm;
import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.opencv.core.Mat;

@Autonomous(name="Blue Auto 1", group="Linear OpMode")
//@Disabled
public class BlueAuto1 extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException{
        FTCUtilities.setOpMode(this);

        Robot robot = new Robot();
        WestCoast westCoast = robot.getWestCoast();
        WobbleArm wobbleArm = robot.getWobbleArm();
        //westCoast.setTurnMode(WestCoast.MovementMode.ACCURATE);


        waitForStart();


        wobbleArm.setState(WobbleArm.State.HOLDING);
        robot.driveStraight(-0.7, -107);

        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        sleep(500);
        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        sleep(100);
        wobbleArm.setState(WobbleArm.State.RETRACTING);
        sleep(500);

        robot.turnGlobal(Math.toRadians(-45));
        robot.driveStraight(-0.7, -45);


    }
}