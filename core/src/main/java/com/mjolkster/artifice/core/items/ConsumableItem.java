package com.mjolkster.artifice.core.items;

import com.mjolkster.artifice.core.entities.PlayableCharacter;

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
    }

    @Override
    public void onLose(PlayableCharacter player) {

    }
}
