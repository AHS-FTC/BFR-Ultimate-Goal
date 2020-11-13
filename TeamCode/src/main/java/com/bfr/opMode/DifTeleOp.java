package com.bfr.opMode;

import com.bfr.hardware.Intake;
import com.bfr.hardware.Robot;
import com.bfr.util.FTCUtilities;
import com.bfr.util.Switch;
import com.bfr.util.Toggle;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name="Dif TeleOp", group="Iterative Opmode")
//@Disabled
public class DifTeleOp extends OpMode {
    Robot robot;
    Intake intake;
    private double shooterPower, intakePower;

    Switch intakeOutSwitch, intakeInSwitch;
    Toggle indexerToggle;
    long waitTime = 300;
    long lastTime;

    //adb connect 192.168.43.1:5555

        @Override
        @SuppressWarnings("all")
        public void init() {
            //BNO055IMU imu = hardwareMap.get(IMU.class, "imu");

            FTCUtilities.setOpMode(this);
            robot = new Robot();
            intake = robot.getIntake();
            shooterPower = 0;

            intakeOutSwitch = new Switch();
            intakeInSwitch = new Switch();

            indexerToggle = new Toggle();

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
        }

        @Override
        public void loop() {
            robot.drive(gamepad1.left_stick_y, gamepad1.right_stick_x);

            shooterPower += gamepad1.right_trigger * .01;
            shooterPower += gamepad1.left_trigger * -.01;
            if (gamepad1.x){
                shooterPower = 0;
            }

            //robot.setShooterPower(shooterPower);
//            shooter1.setPower(shooterPower);
//            shooter2.setPower(shooterPower);



            telemetry.addData("Shooter Power", shooterPower);
            telemetry.update();

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

//            long startTime = System.currentTimeMillis();
//            wc.gateauDrive(gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x);
//
//            Position p = wc.getPosition();
//            telemetry.addData("x", p.x);
//            telemetry.addData("y", p.y);
//            telemetry.addData("h", p.heading);
//            telemetry.addData("deltaTime", System.currentTimeMillis() - startTime);
            robot.update();
        }

        @Override
        public void stop() {
//            wc.kill();
        }

    }

