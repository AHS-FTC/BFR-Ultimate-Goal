package com.bfr.opMode;

import com.bfr.control.path.Position;
import com.bfr.hardware.Intake;
import com.bfr.hardware.Robot;
import com.bfr.hardware.Shooter;
import com.bfr.hardware.WestCoast;
import com.bfr.hardware.WobbleArm;
import com.bfr.util.AllianceColor;
import com.bfr.util.Controller;
import com.bfr.util.FTCUtilities;
import com.bfr.util.loggers.ControlCenter;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import static com.bfr.hardware.Intake.State.*;
import static com.bfr.util.Controller.Input.*;

@TeleOp(name="TeleOp", group="Iterative Opmode")
//@Disabled
public class DTTeleOp extends OpMode {
    private Robot robot;
    private WestCoast westCoast;
    private Shooter shooter;
    private WobbleArm wobbleArm;

    //adb connect 192.168.43.1:5555

    @Override
    public void init() {
        FTCUtilities.setOpMode(this);

        if (gamepad2.x){
            FTCUtilities.setAllianceColor(AllianceColor.BLUE);
        }

        if (gamepad2.b){
            FTCUtilities.setAllianceColor(AllianceColor.RED);
        }

        Controller controller1 = FTCUtilities.getController1();
        Controller controller2 = FTCUtilities.getController2();

        robot = new Robot(new Position(0, 0, Math.toRadians(-90)));
        Intake intake = robot.getIntake();
        westCoast = robot.getWestCoast();
        westCoast.setDefaultState(WestCoast.State.DRIVER_CONTROL);
        westCoast.setTurnMode(WestCoast.MovementMode.FAST);
        westCoast.setRampdownMode(WestCoast.MovementMode.FAST);

        wobbleArm = robot.getWobbleArm();

        shooter = robot.getShooter();
        controller1.setAction(A, () -> shooter.runIndexerServos());

        controller1.setAction(B, () -> robot.nextCycleState());

        controller1.setAction(Y, () -> {
            if (!shooter.isState(Shooter.ShooterState.RESTING)){
                shooter.setState(Shooter.ShooterState.RESTING);
            } else {
                shooter.setState(Shooter.ShooterState.STANDARD);
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

        controller1.setAction(DPAD_L, () -> {
            shooter.setState(Shooter.ShooterState.POWERSHOT);
        });

        controller1.setAction(X, () -> {
            robot.autoAim();
        });

        controller1.setAction(DPAD_DN, () -> robot.setState(Robot.State.GO_TO_HOME));


        //controller2.setAction(B, () ->);

        controller2.setAction(Y, () -> wobbleArm.setState(WobbleArm.State.DEPLOYED_CLOSED));

        controller2.setAction(X, () -> robot.getBrolafActuator().setPosition(1));

        controller2.setAction(A, () -> robot.getMb1242System().runSystem());

        controller2.setAction(DPAD_R, () -> ControlCenter.incrementOffset(Math.toRadians(1)));
        controller2.setAction(DPAD_L, () -> ControlCenter.incrementOffset(Math.toRadians(-1)));

        controller2.setAction(DPAD_UP, () -> robot.getIntakingPoint().y += 1.0);
        controller2.setAction(DPAD_DN, () -> robot.getIntakingPoint().y -= 1.0);
    }

    @Override
    public void init_loop() {
    }

    @Override
    public void start() {
        westCoast.startDriverControl();
        shooter.setState(Shooter.ShooterState.STANDARD);
//        robot.getIntake().setNineTailsState(Intake.NineTailsState.DEPLOYED);
    }

    @Override
    public void loop() {
        Controller.update();
        robot.update();
        ControlCenter.update();
    }

    @Override
    public void stop() {
        robot.stopAll();
    }

}

