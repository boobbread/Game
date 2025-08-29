package com.mjolkster.artifice.actions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.mjolkster.artifice.entities.Entity;
import com.mjolkster.artifice.entities.NonPlayableCharacter;
import com.mjolkster.artifice.entities.PlayableCharacter;
import com.mjolkster.artifice.screen.GameScreen;
import com.mjolkster.artifice.util.Hitbox;
import com.mjolkster.artifice.util.tools.Dice;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AttackAction implements Action{

    private final String id;
    private final String name;
    private final Dice diceRoller;
    private final int actionPointCost;
    private final String damageRoll;

    private PlayableCharacter effector;
    private boolean attacking;
    private Set<Entity> hitEntities;

    private final List<Hitbox> frameHitboxes;
    private final Rectangle activeBounds = new Rectangle();
    private Hitbox frameHb;

    private TextureAtlas atlas;
    private TextureRegion region;
    private Animation<TextureRegion> animation;

    private float stateTime;

    /**
     * Performs an attack action from an effector
     * @param id The ID tag of the attack
     * @param name The readable name of the attack
     * @param damageRoll The damage roll of the attack in format    1 - 4 - 3 -> 1d4+3
     * @param actionPointCost The action point cost of the attack
     * @param frameHitboxes The hitbox for each frame of the attack
     * @param atlasPath The name of the texture atlas
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
        System.out.println("Found region: " + region);

        TextureRegion[] tmp = region.split(
            region.getRegionWidth() / 9,
            region.getRegionHeight()
            )[0];

        this.animation = new Animation<>(0.05f, tmp);

        System.out.println(frameHitboxes.size());
        System.out.println(tmp.length);

        if (frameHitboxes.size() != tmp.length) {
            throw new IllegalArgumentException("Frame hitbox count must equal animation frame count!");
        }

    }

    public void update(float delta) {
        stateTime += delta;

        int frameIndex = animation.getKeyFrameIndex(stateTime);
        frameHb = frameHitboxes.get(frameIndex);
        activeBounds.set(frameHb.getBounds());   // copy size
        activeBounds.setPosition(effector.x + frameHb.getBounds().x, effector.y + frameHb.getBounds().y);

        if (attacking) {
            checkHits();
        }
    }

    public void checkHits() {

        for (NonPlayableCharacter npc : GameScreen.NPCs) {

            if (hitEntities.contains(npc)) return;

            if (npc.getHitbox().overlaps(activeBounds)) {
                hitEntities.add(npc);
                if (effector.actionPoints >= actionPointCost) {
                    int damage = diceRoller.rollDice() + effector.strength;
                    System.out.println("HIT! " + damage + ", " + damageRoll + " + " + effector.strength);
                    npc.changeHealth(-damage);
                }
            }
        }

    }

    public void draw(SpriteBatch batch, float x, float y) {
        TextureRegion currentFrame = animation.getKeyFrame(stateTime);

        float pixelsPerTile = 32f; // or 64, depending on your setup
        float worldWidth = currentFrame.getRegionWidth() / pixelsPerTile;
        float worldHeight = currentFrame.getRegionHeight() / pixelsPerTile;

        batch.draw(
            currentFrame,
            (x - worldWidth / 2f) + 1f,   // center horizontally
            y,                     // anchor at feet
            worldWidth,
            worldHeight
        );
    }

    public void drawHitbox(ShapeRenderer sr) {
        sr.rect(activeBounds.x, activeBounds.y, activeBounds.width, activeBounds.height);
    }

    public Rectangle getActiveHitbox() { return activeBounds; }

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

    @Override
    public void execute(PlayableCharacter effector, Entity target) {

        System.out.println(effector.actionPoints);

        this.effector = effector;
        stateTime = 0;

        if (!hitEntities.isEmpty()) {
            effector.spendActionPoint(actionPointCost);
            this.hitEntities.clear();
        }

        if (effector.actionPoints >= actionPointCost) {
            this.attacking = true;
        }
    }
}
