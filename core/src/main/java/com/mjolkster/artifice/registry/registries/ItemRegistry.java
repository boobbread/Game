package com.mjolkster.artifice.registry.registries;

import com.mjolkster.artifice.core.items.ConsumableItem;
import com.mjolkster.artifice.core.items.Item;
import com.mjolkster.artifice.core.items.PermanentItem;
import com.mjolkster.artifice.core.items.TemporaryItem;
import com.mjolkster.artifice.registry.RegistryManager;
import com.mjolkster.artifice.registry.RegistryObject;

public class ItemRegistry {
    public static final RegistryObject<ConsumableItem> small_health =
        RegistryManager.ITEMS.register("small_health", () ->
            new ConsumableItem(
                "small_health",
                Item.Rarity.COMMON,
                Item.Bonus.HEALTH,
                5,
                0,
                false
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
                false
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
                false
            )
        );

    public static final RegistryObject<TemporaryItem> ruined_copper_bracer =
        RegistryManager.ITEMS.register("ruined_copper_bracer", () ->
            new TemporaryItem(
                "ruined_copper_bracer",
                Item.Rarity.COMMON,
                Item.Bonus.STRENGTH,
                1,
                false
            )
        );

    public static final RegistryObject<TemporaryItem> tarnished_copper_bracer =
        RegistryManager.ITEMS.register("tarnished_copper_bracer", () ->
            new TemporaryItem(
                "tarnished_copper_bracer",
                Item.Rarity.UNCOMMON,
                Item.Bonus.STRENGTH,
                2,
                false
            )
        );

    public static final RegistryObject<TemporaryItem> pristine_copper_bracer =
        RegistryManager.ITEMS.register("pristine_copper_bracer", () ->
            new TemporaryItem(
                "pristine_copper_bracer",
                Item.Rarity.RARE,
                Item.Bonus.STRENGTH,
                4,
                false
            )
        );

    public static final RegistryObject<PermanentItem> nest_feather =
        RegistryManager.ITEMS.register("nest_feather", () ->
            new PermanentItem(
                "nest_feather",
                Item.Rarity.COMMON,
                null,
                0
            )
        );

    public static final RegistryObject<ConsumableItem> charred_flesh =
        RegistryManager.ITEMS.register("charred_flesh", () ->
            new ConsumableItem(
                "charred_flesh",
                Item.Rarity.RARE,
                Item.Bonus.MAX_HEALTH,
                3,
                0,
                false
            )
        );

    public static final RegistryObject<ConsumableItem> sewer_fungus =
        RegistryManager.ITEMS.register("sewer_fungus", () ->
            new ConsumableItem(
                "sewer_fungus",
                Item.Rarity.UNCOMMON,
                Item.Bonus.MAX_HEALTH,
                1,
                0,
                true
            )
        );

    public static final RegistryObject<ConsumableItem> spoiled_meat =
        RegistryManager.ITEMS.register("spoiled_meat", () ->
            new ConsumableItem(
                "spoiled_meat",
                Item.Rarity.UNCOMMON,
                Item.Bonus.MAX_HEALTH,
                2,
                0,
                true
            )
        );

    public static final RegistryObject<ConsumableItem> bitter_root =
        RegistryManager.ITEMS.register("bitter_root", () ->
            new ConsumableItem(
                "bitter_root",
                Item.Rarity.COMMON,
                Item.Bonus.MAX_ACTION_POINTS,
                1,
                0,
                true
            )
        );

    public static final RegistryObject<TemporaryItem> rag_wraps =
        RegistryManager.ITEMS.register("rag_wraps", () ->
            new TemporaryItem(
                "rag_wraps",
                Item.Rarity.UNCOMMON,
                Item.Bonus.MOVEMENT,
                2,
                false
            )
        );

    public static final RegistryObject<TemporaryItem> glass_shard =
        RegistryManager.ITEMS.register("glass_shard", () ->
            new TemporaryItem(
                "glass_shard",
                Item.Rarity.UNCOMMON,
                Item.Bonus.MAX_ACTION_POINTS,
                1,
                true
            )
        );

    public static void init() {
        // nothing needed here; class loading triggers static registry
    }
}
