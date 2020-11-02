package com.bfr.hardware;

import com.qualcomm.robotcore.hardware.DcMotor;

/**
 * Shooter
 */
public class Intake {
    private Motor intakeMotor;

    public Intake() {
        intakeMotor = new Motor("intake", 103.6,true);
    }

    public void brakeMotor(){
        intakeMotor.setPower(0);
    }

    public void setPower(double power){
        intakeMotor.setPower(power);
    }

    public void update(){
    }
}
