package com.mjolkster.artifice.screen.viewports;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class AspectRatioViewport extends ExtendViewport {
    private final float worldHeight;

    public AspectRatioViewport(float worldHeight, Camera camera) {
        super(0, worldHeight, camera);
        this.worldHeight = worldHeight;
    }

    @Override
    public void update(int screenWidth, int screenHeight, boolean centerCamera) {
        float aspect = (float) screenWidth / (float) screenHeight;
        float worldWidth = worldHeight * aspect; // adjust width based on aspect ratio
        setWorldSize(worldWidth, worldHeight);
        super.update(screenWidth, screenHeight, centerCamera);
    }
}

