package com.bfr.control.vision.objects;

public abstract class VisionObject {
    public abstract void release();

    /**
     * Save every intermediate mat in this VisionObject to the robot hard drive.
     */
    public abstract void dump();
}
