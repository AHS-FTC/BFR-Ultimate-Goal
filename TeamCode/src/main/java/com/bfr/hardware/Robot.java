package com.bfr.hardware;

import com.acmerobotics.dashboard.FtcDashboard;
import com.bfr.control.path.Position;
import com.bfr.control.pidf.PIDFConfig;
import com.bfr.control.pidf.TurnConstants;
import com.bfr.hardware.sensors.IMU;
import com.bfr.control.pidf.PIDFController;
import com.bfr.util.FTCUtilities;
import com.qualcomm.hardware.lynx.LynxModule;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.List;

public class Robot {
    private WestCoast westCoast = new WestCoast();
    private Shooter shooter = new Shooter();
    private Intake intake = new Intake();
    private IMU imu;
    private Telemetry dashboardTelemetry = FtcDashboard.getInstance().getTelemetry();

    private List<LynxModule> hubs;

    public Robot() {
        hubs = FTCUtilities.getHardwareMap().getAll(LynxModule.class);

        imu = new IMU("imu", true);

        for (LynxModule hub : hubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }
    }

    public Intake getIntake(){return intake;}

    public Shooter getShooter(){return shooter;}

    public void drive(double forward, double turn){
        westCoast.arcadeDrive(forward, turn);
    }

    //todo this is only temporary as the shooter will be modal
    public void setShooterPower(double power){
        shooter.setPower(power);
    }

    //todo consider fixing?
    public void driveToPosition(Position targetPos){

        //double angleToTarget = FTCMath.ensureIdealAngle(position.angleTo(targetPos), position.heading);
        //double distanceToTarget = position.distanceTo(targetPos);

        //turnToHeading(angleToTarget);

        //driveDistance(distanceToTarget);
        //turnToHeading(targetPos.heading);
    }


    public void driveStraight(double power, double targetRotations){
        westCoast.startDriveStraight(power, targetRotations);

        while (westCoast.getMode() == WestCoast.Mode.DRIVE_STRAIGHT && FTCUtilities.opModeIsActive()){
            update();
        }
    }

    public void turnGlobal(double globalAngle){
        PIDFConfig pidfConfig = new PIDFConfig() {
            @Override
            public double kP() {
                return TurnConstants.kP;
            }

            @Override
            public double kI() {
                return TurnConstants.kI;
            }

            @Override
            public double kD() {
                return TurnConstants.kD;
            }

            @Override
            public double feedForward(double setPoint, double error) {
                if(Math.abs(error) < TurnConstants.finishedThreshold || Math.abs(error) > 20){
                    return 0;
                }
                return TurnConstants.minPower * Math.signum(error);
            }
        };

        PIDFController turnController = new PIDFController(pidfConfig, globalAngle, imu.getHeading(),3);
        turnController.setStabilityThreshold(.005);

        double error;
        do {
            double imuHeading = imu.getHeading();
            error = globalAngle - imuHeading;
            System.out.println("error " + error);

            dashboardTelemetry.addData("heading", imuHeading);
            dashboardTelemetry.update();

            double turnPower = turnController.getOutput(imuHeading);
            westCoast.setTankPower(-turnPower, turnPower);
        } while(!turnController.isStable() || Math.abs(error) > TurnConstants.finishedThreshold);

        westCoast.brakeMotors();
    }

    public void turnLocal(double angle){
        turnGlobal(imu.getHeading() + angle);
    }

    /**
     * The update() method contains maintenance stuff
     * it should be called every iteration in any blocking method.
     */
    public void update(){

        long nanosBefore = System.nanoTime();

        //clear sensor cache
        for(LynxModule hub : hubs) {
            hub.clearBulkCache();
        }

        long nanosAfter = System.nanoTime();
        long bulkReadTimestamp = (nanosBefore + nanosAfter) / 2;

        shooter.update(bulkReadTimestamp);
        westCoast.update();

        if(FTCUtilities.isDebugMode()){
            dashboardTelemetry.update();
        }
        //todo track loop times
    }
}
