package com.mjolkster.artifice.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mjolkster.artifice.GameClass;
import com.mjolkster.artifice.screen.viewports.ExpandingViewport;

public class MainMenuScreen implements Screen {

    private final GameClass game;
    private Stage stage;
    private Texture backgroundTexture;

    public MainMenuScreen(final GameClass game) {
        this.game = game;

        Camera uiCamera = new OrthographicCamera();
        Viewport uiViewport = new ExpandingViewport(90f, true, uiCamera);

        this.stage = new Stage(uiViewport, game.batch);

        game.font.getData().setScale(0.03f);

        // load your flat background texture
        backgroundTexture = new Texture(Gdx.files.internal("GUI/MainMenu.png"));

        createGUI();
    }

    @Override
    public void show() {
        // Input handling goes to the stage
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);

        stage.act(delta);
        stage.draw();
    }

    private void createGUI() {
        Skin skin = new Skin(Gdx.files.internal("GUI/GUISkin.json"));
        stage.clear();

        // Background image
        Image background = new Image(new TextureRegionDrawable(backgroundTexture));
        background.setFillParent(true);
        stage.addActor(background);

        // Menu buttons
        Table table = new Table();
        table.setFillParent(true);
        table.center(); // center on screen
        stage.addActor(table);

        TextButton play = new TextButton("Play", skin, "transparent");
        TextButton options = new TextButton("Options", skin, "transparent");
        TextButton quit = new TextButton("Quit", skin, "transparent");

        // Add with spacing
        table.add(play).width(158).height(53).padTop(310).padBottom(32).row();
        table.add(options).width(158).height(53).padBottom(32).row();
        table.add(quit).width(158).height(53).padBottom(50).row();

        // Add listeners
        play.addListener(event -> {
            if (event.toString().equals("touchDown")) {
                game.setScreen(new HubScreen(game));
                dispose();
                return true;
            }
            return false;
        });

        options.addListener(event -> {
            if (event.toString().equals("touchDown")) {
                // TODO: add options screen
                return true;
            }
            return false;
        });

        quit.addListener(event -> {
            if (event.toString().equals("touchDown")) {
                Gdx.app.exit();
                return true;
            }
            return false;
        });
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        backgroundTexture.dispose();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
