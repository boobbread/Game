package com.mjolkster.artifice.screen;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mjolkster.artifice.GameClass;
import com.mjolkster.artifice.entities.Archetype;
import com.mjolkster.artifice.entities.ChestEntity;
import com.mjolkster.artifice.entities.NonPlayableCharacter;
import com.mjolkster.artifice.entities.PlayableCharacter;
import com.mjolkster.artifice.files.FileHandler;
import com.mjolkster.artifice.generators.MapGenerator;
import com.mjolkster.artifice.items.ConsumableItem;
import com.mjolkster.artifice.items.Item;
import com.mjolkster.artifice.registration.RegistryManager;
import com.mjolkster.artifice.screen.viewports.AspectRatioViewport;
import com.mjolkster.artifice.screen.viewports.ExpandingViewport;
import com.mjolkster.artifice.util.EndPoint;
import com.mjolkster.artifice.util.Sprite;
import com.mjolkster.artifice.util.wrappers.Line;

import java.util.*;
import java.util.List;

public class GameScreen implements Screen {

    // Core
    public static GameClass game;
    public static long seed;

    // Map / Collision
    public static Set<Line> collisionBoxes;
    public static MapGenerator mapGenerator;

    // Entities
    public static PlayableCharacter player;
    public static List<NonPlayableCharacter> NPCs;
    public static List<ChestEntity> chests;
    public static EndPoint endPoint;

    public static int playerSlotNumber;
    boolean showHitBox = false;
    boolean showLighting = true;
    boolean paused = false;

    // Core again
    OrthographicCamera camera;
    Viewport viewport;
    OrthogonalTiledMapRenderer renderer;
    ShapeRenderer shape;
    SpriteBatch spriteBatch;
    TiledMap map;
    List<List<Vector2>> outlines;
    int spawnRate;
    ProgressBar movementBar;
    ProgressBar healthBar;
    private World world;

    // UI
    private Stage stage;
    private Skin skin;
    private Texture whitePixel;
    private BitmapFont font;
    private Label fpsLabel;
    private final List<ImageButton> tempSlots = new ArrayList<>();
    private final Map<ImageButton, Item> tempSlotItems = new HashMap<>();
    private final List<ImageButton> permSlots = new ArrayList<>();

    // Shader
    private RayHandler rayHandler;
    private PointLight playerLight;
    private Box2DDebugRenderer debugRenderer;

    // State
    private TurnPhase currentPhase;
    private int currentNPCIndex;
    private int deadNPCCount;
    private boolean restartRequested = false;

    public GameScreen(final GameClass game, int slotNumber) {

        GameScreen.game = game;
        playerSlotNumber = slotNumber;

        // Camera / Viewport
        camera = new OrthographicCamera();
        viewport = new AspectRatioViewport(6f, camera);

        seed = (long) (Math.random() * 4000000);

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

        // Physics and lighting
        world = new World(new Vector2(0, -9.8f), true);
        rayHandler = new RayHandler(world);
        rayHandler.setAmbientLight(0.1f);
        rayHandler.setBlur(true);
        rayHandler.setCulling(true);

        // Collision
        collisionBoxes = MapGenerator.getCollisionLines();
        outlines = MapGenerator.orderOutline(collisionBoxes);
        createBodiesFromPolygons(world, outlines);

        // Shader

        playerLight = new PointLight(rayHandler, 300); // rays = quality
        playerLight.setColor(Color.GOLDENROD); // warm yellowish
        playerLight.setDistance(5f);
        playerLight.setSoft(true);

        debugRenderer = new Box2DDebugRenderer();

        // Entities
        Vector2 spawnpoint = mapGenerator.getSpawnPoint();
        Vector2 endPointPosition = mapGenerator.getEndPoint();

        FileHandler.SaveSlot save = FileHandler.loadTempSave(slotNumber);

        if (slotNumber >= 0 && slotNumber <= 2 && save != null) {
            FileHandler.Save saveData = save.saveData;

            player = new PlayableCharacter(
                saveData.health,
                saveData.tempInv,
                saveData.permanentInv,
                saveData.archetype,
                saveData.roundsPassed,
                slotNumber,
                spawnpoint
            );
        } else {
            System.out.println("new player");
            player = new PlayableCharacter(Archetype.FIGHTER, spawnpoint);
        }


        endPoint = new EndPoint(endPointPosition);
        deadNPCCount = 0;

        List<Vector2> spawnableAreas = mapGenerator.getSpawnableAreas();
        spawnRate = player.roundsPassed;
        NPCs = new ArrayList<>();
        chests = new ArrayList<>();

        // Test chest
        ChestEntity chestEntity = new ChestEntity(10, 10, 0, 0);
        chestEntity.spawnChest((spawnpoint.x / 32f) + 2f, (spawnpoint.y / 32f));
        System.out.println(chestEntity.x + ", " + chestEntity.y);
        chests.add(chestEntity);

        currentPhase = TurnPhase.PLAYER_TURN;
        currentNPCIndex = 0;

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
                NPCs.add(new NonPlayableCharacter(spawnPos, player));
            }
        } else {
            System.out.println("Warning: No valid spawn locations found for NPCs");
        }

        createGUI();
    }

    public static List<Body> createBodiesFromPolygons(World world, List<List<Vector2>> polygons) {
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
            System.out.println("rounds passed: " + player.roundsPassed);
            clearEverything(); // dispose safely
            game.setScreen(new GameScreen(game, playerSlotNumber));
            return; // exit immediately to avoid using disposed objects
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
            switch (currentPhase) {
                case PLAYER_TURN:
                    handlePlayerTurn(delta);
                    break;

                case NPC_TURN:
                    handleNPCTurn(delta);
                    break;

                case END_TURN:
                    // cleanup / regen AP etc.
                    startNewTurn();
                    break;
            }

            updateInventoryUI();

            world.step(1 / 60f, 6, 2);
            player.handleCamera(viewport, mapGenerator, camera);

            player.update(delta, camera, collisionBoxes);
            playerLight.setPosition(player.x + player.collisionBox.getBounds().width / 2, player.y + player.collisionBox.getBounds().height / 2);
            playerLight.update();
            healthBar.setValue(player.health / player.maxHealth * 100);

            Iterator<NonPlayableCharacter> it = NPCs.iterator();
            while (it.hasNext()) {
                NonPlayableCharacter npc = it.next();
                npc.update(delta, camera, collisionBoxes);

                if (npc.getState() == NonPlayableCharacter.NPCState.DEAD && npc.hasSpawnedChest) {
                    it.remove();
                    deadNPCCount++;
                }
            }

            for (ChestEntity chest : chests) {
                chest.update(delta, camera, collisionBoxes);
            }

            endPoint.update(delta);

            if (Gdx.input.justTouched()) {
                Vector2 mouseWorld = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

                chests.forEach(chest -> {
                    if (chest.getHitbox().contains(mouseWorld.x, mouseWorld.y)) {
                        System.out.println("Opening Chest");
                        chest.openChest(RegistryManager.ITEMS, stage, skin);
                    }
                });

            }

            if (deadNPCCount >= NPCs.size()) {
                endPoint.open();
            }
        }

        draw();
        fpsLabel.setText("FPS: " + Gdx.graphics.getFramesPerSecond());
        stage.act(delta);
        stage.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = !paused;
            if (paused) {
                pause();
            } else resume();
        }
    }

    // Turn Handling
    private void handlePlayerTurn(float delta) {
        movementBar.setValue(100 - (player.distanceTraveledThisTurn / player.moveDistance * 100));

        if (player.hasCompletedMove()) {
            player.sprite.setDirection(Sprite.Direction.IDLE);
            player.canMove = false;
            currentNPCIndex = 0;

            for (NonPlayableCharacter npc : NPCs) {
                npc.recalculatePath();
            }

            currentPhase = TurnPhase.NPC_TURN;
        }
    }

    private void handleNPCTurn(float delta) {

        if (currentNPCIndex >= NPCs.size()) {
            currentPhase = TurnPhase.END_TURN;
            return;
        }

        NonPlayableCharacter npc = NPCs.get(currentNPCIndex);

        if (npc.hasCompletedTurn) {
            npc.hasDamagedPlayer = false;
            currentNPCIndex++;
        }
    }

    private void startNewTurn() {
        player.resetForNewTurn();
        currentPhase = TurnPhase.PLAYER_TURN;
    }

    // Drawing
    private void draw() {
        ScreenUtils.clear(new Color(0x1312121));
        camera.update();

        renderer.setView(camera);
        renderer.render();

        if (showHitBox) {
            drawHitboxes();
            debugRenderer.render(world, camera.combined);
        }
        drawSprites();

        rayHandler.setCombinedMatrix(camera.combined);
        rayHandler.updateAndRender();

        viewport.apply();
        spriteBatch.setProjectionMatrix(camera.combined);
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

    private void drawPauseGUI() {
        createPauseGUI();

        stage.act();
        stage.draw();
    }

    private void createGUI() {
        Camera uiCamera = new OrthographicCamera();
        Viewport uiViewport = new ExpandingViewport(90f, true, uiCamera);

        stage = new Stage(uiViewport, spriteBatch);
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("GUI/GUISkin.json"));


        // Clear previous HUD actors
        stage.clear();

        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        fpsLabel = new Label("FPS: 0", labelStyle);
        fpsLabel.setAlignment(Align.topLeft);
        fpsLabel.setPosition(20, Gdx.graphics.getHeight() - 20); // top-left corner
        stage.addActor(fpsLabel);

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Image centerColImage = new Image(skin, "GUICenter");
        Image leftColImage = new Image(skin, "GUILeft");
        Image rightColImage = new Image(skin, "GUIRight");

        // Left column: Health & Temp Inventory
        Stack leftStack = new Stack();

        Table leftCol = new Table();
        leftCol.left().bottom();

        // Temporary Inventory
        Table tempInv = new Table();
        tempSlots.clear();
        for (int i = 0; i < 3; i++) {
            ImageButton slot = new ImageButton(skin, "GUIslot");
            int finalI = i;
            slot.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Item item = tempSlotItems.get(slot);
                    if (item != null) {
                        if (RegistryManager.ITEMS.get(item.getItemName()) instanceof ConsumableItem) {
                            player.applyBonus(item.getBonus().first, item.getBonus().second, 0);
                            player.invTemp.removeItemFromSlot(finalI);
                            tempSlotItems.replace(slot, null);
                            System.out.println(item.getItemName() + " consumed and removed from slot " + finalI);
                        } else System.out.println(item.getItemName() + " is not consumable.");
                    }
                }
            });

            tempInv.add(slot).size(46).padLeft(21).padRight(21);
            tempSlots.add(slot);
        }
        tempInv.padLeft(2).padBottom(46);
        leftCol.add(tempInv);
        leftCol.row();

        // Health Bar
        healthBar = new ProgressBar(0f, 100f, 1f, false, skin, "red-bar");
        leftCol
            .add(healthBar)
            .width(226)
            .align(Align.left)
            .padLeft(21)
            .padBottom(24);

        leftStack.add(leftColImage);
        leftStack.add(leftCol);

        root.add(leftStack).expand().left().bottom();

        // Middle column: Permanent Inventory (5 slots)
        Stack centerStack = new Stack();

        permSlots.clear();
        Table permInv = new Table();
        for (int i = 0; i < 5; i++) {
            ImageButton slot = new ImageButton(skin, "GUIslot");
            permInv.add(slot).size(96).pad(18);
            permSlots.add(slot);
        }

        centerStack.add(centerColImage);
        centerStack.add(permInv);

        root.add(centerStack).expandX().bottom();

        // Right column: Temporary Inventory (3 slots) + AP slot
        Stack rightStack = new Stack();

        Table rightCol = new Table();
        rightCol.right().bottom();

        // Action points slot
        ImageButton apSlot = new ImageButton(skin, "slot-debug");
        rightCol.add(apSlot).size(46).align(Align.left).padLeft(23).padBottom(46);
        rightCol.row();

        // Movement Bar
        movementBar = new ProgressBar(0f, 100f, 1f, false, skin, "blue-bar");
        rightCol.add(movementBar).width(226).align(Align.left).padLeft(21).padRight(21).padBottom(24);
        rightStack.add(rightColImage);
        rightStack.add(rightCol);

        root.add(rightStack).expand().right().bottom();
    }

    private void createPauseGUI() {
        Camera uiCamera = new OrthographicCamera();
        Viewport uiViewport = new ExpandingViewport(90f, true, uiCamera);

        stage = new Stage(uiViewport, spriteBatch);
        skin = new Skin(Gdx.files.internal("GUI/GUISkin.json"));

        // Clear previous HUD actors
        stage.clear();
    }

    public void updateInventoryUI() {
        // Temporary inventory
        List<Item> tempInventory = player.invTemp.getContents();
        List<Item> permInventory = player.invPerm.getContents();

        for (int i = 0; i < tempSlots.size(); i++) {
            ImageButton slot = tempSlots.get(i);
            slot.clearChildren(); // clear old icons

            if (i < tempInventory.size()) {
                Item item = tempInventory.get(i);
                if (item != null && item.getItemTexture() != null) {
                    Image icon = new Image(new TextureRegionDrawable(item.getItemTexture()));
                    tempSlotItems.put(slot, item);
                    slot.add(icon).size(32, 32).center();
                }
            }
        }

        // Permanent inventory
        for (int i = 0; i < permSlots.size(); i++) {
            ImageButton slot = permSlots.get(i);
            slot.clearChildren();

            if (i < permInventory.size()) {
                Item item = permInventory.get(i);
                if (item != null && item.getItemTexture() != null) {
                    Image icon = new Image(new TextureRegionDrawable(item.getItemTexture()));
                    slot.add(icon).size(64, 64).center();
                }
            }
        }
    }

    // Screen Lifecycle
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        if (map != null) map.dispose();
        if (renderer != null) renderer.dispose();
        spriteBatch.dispose();
        shape.dispose();
        if (rayHandler != null) rayHandler.dispose();
        if (world != null) world.dispose();
        stage.dispose();
        skin.dispose();
        if (whitePixel != null) whitePixel.dispose();
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
        drawPauseGUI();
    }

    @Override
    public void resume() {
        createGUI();
        stage.act();
        stage.draw();
    }

    public void clearEverything() {

        // Dispose disposable objects
        if (map != null) {
            map.dispose();
            map = null;
        }
        if (renderer != null) {
            renderer.dispose();
            renderer = null;
        }
        if (spriteBatch != null) {
            spriteBatch.dispose();
            spriteBatch = null;
        }
        if (shape != null) {
            shape.dispose();
            shape = null;
        }
        if (rayHandler != null) {
            rayHandler.dispose();
            rayHandler = null;
        }
        if (world != null) {
            world.dispose();
            world = null;
        }
        if (stage != null) {
            stage.dispose();
            stage = null;
        }
        if (skin != null) {
            skin.dispose();
            skin = null;
        }
        if (whitePixel != null) {
            whitePixel.dispose();
            whitePixel = null;
        }
        if (mapGenerator != null) {
            MapGenerator.collisionVertexes.clear();
            mapGenerator = null;
        }

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
        fpsLabel = null;
        movementBar = null;
        healthBar = null;
        currentPhase = null;
        currentNPCIndex = 0;

    }

    public void requestRestart() {
        if (player != null) {
            FileHandler.saveTemp(playerSlotNumber, player);
        }
        restartRequested = true;
    }

    public enum TurnPhase {
        PLAYER_TURN,
        NPC_TURN,
        END_TURN
    }


}
