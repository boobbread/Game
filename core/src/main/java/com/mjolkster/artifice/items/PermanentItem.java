package com.mjolkster.artifice.items;

import com.mjolkster.artifice.entities.PlayableCharacter;

public class PermanentItem extends Item {

    public PermanentItem(String name, Rarity rarity, Bonus bonus, int bonusAmount, String itemDescription) {
        super(name, rarity, bonus, bonusAmount, itemDescription);
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
