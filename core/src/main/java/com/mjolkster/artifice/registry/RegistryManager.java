package com.mjolkster.artifice.registry;

import com.mjolkster.artifice.core.actions.AttackAction;
import com.mjolkster.artifice.core.entities.Entity;
import com.mjolkster.artifice.core.items.Item;

public class RegistryManager {

    public static final Registry<Item> ITEMS = new Registry<>("items");
    public static final Registry<Entity> ENTITIES = new Registry<>("entities");
    public static final Registry<AttackAction> ATTACKS = new Registry<>("attacks");

}
