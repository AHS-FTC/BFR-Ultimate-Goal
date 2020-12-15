package com.bfr.hardware;

/**
 * Shooter
 */
public class Intake {
    private Motor intakeMotor;
    private State state = State.STOPPED;

    public enum State {
        STOPPED(0),
        IN(1.0),
        OUT(-.8);

        public final double power;

        State(double power) {
            this.power = power;
        }
    }

    public Intake() {
        intakeMotor = new Motor("intake", 103.6,true);
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
