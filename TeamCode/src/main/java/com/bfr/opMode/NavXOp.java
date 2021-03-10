package com.bfr.opMode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.bfr.hardware.Robot;
import com.bfr.hardware.WestCoast;
import com.bfr.util.FTCUtilities;
import com.qualcomm.hardware.kauailabs.NavxMicroNavigationSensor;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;

@TeleOp(name="NavX OpMode", group="Linear OpMode")
//@Disabled
public class NavXOp extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException{
        FTCUtilities.setOpMode(this);

        NavxMicroNavigationSensor navx = hardwareMap.get(NavxMicroNavigationSensor.class, "navx");

        telemetry = FtcDashboard.getInstance().getTelemetry();

        waitForStart();

        while (opModeIsActive()){
            double heading = navx.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZXY, AngleUnit.DEGREES).firstAngle;


            telemetry.addData("heading", heading);
            telemetry.update();
        }

        //robot.driveStraight();

    }
}