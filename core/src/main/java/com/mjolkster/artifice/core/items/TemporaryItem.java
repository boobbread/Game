package com.mjolkster.artifice.core.items;

import com.mjolkster.artifice.core.entities.PlayableCharacter;

/**
 * An item which is non-consumable and lost on death
 */
public class TemporaryItem extends Item {

    public TemporaryItem(String name, Rarity rarity, Bonus bonus, int bonusAmount, boolean poisonous) {
        super(name, rarity, bonus, bonusAmount, poisonous);
    }

    @Override
    public void onAcquire(PlayableCharacter player) {
        player.applyBonus(getBonus().first, getBonus().second, 0);
    }

    @Override
    public void onLose(PlayableCharacter player) {
        player.removeBonus(getBonus().first, getBonus().second);
    }
}
