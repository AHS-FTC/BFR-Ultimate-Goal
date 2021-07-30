package com.bfr.opMode;

import com.bfr.control.pidf.ShooterConstants;
import com.bfr.hardware.Motor;
import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

/**
 * Simple opMode to find the right power for shooter feedforward
 */
@Autonomous(name="Shooter Testing OpMode", group="Linear OpMode")
@Disabled
public class ShooterOpMode extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException{
        FTCUtilities.setOpMode(this);

        Motor shooterMotor1 = new Motor("s1", 41.0,true);
        Motor shooterMotor2 = new Motor("s2", 41.0,true);
        shooterMotor1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        shooterMotor2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);


        waitForStart();

        while (opModeIsActive()){
            shooterMotor1.setPower(0.0);
            shooterMotor2.setPower(0.0);
        }
    }
}