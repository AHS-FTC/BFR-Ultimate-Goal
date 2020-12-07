package com.bfr.opMode;

import com.bfr.hardware.Robot;
import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

@Autonomous(name="BFR Auto", group="Linear OpMode")
//@Disabled
public class AutoOp extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException{
        FTCUtilities.setOpMode(this);

        Robot robot = new Robot();

        waitForStart();

        robot.driveStraight(.8, 3.0);

//        robot.turnGlobal(10);
//        sleep(1000);
//        robot.turnGlobal(30);
//        sleep(1000);
//        robot.turnGlobal(90);
//        sleep(1000);
//        robot.turnGlobal(180);
//        sleep(1000);
//        robot.turnGlobal(360);

    }
}