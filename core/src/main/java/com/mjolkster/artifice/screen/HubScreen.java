package com.mjolkster.artifice.screen;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mjolkster.artifice.GameClass;
import com.mjolkster.artifice.entities.Archetype;
import com.mjolkster.artifice.entities.PlayableCharacter;
import com.mjolkster.artifice.files.FileHandler;
import com.mjolkster.artifice.generators.MapGenerator;
import com.mjolkster.artifice.screen.viewports.AspectRatioViewport;
import com.mjolkster.artifice.screen.viewports.ExpandingViewport;

public class HubScreen implements Screen {

    private static final float HUB_WIDTH = 50f;
    private static final float HUB_HEIGHT = 40f;
    private boolean restartRequested;

    // Core
    private final GameClass game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch spriteBatch;

    // Map
    private Texture hubBackground;

    // Lighting / physics (kept simple, optional)
    private World world;
    private RayHandler rayHandler;
    private PointLight playerLight;

    // Player
    private PlayableCharacter player;

    // UI
    private Stage stage;

    public HubScreen(final GameClass game) {
        this.game = game;

        // Camera & viewport
        camera = new OrthographicCamera();
        viewport = new AspectRatioViewport(6f, camera);

        spriteBatch = new SpriteBatch();

        // Load hub background PNG (e.g. assets/maps/hub.png)
        hubBackground = new Texture(Gdx.files.internal("HubMap.png"));

        // World + lighting
        world = new World(new Vector2(0, -9.8f), true);
        rayHandler = new RayHandler(world);
        rayHandler.setAmbientLight(0.2f);
        rayHandler.setBlur(true);

        playerLight = new PointLight(rayHandler, 200);
        playerLight.setColor(Color.GOLDENROD);
        playerLight.setDistance(4f);

        // Player rat spawns at center (adjust to taste)
        Vector2 spawnPoint = new Vector2(800, 750);
        player = new PlayableCharacter(Archetype.FIGHTER, spawnPoint, null);
        player.setContext(PlayableCharacter.Context.HUB);

        // Stage for UI (upgrade/crafting menus later)
        stage = new Stage(new ExpandingViewport(90f, true, new OrthographicCamera()), spriteBatch);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage); // allow UI interaction
    }

    @Override
    public void render(float delta) {

        handleInput(delta);

        if (restartRequested) {
            return;
        }

        if (this.player != null) {

            ScreenUtils.clear(Color.BLACK);

            player.update(delta, camera, null);
            playerLight.setPosition(player.x + 0.5f, player.y + 0.5f);

            // camera follows player
            handleCamera(viewport, camera);
            camera.update();

            // draw hub background and player
            spriteBatch.setProjectionMatrix(camera.combined);
            spriteBatch.begin();
            // Draw hub.png at world origin, size matches world units
            spriteBatch.draw(hubBackground, 0, 0, HUB_WIDTH, HUB_HEIGHT);
            player.draw(spriteBatch);
            spriteBatch.end();

            // lighting (optional)
            rayHandler.setCombinedMatrix(camera);
            rayHandler.updateAndRender();

            stage.act(delta);
            stage.draw();
        }
    }

    public void handleCamera(Viewport viewport, OrthographicCamera camera) {
        float halfW = viewport.getWorldWidth() / 2f;
        float halfH = viewport.getWorldHeight() / 2f;

        float mapW = HUB_WIDTH * 32f;
        float mapH = HUB_HEIGHT * 32f;

        float cameraX = player.x;
        float cameraY = player.y;

        // Horizontal clamp
        if (mapW < viewport.getWorldWidth()) {
            cameraX = mapW / 2f;
        } else {
            cameraX = Math.max(halfW, Math.min(cameraX, mapW - halfW + 2));
        }

        // Vertical clamp
        if (mapH < viewport.getWorldHeight()) {
            cameraY = mapH / 2f;
        } else {
            cameraY = Math.max(halfH, Math.min(cameraY, mapH - halfH + 2));
        }

        camera.position.set(cameraX, cameraY, 0);

        player.x = Math.max(0, Math.min(player.x, mapW + player.collisionBox.getBounds().width / 2));
        player.y = Math.max(0, Math.min(player.y, mapH + player.collisionBox.getBounds().height / 2));
    }

    private void handleInput(float delta) {
        // Example: Enter dungeon
        if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
            requestRestart();
        }
    }

    // Replace with real save/slot logic later
    private static int saveSlot() {
        return 0;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        hubBackground.dispose();
        rayHandler.dispose();
        world.dispose();
        stage.dispose();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {
    }

    public void requestRestart() {
        restartRequested = true;
        if (player != null) {
            this.player.sprite.dispose();
            this.player = null;
        }
        game.setScreen(new GameScreen(game, saveSlot()));
        Gdx.app.postRunnable(this::dispose);
    }

}

