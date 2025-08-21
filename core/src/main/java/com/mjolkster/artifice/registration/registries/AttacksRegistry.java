package com.mjolkster.artifice.registration.registries;

import com.badlogic.gdx.math.Rectangle;
import com.mjolkster.artifice.actions.AttackAction;
import com.mjolkster.artifice.registration.GameRegistries;
import com.mjolkster.artifice.registration.RegistryObject;
import com.mjolkster.artifice.screen.GameScreen;
import com.mjolkster.artifice.util.Hitbox;

public class AttacksRegistry {
    public static final RegistryObject<AttackAction> slash =
        GameRegistries.ATTACKS.register("slash", () ->
                new AttackAction(
                "slash",
                "Slash",
                "2 - 4 - 0",
                1,
                GameScreen.player,
                new Hitbox(new Rectangle(GameScreen.player.x, GameScreen.player.y,2,1))
            )
        );

    public static final RegistryObject<AttackAction> dash =
        GameRegistries.ATTACKS.register("dash", () ->
            new AttackAction(
                "dash",
                "Dash",
                "1 - 4 - 3",
                1,
                GameScreen.player,
                new Hitbox(new Rectangle(GameScreen.player.x, GameScreen.player.y,2,1))
            )
        );
}
