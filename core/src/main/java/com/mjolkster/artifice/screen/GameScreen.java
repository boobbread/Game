package com.mjolkster.artifice.screen;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mjolkster.artifice.GameClass;
import com.mjolkster.artifice.entities.Archetype;
import com.mjolkster.artifice.entities.ChestEntity;
import com.mjolkster.artifice.entities.NonPlayableCharacter;
import com.mjolkster.artifice.entities.PlayableCharacter;
import com.mjolkster.artifice.entities.enemy.SlugEnemy;
import com.mjolkster.artifice.files.FileHandler;
import com.mjolkster.artifice.generators.MapGenerator;
import com.mjolkster.artifice.registration.RegistryManager;
import com.mjolkster.artifice.registration.registries.ItemRegistry;
import com.mjolkster.artifice.screen.viewports.AspectRatioViewport;
import com.mjolkster.artifice.screen.viewports.ExpandingViewport;
import com.mjolkster.artifice.util.*;
import com.mjolkster.artifice.util.tools.CalculateGaussians;
import com.mjolkster.artifice.util.wrappers.Gaussian;
import com.mjolkster.artifice.util.wrappers.Line;

import java.util.*;
import java.util.List;

public class GameScreen implements Screen {

    // Camera / viewport
    public OrthographicCamera camera;
    private Viewport viewport;

    // Rendering
    private OrthogonalTiledMapRenderer renderer;
    private SpriteBatch spriteBatch;
    private ShapeRenderer shape;
    private TiledMap map;
    private List<List<Vector2>> outlines;

    // Physics / lighting
    private World world;
    private RayHandler rayHandler;
    private PointLight playerLight;
    private Box2DDebugRenderer debugRenderer;

    // UI
    private Stage stage;
    private Skin skin;
    private Texture whitePixel;
    private BitmapFont font;

    // Entities / game state
    public PlayableCharacter player;
    private List<NonPlayableCharacter> NPCs;
    public List<ChestEntity> chests;
    private EndPoint endPoint;
    private Set<Line> collisionBoxes;
    public MapGenerator mapGenerator;
    private int deadNPCCount;
    private TurnManager turnManager;

    // Core
    public static GameClass game;
    public long seed;
    public static int playerSlotNumber;
    boolean showHitBox = false;
    boolean showLighting = true;
    boolean paused = false;
    int spawnRate;
    private final PlayerHUD hud;
    private boolean restartRequested = false;
    public static boolean closeRequested = false;

    public GameScreen(final GameClass game, int slotNumber) {

        GameScreen.game = game;
        playerSlotNumber = slotNumber;

        // Camera / Viewport
        camera = new OrthographicCamera();
        viewport = new AspectRatioViewport(6f, camera);

        seed = (long) (Math.random() * 4000000);

        // Physics and lighting
        world = new World(new Vector2(0, -9.8f), true);
        rayHandler = new RayHandler(world);
        rayHandler.setAmbientLight(0.1f);
        rayHandler.setBlur(true);
        rayHandler.setCulling(true);

        // Shader
        playerLight = new PointLight(rayHandler, 300); // rays = quality
        playerLight.setColor(Color.GOLDENROD); // warm yellowish
        playerLight.setDistance(5f);
        playerLight.setSoft(true);

        // Map
        mapGenerator = new MapGenerator(32, 32, seed);
        map = mapGenerator.generate(64, 16, 6);
        float unitScale = 1f / 32f;
        renderer = new OrthogonalTiledMapRenderer(map, unitScale);

        // Rendering
        spriteBatch = new SpriteBatch();
        shape = new ShapeRenderer();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        skin = new Skin(Gdx.files.internal("GUI/GUISkin.json"));
        Viewport uiViewport = new ExpandingViewport(90f, true, new OrthographicCamera());
        stage = new Stage(uiViewport, spriteBatch);

        // Collision
        collisionBoxes = mapGenerator.getCollisionLines();
        outlines = MapGenerator.orderOutline(collisionBoxes);
        createBodiesFromPolygons(world, outlines);

        debugRenderer = new Box2DDebugRenderer();

        // Entities
        Vector2 spawnpoint = mapGenerator.getSpawnPoint();
        Vector2 endPointPosition = mapGenerator.getEndPoint();

        FileHandler.SaveSlot save = FileHandler.loadTempSave(slotNumber);

        if (slotNumber >= 0 && slotNumber <= 2 && save != null) {
            FileHandler.Save saveData = save.saveData;

            player = new PlayableCharacter(
                saveData.health,
                new Inventory(3).getContents(),
                new Inventory(5).getContents(),
                saveData.archetype,
                saveData.roundsPassed,
                slotNumber,
                spawnpoint,
                this
            );

            saveData.tempInv.forEach(item -> {
                if (item != null) {
                    player.addItemToTemporaryInv(item);
                }
            });
            saveData.permanentInv.forEach(item -> {
                if (item != null) {
                    player.addItemToPermanentInv(item);
                }
            });

        } else {
            player = new PlayableCharacter(Archetype.FIGHTER, spawnpoint, this);
            player.addItemToPermanentInv(ItemRegistry.nest_feather.get());
        }

        player.actionPoints = player.maxActionPoints;
        player.setContext(PlayableCharacter.Context.DUNGEON);


        endPoint = new EndPoint(endPointPosition,this);
        deadNPCCount = 0;

        List<Vector2> spawnableAreas = mapGenerator.getSpawnableAreas();

        ArrayList<Gaussian> slugGaussians = SlugEnemy.getGaussians();
        spawnRate = CalculateGaussians.calculateMultiGaussian(slugGaussians, player.roundsPassed);
        NPCs = new ArrayList<>();
        chests = new ArrayList<>();

        if (spawnableAreas != null && !spawnableAreas.isEmpty()) {
            // Limit the number of NPCs to the number of available spawn points
            int maxNPCs = Math.min(spawnRate, spawnableAreas.size());

            // Create a shuffled list of spawn points
            List<Vector2> shuffledSpawns = new ArrayList<>(spawnableAreas);
            Collections.shuffle(shuffledSpawns);

            // Spawn NPCs at random locations
            for (int i = 0; i < maxNPCs; i++) {
                Vector2 spawnPos = shuffledSpawns.get(i);
                NPCs.add(new SlugEnemy(spawnPos, player, this));
            }
        } else {
            Gdx.app.log("GameScreen: Warning", "No valid spawn locations found for NPCs");
        }

        turnManager = new TurnManager(player, NPCs);

        hud = new PlayerHUD(player);

        BitmapFont font = new BitmapFont(Gdx.files.internal("GUI/default.fnt"));
        DamageIndicatorManager.init(stage, font, 2f);

        Gdx.app.log("GameScreen", "Initialisation complete");
    }

    public List<Body> createBodiesFromPolygons(World world, List<List<Vector2>> polygons) {
        List<Body> bodies = new ArrayList<>();

        for (List<Vector2> loop : polygons) {
            // Create a body for this polygon outline
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.StaticBody;
            Body body = world.createBody(bodyDef);

            // Create edge chains for the outer perimeter
            for (int i = 0; i < loop.size(); i++) {
                Vector2 current = loop.get(i);
                Vector2 next = loop.get((i + 1) % loop.size());

                // Skip duplicate points (degenerate edges)
                if (current.epsilonEquals(next, 0.001f)) {
                    continue;
                }

                EdgeShape edgeShape = new EdgeShape();
                edgeShape.set(current, next);

                FixtureDef fixtureDef = new FixtureDef();
                fixtureDef.shape = edgeShape;
                fixtureDef.density = 0f; // Static bodies don't need density
                fixtureDef.friction = 0.5f;

                body.createFixture(fixtureDef);
                edgeShape.dispose();
            }

            bodies.add(body);
        }

        return bodies;
    }

    @Override
    public void render(float delta) {

        if (restartRequested) {

            player.incrementRounds();
            FileHandler.saveTemp(playerSlotNumber, player);

            GameScreen newScreen = new GameScreen(game, playerSlotNumber);
            game.setScreen(newScreen);

            this.dispose();
            this.clearEverything();
            return; // exit immediately to avoid using disposed objects
        }

        if (closeRequested) {
            this.dispose();
            this.clearEverything();
            game.setScreen(new HubScreen(game));
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            game.toggleFullscreen();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            showLighting = !showLighting;
            Gdx.app.log("GameScreen", "Lighting: " + showLighting);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            showHitBox = !showHitBox;
            Gdx.app.log("GameScreen", "Hitboxes: " + showLighting);
        }

        if (!paused) {
            turnManager.update(delta);

            world.step(1 / 60f, 6, 2);
            player.handleCamera(viewport, mapGenerator, camera);

            player.update(delta, camera, collisionBoxes);
            playerLight.setPosition(player.x + player.collisionBox.getBounds().width / 2, player.y + player.collisionBox.getBounds().height / 2);
            playerLight.update();

            Iterator<NonPlayableCharacter> it = NPCs.iterator();
            while (it.hasNext()) {
                NonPlayableCharacter npc = it.next();
                npc.update(delta, camera, collisionBoxes);

                if (npc.getState() == NonPlayableCharacter.NPCState.DEAD && npc.hasSpawnedChest()) {
                    it.remove();
                    deadNPCCount++;
                }
            }

            chests.forEach(chest -> chest.update(delta, camera, collisionBoxes));

            endPoint.update(delta);

            if (Gdx.input.justTouched()) {
                Vector2 mouseWorld = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

                chests.forEach(chest -> {
                    if (chest.getHitbox().contains(mouseWorld.x, mouseWorld.y)) {
                        chest.openChest(RegistryManager.ITEMS, stage, skin);
                    }
                });

            }

            if (deadNPCCount >= NPCs.size()) {
                endPoint.open();
            }
        }

        draw();

        hud.update(delta);
        hud.render();
        DamageIndicatorManager.update(delta);
        DamageIndicatorManager.render();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = !paused;
            if (paused) {
                pause();
            } else resume();
        }
    }

    // Drawing
    private void draw() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();

        // Use the normal mapping renderer - this will render the regular tilemap first
        renderer.setView(camera);
        renderer.render();

        drawSprites();

        // Draw lights and other effects on top if needed
        if (showLighting) {
            rayHandler.setCombinedMatrix(camera);
            rayHandler.updateAndRender();
        }

        // Draw hitboxes if enabled
        if (showHitBox) {
            drawHitboxes();
            debugRenderer.render(world, camera.combined);
        }
    }

    private void drawHitboxes() {
        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(Color.RED);

        collisionBoxes.forEach(line -> shape.line(line.start, line.end));
        drawEntityHitboxes();

        shape.end();
    }

    private void drawEntityHitboxes() {
        Rectangle playerBox = player.collisionBox.getBounds();
        shape.rect(playerBox.x, playerBox.y, playerBox.width, playerBox.height);

        for (NonPlayableCharacter npc : NPCs) {
            shape.rect(npc.getHitbox().x, npc.getHitbox().y,
                npc.getHitbox().width, npc.getHitbox().height);
            npc.drawPath(shape);
        }

        if (player.currentAttack != null) {
            player.currentAttack.drawHitbox(shape);
        }
    }

    private void drawSprites() {
        spriteBatch.begin();
        spriteBatch.setProjectionMatrix(camera.combined);

        player.draw(spriteBatch);
        NPCs.forEach(npc -> npc.draw(spriteBatch));
        chests.forEach(chest -> chest.draw(spriteBatch));
        endPoint.draw(spriteBatch);

        spriteBatch.end();
    }

    // Screen Lifecycle
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        hud.resize(width, height);
    }

    @Override
    public void dispose() {
        if (map != null) map.dispose();
        if (renderer != null) renderer.dispose();
        spriteBatch.dispose();
        shape.dispose();
        if (rayHandler != null) rayHandler.dispose();
        if (world != null) {
            world.dispose();
        }
        stage.dispose();
        skin.dispose();
        if (whitePixel != null) whitePixel.dispose();
        hud.dispose();
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
        ScreenUtils.clear(Color.BLACK);
    }

    @Override
    public void resume() {
        stage.act();
        stage.draw();
    }

    public void clearEverything() {
        restartRequested = false;
        closeRequested = false;

        // Clear static / instance references
        player = null;
        collisionBoxes = null;
        NPCs = null;
        chests = null;
        outlines = null;
        endPoint = null;
        deadNPCCount = 0;
        playerLight = null;
        debugRenderer = null;
        font = null;
    }

    public void requestRestart() {
        if (player != null) {
            FileHandler.saveTemp(playerSlotNumber, player);
        }
        restartRequested = true;
    }

    public void requestClose() {
        if (player != null) {
            FileHandler.saveTemp(playerSlotNumber, player);
        }
        closeRequested = true;
    }

    public List<NonPlayableCharacter> getNPCs() {
        return NPCs;
    }

}
