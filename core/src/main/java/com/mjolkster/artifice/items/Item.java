package com.mjolkster.artifice.items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mjolkster.artifice.entities.PlayableCharacter;
import com.mjolkster.artifice.util.wrappers.Pair;

public abstract class Item {

    public enum Rarity {
        COMMON,
        UNCOMMON,
        RARE,
        ANOMALOUS
    }

    public enum Bonus {
        MAX_HEALTH,
        MOVEMENT,
        MAX_ACTION_POINTS,
        STRENGTH,
        HEALTH
    }

    TextureRegion itemTexture;
    String itemName;
    Rarity itemRarity;
    Bonus itemBonus;
    int bonusAmount;
    String itemDescription;

    private static final TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("items.atlas"));

    public Item(String name, Rarity rarity, Bonus bonus, int bonusAmount, String description) {
        this.itemName = name;
        this.itemRarity = rarity;
        this.itemBonus = bonus;
        this.bonusAmount = bonusAmount;
        this.itemDescription = description;

        TextureRegion region = atlas.findRegion(name);
        if (region == null) {
            throw new IllegalArgumentException("No region '" + name + "' found in items.atlas");
        } else this.itemTexture = region;

    }

    public Rarity getRarity() {
        return itemRarity;
    }

    public Pair<Bonus, Integer> getBonus() {
        return new Pair<>(this.itemBonus, this.bonusAmount);
    }

    public String getItemName() {
        return itemName;
    }

    public TextureRegion getItemTexture() {
        return itemTexture;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public abstract void onAcquire(PlayableCharacter player);
    public abstract void onLose(PlayableCharacter player);

}
