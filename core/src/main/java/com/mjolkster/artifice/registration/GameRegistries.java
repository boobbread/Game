package com.mjolkster.artifice.registration;

import com.mjolkster.artifice.actions.AttackAction;
import com.mjolkster.artifice.entities.Entity;
import com.mjolkster.artifice.util.Item;

public class GameRegistries {

    public static final Registry<Item> ITEMS = new Registry<>("items");
    public static final Registry<Entity> ENTITIES = new Registry<>("entities");
    public static final Registry<AttackAction> ATTACKS = new Registry<>("attacks");

}
