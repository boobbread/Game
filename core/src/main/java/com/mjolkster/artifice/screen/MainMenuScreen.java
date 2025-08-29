package com.mjolkster.artifice.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mjolkster.artifice.GameClass;
import com.mjolkster.artifice.screen.viewports.ExpandingViewport;

public class MainMenuScreen implements Screen {

    private final GameClass game;
    private Stage stage;

    public MainMenuScreen(final GameClass game) {
        this.game = game;

        Camera uiCamera = new OrthographicCamera();
        Viewport uiViewport = new ExpandingViewport(90f, true, uiCamera);

        this.stage = new Stage(uiViewport, game.batch);;

        game.font.getData().setScale(0.03f);

        createGUI();
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

        game.batch.end();

        stage.act();
        stage.draw();

        if (Gdx.input.isTouched()) {
            game.setScreen(new GameScreen(game, 2));
            dispose();
        }
    }

    private void createGUI() {

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        // Clear previous HUD actors
        stage.clear();

        Table table = new Table();
        table.setFillParent(true);
        table.top().padTop(50);
        stage.addActor(table);

        TextButton play = new TextButton("play", skin, "default");
        table.add(play);

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
