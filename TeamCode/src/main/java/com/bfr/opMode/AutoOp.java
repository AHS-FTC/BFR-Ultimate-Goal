package com.bfr.opMode;

import android.graphics.Rect;

import com.bfr.control.path.Position;
import com.bfr.hardware.Robot;
import com.bfr.hardware.WestCoast;
import com.bfr.hardware.sensors.IMU;
import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
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
        westCoast.setRampdownMode(WestCoast.MovementMode.FAST);

        //robot.getWestCoast().setRampdownMode(WestCoast.RampdownMode.FAST);

        waitForStart();

        robot.driveStraight(.9, 60, WestCoast.Direction.FORWARDS);

        robot.turnGlobal(Math.toRadians(-10));

        sleep(1000);

        robot.turnGlobal(Math.toRadians(-40));

    }
}