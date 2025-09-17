package com.mjolkster.artifice.core.actions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Null;
import com.mjolkster.artifice.core.entities.Entity;
import com.mjolkster.artifice.core.entities.enemy.BaseEnemy;
import com.mjolkster.artifice.core.entities.PlayableCharacter;
import com.mjolkster.artifice.core.entities.enemy.WaspEnemy;
import com.mjolkster.artifice.graphics.screen.GameScreen;
import com.mjolkster.artifice.util.geometry.Hitbox;
import com.mjolkster.artifice.util.math.Dice;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AttackAction implements Action {

    private final String id;
    private final String name;
    private final Dice diceRoller;
    private final int actionPointCost;
    private final String damageRoll;
    private final List<Hitbox> frameHitboxes;
    private final Rectangle activeBounds = new Rectangle();
    private PlayableCharacter effector;
    private boolean attacking;
    private final Set<Entity> hitEntities;
    private Hitbox frameHb;

    private final TextureAtlas atlas;
    private final TextureRegion region;
    private final Animation<TextureRegion> animation;

    private float stateTime;
    private GameScreen gameScreen;

    private boolean apSpent = false;


    /**
     * Performs an attack action from an effector
     *
     * @param id              The ID tag of the attack
     * @param name            The readable name of the attack
     * @param damageRoll      The damage roll of the attack in format    1 - 4 - 3 -> 1d4+3
     * @param actionPointCost The action point cost of the attack
     * @param frameHitboxes   The hitbox for each frame of the attack
     * @param atlasPath       The name of the texture atlas
     */

    public AttackAction(String id, String name, String damageRoll, int actionPointCost,
                        List<Hitbox> frameHitboxes, String atlasPath, String atlasRegion) {

        this.id = id;
        this.name = name;
        this.diceRoller = new Dice(damageRoll);

        this.actionPointCost = actionPointCost;
        this.damageRoll = damageRoll;
        this.hitEntities = new HashSet<>();

        this.frameHitboxes = frameHitboxes;
        this.atlas = new TextureAtlas(Gdx.files.internal(atlasPath));
        this.region = atlas.findRegion(atlasRegion);

        TextureRegion[] tmp = region.split(
            region.getRegionWidth() / 9,
            region.getRegionHeight()
        )[0];

        this.animation = new Animation<>(0.05f, tmp);

        if (frameHitboxes.size() != tmp.length) {
            throw new IllegalArgumentException("Frame hitbox count must equal animation frame count!");
        }

    }

    @Override
    public void update(float delta) {
        stateTime += delta;

        int frameIndex = animation.getKeyFrameIndex(stateTime);
        frameHb = frameHitboxes.get(frameIndex);
        activeBounds.set(frameHb.getBounds());
        activeBounds.setPosition(effector.x + frameHb.getBounds().x, effector.y + frameHb.getBounds().y);

        if (attacking) {
            checkHits();
        }
    }

    public void checkHits() {

        for (BaseEnemy npc : gameScreen.getNPCs()) {

            if (hitEntities.contains(npc)) continue; // already hit this entity

            if (npc.getHitbox().overlaps(activeBounds)) {
                if (!apSpent) {
                    effector.actionPoints -= actionPointCost;
                    apSpent = true;
                }

                hitEntities.add(npc);

                int damage = diceRoller.rollDice() + effector.strength;

                if (npc instanceof WaspEnemy) {
                    if (((WaspEnemy) npc).getWaspState() == WaspEnemy.WaspState.WAIT) {
                        npc.changeHealth(-damage);
                    }
                } else {
                    npc.changeHealth(-damage);
                }
            }
        }
    }

    @Override
    public void draw(SpriteBatch batch, float x, float y) {
        TextureRegion currentFrame = animation.getKeyFrame(stateTime);

        float pixelsPerTile = 32f;
        float worldWidth = currentFrame.getRegionWidth() / pixelsPerTile;
        float worldHeight = currentFrame.getRegionHeight() / pixelsPerTile;

        batch.draw(
            currentFrame,
            (x - worldWidth / 2f) + 1f,
            y,
            worldWidth,
            worldHeight
        );
    }

    public void drawHitbox(ShapeRenderer sr) {
        sr.rect(activeBounds.x, activeBounds.y, activeBounds.width, activeBounds.height);
    }

    public Rectangle getActiveHitbox() {
        return activeBounds;
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

    @Override
    public int getActionPointCost() {
        return actionPointCost;
    }

    /**
     * Executes the attack, starting the animation and checking for hits
     * @param effector The {@link com.mjolkster.artifice.core.entities.Entity Entity} performing the attack
     * @param nullableTarget Not used, just inherited from Action interface.
     * @param gameScreen The {@link com.mjolkster.artifice.graphics.screen.GameScreen GameScreen} in which the attack is taking place
     * @return True if the attack was executed, false if not
     */
    @Override
    public boolean execute(PlayableCharacter effector, @Null Entity nullableTarget, GameScreen gameScreen) {
        if (effector.actionPoints < actionPointCost) return false; // not enough AP

        this.gameScreen = gameScreen;
        this.effector = effector;
        this.stateTime = 0;
        this.attacking = true;

        this.hitEntities.clear();
        this.apSpent = false;

        return true; // attack started
    }

}
