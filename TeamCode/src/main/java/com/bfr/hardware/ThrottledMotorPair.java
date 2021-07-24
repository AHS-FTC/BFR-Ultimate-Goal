package com.bfr.hardware;

import com.acmerobotics.dashboard.FtcDashboard;
import com.bfr.control.pidf.RingBuffer;

public class ThrottledMotorPair extends MotorPair {
    private double manuallySetPower = 0.0;
    private RingBuffer<Boolean> currentHistory = new RingBuffer<>(20, false);

    public ThrottledMotorPair(Motor m1, Motor m2) {
        super(m1, m2);
    }

    public void update() {
        currentHistory.insert(m1.isOverCurrent());
        double throttle = mapPctToPower(pctOfHistoryAboveCurrent());
        FtcDashboard.getInstance().getTelemetry().addData("Throttle " + this.toString(), throttle);
        super.setPower(manuallySetPower * throttle);
    }

    @Override
    public void setPower(double power){
        manuallySetPower = power;
    }

    double pctOfHistoryAboveCurrent() {
        int trues = 0;
        for (Boolean aboveCurrent : currentHistory.getBuffer()) {
            if (aboveCurrent) {
                trues++;
            }
        }

        return trues / (double) currentHistory.getBuffer().size();
    }

    //as pct of history above the current threshold increases, throttle more.
    double mapPctToPower(double pct) {

        //100 percent above current thresh = 75 percent throttle
        //0 percent above current thresh = 0 percent throttle
        //linear relationship
        return 1 + ((-0.5) * pct);
    }

}
