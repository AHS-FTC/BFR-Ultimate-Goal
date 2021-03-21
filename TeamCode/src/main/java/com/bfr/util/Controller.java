package com.bfr.util;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bfr.util.Controller.Input.*;

public class Controller {
    private Map<Input, Button> buttons = new HashMap<>();
    private Gamepad gamepad;

    private static List<Controller> instances = new ArrayList<>();

    public enum Input {
        A, B, X, Y,
        L_BUMPER, R_BUMPER,
        DPAD_UP, DPAD_DN, DPAD_L, DPAD_R;
    }

    public Controller(Gamepad gamepad){
        instances.add(this);
        this.gamepad = gamepad;

        buttons.put(A, new Button(() -> gamepad.a));
        buttons.put(B, new Button(() -> gamepad.b));
        buttons.put(X, new Button(() -> gamepad.x));
        buttons.put(Y, new Button(() -> gamepad.y));
        buttons.put(L_BUMPER, new Button(() -> gamepad.left_bumper));
        buttons.put(R_BUMPER, new Button(() -> gamepad.right_bumper));
        buttons.put(DPAD_UP, new Button(() -> gamepad.dpad_up));
        buttons.put(DPAD_DN, new Button(() -> gamepad.dpad_down));
        buttons.put(DPAD_L, new Button(() -> gamepad.dpad_left));
        buttons.put(DPAD_R, new Button(() -> gamepad.dpad_right));
    }

    public boolean areSticksNonZero(){
        return (gamepad.left_stick_y != 0.0 || gamepad.right_stick_x != 0.0);
    }

    public static void update(){
        for (Controller c : instances) {
            c.updateInstance();
        }
    }

    private void updateInstance(){
        for (Button b : buttons.values()) {
            b.update();
        }
    }

    public void setAction(Input input, Action action){
        buttons.get(input).action = action;
    }

    public interface Action {
        void onAction();
    }

    private class EmptyAction implements Action{
        @Override
        public void onAction() {

        }
    }


    /**
     * Specify the gamepad attribute to check for this Button.
     * Example: gamepad1.a;
     */
    private interface ButtonCheck {
        boolean check();
    }

    private class Button{
        private boolean last = false;
        private final ButtonCheck buttonCheck;
        private Action action = new EmptyAction();

        public void update(){
            if(!last && buttonCheck.check()) {
                action.onAction();
            }
            last = buttonCheck.check();
        }

        public Button(ButtonCheck buttonCheck) {
            this.buttonCheck = buttonCheck;
        }
    }
}
