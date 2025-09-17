package com.mjolkster.artifice.core.items;

import com.mjolkster.artifice.core.entities.PlayableCharacter;

/**
 * An item which is consumable and lost on death
 */
public class ConsumableItem extends Item {

    int effectTime;
    boolean poisonous;

    public ConsumableItem(String name, Rarity rarity, Bonus bonus, int bonusAmount, int effectTime, boolean poisonous) {
        super(name, rarity, bonus, bonusAmount, poisonous);

        this.effectTime = effectTime;
        this.poisonous = poisonous;
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

    public boolean isPoisonous() {
        return poisonous;
    }
}
