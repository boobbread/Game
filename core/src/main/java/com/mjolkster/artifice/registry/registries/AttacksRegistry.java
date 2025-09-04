package com.mjolkster.artifice.registry.registries;

import com.badlogic.gdx.math.Rectangle;
import com.mjolkster.artifice.core.actions.AttackAction;
import com.mjolkster.artifice.registry.RegistryManager;
import com.mjolkster.artifice.registry.RegistryObject;
import com.mjolkster.artifice.util.geometry.Hitbox;

import java.util.List;

public class AttacksRegistry {
    public static final RegistryObject<AttackAction> slash_left =
        RegistryManager.ATTACKS.register("slash_left", () ->
            new AttackAction(
                "slash_left",
                "Slash",
                "2 - 4 - 0",
                1,
                List.of(
                    new Hitbox(new Rectangle(0f, 0, 0f, 0f)),
                    new Hitbox(new Rectangle(0f, 0, 0f, 0f)),
                    new Hitbox(new Rectangle(0f, 0, 0f, 0f)),
                    new Hitbox(new Rectangle(31 / 32f, 8 / 32f, 13 / 32f, 9 / 32f)),
                    new Hitbox(new Rectangle(-16 / 32f, 2 / 32f, 54 / 32f, 12 / 32f)),
                    new Hitbox(new Rectangle(-14 / 32f, 2 / 32f, 16 / 32f, 19 / 32f)),
                    new Hitbox(new Rectangle(-9 / 32f, 7 / 32f, 12 / 32f, 17 / 32f)),
                    new Hitbox(new Rectangle(0f, 0, 0f, 0f)),
                    new Hitbox(new Rectangle(0f, 0, 0f, 0f))
                ),
                "fighter_sprites.atlas",
                "slash_left"
            )
        );

    public static final RegistryObject<AttackAction> slash_right =
        RegistryManager.ATTACKS.register("slash_right", () ->
            new AttackAction(
                "slash_right",
                "Slash",
                "2 - 4 - 0",
                1,
                List.of(
                    new Hitbox(new Rectangle(0f, 0, 0f, 0f)),
                    new Hitbox(new Rectangle(0f, 0, 0f, 0f)),
                    new Hitbox(new Rectangle(0f, 0, 0f, 0f)),
                    new Hitbox(new Rectangle(-13 / 32f, 8 / 32f, 13 / 32f, 9 / 32f)),
                    new Hitbox(new Rectangle(-6 / 32f, 2 / 32f, 54 / 32f, 12 / 32f)),
                    new Hitbox(new Rectangle(30 / 32f, 2 / 32f, 16 / 32f, 19 / 32f)),
                    new Hitbox(new Rectangle(29 / 32f, 7 / 32f, 12 / 32f, 17 / 32f)),
                    new Hitbox(new Rectangle(0f, 0, 0f, 0f)),
                    new Hitbox(new Rectangle(0f, 0, 0f, 0f))
                ),
                "fighter_sprites.atlas",
                "slash_right"
            )
        );

    public static final RegistryObject<AttackAction> dash_left =
        RegistryManager.ATTACKS.register("dash_left", () ->
            new AttackAction(
                "dash_left",
                "Dash",
                "1 - 4 - 3",
                1,
                List.of(
                    new Hitbox(new Rectangle(0f, 0, 0f, 0f)),
                    new Hitbox(new Rectangle(0f, 0, 0f, 0f)),
                    new Hitbox(new Rectangle(0f, 0, 0f, 0f)),
                    new Hitbox(new Rectangle(-5 / 32f, 10 / 32f, 19 / 32f, 4 / 32f)),
                    new Hitbox(new Rectangle(-16 / 32f, 10 / 32f, 19 / 32f, 4 / 32f)),
                    new Hitbox(new Rectangle(-16 / 32f, 10 / 32f, 19 / 32f, 4 / 32f)),
                    new Hitbox(new Rectangle(-11 / 32f, 10 / 32f, 19 / 32f, 4 / 32f)),
                    new Hitbox(new Rectangle(0f, 0, 0f, 0f)),
                    new Hitbox(new Rectangle(0f, 0, 0f, 0f))
                ),
                "fighter_sprites.atlas",
                "dash_left"
            )
        );

    public static final RegistryObject<AttackAction> dash_right =
        RegistryManager.ATTACKS.register("dash_right", () ->
            new AttackAction(
                "dash_right",
                "Dash",
                "1 - 4 - 3",
                1,
                List.of(
                    new Hitbox(new Rectangle(0f, 0, 0f, 0f)),
                    new Hitbox(new Rectangle(0f, 0, 0f, 0f)),
                    new Hitbox(new Rectangle(0f, 0, 0f, 0f)),
                    new Hitbox(new Rectangle(18 / 32f, 10 / 32f, 19 / 32f, 4 / 32f)),
                    new Hitbox(new Rectangle(29 / 32f, 10 / 32f, 19 / 32f, 4 / 32f)),
                    new Hitbox(new Rectangle(29 / 32f, 10 / 32f, 19 / 32f, 4 / 32f)),
                    new Hitbox(new Rectangle(24 / 32f, 10 / 32f, 19 / 32f, 4 / 32f)),
                    new Hitbox(new Rectangle(0f, 0, 0f, 0f)),
                    new Hitbox(new Rectangle(0f, 0, 0f, 0f))
                ),
                "fighter_sprites.atlas",
                "dash_right"
            )
        );
}
