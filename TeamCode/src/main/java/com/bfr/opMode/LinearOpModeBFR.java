package com.bfr.opMode;

import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

/**
 * Subclass of LinearOpMode that does some nice things automatically and has nicer structure.
 */
public abstract class LinearOpModeBFR extends LinearOpMode {
    @Override
    public final void runOpMode(){

        //try catch prevents FTC SDK from swallowing errors
        //relays them to system.err. if using ADB, this will print to the computer.
        try {
            FTCUtilities.setOpMode(this);
            initialize();

            waitForStart();

            run();

            teardown();
        } catch (Throwable t){
            t.printStackTrace(System.err);
        }

    }

    protected abstract void initialize();
    protected abstract void run();

    /**
     * optional override, called after run()
     */
    protected void teardown(){}
}
