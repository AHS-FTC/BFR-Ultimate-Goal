package com.bfr.opMode;

import com.bfr.control.path.Position;
import com.bfr.hardware.Intake;
import com.bfr.hardware.Robot;
import com.bfr.hardware.Shooter;
import com.bfr.hardware.WestCoast;
import com.bfr.hardware.WobbleArm;
import com.bfr.util.FTCUtilities;
import com.qualcomm.hardware.kauailabs.NavxMicroNavigationSensor;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;

@Autonomous(name="Blue Auto 0", group="Linear OpMode")
//@Disabled
public class BlueAuto0 extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException{
        NavxMicroNavigationSensor navx = hardwareMap.get(NavxMicroNavigationSensor.class, "navx");

        FTCUtilities.setOpMode(this);

        Robot robot = new Robot(new Position(0, 0, -Math.toRadians(90)));
        WobbleArm wobbleArm = robot.getWobbleArm();
        Shooter shooter = robot.getShooter();
        Intake intake = robot.getIntake();
        //westCoast.setTurnMode(WestCoast.MovementMode.ACCURATE);

        waitForStart();

        wobbleArm.setState(WobbleArm.State.HOLDING);
        robot.driveStraight(0.7, 58, WestCoast.Direction.REVERSE);

        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        sleep(500);
        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        sleep(100);
        wobbleArm.setState(WobbleArm.State.RETRACTING);
        shooter.runShooter();
        sleep(500);

        robot.driveStraight(0.7, 5, WestCoast.Direction.FORWARDS);

        robot.turnGlobal(Math.toRadians(256));

        shooter.runIndexerServos();

        while (!shooter.isResting()){
            robot.update();
            //wait until indexing is finished
        }
        shooter.stopShooter();

        robot.turnGlobal(Math.toRadians(300));

        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        robot.driveStraight(0.5, 32, WestCoast.Direction.REVERSE);

        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        sleep(500);
        wobbleArm.setState(WobbleArm.State.HOLDING);
        sleep(500);

        robot.turnGlobal(Math.toRadians(470));
        robot.driveStraight(0.7, 32, WestCoast.Direction.REVERSE);

        wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED);
        sleep(500);
        wobbleArm.setState(WobbleArm.State.DEPLOYED_OPEN);
        sleep(100);
        wobbleArm.setState(WobbleArm.State.RETRACTING);
        sleep(500);

        robot.turnGlobal(Math.toRadians(400));
        robot.driveStraight(0.5, 24, WestCoast.Direction.REVERSE);

        robot.turnGlobal(Math.toRadians(270));

    }
}