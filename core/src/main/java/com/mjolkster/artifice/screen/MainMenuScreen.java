package com.mjolkster.artifice.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mjolkster.artifice.GameClass;

public class MainMenuScreen implements Screen {

    private final GameClass game;

    public MainMenuScreen(final GameClass game) {
        this.game = game;

        game.font.getData().setScale(0.03f);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);

        game.viewport.apply();
        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);

        game.batch.begin();

        float centerX = game.viewport.getWorldWidth() / 2;
        float centerY = game.viewport.getWorldHeight() / 2;

        // Adjust offsets to roughly center the text
        game.font.draw(game.batch, "Welcome to RatGame!", centerX - 2f, centerY + 1f);
        game.font.draw(game.batch, "Tap anywhere to begin!", centerX - 2f, centerY - 0.5f);

        game.batch.end();

        if (Gdx.input.isTouched()) {
            game.setScreen(new GameScreen(game));
            dispose();
        }
    }

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }
}
