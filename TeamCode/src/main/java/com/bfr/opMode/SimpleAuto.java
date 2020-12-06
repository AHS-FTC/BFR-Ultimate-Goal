package com.bfr.opMode;

import com.bfr.control.path.Position;
import com.bfr.hardware.Motor;
import com.bfr.hardware.Robot;
import com.bfr.hardware.WestCoast;
import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name="Simple Auto", group="Iterative Opmode")
@Disabled
public class SimpleAuto extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        FTCUtilities.setOpMode(this);

        Robot robot = new Robot();

        waitForStart();

        robot.driveToPosition(new Position(40, -10, 0));
        robot.driveToPosition(new Position(0, 0, 0));

        telemetry.addData("x", robot.getPosition().x);
        telemetry.addData("y", robot.getPosition().y);
        telemetry.addData("h", robot.getPosition().heading);
        telemetry.update();
        sleep(5000);
    }
}

