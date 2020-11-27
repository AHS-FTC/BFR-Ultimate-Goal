package com.bfr.control.pidf;

import com.bfr.util.FTCUtilities;

public class PIDFController {
    private final PIDFConfig constants;
    private double setPoint;
    private long lastTime = FTCUtilities.getCurrentTimeMillis();

    private double lastError;
    private double errorSum = 0.0;

    /**
     * Static controller constructor - This constructor creates a PID controller with fixed values.
     * It cannot be tuned live. If you want to tune with FTC Dashboard (or any other live method)
     * use the other constructor.
     *
     * Additionally, if you'd like to program a feedforward model into the controller, use the other constructor.
     */
    public PIDFController(double kP, double kI, double kD, double setPoint, double initialValue) {
        this(new PIDFConfig() {
                    @Override
                    public double kP() {
                        return kP;
                    }

                    @Override
                    public double kI() {
                        return kI;
                    }

                    @Override
                    public double kD() {
                        return kD;
                    }

                    @Override
                    public double feedForward(double setPoint) {
                        return 0;
                    }
                },
                setPoint,
                initialValue);
    }

    /**
     * @param constants Contains PIDF constant information, and can be updated live for tuning or other purposes.
     *                  You can also write a feedforward model into it.
     * @param setPoint the target value for this controller.
     * @param initialValue the first sensor reading at time of construction. Used to initialize system.
     */
    public PIDFController(PIDFConfig constants, double setPoint, double initialValue){
        this.constants = constants;
        this.setPoint = setPoint;
        lastError = setPoint - initialValue;
    }

    public double getOutput(double current){
        double error = setPoint - current;

        errorSum += error;

        long currentTime = FTCUtilities.getCurrentTimeMillis();
        long deltaTime = currentTime - lastTime;

        //using deltaTime normalizes for time
        double integral = errorSum * deltaTime;
        double derivative = (error - lastError) / deltaTime;

        lastError = error;
        lastTime = currentTime;
        return (constants.kP() * error) + (constants.kI() * integral) + (constants.kD() *  derivative) + constants.feedForward(setPoint);
    }
}
