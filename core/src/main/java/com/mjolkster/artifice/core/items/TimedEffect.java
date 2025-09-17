package com.mjolkster.artifice.core.items;

/**
 * A helper class for {@link com.mjolkster.artifice.core.items.ConsumableItem ConsumableItem}
 */
public class TimedEffect {
    public Item.Bonus bonus;
    public int amount;
    public int duration;

    public TimedEffect(Item.Bonus bonus, int amount, int duration) {
        this.bonus = bonus;
        this.amount = amount;
        this.duration = duration;
    }
}
