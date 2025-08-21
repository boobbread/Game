package com.mjolkster.artifice.util.tools;

import java.util.Random;

public class Dice {

    int numDice;
    int die;
    int modifier;
    Random random;

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
            result = (int) (random.nextFloat() * die);
        }

        result += modifier;

        return result;
    }
}
