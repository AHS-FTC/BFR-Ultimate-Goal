package com.bfr.hardware;

import com.bfr.util.FTCUtilities;
import com.bfr.util.loggers.ControlCenter;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

import java.util.ArrayList;
import java.util.List;

//public class CurrentLimitedMotor extends Motor {
//    private enum State {
//        NORMAL(1.0),
//        THROTTLED(0.5);
//
//        private final double multiplier;
//
//        State(double multiplier) {
//            this.multiplier = multiplier;
//        }
//    }
//
//    private String deviceName;
//    private double maxCurrent;
//    private static State systemState = State.NORMAL;
//    private static List<CurrentLimitedMotor> allMotors = new ArrayList<>();
//    private static long throttleStartTime = 0;
//
//    public CurrentLimitedMotor(String deviceName, double ticksPerRotation, boolean flipped, double maxCurrent) {
//        super(deviceName, ticksPerRotation, flipped);
//
//        this.deviceName = deviceName;
//        this.maxCurrent = maxCurrent;
//        motor.setCurrentAlert(maxCurrent, CurrentUnit.AMPS);
//
//        allMotors.add(this);
//    }
//
//    @Override
//    public void setPower(double newMotorPower) {
//        super.setPower(newMotorPower * systemState.multiplier);
//    }
//
//    private static void setSystemState(State state) {
//        systemState = state;
//
//        if (state.equals(State.THROTTLED)) {
//            throttleStartTime = FTCUtilities.getCurrentTimeMillis();
//        }
//
//        for (CurrentLimitedMotor motor : allMotors) {
//            //note that this updates the powers with the new systemState, either dethrottling or throttling.
//            motor.setPower(motor.currentMotorPower);
//        }
//    }
//
//    public static void updateSystem() {
//        switch (systemState) {
//            case NORMAL:
//                break;
//            case THROTTLED:
//                if (FTCUtilities.getCurrentTimeMillis() - throttleStartTime > 1000){
//                    setSystemState(State.NORMAL);
//                }
//        }
//    }
//
//    @Override
//    public void update() {
//        if(motor.isOverCurrent()) {
//            setSystemState(State.THROTTLED);
//            ControlCenter.addNotice("Throttled system because motor " + deviceName + " exceeded " + maxCurrent + " amps");
//        }
//        super.update();
//    }
//}
