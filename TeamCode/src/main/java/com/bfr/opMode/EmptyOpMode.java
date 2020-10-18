package com.bfr.opMode;

import com.bfr.hardware.Robot;
import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "Empty OpMode Time Test", group = "Iterative Opmode")
//@Disabled
public class EmptyOpMode extends OpMode {
    Robot robot;

    @Override
    public void init() {
        FTCUtilities.setOpMode(this);
        robot = new Robot();
    }

    @Override
    public void init_loop() {
    }

    @Override
    public void start() {
    }

    @Override
    public void loop() {
        long before = System.nanoTime();
        robot.update();
        long after = System.nanoTime();
        System.out.println(after - before);
    }

    @Override
    public void stop() {
    }

}

