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

@Autonomous(name = "MTI Blue Auto - Inside", group = "MTI")
//@Disabled
public class MTIBlueAutoInside extends LinearOpMode {
    private Robot robot;
    private Shooter shooter;
    private Intake intake;
    private WobbleArm wobbleArm;

    @Override
    public void runOpMode() throws InterruptedException {
        FTCUtilities.setOpMode(this);
        FTCUtilities.setAllianceColor(AllianceColor.BLUE);
        FTCUtilities.setIsInside(true);
        robot = new Robot(new Position(-8,124, Math.toRadians(90)));
        shooter = robot.getShooter();
        intake = robot.getIntake();
        wobbleArm = robot.getWobbleArm();

        robot.setState(Robot.State.DETECTING_STACK);
        while(!isStarted() && !isStopRequested()){
            robot.update();
            telemetry.addData("Field Configuration: ", robot.getFieldConfiguration().toString());
            telemetry.update();
        }

        waitForStart();

        robot.setState(Robot.State.FREE);

        StackDetector.FieldConfiguration fieldConfiguration = robot.getFieldConfiguration();

        switch (fieldConfiguration){
            case ZERO:
                path0();
                break;
            case ONE:
                path1();
                break;
            case FOUR:
                path4();
                break;
        }

        try {
            AutoTransitioner.writeJSON(robot.getOdometry().getPosition());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void shootPreloads() {
        shooter.setState(Shooter.ShooterState.STANDARD);
        wobbleArm.setState(WobbleArm.State.HOLDING);
        robot.driveStraight(0.6, 54, WestCoast.Direction.REVERSE);
        robot.turnGlobal(Math.toRadians(-75));

        shoot();
    }

    private void path0() {
        shootPreloads();

        robot.turnGlobal(Math.toRadians(90));
        robot.driveStraight(.6, 36, WestCoast.Direction.REVERSE);

        robot.turnGlobal(Math.toRadians(180));
        robot.driveStraight(.6, 26, WestCoast.Direction.REVERSE);
        dropWobbleGoal();

        robot.driveStraight(.6, 30, WestCoast.Direction.FORWARDS);

        robot.turnGlobal(Math.toRadians(90));
        robot.driveStraight(.6, 14, WestCoast.Direction.FORWARDS);
    }

    private void path1() {
        shootPreloads();

        robot.turnGlobal(Math.toRadians(-89));
        robot.driveStraight(0.6, 57, WestCoast.Direction.FORWARDS);

        robot.turnGlobal(Math.toRadians(-180));
        dropWobbleGoal();

        robot.turnGlobal(Math.toRadians(-90));
        robot.driveStraight(0.6, 37, WestCoast.Direction.REVERSE);

    }

    private void path4() {
        shootPreloads();
        robot.turnGlobal(Math.toRadians(-90));
        robot.driveStraight(0.6, 60, WestCoast.Direction.FORWARDS);

        robot.turnGlobal(Math.toRadians(-180));
        robot.driveStraight(0.6,30, WestCoast.Direction.REVERSE);

        robot.turnGlobal(Math.toRadians(-260));

        dropWobbleGoal();

        robot.turnGlobal(Math.toRadians(-180));
        robot.driveStraight(0.6, 35, WestCoast.Direction.FORWARDS);

        robot.turnGlobal(Math.toRadians(-90));
        robot.driveStraight(0.6, 40, WestCoast.Direction.REVERSE);

    }
    private void shoot() {
        robot.sleep(250);
        shooter.runIndexerServos();
        while (!shooter.areIndexerServosResting()) {
            robot.update();
            //wait until indexing is finished
        }
    }

    private void dropWobbleGoal() {
        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        robot.sleep(500);
        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        robot.sleep(100);
        wobbleArm.setState(WobbleArm.State.RETRACTING);
        robot.sleep(500);
    }
}
