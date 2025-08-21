package com.mjolkster.artifice.actions;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.mjolkster.artifice.entities.Entity;
import com.mjolkster.artifice.entities.NonPlayableCharacter;
import com.mjolkster.artifice.screen.GameScreen;
import com.mjolkster.artifice.util.Hitbox;
import com.mjolkster.artifice.util.Sprite;
import com.mjolkster.artifice.util.tools.Dice;

public class AttackAction implements Action{

    private final String id;
    private final String name;
    private final Dice diceRoller;
    private final int actionPointCost;
    private final String damageRoll;
    private final Entity effector;
    private final Hitbox hitbox;

    /**
     * Performs an attack action from an effector
     * @param id The ID tag of the attack
     * @param name The readable name of the attack
     * @param damageRoll The damage roll of the attack in format    1 - 4 - 3 -> 1d4+3
     * @param actionPointCost The action point cost of the attack
     * @param effector The Entity doing the action
     * @param hitbox The attack's hitbox
     */
    public AttackAction(String id, String name, String damageRoll, int actionPointCost, Entity effector,
                        Hitbox hitbox) {

        this.id = id;
        this.name = name;
        this.diceRoller = new Dice(damageRoll);

        this.actionPointCost = actionPointCost;
        this.damageRoll = damageRoll;
        this.effector = effector;

        this.hitbox = hitbox;

    }


    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getDamageRoll() {
        return damageRoll;
    }

    public Hitbox getHitbox() { return hitbox; }

    public void drawHitbox(ShapeRenderer batch) {
        batch.rect(hitbox.getBounds().x, hitbox.getBounds().y, hitbox.getBounds().width, hitbox.getBounds().height);
    }

    @Override
    public int getActionPointCost() {
        return actionPointCost;
    }

    @Override
    public void execute(Entity effector, Entity target) {

        hitbox.setPosition(effector.x - effector.sprite.getHitbox().getBounds().getWidth() / 2,
            effector.y);

        boolean hitTarget = false;

        for (Entity t : GameScreen.NPCs) {

            if (hitbox.overlaps(t.sprite.getHitbox())) {
                int damage = diceRoller.rollDice();
                t.changeHealth(-damage);
                hitTarget = true;
                System.out.println("damage");
            }
        }

        if (hitTarget) {
            effector.spendActionPoint(actionPointCost);
        }
    }
}
