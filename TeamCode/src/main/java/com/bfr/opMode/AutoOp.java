package com.bfr.opMode;

import com.bfr.hardware.Robot;
import com.bfr.hardware.WestCoast;
import com.bfr.hardware.sensors.IMU;
import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

@Autonomous(name="BFR Auto", group="Linear OpMode")
@Disabled
public class AutoOp extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException{
        FTCUtilities.setOpMode(this);

        Robot robot = new Robot();
        WestCoast westCoast = robot.getWestCoast();
        westCoast.setTurnMode(WestCoast.MovementMode.ACCURATE);
        //robot.getWestCoast().setRampdownMode(WestCoast.RampdownMode.FAST);

        waitForStart();

        robot.turnLocal(2*Math.PI);
        //robot.driveStraight(-0.9, -48.0);

    }
}