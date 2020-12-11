package com.bfr.control.pidf;

/**
 * Contains information about the constants and properties of a system.
 * Use by creating an anonymous inner class that extends, then override as you'd like.
 *
 * Using this class enables constants and the feedforward model to change with time. Useful for tuning
 * and more sophisticated behaviour.
 */
public interface PIDFConfig {
    double kP();
    double kI();
    double kD();
    double feedForward(double setPoint, double error);
}
