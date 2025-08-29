package com.mjolkster.artifice.screen.viewports;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class ExpandingViewport extends ScreenViewport {
    private final float fixedSize;
    private final boolean fixHeight;

    /**
     * @param fixedSize The size of the fixed dimension (e.g., 900 units if fixHeight = true).
     * @param fixHeight If true, the height stays fixed and width expands; if false, width stays fixed and height expands.
     * @param camera    The camera to use.
     */
    public ExpandingViewport(float fixedSize, boolean fixHeight, Camera camera) {
        super(camera);
        this.fixedSize = fixedSize;
        this.fixHeight = fixHeight;
    }

    @Override
    public void update(int screenWidth, int screenHeight, boolean centerCamera) {
        if (fixHeight) {
            float aspect = (float) screenWidth / (float) screenHeight;
            float worldWidth = fixedSize * aspect;
            setWorldSize(worldWidth, fixedSize);
        } else {
            float aspect = (float) screenHeight / (float) screenWidth;
            float worldHeight = fixedSize * aspect;
            setWorldSize(fixedSize, worldHeight);
        }

        super.update(screenWidth, screenHeight, centerCamera);
    }
}
