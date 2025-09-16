package com.mjolkster.artifice.io.input;

public interface InputHandler {
    void update(InputState state, float delta);
    boolean isController();
    void dispose();
}
