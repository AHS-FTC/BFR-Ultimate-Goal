package com.bfr.opMode;

import com.bfr.hardware.Motor;
import com.bfr.hardware.Shooter;
import com.bfr.hardware.WestCoast;
import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name="Dif TeleOp", group="Iterative Opmode")
//@Disabled
public class DifTeleOp extends OpMode {
    WestCoast wc;
    Motor intake;
    Shooter shooter;
    Motor shooter1, shooter2;
    double shooterPower;
    CRServo s1;

    //adb connect 192.168.43.1:5555

        @Override
        @SuppressWarnings("all")
        public void init() {
            //BNO055IMU imu = hardwareMap.get(IMU.class, "imu");

            FTCUtilities.setOpMode(this);


            wc = new WestCoast();
            intake = new Motor("intake", 0, true);
            shooter = new Shooter();
//            shooter1 = new Motor("s1", 0, true);
//            shooter2 = new Motor("s2", 0, true);
            s1 = hardwareMap.get(CRServo.class,"s1");

            shooterPower = 0;


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
        }

        @Override
        public void loop() {
            wc.arcadeDrive(gamepad1.left_stick_y, gamepad1.right_stick_x);

            shooterPower += gamepad1.right_trigger * .01;
            shooterPower += gamepad1.left_trigger * -.01;
            if (gamepad1.x){
                shooterPower = 0;
            }

            shooter.setPower(shooterPower);
//            shooter1.setPower(shooterPower);
//            shooter2.setPower(shooterPower);

            telemetry.addData("Power", shooterPower);
            telemetry.update();

            if (gamepad1.a){
                s1.setPower(1);
            }
            if (gamepad1.b){
                s1.setPower(0);
            }

//            long startTime = System.currentTimeMillis();
//            wc.gateauDrive(gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x);
//
//            Position p = wc.getPosition();
//            telemetry.addData("x", p.x);
//            telemetry.addData("y", p.y);
//            telemetry.addData("h", p.heading);
//            telemetry.addData("deltaTime", System.currentTimeMillis() - startTime);
        }

        @Override
        public void stop() {
//            wc.stop();
        }

    }

