package com.bfr.hardware;

import com.bfr.util.FTCUtilities;
import com.bfr.util.OpModeType;

public class WobbleArm {
    private SerialServo gripper;
    private SerialServo arm;

    private static final double DEPLOYED = 1.0;
    private static final double STORED = 0.0;
    private static final double HOLDING = 0.35;

    private long lastStateChange;
    private State state;

    public enum State {
        STORED,
        STORED_IDLE,
        HOLDING,
        DEPLOYED_OPEN,
        DEPLOYED_CLOSED,
        RETRACTING;
    }
    
    public WobbleArm() {
        gripper = new SerialServo("gripper", false);
        arm = new SerialServo("arm", true);
        lastStateChange = FTCUtilities.getCurrentTimeMillis();

        setState(State.STORED);
    }

    public void setState(State state){
        this.state = state;

        lastStateChange = FTCUtilities.getCurrentTimeMillis();
        switch (state){
            case STORED:
                gripper.setPosition(1);
                arm.setPosition(STORED);
                break;
            case HOLDING:
                gripper.setPosition(1);
                arm.setPosition(HOLDING);
                break;
            case DEPLOYED_OPEN:
                gripper.setPosition(0);
                arm.setPosition(DEPLOYED);
                break;
            case DEPLOYED_CLOSED:
                gripper.setPosition(1);
                arm.setPosition(DEPLOYED);
                break;
            case RETRACTING:
                gripper.setPosition(0);
                arm.setPosition(HOLDING);
        }
    }

    public State getState() {
        return state;
    }

    public void update(){

        switch (state) {
            case STORED:
                if (FTCUtilities.getCurrentTimeMillis() - lastStateChange > 500) {
                    arm.setPosition(0);
                    setState(State.STORED_IDLE);
                }
                break;
            case DEPLOYED_CLOSED:
                if(FTCUtilities.getOpModeType().equals(OpModeType.TELE)){
                    if (FTCUtilities.getCurrentTimeMillis() - lastStateChange > 250) {
                        setState(State.HOLDING);
                    }
                }
                break;
            case RETRACTING:
                if (FTCUtilities.getCurrentTimeMillis() - lastStateChange > 500) {
                    setState(State.HOLDING);
                }
                break;
        }
    }
}
