package com.bfr.opMode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.bfr.control.path.Position;
import com.bfr.hardware.sensors.DifOdometry;
import com.bfr.hardware.sensors.IMU;
import com.bfr.hardware.sensors.Odometer;
import com.bfr.hardware.sensors.OdometerImpl;
import com.bfr.hardware.sensors.Odometry;
import com.bfr.util.FTCUtilities;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.List;

@TeleOp(name="Dif Odometry Logger", group="Iterative Opmode")
//@Disabled
public class DifOdomOp extends OpMode {
    List<LynxModule> allHubs;

    Odometer left, right;
    Odometry odometry;
    Telemetry logger;
    IMU imu;

    @Override
    public void init() {
        FTCUtilities.setOpMode(this);

        left = new OdometerImpl("l_odo",3.95 ,false, 1440.0);
        right = new OdometerImpl("r_odo", 3.95, true, 1440.0);


        odometry = new DifOdometry(left, right, Position.origin,16.16);

        logger = FtcDashboard.getInstance().getTelemetry();

        imu = new IMU("imu", true);

        allHubs = hardwareMap.getAll(LynxModule.class);

        for (LynxModule module : allHubs) {
            module.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }

    }

    @Override
    public void init_loop() {
    }

    @Override
    public void start() {
        odometry.start();
    }

    @Override
    public void loop() {
        for(LynxModule module : allHubs){
            module.clearBulkCache();
        }
        odometry.update();

        logger.addData("imu", imu.getHeading());

        logger.addData("left wheel", left.getDistance());
        logger.addData("right wheel", right.getDistance());

        Position p = odometry.getPosition();
        logger.addData("x", p.x);
        logger.addData("y", p.y);
        logger.addData("h", p.heading);
        logger.update();
    }

    @Override
    public void stop() {

    }

}

