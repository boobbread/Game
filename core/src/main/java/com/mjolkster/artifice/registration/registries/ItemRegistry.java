package com.mjolkster.artifice.registration.registries;

import com.mjolkster.artifice.items.ConsumableItem;
import com.mjolkster.artifice.items.Item;
import com.mjolkster.artifice.items.PermanentItem;
import com.mjolkster.artifice.items.TemporaryItem;
import com.mjolkster.artifice.registration.RegistryManager;
import com.mjolkster.artifice.registration.RegistryObject;

public class ItemRegistry {
    public static final RegistryObject<ConsumableItem> small_health =
        RegistryManager.ITEMS.register("small_health", () ->
            new ConsumableItem(
                "small_health",
                Item.Rarity.COMMON,
                Item.Bonus.HEALTH,
                5,
                0,
                "A small health boost."
            )
        );

    public static final RegistryObject<ConsumableItem> medium_health =
        RegistryManager.ITEMS.register("medium_health", () ->
            new ConsumableItem(
                "medium_health",
                Item.Rarity.UNCOMMON,
                Item.Bonus.HEALTH,
                10,
                0,
                "A medium health boost."
            )
        );

    public static final RegistryObject<ConsumableItem> large_health =
        RegistryManager.ITEMS.register("large_health", () ->
            new ConsumableItem(
                "large_health",
                Item.Rarity.RARE,
                Item.Bonus.HEALTH,
                15,
                0,
                "A large health boost."
            )
        );

    public static final RegistryObject<TemporaryItem> ruined_copper_bracer =
        RegistryManager.ITEMS.register("ruined_copper_bracer", () ->
            new TemporaryItem(
                "ruined_copper_bracer",
                Item.Rarity.COMMON,
                Item.Bonus.STRENGTH,
                2,
                "A small strength boost."
            )
        );

    public static final RegistryObject<TemporaryItem> tarnished_copper_bracer =
        RegistryManager.ITEMS.register("tarnished_copper_bracer", () ->
            new TemporaryItem(
                "tarnished_copper_bracer",
                Item.Rarity.UNCOMMON,
                Item.Bonus.STRENGTH,
                4,
                "A medium strength boost."
            )
        );

    public static final RegistryObject<TemporaryItem> pristine_copper_bracer =
        RegistryManager.ITEMS.register("pristine_copper_bracer", () ->
            new TemporaryItem(
                "pristine_copper_bracer",
                Item.Rarity.RARE,
                Item.Bonus.STRENGTH,
                6,
                "A large strength boost."
            )
        );

    public static void init() {
        // nothing needed here; class loading triggers static registration
    }
}
