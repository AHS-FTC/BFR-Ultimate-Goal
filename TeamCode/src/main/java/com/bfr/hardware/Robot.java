package com.bfr.hardware;

import com.acmerobotics.dashboard.FtcDashboard;
import com.bfr.control.path.Position;
import com.bfr.control.pidf.PIDFConfig;
import com.bfr.control.pidf.TurnConstants;
import com.bfr.hardware.sensors.IMU;
import com.bfr.control.pidf.PIDFController;
import com.bfr.util.FTCUtilities;
import com.bfr.util.math.FTCMath;
import com.qualcomm.hardware.lynx.LynxModule;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.List;

public class Robot {
    private WestCoast westCoast = new WestCoast();
    private Shooter shooter = new Shooter();
    private Intake intake = new Intake();
    private IMU imu;
    private Position position;
    private Telemetry dashboardTelemetry;

    private final double POWER = .6;

    private List<LynxModule> hubs;

    public Robot() {
        hubs = FTCUtilities.getHardwareMap().getAll(LynxModule.class);

        imu = new IMU("imu", true);

        for (LynxModule hub : hubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }

        position = westCoast.getPosition();

        dashboardTelemetry = FtcDashboard.getInstance().getTelemetry();
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

    public void driveToPosition(Position targetPos){

        double angleToTarget = FTCMath.ensureIdealAngle(position.angleTo(targetPos), position.heading);
        double distanceToTarget = position.distanceTo(targetPos);

        turnToHeading(angleToTarget);

        driveDistance(distanceToTarget);

        turnToHeading(targetPos.heading);
    }

    /**
     * For use with encoder values and a global positioning system
     * @param targetHeading
     */
    public void turnToHeading(double targetHeading){
        //turning CCW -> positive to agree with rads (and vice versa)
        double turnDir = Math.signum(targetHeading - position.heading);

        westCoast.setTankPower(turnDir * -POWER, turnDir * POWER);
        while (Math.abs(position.heading - targetHeading) > Math.toRadians(2.5) && FTCUtilities.opModeIsActive()){
            FTCUtilities.addData("heading", position.heading);
            FTCUtilities.addData("error", Math.abs(position.heading - targetHeading));
            FTCUtilities.updateTelemetry();
            update();
        }
        westCoast.brakeMotors();
    }

    /**
     * Drives straight forward or backwards using encoder values and PID. Use with a global positioning system
     * @param targetDistance
     */
    public void driveDistance(double targetDistance){
        double driveDir = Math.signum(targetDistance);

        Position startPosition = new Position(0,0,0);
        startPosition.copyFrom(position);

        westCoast.setTankPower(driveDir * POWER, driveDir * POWER);
        while (startPosition.distanceTo(position) < targetDistance && FTCUtilities.opModeIsActive()){
            update();
            //todo implement pid
        }

        westCoast.brakeMotors();
    }

    public void driveStraight(double targetDistance){
        westCoast.resetEncoders();
        double driveDirection = Math.signum(targetDistance);

        westCoast.setTankPower(-driveDirection*.3, -driveDirection*.3);
        while ((driveDirection * targetDistance) <= westCoast.getAvgDistance()){
            //Keep the robot driving straight
        }
//        westCoast.brakeMotors();
    }

    public void turn(double globalAngle){
        PIDFConfig pidfConfig = new PIDFConfig() {
            @Override
            public double kP() {
                return TurnConstants.kP;
            }

            @Override
            public double kI() {
                return 0;
            }

            @Override
            public double kD() {
                return TurnConstants.kD;
            }

            @Override
            public double feedForward(double setPoint) {
                return 0;
            }
        };

        PIDFController PIDFController = new PIDFController(pidfConfig, globalAngle, imu.getHeading());

        double error;
        do {
            double imuHeading = imu.getHeading();
            error = globalAngle - imuHeading;
            System.out.println("error " + error);

            dashboardTelemetry.addData("heading", imuHeading);
            dashboardTelemetry.update();

            double turnPower = PIDFController.getOutput(imuHeading);
            westCoast.setTankPower(-turnPower, turnPower);
        } while(Math.abs(error) > 1);

        westCoast.brakeMotors();
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

        //run sensor reads
        position = westCoast.getPosition();
        //finish sensor reads

        //todo track loop times
    }

    public Position getPosition() {
        return position;
    }
}
