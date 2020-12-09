package com.bfr.opMode;

import com.bfr.hardware.Intake;
import com.bfr.hardware.Robot;
import com.bfr.hardware.WestCoast;
import com.bfr.util.FTCUtilities;
import com.bfr.util.Switch;
import com.bfr.util.Toggle;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name="Dif TeleOp", group="Iterative Opmode")
//@Disabled
public class DifTeleOp extends OpMode {
    private Robot robot;
    private Intake intake;
    private WestCoast westCoast;
    private double intakePower;

    private Switch intakeOutSwitch, intakeInSwitch, autoAimSwitch;
    private Toggle indexerToggle, shooterToggle;
    long waitTime = 300;
    long lastTime;

    //Controller gamepad1, gamepad2;
    //adb connect 192.168.43.1:5555

        @Override
        @SuppressWarnings("all")
        public void init() {
            //BNO055IMU imu = hardwareMap.get(IMU.class, "imu");

            FTCUtilities.setOpMode(this);
            WestCoast.setDefaultMode(WestCoast.Mode.DRIVER_CONTROL);

            robot = new Robot();
            intake = robot.getIntake();
            westCoast = robot.getWestCoast();

            intakeOutSwitch = new Switch();
            intakeInSwitch = new Switch();
            autoAimSwitch = new Switch();

            indexerToggle = new Toggle();
            shooterToggle = new Toggle();

//            gamepad1 = FTCUtilities.getGamepad1();
//            gamepad2 = FTCUtilities.getGamepad2();

//            Telemetry tel = FtcDashboard.getInstance().getTelemetry();


//            double turnTarget = 90;
//            double current = imu.getAngularOrientation().firstAngle;
//
//            int direction;
//            if(current < turnTarget){
//                direction = 1;
//            } else {
//                direction = -1;
//            }
//
//            leftMotor.setPower(direction * 1.0);
//            rightMotor.setPower(direction * -1.0);
//
//            while (Math.abs(imu.getAngularOrientation().firstAngle - turnTarget) < 5.0){
//                //do nothing
//            }
//
//            leftMotor.setPower(0.0);
//            rightMotor.setPower(0.0);

        }

        @Override
        public void init_loop() {
        }

        @Override
        public void start() {
            lastTime = FTCUtilities.getCurrentTimeMillis();
            westCoast.startDriverControl();
        }

        @Override
        public void loop() {
            //press l bumper to reverse intake
            if (gamepad1.left_bumper) {
                if(intakeOutSwitch.canFlip()) {
                    if (intakePower == -.8) {
                        intakePower = 0;
                    } else {
                        intakePower = -.8;
                    }
                    intake.setPower(intakePower);
                }
            }

            //press r bumper to intake
            if (gamepad1.right_bumper) {
                if (intakeInSwitch.canFlip()) {
                    if (intakePower == 1.0) {
                        intakePower = 0;
                    } else {
                        intakePower = 1.0;
                    }
                    intake.setPower(intakePower);
                }
            }

            if (gamepad1.a && (waitTime < (FTCUtilities.getCurrentTimeMillis() - lastTime))){
                lastTime = FTCUtilities.getCurrentTimeMillis();
                robot.getShooter().runIndexerServos();
            }

            if (gamepad2.y && (waitTime < (FTCUtilities.getCurrentTimeMillis() - lastTime))){
//TODO          fix time thingy
                lastTime = FTCUtilities.getCurrentTimeMillis();
                shooterToggle.canFlip();
                updateShooter();
            }

            if (autoAimSwitch.canFlip() && gamepad1.x){
                robot.autoAim();
            }

            robot.update();
        }

    private void updateShooter() {
        if (shooterToggle.isEnabled()){
            robot.getShooter().runShooter();
        } else {
            robot.getShooter().stopShooter();
        }
    }

    @Override
        public void stop() {

        }

    }

