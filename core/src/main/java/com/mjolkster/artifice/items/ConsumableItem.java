package com.mjolkster.artifice.items;

import com.mjolkster.artifice.entities.PlayableCharacter;

public class ConsumableItem extends Item {

    int effectTime;
    public ConsumableItem(String name, Rarity rarity, Bonus bonus, int bonusAmount, int effectTime, String itemDescription) {
        super(name, rarity, bonus, bonusAmount, itemDescription);

        this.effectTime = effectTime;
    }

    public int getEffectTime() {
        return effectTime;
    }

    @Override
    public void onAcquire(PlayableCharacter player) {
        player.applyBonus(getBonus().first, getBonus().second, effectTime);
    }

    @Override
    public void onLose(PlayableCharacter player) {

    }
}
