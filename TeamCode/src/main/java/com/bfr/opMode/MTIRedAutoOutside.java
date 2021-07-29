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

@Autonomous(name = "MTI Red Auto - Outside", group = "MTI")
//@Disabled
public class MTIRedAutoOutside extends LinearOpMode {
    private Robot robot;
    private Shooter shooter;
    private Intake intake;
    private WobbleArm wobbleArm;

    @Override
    public void runOpMode() throws InterruptedException {
        FTCUtilities.setOpMode(this);

        FTCUtilities.setAllianceColor(AllianceColor.RED);
        FTCUtilities.setIsInside(false);

        robot = new Robot(new Position(8,124, Math.toRadians(90)));
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
        robot.turnGlobal(Math.toRadians(-77));

        shoot();
    }

    private void path0() {
        robot.sleep(15000);

        shootPreloads();

        robot.turnGlobal(Math.toRadians(45));

        dropWobbleGoal();

        robot.turnGlobal(Math.toRadians(0));
        robot.driveStraight(.6, 14, WestCoast.Direction.FORWARDS);

        robot.turnGlobal(Math.toRadians(-90));
        robot.driveStraight(.6, 12, WestCoast.Direction.FORWARDS);
    }

    private void path1() {
        shootPreloads();

        robot.turnGlobal(Math.toRadians(45));

        intake.changeState(Intake.State.IN);
        robot.driveStraight(0.6, 16, WestCoast.Direction.FORWARDS);

        robot.sleep(500);
        intake.changeState(Intake.State.STOPPED);

        robot.turnGlobal(Math.toRadians(-83));
        shoot();

        shooter.setState(Shooter.ShooterState.RESTING);

        robot.turnGlobal(Math.toRadians(95));

        robot.driveStraight(0.6, 34, WestCoast.Direction.REVERSE);

        robot.turnGlobal(Math.toRadians(75));
        dropWobbleGoal();


    }

    private void path4() {

        shootPreloads();
        //shooter.setState(Shooter.ShooterState.RESTING);

        robot.turnGlobal(Math.toRadians(45));

        intake.changeState(Intake.State.IN);
        robot.driveStraight(0.6, 6, WestCoast.Direction.FORWARDS);

        robot.sleep(3000);
        intake.changeState(Intake.State.STOPPED);

        robot.turnGlobal(Math.toRadians(-80));

        shoot();

        robot.turnGlobal(Math.toRadians(45));

        intake.changeState(Intake.State.IN);

        robot.driveStraight(0.6, 8, WestCoast.Direction.FORWARDS);

        robot.sleep(500);

        intake.changeState(Intake.State.STOPPED);

        robot.turnGlobal(Math.toRadians(-83));

        shoot();
        shooter.setState(Shooter.ShooterState.RESTING);

        robot.turnGlobal(Math.toRadians(-120));

        robot.driveStraight(0.6, 18, WestCoast.Direction.FORWARDS);

        robot.turnGlobal(Math.toRadians(-270));

        robot.driveStraight(0.6, 40, WestCoast.Direction.REVERSE);

        robot.turnGlobal(Math.toRadians(-320));

        dropWobbleGoal();

        robot.turnGlobal(Math.toRadians(-270));

        robot.driveStraight(0.6, 30, WestCoast.Direction.FORWARDS);
    }

    private void shoot() {
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
