package com.bfr.opMode;

import com.bfr.hardware.Motor;
import com.bfr.hardware.WestCoast;
import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name="Prototype TeleOp", group="Iterative Opmode")
//@Disabled
public class PrototypeOp extends OpMode {
    //adb connect 192.168.43.1:5555

    private CRServo s1, s2, s3;

    @Override
    public void init() {
        s1 = hardwareMap.get(CRServo.class,"s1");
        s2 = hardwareMap.get(CRServo.class,"s2");
        s3 = hardwareMap.get(CRServo.class,"s3");
    }

    @Override
    public void init_loop() {
    }

    @Override
    public void start() {
        s1.setPower(1);
        s2.setPower(1);
        s3.setPower(1);
    }

    @Override
    public void loop() {

    }

    @Override
    public void stop() {
//            wc.stop();
    }

    }

