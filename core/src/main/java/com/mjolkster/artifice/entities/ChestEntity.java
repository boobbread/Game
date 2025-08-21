package com.mjolkster.artifice.entities;

import com.badlogic.gdx.math.Vector2;
import com.mjolkster.artifice.util.Sprite;
import com.mjolkster.artifice.util.Item;

import java.util.List;

public class ChestEntity extends Entity{

    boolean hasBeenOpened;

    public ChestEntity(int maxHealth, int armorClass, float movement, int maxActionPoints) {
        super(maxHealth, armorClass, movement, maxActionPoints, new Sprite("ChestSprite.png", 4, 5, 0.2f));

        hasBeenOpened = false;
    }

    public List<Item> openChest() {
        return null;
    }

    public void closeChest() {
        hasBeenOpened = false;
    }

    public void spawnChest(Vector2 spawnpoint) {
        this.x = spawnpoint.x * 1 / 32f + 2;
        this.y = spawnpoint.y * 1 / 32f;
    }

}
