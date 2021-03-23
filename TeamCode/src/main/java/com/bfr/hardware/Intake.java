package com.bfr.hardware;

/**
 * bfr is rartded
 *
 * @author alex and andie
 */
public class Intake {
    private Motor intakeMotor;
    private State state = State.STOPPED;

    private SerialServo nineTails;

    public enum State {
        STOPPED(0),
        IN(1.0),
        OUT(-.8),
        STARTER_STACK(-1.0);

        public final double power;

        State(double power) {
            this.power = power;
        }
    }

    public enum NineTailsState {
        STORED(0),
        DEPLOYED(.85),
        RING_4(1.0);

        public final double position;

        NineTailsState(double position) {
            this.position = position;
        }
    }

    public Intake() {
        intakeMotor = new Motor("intake", 103.6,true);
        nineTails = new SerialServo("intake_actuator", true);
        setNineTailsState(NineTailsState.STORED);
    }

    public void setNineTailsState(NineTailsState state){
        nineTails.setPosition(state.position);
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
