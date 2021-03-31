package com.bfr.hardware;

/**
 * bfr is rartded
 *
 * @author alex and andie
 */
public class Intake {
    private Motor intakeMotor1, intakeMotor2;
    private State state = State.STOPPED;

    private SerialServo nineTails;

    public enum State {
        STOPPED(0),
        IN(1.0),
        OUT(-.8),
        STARTER_STACK(-.3);

        public final double power;

        State(double power) {
            this.power = power;
        }
    }

//    public enum NineTailsState {
//        STORED(0),
//        DEPLOYED(1.0),
//        RING_4(1.0);
//
//        public final double position;
//
//        NineTailsState(double position) {
//            this.position = position;
//        }
//    }

    public Intake() {
        intakeMotor1 = new Motor("intake_1", 103.6,true);
        intakeMotor2 = new Motor("intake_2", 103.6, true);

    }

//    public void setNineTailsState(NineTailsState state){
//        nineTails.setPosition(state.position);
//    }

    public void setPower(double power){
        intakeMotor1.setPower(power);
        intakeMotor2.setPower(power);
    }

    public State getState() {
        return state;
    }

    public void changeState(State state){
        this.state = state;
        setPower(state.power);
    }

}
