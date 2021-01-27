package com.bfr.opMode;

import com.bfr.hardware.Intake;
import com.bfr.hardware.Robot;
import com.bfr.hardware.Shooter;
import com.bfr.hardware.WestCoast;
import com.bfr.util.Controller;
import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import static com.bfr.hardware.Intake.State.*;
import static com.bfr.util.Controller.Input.*;

@TeleOp(name="Dif TeleOp", group="Iterative Opmode")
//@Disabled
public class DifTeleOp extends OpMode {
    private Robot robot;
    private WestCoast westCoast;

    //adb connect 192.168.43.1:5555

    @Override
    public void init() {
        FTCUtilities.setOpMode(this);

        Controller controller1 = FTCUtilities.getController1();
        Controller controller2 = FTCUtilities.getController2();

        WestCoast.setDefaultMode(WestCoast.Mode.DRIVER_CONTROL);

        robot = new Robot();
        Intake intake = robot.getIntake();
        westCoast = robot.getWestCoast();

        Shooter shooter = robot.getShooter();
        controller1.setAction(A, shooter::runIndexerServos);

        controller1.setAction(B, () -> robot.nextCycleState());


        controller1.setAction(Y, () -> {
            if (shooter.isRunning()){
                shooter.stopShooter();
            } else {
                shooter.runShooter();
            }
        });

        controller1.setAction(R_BUMPER, () -> {
            if(intake.getState() == STOPPED || intake.getState() == OUT){
                intake.changeState(IN);
            } else {
                intake.changeState(STOPPED);
            }
        });

        controller1.setAction(L_BUMPER, () -> {
            if(intake.getState() == STOPPED || intake.getState() == IN){
                intake.changeState(OUT);
            } else {
                intake.changeState(STOPPED);
            }
        });

        controller1.setAction(X, () -> {
            robot.autoAim();
        });
    }

    @Override
    public void init_loop() {
    }

    @Override
    public void start() {
        westCoast.startDriverControl();
    }

    @Override
    public void loop() {
        Controller.update();
        robot.update();
    }

    @Override
    public void stop() {
        robot.stopAll();
    }

}

