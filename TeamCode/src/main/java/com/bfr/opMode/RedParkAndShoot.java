package com.bfr.opMode;

import com.bfr.control.path.Position;
import com.bfr.control.vision.StackDetector;
import com.bfr.hardware.Intake;
import com.bfr.hardware.Robot;
import com.bfr.hardware.Shooter;
import com.bfr.hardware.WestCoast;
import com.bfr.hardware.WobbleArm;
import com.bfr.util.AllianceColor;
import com.bfr.util.AutoTransitioner;
import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import java.io.IOException;

@Autonomous(name="Red Park And Shoot", group="Linear OpMode")
//@Disabled
public class RedParkAndShoot extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException{
        FTCUtilities.setOpMode(this);
        FTCUtilities.setAllianceColor(AllianceColor.RED);
        Robot robot = new Robot(new Position(46.5,124, Math.toRadians(-90)));
        Shooter shooter = robot.getShooter();
        Intake intake = robot.getIntake();

        waitForStart();

        robot.setState(Robot.State.FREE);
        shooter.setState(Shooter.ShooterState.STANDARD);
        intake.changeState(Intake.State.IN);

        robot.driveStraight(.9, 52, WestCoast.Direction.FORWARDS);
        intake.changeState(Intake.State.STOPPED);

        robot.turnGlobal(Math.toRadians(-106.5));

        shooter.runIndexerServos();

        while (!shooter.areIndexerServosResting()){
            robot.update();
            //wait until indexing is finished
        }
        robot.sleep(500);

        shooter.setState(Shooter.ShooterState.RESTING);

        robot.turnGlobal(Math.toRadians(-90));

        robot.driveStraight(.9,18, WestCoast.Direction.FORWARDS);

        try {
            AutoTransitioner.writeJSON(robot.getOdometry().getPosition());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}