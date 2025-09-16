package com.mjolkster.artifice.util.combat;

import com.mjolkster.artifice.core.entities.enemy.BaseEnemy;
import com.mjolkster.artifice.core.entities.PlayableCharacter;

import java.util.List;

public class TurnManager {

    private final PlayableCharacter player;
    private final List<BaseEnemy> npcs;

    public TurnManager(PlayableCharacter player, List<BaseEnemy> npcs) {
        this.player = player;
        this.npcs = npcs;
    }

    public void update(float delta) {

        if (player.hasCompletedMove()) {
            for (BaseEnemy npc : npcs) {
                npc.recalculatePath();
                npc.followPath(delta);
                npc.performAction();
                npc.damagedPlayer(false);
            }

            player.resetForNewTurn();
        }
    }
}


