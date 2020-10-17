package com.bfr.hardware;

import com.bfr.control.path.Position;
import com.qualcomm.robotcore.hardware.DcMotor;

/**
 * Shooter
 */
public class Shooter {
    private Motor shooterMotor1, shooterMotor2;
    private double rpm;

    public Shooter() {
        shooterMotor1 = new Motor("s1", 41.0,true);
        shooterMotor2 = new Motor("s2", 41.0,true);

        shooterMotor1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        shooterMotor2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
    }

    public void brakeMotors(){
        shooterMotor1.setPower(0);
        shooterMotor2.setPower(0);
    }

    public void setPower(double power){
        shooterMotor1.setPower(power);
        shooterMotor2.setPower(power);
    }

    public void update(){
        rpm = shooterMotor1.getRotations();
    }
}
