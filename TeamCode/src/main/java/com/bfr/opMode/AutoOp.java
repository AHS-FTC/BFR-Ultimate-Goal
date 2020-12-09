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

        robot.driveStraight(1, 8.0);
        //robot.turnLocal(180.0);

    }
}