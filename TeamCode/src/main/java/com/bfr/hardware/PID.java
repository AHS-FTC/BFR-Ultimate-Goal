package com.bfr.hardware;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import java.text.SimpleDateFormat;


public class PID {
    double errorPrior = 0;
    double integralPrior = 0;
    int bias = 0; // just in case everything sums to zero and you need motion.


    final double K_P = 1; //the proportional term is your primary term for controlling the error. Scale for error.
    final double K_I = 1; //the integral term lets the controller handle errors that are accumulating over time.
    final double K_D = 1; //the derivative term is looking at how your system is behaving between time intervals.

    double error;
    double integral;
    double derivative;
    double output;

    private long startDate;
    private long startNanoseconds;
    private SimpleDateFormat dateFormat;

    double desiredValue;
    double actualValue;
    final long  iteration_time_microseconds = (System.nanoTime() - this.startNanoseconds)/1000;

    public PID(double desiredValue, double actualValue) {
        this.desiredValue = desiredValue;
        this.actualValue = actualValue;
    }

    private void MicroTimestamp(){
        this.startDate = System.currentTimeMillis();
        this.startNanoseconds = System.nanoTime();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    }

    public double output(){
        while (true) {
            error = desiredValue - actualValue;
            integral = integralPrior+error*iteration_time_microseconds;
            derivative = (error- errorPrior)/iteration_time_microseconds;
            output = K_P*error+K_I*integral+K_D*derivative + bias;

            errorPrior = error;
            integralPrior = integral;
            return output;
        }
    }


}
