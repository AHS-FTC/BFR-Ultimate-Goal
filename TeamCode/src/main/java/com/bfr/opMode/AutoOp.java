package com.bfr.opMode;

import android.graphics.Rect;

import com.bfr.control.path.Position;
import com.bfr.hardware.Intake;
import com.bfr.hardware.Robot;
import com.bfr.hardware.SerialServo;
import com.bfr.hardware.Shooter;
import com.bfr.hardware.WestCoast;
import com.bfr.hardware.WobbleArm;
import com.bfr.hardware.sensors.IMU;
import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@Autonomous(name="Testing OpMode", group="Linear OpMode")
//@Disabled
public class AutoOp extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException{
        FTCUtilities.setOpMode(this);

        Robot robot = new Robot(new Position(0, 0, Math.toRadians(0)));
        WestCoast westCoast = robot.getWestCoast();
       // westCoast.setTurnMode(WestCoast.MovementMode.FAST);
       // westCoast.setRampdownMode(WestCoast.MovementMode.ACCURATE);

        westCoast.startTurnLocal(Math.toRadians(90));
//        Intake intake = robot.getIntake();
        robot.getShooter().setState(Shooter.ShooterState.RESTING);
//
//        WobbleArm wobbleArm = new WobbleArm();
//
//        wobbleArm.setState(WobbleArm.State.STORED);
//
        waitForStart();

        while (opModeIsActive()) {
            robot.update();
        }

    }
}