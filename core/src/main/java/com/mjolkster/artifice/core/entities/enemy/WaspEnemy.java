package com.mjolkster.artifice.core.entities.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mjolkster.artifice.core.entities.PlayableCharacter;
import com.mjolkster.artifice.graphics.screen.GameScreen;
import com.mjolkster.artifice.util.geometry.Hitbox;
import com.mjolkster.artifice.util.graphics.Sprite;
import com.mjolkster.artifice.util.math.Dice;
import com.mjolkster.artifice.util.math.Gaussian;

import java.util.ArrayList;
import java.util.List;

/**
 * A flying enemy that attacks by dropping down on the player
 */
public class WaspEnemy extends BaseEnemy {

    public enum WaspState {
        TRACK,
        READY,
        DIVE,
        WAIT,
        RESET
    }

    private WaspState state;
    public boolean hasFinishedAttack;
    private final Dice dice;

    public WaspEnemy(Vector2 spawnPoint, PlayableCharacter target, GameScreen gameScreen) {
        super(spawnPoint, target, new Sprite("wasp.png", 5, 6, 0.1f),
            (int) (10 + Math.random() * 5), 10f, gameScreen);

        TextureRegion[][] waspFrames = TextureRegion.split(new Texture("wasp.png"), 32, 64);

        sprite.addAnimation("ready", waspFrames[0], 0.1f, false);
        sprite.addAnimation("dive", waspFrames[4], 0.1f, false);
        sprite.addAnimation("reset", waspFrames[1], 0.1f, false);
        sprite.addAnimation("wait", waspFrames[5], 0.1f, false);
        sprite.setDirection(Sprite.Direction.LEFT);

        sprite.setHitbox(Sprite.Direction.UP, new Hitbox(new Rectangle()));
        sprite.setHitbox(Sprite.Direction.DOWN, new Hitbox(new Rectangle()));
        sprite.setHitbox(Sprite.Direction.LEFT, new Hitbox(new Rectangle(13f/32f, 4f/32f, 14f/32f, 42f/32f)));
        sprite.setHitbox(Sprite.Direction.RIGHT, new Hitbox(new Rectangle(13f/32f, 4f/32f, 14f/32f, 42f/32f)));
        sprite.setHitbox(Sprite.Direction.IDLE, new Hitbox(new Rectangle(10f/32f, 5f/32f, 17f/32f, 16f/32f)));

        this.dice = new Dice("2 - 4 - 0");
        // Start in TRACK state
        changeState(WaspState.TRACK);
    }

    public static ArrayList<Gaussian> getGaussians() {
        ArrayList<Gaussian> gaussians = new ArrayList<>();

        gaussians.add(new Gaussian(9, 14, 4.3));
        gaussians.add(new Gaussian(6, 40, 4.3));
        gaussians.add(new Gaussian(3, 50, 4.3));

        return gaussians;
    }

    @Override
    public void performAction() {
        // Ensure animation progresses
        float delta = Gdx.graphics.getDeltaTime();
        sprite.update(delta);

        switch (state) {
            case TRACK:
                if (hitbox.overlaps(target.collisionBox.getBounds())) {
                    changeState(WaspState.READY);
                    this.hasFinishedAttack = false;
                    hasDamagedPlayer = false;
                }
                break;

            case READY:
                if (sprite.isAnimationFinished()) {
                    changeState(WaspState.DIVE);
                }
                break;

            case DIVE:
                if (sprite.isAnimationFinished()) {
                    if (!hasDamagedPlayer) {
                        damagePlayer(target); // apply once immediately
                    }
                    changeState(WaspState.WAIT);
                }
                break;

            case WAIT:
                if (sprite.isAnimationFinished()) {
                    sprite.setDirection(Sprite.Direction.IDLE);
                    changeState(WaspState.RESET);
                }
            case RESET:
                if (sprite.isAnimationFinished()) {
                    changeState(WaspState.TRACK);
                    sprite.setDirection(Sprite.Direction.LEFT); // default flight direction
                    hasCompletedTurn = true;
                    this.hasFinishedAttack = true;
                }
                break;
        }
    }

    @Override
    protected void damagePlayer(PlayableCharacter target) {

        for (Rectangle rect : target.collisionBox) {
            for (Rectangle otherRect : this.hitbox) {
                if (rect.overlaps(otherRect)) {
                    int damageRoll = dice.rollDice();
                    target.changeHealth(-damageRoll);
                    hasDamagedPlayer = true;
                    Gdx.app.log("WaspEnemy", "Damaged player: " + damageRoll);
                }
            }
        }
    }

    @Override
    protected void updateSpriteDirection() {
        if (averageDirection.isZero(0.05f)) {
            sprite.setDirection(Sprite.Direction.IDLE);
            return;
        }

        float angle = averageDirection.angleDeg();
        if (angle >= 135 && angle < 315) {
            sprite.setDirection(Sprite.Direction.LEFT);
        } else {
            sprite.setDirection(Sprite.Direction.RIGHT);
        }
    }

    private void changeState(WaspState newState) {
        if (state == newState) return; // prevent repeated resets
        this.state = newState;

        switch (newState) {
            case READY:
                sprite.playAnimation("ready", false);
                break;
            case DIVE:
                sprite.playAnimation("dive", false);
                break;
            case WAIT:
                sprite.playAnimation("wait", false);
                break;
            case RESET:
                sprite.playAnimation("reset", false);
                break;
            case TRACK:
                sprite.stopAnimation();
                break;
        }
    }

    public WaspState getWaspState() {
        return state;
    }

    @Override
    public Hitbox getHitbox() {
        return this.sprite.getHitbox();
    }
}
