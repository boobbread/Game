package com.mjolkster.artifice.util.math;

import java.util.Random;

public class Dice {

    int numDice;
    int die;
    int modifier;
    Random random;

    /**
     * Rolls a dice
     * @param roll The dice roll you want (number - dice - modifier)
     */
    public Dice(String roll) {
        String[] splitDamageRoll = roll.split("\\s*-\\s*");
        this.numDice = Integer.parseInt(splitDamageRoll[0]);
        this.die = Integer.parseInt(splitDamageRoll[1]);
        this.modifier = Integer.parseInt(splitDamageRoll[2]);

        random = new Random();
    }

    public int rollDice() {

        int result = 0;

        for (int i = 0; i < numDice; i++) {
            int roll = (int) (random.nextFloat() * die) + 1;
            result += roll;
        }
        if (result > numDice * die) result -= 1;
        result += modifier;

        return result;
    }
}
