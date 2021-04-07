package com.bfr.util.math;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LinearInterpolationTest {

    @Test
    void interpolatePower() {
        List<Point> rpmPowerModel = new ArrayList<>();
        rpmPowerModel.add(new Point(500,.2));
        rpmPowerModel.add(new Point(1000, .4));
        rpmPowerModel.add(new Point(2000, .6));
        rpmPowerModel.add(new Point(3200, .7));



        LinearInterpolation linearInterpolation = new LinearInterpolation(rpmPowerModel);

        assertEquals(.5, linearInterpolation.interpolatePower(1500), .01);
        assertEquals(.65, linearInterpolation.interpolatePower(2600), .01);
    }
}