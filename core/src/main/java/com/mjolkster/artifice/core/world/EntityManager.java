package com.mjolkster.artifice.core.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mjolkster.artifice.core.entities.Archetype;
import com.mjolkster.artifice.core.entities.PlayableCharacter;
import com.mjolkster.artifice.core.entities.enemy.BaseEnemy;
import com.mjolkster.artifice.core.entities.enemy.SlugEnemy;
import com.mjolkster.artifice.core.items.Inventory;
import com.mjolkster.artifice.graphics.screen.GameScreen;
import com.mjolkster.artifice.io.FileHandler;
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

public class EntityManager {
    private PlayableCharacter player;
    private final List<BaseEnemy> NPCs = new ArrayList<>();
    private final List<ChestEntity> chests = new ArrayList<>();
    private final TurnManager turnManager;
    private final GameScreen gameScreen;

    public EntityManager(GameMap map, int slotNumber, GameScreen gameScreen) {

        this.gameScreen = gameScreen;

        // load player from save / new
        Vector2 spawnpoint = map.getPlayerSpawnpoint();
        FileHandler.SaveSlot save = FileHandler.loadTempSave(slotNumber);

        if (slotNumber >= 0 && slotNumber <= 2 && save != null) {
            FileHandler.Save saveData = save.saveData;

            player = new PlayableCharacter(
                saveData.health,
                new Inventory(3).getContents(),
                new Inventory(5).getContents(),
                saveData.archetype, saveData.roundsPassed,
                slotNumber, spawnpoint, gameScreen
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
            player = new PlayableCharacter(Archetype.FIGHTER, spawnpoint, gameScreen);
            player.addItemToPermanentInv(ItemRegistry.nest_feather.get());
        }

        player.actionPoints = player.maxActionPoints;
        player.setContext(PlayableCharacter.Context.DUNGEON);

        // spawn NPCs using map.getSpawnableAreas()
        List<Vector2> spawnableAreas = map.getSpawnableAreas();
        spawnNPCs(spawnableAreas, player.roundsPassed);

        // init TurnManager
        turnManager = new TurnManager(player, NPCs);

        Gdx.app.log("EntityManager", "Initialisation complete");

    }

    public void update(float delta, OrthographicCamera camera, Set<Line> collisionBoxes) {
        turnManager.update(delta);
        player.update(delta, camera, collisionBoxes);

        List<BaseEnemy> deadNPCs = new ArrayList<>();
        NPCs.forEach(npc -> {
            npc.update(delta, camera, collisionBoxes);
            if (npc.getState() == BaseEnemy.NPCState.DEAD) {
                deadNPCs.add(npc);
                spawnChestForNPC(npc);
            }
        });
        NPCs.removeAll(deadNPCs);

        chests.forEach(chest -> {
            chest.update(delta, camera, collisionBoxes);
            chest.checkForOpen(camera);
        });
    }

    public void render(SpriteBatch batch) {
        player.draw(batch);
        NPCs.forEach(npc -> npc.draw(batch));
        chests.forEach(chest -> chest.draw(batch));
    }

    public void renderHitboxes(ShapeRenderer shape) {
        Rectangle playerBox = player.collisionBox.getBounds();
        shape.rect(playerBox.x, playerBox.y, playerBox.width, playerBox.height);

        NPCs.forEach(npc -> {
            shape.rect(npc.getHitbox().x, npc.getHitbox().y,
                npc.getHitbox().width, npc.getHitbox().height);
            npc.drawPath(shape);
        });
    }

    private void spawnNPCs(List<Vector2> spawnableAreas, int roundsPassed) {
        if (spawnableAreas == null || spawnableAreas.isEmpty()) {
            Gdx.app.log("EntityManager", "No valid spawn locations found for NPCs");
            return;
        }

        ArrayList<Gaussian> slugGaussians = SlugEnemy.getGaussians();
        int spawnRate = CalculateGaussians.calculateMultiGaussian(slugGaussians, roundsPassed);
        int maxNPCs = Math.min(spawnRate, spawnableAreas.size());

        Collections.shuffle(spawnableAreas);
        for (int i = 0; i < maxNPCs; i++) {
            NPCs.add(new SlugEnemy(spawnableAreas.get(i), player, gameScreen));
        }
    }

    private void spawnChestForNPC(BaseEnemy npc) {
        if (Math.random() > 0.7) {
            float x = npc.getX();
            float y = npc.getY();

            ChestEntity chest = new ChestEntity(10, 20, 0, 0, gameScreen);
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
    }

    public void reset() {
        NPCs.clear();
        chests.clear();
    }

    public PlayableCharacter getPlayer() { return player; }

    public List<BaseEnemy> getNPCs() {
        return NPCs;
    }

    public List<ChestEntity> getChests() {
        return chests;
    }
}
