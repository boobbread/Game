package com.mjolkster.artifice.entities;

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

    public ChestEntity(int maxHealth, int armorClass, float movement, int maxActionPoints) {
        super(maxHealth, armorClass, movement, maxActionPoints, new Sprite("ChestSprite.png", 4, 5, 0.2f));
    }

    public void openChest(Registry<Item> itemRegistry, Stage stage, Skin skin) {
        if (state == ChestState.OPEN) return; // already open

        this.sprite.setDirection(Sprite.Direction.DOWN);
        System.out.println("Chest Opened");
        state = ChestState.OPEN;
        generateLoot(itemRegistry); // fill the chest with random items

        ChestGUI gui = new ChestGUI(stage, skin);
        if (loot.isEmpty()) System.out.println("loot is empty");
        gui.showLoot(loot, this,  gui::hide);
    }

    public void closeChest() {
        state = ChestState.CLOSED;
        GameScreen.chests.remove(this);
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
                allItems.add(item);
            }
        }

        if (allItems.isEmpty()) {
            System.out.println("All items is empty");
            return; // nothing to give
        }

        Collections.shuffle(allItems);
        int itemsToGive = Math.min(3, allItems.size());

        for (int i = 0; i < itemsToGive; i++) {
            loot.add(allItems.get(i));
        }
    }

    public Rectangle getHitbox() {
        if (hitbox == null) hitbox = new Rectangle(x, y, 1f, 1f); // 1x1 tile
        else hitbox.setPosition(x, y);
        return hitbox;
    }
}
