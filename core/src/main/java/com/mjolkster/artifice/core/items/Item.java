package com.mjolkster.artifice.core.items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mjolkster.artifice.core.entities.PlayableCharacter;
import com.mjolkster.artifice.util.data.ItemLocalization;
import com.mjolkster.artifice.util.data.Pair;

public abstract class Item {

    private static final TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("items.atlas"));
    TextureRegion itemTexture;
    String displayName;
    String itemName;
    Rarity itemRarity;
    Bonus itemBonus;
    int bonusAmount;
    String itemDescription;
    boolean poisonous;

    public Item(String name, Rarity rarity, Bonus bonus, int bonusAmount, boolean poisonous) {
        this.itemName = name;
        this.itemRarity = rarity;
        this.itemBonus = bonus;
        this.bonusAmount = bonusAmount;
        this.poisonous = poisonous;

        // Load from JSON
        ItemLocalization.ItemStrings loc = ItemLocalization.get(name);
        if (loc == null) {
            throw new IllegalArgumentException("Missing localization for item: " + name);
        }
        this.displayName = loc.displayName;
        this.itemDescription = loc.description;

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

    public String getDisplayName() { return displayName; }

    public TextureRegion getItemTexture() {
        return itemTexture;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public abstract void onAcquire(PlayableCharacter player);

    public abstract void onLose(PlayableCharacter player);

    public String isItemPoisonous() {
        if (poisonous) {
            return "[#7CD942]POISONOUS[]";
        }
        return "";
    }

    public String getFormattedBonus() {
        if (itemBonus == null || bonusAmount == 0) return "";
        String color = "";

        switch (itemBonus) {
            case HEALTH : color = "#cd3232"; break;
            case MAX_HEALTH : color = "#ff4444"; break;
            case STRENGTH : color = "#ffaa00"; break;
            case MOVEMENT : color = "#00aaff"; break;
            case MAX_ACTION_POINTS : color = "#aa00ff"; break;
        };

        return "[" + color + "]+" + bonusAmount + " " + itemBonus.name().replace("_", " ") + "[]";
    }

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

}
