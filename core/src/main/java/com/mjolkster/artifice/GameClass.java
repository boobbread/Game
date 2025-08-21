package com.mjolkster.artifice;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.mjolkster.artifice.screen.MainMenuScreen;

public class GameClass extends Game {

    public SpriteBatch batch;
    public BitmapFont font;
    public ScalingViewport viewport;

    @Override
    public void create() {
        batch = new SpriteBatch();
        // use libGDX's default font
        font = new BitmapFont();
        viewport = new ScalingViewport(Scaling.fill,8, 5);

        //font has 15pt, but we need to scale it to our viewport by ratio of viewport height to screen height
        font.setUseIntegerPositions(false);
        font.getData().setScale(viewport.getWorldHeight() / Gdx.graphics.getHeight());

        this.setScreen(new MainMenuScreen(this));
    }

    public void render() {
        super.render();
    }

    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
