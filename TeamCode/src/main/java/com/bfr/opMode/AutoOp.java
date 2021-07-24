package com.bfr.opMode;

import com.bfr.control.path.Position;
import com.bfr.hardware.Robot;

import com.bfr.hardware.WestCoast;

import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name="Testing OpMode", group="Linear OpMode")
//@Disabled
public class AutoOp extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException{
        FTCUtilities.setOpMode(this);

        Robot robot = new Robot(new Position(0, 0, Math.toRadians(0)));
        WestCoast westCoast = robot.getWestCoast();
        westCoast.setTurnMode(WestCoast.MovementMode.FAST);
        westCoast.setRampdownMode(WestCoast.MovementMode.ACCURATE);

        waitForStart();

        robot.driveStraight(0.8, 70, WestCoast.Direction.FORWARDS);

        sleep(1000);

        robot.driveStraight(0.8, 10, WestCoast.Direction.REVERSE);


    }
}