package com.mjolkster.artifice.screen;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mjolkster.artifice.GameClass;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.math.Rectangle;
import com.mjolkster.artifice.actions.AttackAction;
import com.mjolkster.artifice.generators.MapGenerator;
import com.mjolkster.artifice.entities.Archetype;
import com.mjolkster.artifice.entities.NonPlayableCharacter;
import com.mjolkster.artifice.entities.PlayableCharacter;
import com.mjolkster.artifice.registration.registries.AttacksRegistry;
import com.mjolkster.artifice.util.wrappers.Line;
import com.mjolkster.artifice.util.Sprite;

import java.util.*;

public class GameScreen implements Screen {
    final GameClass game;
    private final Set<Line> collisionBoxes;
    boolean showHitBox = true;
    boolean showLighting = false;

    ShapeRenderer shape;
    Viewport viewport;
    OrthographicCamera camera;
    OrthogonalTiledMapRenderer renderer;
    TiledMap map;
    public static MapGenerator mapGenerator;
    SpriteBatch spriteBatch;
    Sprite sprite;
    World world;
    RayHandler rayHandler;
    public static PlayableCharacter player;
    private Stage stage;
    private Skin skin;
    private Label label;
    public static List<NonPlayableCharacter> NPCs;

    public GameScreen(final GameClass game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new ScalingViewport(Scaling.fill, 8, 5, camera);

        this.mapGenerator = new MapGenerator(32, 32, 436576334);
        map = mapGenerator.generate(64, 32, 6);

        float unitScale = 1f / 32f;
        renderer = new OrthogonalTiledMapRenderer(map, unitScale);

        spriteBatch = new SpriteBatch();
        shape = new ShapeRenderer();

        world = new World(new Vector2(0, -9.8f), true);

        collisionBoxes = MapGenerator.getCollisionLines();

        // Current light engine, to be changed
        rayHandler = new RayHandler(world);
        rayHandler.setAmbientLight(0.1f);
        rayHandler.setBlur(true);
        rayHandler.setCulling(true);

        Vector2 spawnpoint = mapGenerator.getSpawnPoint();
        player = new PlayableCharacter(Archetype.FIGHTER, spawnpoint, rayHandler);

        List<Vector2> spawnableAreas = mapGenerator.getSpawnableAreas();
        System.out.println(spawnableAreas);
        int spawnRate = 10;
        NPCs = new ArrayList<>();

        if (spawnableAreas != null && !spawnableAreas.isEmpty()) {
            Random random = new Random();

            // Limit the number of NPCs to the number of available spawn points
            int maxNPCs = Math.min(spawnRate, spawnableAreas.size());

            // Create a shuffled list of spawn points
            List<Vector2> shuffledSpawns = new ArrayList<>(spawnableAreas);
            Collections.shuffle(shuffledSpawns);

            // Spawn NPCs at random locations
            for (int i = 0; i < maxNPCs; i++) {
                Vector2 spawnPos = shuffledSpawns.get(i);
                System.out.println(spawnPos);
                NPCs.add(new NonPlayableCharacter(spawnPos, player));
            }
        } else {
            System.out.println("Warning: No valid spawn locations found for NPCs");
        }

        createGUI();
    }

    @Override
    public void show() {
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void render(float delta) {
        player.update(delta, camera, collisionBoxes);

        for (NonPlayableCharacter npc : NPCs) {
            npc.update(delta, camera, collisionBoxes);
        }

        if (player.hasCompletedMove()) {
            for (NonPlayableCharacter npc : NPCs) {
                npc.recalculatePath();
            }
        }

        world.step(1 / 60f, 6, 2);
        player.handleCamera(viewport, mapGenerator, camera);
        draw();
        stage.act(delta);
        stage.draw();
    }

    private void draw() {

        ScreenUtils.clear(new Color(1312121));
        camera.update();

        renderer.setView(camera);
        renderer.render();

        if (showHitBox) {
            shape.setProjectionMatrix(camera.combined);
            shape.begin(ShapeRenderer.ShapeType.Line);
            shape.setColor(Color.RED);
            for (Line line : collisionBoxes) {
                shape.line(line.start, line.end);
            }
            Rectangle collisionBox = player.collisionBox.getBounds();
            shape.rect(collisionBox.x, collisionBox.y, collisionBox.width, collisionBox.height);
            for (NonPlayableCharacter npc : NPCs) {
                shape.rect(npc.getHitbox().x, npc.getHitbox().y, npc.getHitbox().width, npc.getHitbox().height);
                npc.drawPath(shape);
            }
            if (player.attacking){
                player.slash.drawHitbox(shape);
            }
            shape.end();
        }

        spriteBatch.begin();
        spriteBatch.setProjectionMatrix(camera.combined);
        player.draw(spriteBatch);
        for (NonPlayableCharacter npc : NPCs) {
            npc.draw(spriteBatch);
        }
        spriteBatch.end();

        if (showLighting) {
            rayHandler.updateAndRender();
        }

        viewport.apply();
        spriteBatch.setProjectionMatrix(camera.combined);
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
        if (map != null) map.dispose();
        if (renderer != null) renderer.dispose();
        spriteBatch.dispose();
        if (rayHandler != null) rayHandler.dispose();
        if (world != null) world.dispose();
        stage.dispose();
        skin.dispose();
    }

    private void createGUI() {
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        label = new Label("Hello Scene2D UI!", skin);

        TextButton button = new TextButton("Click me!", skin);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showHitBox = !showHitBox;
            }
        });

        // Layout with a table
        Table table = new Table();
        table.setFillParent(true); // Fill the whole stage
        table.top().left().pad(20); // Position at top left

        table.add(label).padBottom(10).row(); // Add label
        table.add(button).width(200).height(50); // Add button

        // Add table to stage
        stage.addActor(table);

        // Important: Send input to the stage
        Gdx.input.setInputProcessor(stage);
    }
}
