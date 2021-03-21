package com.bfr.hardware;

/**
 * Shooter
 */
public class Intake {
    private Motor intakeMotor;
    private State state = State.STOPPED;

    private SerialServo actuator;

    public enum State {
        STOPPED(0),
        IN(1.0),
        OUT(-.8),
        STARTER_STACK(-.1);

        public final double power;

        State(double power) {
            this.power = power;
        }
    }

    public Intake() {
        intakeMotor = new Motor("intake", 103.6,true);
        actuator = new SerialServo("intake_actuator", true);
        actuator.setPosition(0);
    }

    public void extend(){
        actuator.setPosition(.85);
    }

    public void setPower(double power){
        intakeMotor.setPower(power);
    }

    public State getState() {
        return state;
    }

    public void changeState(State state){
        this.state = state;
        setPower(state.power);
    }

}
