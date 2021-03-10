package com.bfr.opMode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.bfr.hardware.Robot;
import com.bfr.hardware.WestCoast;
import com.bfr.hardware.sensors.IMU;
import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

@Autonomous(name="IMU Op", group="Linear OpMode")
//@Disabled
public class ImuOp extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException{
        FTCUtilities.setOpMode(this);

        telemetry = FtcDashboard.getInstance().getTelemetry();

        IMU imu = new IMU("imu_ch", true, -Math.PI/2);
        //robot.getWestCoast().setRampdownMode(WestCoast.RampdownMode.FAST);

        waitForStart();
        while(opModeIsActive()){
            telemetry.addData("heading", Math.toDegrees(imu.getHeading()));
            telemetry.update();
        }


        //robot.driveStraight(-0.9, -48.0);

    }
}