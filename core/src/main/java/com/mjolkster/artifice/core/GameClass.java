package com.mjolkster.artifice.core;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.mjolkster.artifice.registry.registries.ItemRegistry;
import com.mjolkster.artifice.graphics.screen.MainMenuScreen;

/**
 * Main class
 */

public class GameClass extends Game {

    public SpriteBatch batch;
    public BitmapFont font;
    public ScalingViewport viewport;
    private boolean isFullscreen = true;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        viewport = new FitViewport(16, 9);

        setFullscreen();
        ItemRegistry.init();

        font.setUseIntegerPositions(false);
        font.getData().setScale(viewport.getWorldHeight() / Gdx.graphics.getHeight());

        this.setScreen(new MainMenuScreen(this));
    }

    private void setFullscreen() {
        DisplayMode displayMode = Gdx.graphics.getDisplayMode();

        if (Gdx.graphics.setFullscreenMode(displayMode)) {
            isFullscreen = true;
            Gdx.app.log("GameClass", "Fullscreen mode enabled");
        } else {
            setWindowedMode();
        }
    }

    private void setWindowedMode() {
        if (Gdx.graphics.setWindowedMode(1280, 720)) {
            isFullscreen = false;
            Gdx.app.log("GameClass", "Windowed mode enabled (1280x720)");
        }
    }

    public void toggleFullscreen() {
        if (isFullscreen) {
            setWindowedMode();
        } else {
            setFullscreen();
        }
        updateFontScale();
    }

    private void updateFontScale() {
        font.getData().setScale(viewport.getWorldHeight() / Gdx.graphics.getHeight());
    }

    public void render() {
        super.render();
    }

    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
