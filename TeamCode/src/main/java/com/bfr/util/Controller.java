package com.bfr.util;

import com.qualcomm.robotcore.hardware.Gamepad;

public class Controller {
    private Action onA, onX, onY, onB;
    private Gamepad gamepad;

    public Controller(Gamepad gamepad){
        this.gamepad = gamepad;
    }

    public void setOnA(Action onA) {
        this.onA = onA;
    }

    public void setOnX(Action onX) {
        this.onX = onX;
    }

    public void setOnY(Action onY) {
        this.onY = onY;
    }

    public void setOnB(Action onB) {
        this.onB = onB;
    }

    public void update(){
        if (gamepad.a){
            onA.onAction();
        }

        if (gamepad.b){
            onB.onAction();
        }

        if (gamepad.x){
            onX.onAction();
        }

        if (gamepad.y){
            onY.onAction();
        }
    }

    public interface Action {
        void onAction();
    }
}
