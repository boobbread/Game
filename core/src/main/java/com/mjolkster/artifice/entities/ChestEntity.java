package com.mjolkster.artifice.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mjolkster.artifice.items.ConsumableItem;
import com.mjolkster.artifice.items.TemporaryItem;
import com.mjolkster.artifice.registration.Registry;
import com.mjolkster.artifice.screen.ChestGUI;
import com.mjolkster.artifice.screen.GameScreen;
import com.mjolkster.artifice.util.Sprite;
import com.mjolkster.artifice.items.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChestEntity extends Entity {

    public enum ChestState {
        CLOSED,
        OPEN
    }

    private ChestState state = ChestState.CLOSED;
    private List<Item> loot;
    private Rectangle hitbox;

    public Boolean itemPicked;

    public ChestEntity(int maxHealth, int armorClass, float movement, int maxActionPoints, GameScreen gameScreen) {
        super(maxHealth, armorClass, movement, maxActionPoints, new Sprite("ChestSprite.png", 4, 5, 0.2f), gameScreen);
    }

    public void openChest(Registry<Item> itemRegistry, Stage stage, Skin skin) {
        if (state == ChestState.OPEN) return; // already open

        this.sprite.setDirection(Sprite.Direction.DOWN);
        state = ChestState.OPEN;
        generateLoot(itemRegistry); // fill the chest with random items

        ChestGUI gui = new ChestGUI(stage, skin, gameScreen);
        if (loot.isEmpty()) Gdx.app.log("ChestEntity", "loot is empty");
        gui.showLoot(loot, this,  gui::hide);
    }

    public void closeChest() {
        state = ChestState.CLOSED;
        gameScreen.chests.remove(this);
        this.sprite.setDirection(Sprite.Direction.LEFT);
    }

    public void spawnChest(Vector2 spawnpoint) {
        this.x = spawnpoint.x * 1 / 32f + 2;
        this.y = spawnpoint.y * 1 / 32f;
    }

    public void spawnChest(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void generateLoot(Registry<Item> itemRegistry) {
        if (loot != null) return; // already generated

        loot = new ArrayList<>();

        List<Item> allItems = new ArrayList<>();
        for (Item item : itemRegistry.getAll().values()) {
            if (item instanceof TemporaryItem || item instanceof ConsumableItem) {
                Item.Rarity rarity = item.getRarity();

                int i = 0;

                switch (rarity) {
                    case COMMON: i = 6; break;
                    case UNCOMMON: i = 4; break;
                    case RARE: i = 2; break;
                    case ANOMALOUS: i = 1; break;
                }

                for (int x = 0; x <= i; x++) {
                    allItems.add(item);
                }

            }
        }

        if (allItems.isEmpty()) {
            return; // nothing to give
        }

        Collections.shuffle(allItems);
        int itemsToGive = Math.min(3, allItems.size());

        for (int i = 0; i < itemsToGive; i++) {
            loot.add(allItems.get(i));
        }
    }

    public Rectangle getHitbox() {
        if (hitbox == null) hitbox = new Rectangle(x + 3/32f, y + 2/32f, 26/32f, 21/32f); // 1x1 tile
        else hitbox.setPosition(x, y);
        return hitbox;
    }
}
