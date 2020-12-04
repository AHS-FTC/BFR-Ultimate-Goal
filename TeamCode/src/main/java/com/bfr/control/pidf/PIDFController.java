package com.bfr.control.pidf;

import com.acmerobotics.dashboard.FtcDashboard;
import com.bfr.util.FTCUtilities;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.opencv.core.Mat;

public class PIDFController {
    private final PIDFConfig constants;
    private double setPoint;
    private long lastTime;

    private Telemetry dashboardTelemetry = FtcDashboard.getInstance().getTelemetry();

    private double lastError;
    private double errorSum = 0.0;
    private double initialValue;

    private double stabilityThreshold;

    private double derivative;

    private RingBuffer<PIDFDataPoint> derivativeBuffer;

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
                initialValue,
                1);

    }

    /**
     * @param constants Contains PIDF constant information, and can be updated live for tuning or other purposes.
     *                  You can also write a feedforward model into it.
     * @param setPoint the target value for this controller.
     * @param initialValue the first sensor reading at time of construction. Used to initialize system.
     */
    public PIDFController(PIDFConfig constants, double setPoint, double initialValue, int bufferLength){
        this.constants = constants;
        this.setPoint = setPoint;
        reset(initialValue);

        PIDFDataPoint pidfDataPoint = new PIDFDataPoint(lastError, lastTime);
        derivativeBuffer = new RingBuffer<>(bufferLength, pidfDataPoint);
    }

    public double getOutput(double current){
        double error = setPoint - current;

        long currentTime = FTCUtilities.getCurrentTimeMillis();

        PIDFDataPoint bufferedPoint = derivativeBuffer.insert(new PIDFDataPoint(error, currentTime));

        //using bufferedValues to smooth derivative
        derivative = (error - bufferedPoint.value) / (currentTime - bufferedPoint.time);

        //using deltaTime normalizes for time
        if (isStable() && (initialValue != current)){
            errorSum += error * (currentTime - lastTime);
        }

        lastTime = currentTime;

        double pCorrection = constants.kP() * error;
        double iCorrection = constants.kI() * errorSum;
        double dCorrection = constants.kD() * derivative;

        dashboardTelemetry.addData("pcorr", pCorrection);
        dashboardTelemetry.addData("icorr", iCorrection);
        dashboardTelemetry.addData("dcorr", dCorrection);

        return pCorrection + iCorrection + dCorrection + constants.feedForward(setPoint);
    }

    public void setStabilityThreshold(double threshold){
        stabilityThreshold = threshold;
    }

    public boolean isStable(){
        System.out.println("derivative " + derivative);
        return (Math.abs(derivative) < stabilityThreshold);
    }

    public void reset(double currentValue){
        errorSum = 0.0;
        initialValue = currentValue;
        lastError = setPoint - currentValue;
        lastTime = FTCUtilities.getCurrentTimeMillis();
    }

}
