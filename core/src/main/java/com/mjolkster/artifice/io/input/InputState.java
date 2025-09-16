package com.mjolkster.artifice.io.input;

public class InputState {

    public float moveX = 0f;
    public float moveY = 0f;
    public boolean spaceAction = false;
    public boolean lAltAction = false;
    public boolean zoomIn = false;
    public boolean zoomOut = false;
    public boolean idle = true;

    public void reset() {
        moveX = 0f;
        moveY = 0f;
        spaceAction = false;
        lAltAction = false;
        zoomIn = false;
        zoomOut = false;
        idle = true;
    }
}
