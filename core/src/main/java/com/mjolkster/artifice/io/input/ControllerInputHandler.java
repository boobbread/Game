package com.mjolkster.artifice.io.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;

public class ControllerInputHandler implements InputHandler, Disposable {

    private final float moveSpeed = 2.5f;
    private Controller controller;
    private Stage uiStage;
    private boolean dpadLeftPressed = false;
    private boolean dpadRightPressed = false;
    private boolean aPressed = false;

    private boolean aDown = false;
    private long lastAPressTime = 0L;
    private static final long DOUBLE_TAP_THRESHOLD = 250; // ms

    public ControllerInputHandler(Controller controller, Stage uiStage) {
        this.controller = controller;
        this.uiStage = uiStage;
    }

    @Override
    public void update(InputState state, float delta) {
        state.reset();
        if (controller == null) return;

        float axisX = controller.getAxis(0);
        float axisY = controller.getAxis(1);

        // Deadzone check
        if (Math.abs(axisX) < 0.2f) axisX = 0;
        if (Math.abs(axisY) < 0.2f) axisY = 0;

        // Only allow one direction
        if (Math.abs(axisX) > Math.abs(axisY)) {
            state.moveX = Math.signum(axisX) * moveSpeed * delta;
            state.idle = false;
        } else if (Math.abs(axisY) > 0) {
            state.moveY = -Math.signum(axisY) * moveSpeed * delta;
            state.idle = false;
        }

        // Buttons
        if (controller.getButton(controller.getMapping().buttonX)) { // X button
            state.spaceAction = true;
        }
        if (controller.getButton(controller.getMapping().buttonY)) { // Y button
            state.lAltAction = true;
        }

        // UI interaction
        // D-pad LEFT
        boolean leftNow = controller.getButton(controller.getMapping().buttonDpadLeft);
        if (leftNow && !dpadLeftPressed) {
            uiStage.keyDown(Input.Keys.LEFT);
        }
        dpadLeftPressed = leftNow;

        // D-pad RIGHT
        boolean rightNow = controller.getButton(controller.getMapping().buttonDpadRight);
        if (rightNow && !dpadRightPressed) {
            uiStage.keyDown(Input.Keys.RIGHT);
        }
        dpadRightPressed = rightNow;

        // A button (Enter / AT)
        boolean aNow = controller.getButton(controller.getMapping().buttonA);

        if (aNow && !aDown) { // just pressed
            long now = System.currentTimeMillis();
            if (now - lastAPressTime <= DOUBLE_TAP_THRESHOLD) {
                // Double tap detected
                uiStage.keyDown(Input.Keys.AT);
                lastAPressTime = 0L; // reset
            } else {
                // Single tap
                uiStage.keyDown(Input.Keys.ENTER);
                lastAPressTime = now;
            }
        }

        aDown = aNow;

        if (controller.getButton(controller.getMapping().buttonB)) {
            uiStage.keyDown(Input.Keys.ESCAPE);
        }

    }

    @Override
    public boolean isController() {
        return true;
    }

    @Override
    public void dispose() {

    }

    public void vibrate() {
        controller.startVibration(200, 0.5f);
    }

    public boolean isaPressed() {
        return aDown;
    }
}
