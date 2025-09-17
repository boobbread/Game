package com.mjolkster.artifice.core.entities;

/**
 * Provides information about the player, such as max health and move speed
 */
// TODO : implement the other archetypes and a way to chose which one you take
    // TODO : add attack sets to each archetype
public enum Archetype {

    FIGHTER(10f, 2, 14, 30, "fighter"),
    MAGE(10f, 1, 12, 20, "mage"),
    ROGUE(15f, 1, 10, 25, "rogue"),
    RANGER(10f, 1, 12, 25, "ranger"),
    BARBARIAN(7f, 3, 14, 50, "barbarian");

    final String name;
    final float moveDistance;
    final int actionPoints;
    final int armorClass;
    final int healthMax;

    Archetype(float moveDistance, int actionPoints, int armorClass, int healthMax, String name) {
        this.moveDistance = moveDistance;
        this.actionPoints = actionPoints;
        this.armorClass = armorClass;
        this.healthMax = healthMax;
        this.name = name;
    }

}
