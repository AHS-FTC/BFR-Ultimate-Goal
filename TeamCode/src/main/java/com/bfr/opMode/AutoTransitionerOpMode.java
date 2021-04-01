package com.bfr.opMode;

import com.bfr.control.path.Position;
import com.bfr.hardware.Intake;
import com.bfr.hardware.Robot;
import com.bfr.hardware.Shooter;
import com.bfr.hardware.WestCoast;
import com.bfr.hardware.WobbleArm;
import com.bfr.util.AutoTransitioner;
import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import java.io.IOException;

@Autonomous(name="AutoTransitioner OpMode", group="Linear OpMode")
@Disabled
public class AutoTransitionerOpMode extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        FTCUtilities.setOpMode(this);

        Position position = null;

        waitForStart();
        try {
            AutoTransitioner.writeJSON(new Position(10, 69, 420));
            position = AutoTransitioner.readJSON();
        } catch (IOException e) {
            e.printStackTrace();
        }

        telemetry.addData("x", position.x);
        telemetry.addData("y", position.y);
        telemetry.addData("h", position.heading);
        telemetry.update();

    }
}