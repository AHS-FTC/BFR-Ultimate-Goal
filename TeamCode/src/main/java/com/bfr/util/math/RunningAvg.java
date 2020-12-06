package com.bfr.util.math;

public class RunningAvg {
    private int index = 0;

    private double[] vals;

    public RunningAvg(int length) {
        vals = new double[length];
    }

    public double calc(double val){
        vals[index] = val;

        if(++index >= vals.length){
            index = 0;
        }

        double sum = 0;

        for (int i = 0; i < vals.length; i++) {
            sum += vals[i];
        }


        return sum / vals.length;
    }
}
