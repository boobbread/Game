package com.mjolkster.artifice.core.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mjolkster.artifice.core.entities.Archetype;
import com.mjolkster.artifice.core.entities.CampFireEntity;
import com.mjolkster.artifice.core.entities.PlayableCharacter;
import com.mjolkster.artifice.core.entities.enemy.BaseEnemy;
import com.mjolkster.artifice.core.entities.enemy.SlugEnemy;
import com.mjolkster.artifice.core.entities.enemy.WaspEnemy;
import com.mjolkster.artifice.core.items.Inventory;
import com.mjolkster.artifice.graphics.screen.GameScreen;
import com.mjolkster.artifice.graphics.screen.SublevelScreen;
import com.mjolkster.artifice.io.FileHandler;
import com.mjolkster.artifice.io.input.HybridInputHandler;
import com.mjolkster.artifice.io.input.InputHandler;
import com.mjolkster.artifice.registry.registries.ItemRegistry;
import com.mjolkster.artifice.util.combat.TurnManager;
import com.mjolkster.artifice.util.geometry.EndPoint;
import com.mjolkster.artifice.util.geometry.Line;
import com.mjolkster.artifice.util.math.CalculateGaussians;
import com.mjolkster.artifice.util.math.Gaussian;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.mjolkster.artifice.graphics.screen.GameScreen.game;

public class EntityManager {
    private PlayableCharacter player;
    private final List<BaseEnemy> NPCs = new ArrayList<>();
    private final List<CampFireEntity> campfires = new ArrayList<>();
    private final List<ChestEntity> chests = new ArrayList<>();
    private final TurnManager turnManager;
    private final GameScreen gameScreen;
    private final EndPoint endPoint;
    private final InputHandler inputHandler;

    public EntityManager(GameMap map, int slotNumber, GameScreen gameScreen) {

        this.gameScreen = gameScreen;

        // load player from save / new
        Vector2 spawnpoint = map.getPlayerSpawnpoint();
        FileHandler.SaveSlot save = FileHandler.loadTempSave(slotNumber);

        inputHandler = new HybridInputHandler(gameScreen.getStage());

        if (slotNumber >= 0 && slotNumber <= 2 && save != null) {
            FileHandler.Save saveData = save.saveData;

            player = new PlayableCharacter(
                saveData.health,
                new Inventory(3).getContents(),
                new Inventory(5).getContents(),
                saveData.archetype, saveData.roundsPassed,
                slotNumber, spawnpoint, gameScreen, inputHandler
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
            player = new PlayableCharacter(Archetype.FIGHTER, spawnpoint, gameScreen, inputHandler);
            player.addItemToPermanentInv(ItemRegistry.nest_feather.get());
        }

        player.actionPoints = player.maxActionPoints;
        player.setContext(PlayableCharacter.Context.DUNGEON);

        List<Vector2> spawnableAreas = map.getSpawnableAreas();
        spawnNPCs(spawnableAreas, player.roundsPassed);

        turnManager = new TurnManager(player, NPCs);

        endPoint = new EndPoint(gameScreen.getGameMap().getEndPointPosition(), gameScreen);

        Gdx.app.log("EntityManager", "Initialisation complete");

    }

    public void update(float delta, OrthographicCamera camera, Set<Line> collisionBoxes) {
        turnManager.update(delta);
        player.update(delta, camera, collisionBoxes);
        endPoint.update(delta);

        List<BaseEnemy> deadNPCs = new ArrayList<>();
        NPCs.forEach(npc -> {
            npc.update(delta, camera, collisionBoxes);
            if (npc.getState() == BaseEnemy.NPCState.DEAD) {
                deadNPCs.add(npc);
                spawnChestForNPC(npc);
            }
        });
        NPCs.removeAll(deadNPCs);

        if (NPCs.isEmpty()) endPoint.open();

        chests.forEach(chest -> {
            chest.update(delta, camera, collisionBoxes);
            chest.checkForOpen(camera);
        });

        campfires.forEach(campfire -> campfire.update(delta, camera, collisionBoxes));
    }

    public void updateOnlyChests(float delta, OrthographicCamera camera, Set<Line> collisionBoxes) {
        chests.forEach(chest -> {
            chest.update(delta, camera, collisionBoxes);
            chest.checkForOpen(camera);
        });
        player.update(delta, camera, collisionBoxes);
    }

    public void render(SpriteBatch batch) {

        endPoint.draw(batch);
        NPCs.forEach(npc -> npc.draw(batch));
        chests.forEach(chest -> chest.draw(batch));
        campfires.forEach(campfire -> campfire.draw(batch));
        player.draw(batch);

    }

    public void renderHitboxes(ShapeRenderer shape) {
        Rectangle playerBox = player.collisionBox.getBounds();
        shape.rect(playerBox.x, playerBox.y, playerBox.width, playerBox.height);

        for(Rectangle rect : player.collisionBox) {
            shape.rect(rect.x, rect.y, rect.width, rect.height);
        }

        NPCs.forEach(npc -> {
            for(Rectangle rect : npc.getHitbox()) {
                shape.rect(rect.x, rect.y, rect.width, rect.height);
            }
            npc.drawPath(shape);
        });
    }

    private void spawnNPCs(List<Vector2> spawnableAreas, int roundsPassed) {
        if (spawnableAreas == null || spawnableAreas.isEmpty()) {
            Gdx.app.log("EntityManager", "No valid spawn locations found for NPCs");
            return;
        }

        ArrayList<Gaussian> slugGaussians = SlugEnemy.getGaussians();
        ArrayList<Gaussian> waspGaussians = WaspEnemy.getGaussians();

        Collections.shuffle(spawnableAreas);
        for (int i = 0; i < Math.min(CalculateGaussians.calculateMultiGaussian(slugGaussians, roundsPassed), spawnableAreas.size()); i++) {
            NPCs.add(new SlugEnemy(spawnableAreas.get(i), player, gameScreen));
        }

        Collections.shuffle(spawnableAreas);
        for (int i = 0; i < Math.min(CalculateGaussians.calculateMultiGaussian(waspGaussians, roundsPassed), spawnableAreas.size()); i++) {
            NPCs.add(new WaspEnemy(spawnableAreas.get(i), player, gameScreen));
        }

        Collections.shuffle(spawnableAreas);
        for (int j = 0; j < 5; j++) {
            CampFireEntity campFire = new CampFireEntity(gameScreen, gameScreen.getGameMap().getRayHandler());
            campFire.setPosition(spawnableAreas.get(j));
            campfires.add(campFire);
        }
    }

    private void spawnChestForNPC(BaseEnemy npc) {
        if (Math.random() > 0.0) {
            float x = npc.getX();
            float y = npc.getY();

            ChestEntity chest = new ChestEntity(gameScreen);
            Rectangle playerBounds = player.collisionBox.getBounds();
            Rectangle chestBox = new Rectangle(x, y, 0.5f, 0.5f);

            if (chestBox.overlaps(playerBounds)) {
                x += 1;
                y += 1;
            }

            chest.spawnChest(x, y);
            chest.setHitbox(chestBox);
            chests.add(chest);
        }
    }

    public void dispose() {
        NPCs.clear();
        chests.clear();
        campfires.clear();
    }

    public void reset() {
        NPCs.clear();
        chests.clear();
        campfires.clear();
    }

    public PlayableCharacter getPlayer() { return player; }

    public List<BaseEnemy> getNPCs() {
        return NPCs;
    }

    public List<ChestEntity> getChests() {
        return chests;
    }

    public InputHandler getInputHandler() {
        return inputHandler;
    }
}
