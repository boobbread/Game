package com.mjolkster.artifice.graphics.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mjolkster.artifice.core.GameClass;
import com.mjolkster.artifice.core.entities.PlayableCharacter;
import com.mjolkster.artifice.core.entities.enemy.BaseEnemy;
import com.mjolkster.artifice.core.world.EntityManager;
import com.mjolkster.artifice.core.world.GameMap;
import com.mjolkster.artifice.core.world.MapGenerator;
import com.mjolkster.artifice.graphics.viewports.AspectRatioViewport;
import com.mjolkster.artifice.graphics.viewports.ExpandingViewport;
import com.mjolkster.artifice.io.FileHandler;
import com.mjolkster.artifice.util.combat.DamageIndicatorManager;

import java.util.List;

public class GameScreen implements Screen {

    // Core
    public static GameClass game;
    public static int playerSlotNumber;
    public static boolean closeRequested = false;
    private final PlayerHUD hud;

    // Camera / viewport
    private final OrthographicCamera camera;
    private final Viewport viewport;

    // Rendering
    private final SpriteBatch spriteBatch;
    private final ShapeRenderer shape;

    // Map & Entities
    private final GameMap gameMap;
    private final long seed;
    private final EntityManager entityManager;

    // UI
    private final Stage stage;
    private final Skin skin;
    private BitmapFont font;

    // Settings
    private boolean paused = false;
    private boolean showHitBox = false;
    private boolean showLighting = true;

    private boolean restartRequested = false;

    public GameScreen(GameClass game, int slotNumber) {
        GameScreen.game = game;
        playerSlotNumber = slotNumber;

        // Camera and viewport
        camera = new OrthographicCamera();
        viewport = new AspectRatioViewport(6f, camera);

        // Map
        seed = (long) (Math.random() * 4000000);
        gameMap = new GameMap(seed);

        // Entities
        entityManager = new EntityManager(gameMap, slotNumber, this);

        // Rendering
        spriteBatch = new SpriteBatch();
        shape = new ShapeRenderer();
        font = new BitmapFont();
        font.setColor(Color.WHITE);

        // UI
        skin = new Skin(Gdx.files.internal("GUI/GUISkin.json"));
        Viewport uiViewport = new ExpandingViewport(90f, true, new OrthographicCamera());
        stage = new Stage(uiViewport, spriteBatch);

        hud = new PlayerHUD(entityManager.getPlayer());

        DamageIndicatorManager.init(stage, new BitmapFont(Gdx.files.internal("GUI/default.fnt")), 2f);

        Gdx.app.log("GameScreen", "Initialisation complete");
    }

    @Override
    public void render(float delta) {

        // Handle restart
        if (restartRequested) {
            restartRequested = false;
            FileHandler.saveTemp(playerSlotNumber, entityManager.getPlayer());
            GameScreen newScreen = new GameScreen(game, playerSlotNumber);
            game.setScreen(newScreen);
            dispose();
            return;
        }

        // Handle close
        if (closeRequested) {
            closeRequested = false;
            FileHandler.saveTemp(playerSlotNumber, entityManager.getPlayer());
            dispose();
            game.setScreen(new HubScreen(game));
            return;
        }

        handleInput();

        if (!paused) {
            gameMap.step(delta);
            entityManager.update(delta, camera, gameMap.getCollisionBoxes());
        }

        draw();

        hud.update(delta);
        hud.render();
        DamageIndicatorManager.update(delta);
        DamageIndicatorManager.render();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            game.toggleFullscreen();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            showLighting = !showLighting;
            Gdx.app.log("GameScreen", "Lighting: " + showLighting);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            showHitBox = !showHitBox;
            Gdx.app.log("GameScreen", "Hitboxes: " + showHitBox);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = !paused;
        }
    }

    private void draw() {
        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        PlayableCharacter player = entityManager.getPlayer();
        float playerCenterX = player.x + player.collisionBox.getBounds().width / 2f;
        float playerCenterY = player.y + player.collisionBox.getBounds().height / 2f;
        camera.position.set(playerCenterX, playerCenterY, 0);
        camera.update();

        // Draw map
        gameMap.render(camera);

        // Draw entities
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        entityManager.render(spriteBatch);
        spriteBatch.end();

        // Draw lighting
        if (showLighting) {
            gameMap.renderLighting(camera, player);
        }

        // Draw hitboxes
        if (showHitBox) {
            shape.setProjectionMatrix(camera.combined);
            shape.begin(ShapeRenderer.ShapeType.Line);
            entityManager.renderHitboxes(shape);
            shape.end();
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        hud.resize(width, height);
    }

    @Override
    public void dispose() {
        gameMap.dispose();
        entityManager.dispose();
        spriteBatch.dispose();
        shape.dispose();
        stage.dispose();
        skin.dispose();
        font.dispose();
        hud.dispose();
    }

    @Override public void show() { }
    @Override public void hide() { }
    @Override public void pause() { }
    @Override public void resume() {
        stage.act();
        stage.draw();
    }

    public void requestRestart() { restartRequested = true; }
    public void requestClose() { closeRequested = true; }

    public PlayerHUD getHud() { return hud; }
    public Stage getStage() { return stage; }

    public Skin getSkin() {
        return skin;
    }

    public EntityManager getEntityManager() { return entityManager; }
    public GameMap getGameMap() { return gameMap; }
    public static GameClass getGame() { return game; }
    public OrthographicCamera getCamera() { return camera; }

    public long getSeed() { return seed; }

    public PlayableCharacter getPlayer() { return entityManager.getPlayer(); }
    public List<BaseEnemy> getNPCs() { return entityManager.getNPCs();}

}
