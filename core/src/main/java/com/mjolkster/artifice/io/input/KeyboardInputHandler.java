package com.mjolkster.artifice.io.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Disposable;

public class KeyboardInputHandler implements InputHandler, Disposable {

    private final float moveSpeed = 2.5f;

    @Override
    public void update(InputState state, float delta) {
        state.reset();

        // Movement
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            state.moveY = moveSpeed * delta;
            state.idle = false;
        } else if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            state.moveY = -moveSpeed * delta;
            state.idle = false;
        } else if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            state.moveX = -moveSpeed * delta;
            state.idle = false;
        } else if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            state.moveX = moveSpeed * delta;
            state.idle = false;
        }

        // Actions
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            state.spaceAction = true;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ALT_LEFT)) {
            state.lAltAction = true;
        }

        // Zoom
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            state.zoomIn = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            state.zoomOut = true;
        }
    }

    @Override
    public boolean isController() {
        return false;
    }

    @Override
    public void dispose() {

    }
}
