package com.mjolkster.artifice.graphics.screen;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mjolkster.artifice.core.GameClass;
import com.mjolkster.artifice.core.entities.Archetype;
import com.mjolkster.artifice.core.entities.PlayableCharacter;
import com.mjolkster.artifice.core.world.generation.LineHandler;
import com.mjolkster.artifice.graphics.viewports.AspectRatioViewport;
import com.mjolkster.artifice.graphics.viewports.ExpandingViewport;
import com.mjolkster.artifice.io.input.ControllerInputHandler;
import com.mjolkster.artifice.io.input.HybridInputHandler;
import com.mjolkster.artifice.io.input.InputHandler;
import com.mjolkster.artifice.util.geometry.Line;

import java.util.*;

public class HubScreen implements Screen {

    private static final float HUB_WIDTH = 50f;
    private static final float HUB_HEIGHT = 40f;

    // Core
    private final GameClass game;
    private boolean restartRequested;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final SpriteBatch spriteBatch;
    private final InputHandler inputHandler;

    // Map
    private final Texture hubBackground;
    private final Set<Line> rawHubCollisions = new HashSet<>();
    private final Set<Line> hubCollisions = new HashSet<>();
    private final Ellipse holeHitbox;

    // Lighting / physics (kept simple, optional)
    private final World world;
    private final RayHandler rayHandler;
    private final PointLight playerLight;
    private final Box2DDebugRenderer debugRenderer;
    private final ShapeRenderer shape;

    // Player
    private PlayableCharacter player;

    // UI
    private final Stage stage;

    public HubScreen(final GameClass game) {
        this.game = game;

        // Camera & viewport
        camera = new OrthographicCamera();
        viewport = new AspectRatioViewport(6f, camera);

        spriteBatch = new SpriteBatch();

        // Load hub background PNG (e.g. assets/maps/hub.png)
        hubBackground = new Texture(Gdx.files.internal("HubMap.png"));
        loadCollisionMask("HubCollisions.png");
        holeHitbox = new Ellipse(22 + 25/32f, 11 + 6/32f, 206/32f, 124/32f);

        // World + lighting
        debugRenderer = new Box2DDebugRenderer(true, false, false, false, false, false);
        world = new World(new Vector2(0, -9.8f), true);
        List<List<Vector2>> outlines = LineHandler.orderOutline(hubCollisions);
        createBodiesFromPolygons(world, outlines);
        rayHandler = new RayHandler(world);
        rayHandler.setAmbientLight(0.2f);
        rayHandler.setBlur(true);

        playerLight = new PointLight(rayHandler, 200);
        playerLight.setColor(Color.GOLDENROD);
        playerLight.setDistance(4f);

        // Stage for UI (upgrade/crafting menus later)
        stage = new Stage(new ExpandingViewport(90f, true, new OrthographicCamera()), spriteBatch);
        inputHandler = new HybridInputHandler(stage);
        // Player rat spawns at center (adjust to taste)
        Vector2 spawnPoint = new Vector2(800, 750);
        player = new PlayableCharacter(Archetype.FIGHTER, spawnPoint, null, inputHandler);
        player.setContext(PlayableCharacter.Context.HUB);

        shape = new ShapeRenderer();
    }

    // Replace with real save/slot logic later
    private static int saveSlot() {
        return 0;
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

            player.update(delta, camera, hubCollisions);
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

            shape.begin(ShapeRenderer.ShapeType.Line);
            shape.setProjectionMatrix(camera.combined);
            shape.ellipse(holeHitbox.x, holeHitbox.y, holeHitbox.width, holeHitbox.height);
            shape.end();

//            debugRenderer.render(world, camera.combined);

            // lighting (optional)
            rayHandler.setCombinedMatrix(camera);
            rayHandler.updateAndRender();

            stage.act(delta);
            stage.draw();

            if (rectOverlapsEllipse(player.collisionBox.getBounds(), holeHitbox)) {
                requestRestart();
            }
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
            requestRestart();
        }

        if (inputHandler.isController() && inputHandler instanceof HybridInputHandler) {
            ControllerInputHandler controller = ((HybridInputHandler) inputHandler).getControllerHandler();
            if (controller.isaPressed()) {
                requestRestart();
            }
        }
    }

    private void loadCollisionMask(String maskPath) {
        Pixmap mask = new Pixmap(Gdx.files.internal(maskPath));

        int width = mask.getWidth();
        int height = mask.getHeight();

        Gdx.app.log("HubScreen", "Loading collision mask: " + maskPath);
        Gdx.app.log("HubScreen", "Mask size: " + width + "x" + height);

        int solidCount = 0;

        // Step 1: collect horizontal runs for each row
        List<List<int[]>> runsPerRow = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            List<int[]> runs = new ArrayList<>();
            int runStart = -1;

            for (int x = 0; x < width; x++) {
                int pixel = mask.getPixel(x, y);
                Color c = new Color(pixel);

                boolean solid = (c.r == 0 && c.g == 0 && c.b == 0);
                if (solid) solidCount++;

                if (solid && runStart == -1) {
                    runStart = x;
                } else if ((!solid || x == width - 1) && runStart != -1) {
                    int runEnd = solid ? x : x - 1;
                    runs.add(new int[]{runStart, runEnd});
                    runStart = -1;
                }
            }

            runsPerRow.add(runs);
        }

        mask.dispose();

        Gdx.app.log("HubScreen", "Solid pixels found: " + solidCount);

        // Step 2: vertical merge into rectangles
        List<int[]> rectangles = new ArrayList<>(); // {x1, x2, yStart, yEnd}
        Map<String, int[]> activeRuns = new HashMap<>();

        for (int y = 0; y < height; y++) {
            Map<String, int[]> newActiveRuns = new HashMap<>();

            for (int[] run : runsPerRow.get(y)) {
                String key = run[0] + ":" + run[1];

                if (activeRuns.containsKey(key)) {
                    // Extend existing rectangle
                    int[] rect = activeRuns.get(key);
                    rect[3] = y; // extend yEnd
                    newActiveRuns.put(key, rect);
                } else {
                    // Start new rectangle
                    int[] rect = new int[]{run[0], run[1], y, y};
                    newActiveRuns.put(key, rect);
                }
            }

            // Finalize runs that did not continue
            for (int[] rect : activeRuns.values()) {
                if (!newActiveRuns.containsValue(rect)) {
                    rectangles.add(rect);
                }
            }

            activeRuns = newActiveRuns;
        }

        // Flush remaining active rectangles
        rectangles.addAll(activeRuns.values());

        Gdx.app.log("HubScreen", "Merged rectangles: " + rectangles.size());

        // Step 3: convert rectangles into Lines
        for (int[] rect : rectangles) {
            float x1 = (float) rect[0] / 32f;
            float x2 = (float) (rect[1] + 1) / 32f;
            float y1 = (float) (height - rect[2]) / 32f;
            float y2 = (float) (height - rect[3] - 1) / 32f;

            // Add 4 edges
            rawHubCollisions.add(new Line(x1, y1, x2, y1)); // top
            rawHubCollisions.add(new Line(x2, y1, x2, y2)); // right
            rawHubCollisions.add(new Line(x2, y2, x1, y2)); // bottom
            rawHubCollisions.add(new Line(x1, y2, x1, y1)); // left
        }

        Gdx.app.log("HubScreen", "Raw hub collision lines (rects expanded): " + rawHubCollisions.size());

        // Step 4: outline + simplify if needed
        List<List<Vector2>> outlines = LineHandler.orderOutline(rawHubCollisions);
        List<List<Vector2>> simplifiedOutlines = new ArrayList<>();
        for (List<Vector2> outline : outlines) {
            simplifiedOutlines.add(LineHandler.unifySegments(outline, 0.01));
        }
        for (List<Vector2> outline : simplifiedOutlines) {
            hubCollisions.addAll(LineHandler.reconstructLines(outline));
        }

        Gdx.app.log("HubScreen", "Outlines found: " + outlines.size());
        Gdx.app.log("HubScreen", "Final hub collision lines: " + hubCollisions.size());
    }

    private boolean rectOverlapsEllipse(Rectangle rect, Ellipse ellipse) {
        // Get rect center
        float rectCenterX = rect.x + rect.width / 2f;
        float rectCenterY = rect.y + rect.height / 2f;

        // Translate relative to ellipse center
        float dx = rectCenterX - (ellipse.x + ellipse.width / 2f);
        float dy = rectCenterY - (ellipse.y + ellipse.height / 2f);

        // Normalize by ellipse radii
        float rx = ellipse.width / 2f;
        float ry = ellipse.height / 2f;

        float nx = dx / rx;
        float ny = dy / ry;

        return (nx * nx + ny * ny) <= 1f; // inside ellipse
    }

    public List<Body> createBodiesFromPolygons(World world, List<List<Vector2>> polygons) {
        List<Body> bodies = new ArrayList<>();

        for (List<Vector2> loop : polygons) {
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.StaticBody;
            Body body = world.createBody(bodyDef);

            for (int i = 0; i < loop.size(); i++) {
                Vector2 current = loop.get(i);
                Vector2 next = loop.get((i + 1) % loop.size());

                if (current.epsilonEquals(next, 0.001f)) {
                    continue;
                }

                EdgeShape edgeShape = new EdgeShape();
                edgeShape.set(current, next);

                FixtureDef fixtureDef = new FixtureDef();
                fixtureDef.shape = edgeShape;
                fixtureDef.density = 0f;
                fixtureDef.friction = 0.5f;

                body.createFixture(fixtureDef);
                edgeShape.dispose();
            }

            bodies.add(body);
        }

        return bodies;
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
        inputHandler.dispose();
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

    public void requestRestart() {
        restartRequested = true;
        if (player != null) {
            this.player.sprite.dispose();
            this.player = null;
        }
        game.setScreen(new GameScreen(game, saveSlot(), false));
        Gdx.app.postRunnable(this::dispose);
    }

}

