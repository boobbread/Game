package com.mjolkster.artifice.items;

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
