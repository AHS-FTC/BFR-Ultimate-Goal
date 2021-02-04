package com.bfr.hardware;

public class WobbleArm {
    private SerialServo gripper;
    private SerialServo arm;

    public enum State {
        STORED,
        HOLDING,
        DEPLOYED_OPEN,
        DEPLOYED_CLOSED,
        RETRACTING;
    }

    public WobbleArm() {
        gripper = new SerialServo("gripper", false);
        arm = new SerialServo("arm", true);
    }

    public void setState(State state){
        switch (state){
            case STORED:
                gripper.setPosition(1);
                arm.setPosition(0);
                break;
            case HOLDING:
                gripper.setPosition(1);
                arm.setPosition(.3);
                break;
            case DEPLOYED_OPEN:
                gripper.setPosition(0);
                arm.setPosition(1);
                break;
            case DEPLOYED_CLOSED:
                gripper.setPosition(1);
                arm.setPosition(1);
                break;
            case RETRACTING:
                gripper.setPosition(0);
                arm.setPosition(.3);
        }
    }
}
