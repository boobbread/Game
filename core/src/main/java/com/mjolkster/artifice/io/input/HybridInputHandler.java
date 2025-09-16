package com.mjolkster.artifice.io.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;

public class HybridInputHandler implements InputHandler, Disposable {
    private final KeyboardInputHandler keyboardHandler = new KeyboardInputHandler();
    private final ControllerInputHandler controllerHandler;
    private boolean useController = false;
    private Controller controller;

    public HybridInputHandler(Stage uiStage) {
        if (!Controllers.getControllers().isEmpty()) {
            controller = Controllers.getControllers().first();
            controllerHandler = new ControllerInputHandler(controller, uiStage);
        } else {
            controllerHandler = null;
        }
    }

    @Override
    public void update(InputState state, float delta) {
        // Detect keyboard activity
        if (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) {
            useController = false;
        }

        // Detect controller activity
        if (controller != null) {
            if (controller.getButton(0) || controller.getButton(1) ||
                Math.abs(controller.getAxis(0)) > 0.2f || Math.abs(controller.getAxis(1)) > 0.2f) {
                useController = true;
            }
        }

        // Delegate to the active handler
        if (useController && controllerHandler != null) {
            controllerHandler.update(state, delta);
        } else {
            keyboardHandler.update(state, delta);
        }
    }

    @Override
    public boolean isController() {
        return useController;
    }

    @Override
    public void dispose() {

    }

    public ControllerInputHandler getControllerHandler() {
        return controllerHandler;
    }
}
