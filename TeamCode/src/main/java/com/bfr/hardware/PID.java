package com.bfr.hardware;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;


public class PID {
    double errorPrior = 0;
    double integralPrior = 0;
    int bias = 0; // just in case everything sums to zero and you need motion.


    double K_P = 1; //the proportional term is your primary term for controlling the error. Scale for error.
    double K_I = 1; //the integral term lets the controller handle errors that are accumulating over time.
    double K_D = 1; //the derivative term is looking at how your system is behaving between time intervals.

    double error;
    double integral;
    double derivative;
    double output;


    double desiredValue;
    double actualValue;
    long  iteration_time;

    public PID(double desiredValue, double actualValue, long  iteration_time) {
        this.desiredValue = desiredValue;
        this.actualValue = actualValue;
        this.iteration_time = iteration_time;
    }

    public double output(){
        while (true) {
            error = desiredValue - actualValue;
            integral = integralPrior+error*iteration_time;
            derivative = (error- errorPrior)/iteration_time;
            output = K_P*error+K_I*integral+K_D*derivative + bias;

            errorPrior = error;
            integralPrior = integral;
            return output;
        }
    }


}
