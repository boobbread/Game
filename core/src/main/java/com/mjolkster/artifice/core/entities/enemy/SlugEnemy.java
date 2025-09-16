package com.mjolkster.artifice.core.entities.enemy;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mjolkster.artifice.core.entities.PlayableCharacter;
import com.mjolkster.artifice.graphics.screen.GameScreen;
import com.mjolkster.artifice.util.geometry.Hitbox;
import com.mjolkster.artifice.util.graphics.Sprite;
import com.mjolkster.artifice.util.math.Gaussian;

import java.util.ArrayList;
import java.util.List;

public class SlugEnemy extends BaseEnemy {

    public SlugEnemy(Vector2 spawnPoint, PlayableCharacter target, GameScreen gameScreen) {
        super(spawnPoint, target, new Sprite("SlimeTest.png", 5, 5, 0.2f), (int) (10 + (Math.random() * 5)), 5f, gameScreen);

        this.sprite.setDirection(Sprite.Direction.IDLE);

        this.sprite.setHitbox(Sprite.Direction.UP, new Hitbox(new Rectangle(11/32f, 3/32f, 8/32f, 21/32f)));
        this.sprite.setHitbox(Sprite.Direction.DOWN, new Hitbox(new Rectangle(11/32f, 2/32f, 8/32f, 19/32f)));
        this.sprite.setHitbox(Sprite.Direction.LEFT, new Hitbox(List.of(
            new Rectangle( 4f/32f,  9f/32f, 8f/32f,  7f/32f),
            new Rectangle(5f/32f,  1f/32f,  20f/32f,  8f/32f)
        )));
        this.sprite.setHitbox(Sprite.Direction.RIGHT, new Hitbox(List.of(
            new Rectangle( 20f/32f,  9f/32f, 8f/32f,  7f/32f),
            new Rectangle(7f/32f,  1f/32f,  20f/32f,  8f/32f)
        )));
        this.sprite.setHitbox(Sprite.Direction.IDLE, new Hitbox(List.of(
            new Rectangle( 20f/32f,  9f/32f, 8f/32f,  7f/32f),
            new Rectangle(7f/32f,  1f/32f,  20f/32f,  8f/32f)
        )));

    }

    public static ArrayList<Gaussian> getGaussians() {
        ArrayList<Gaussian> gaussians = new ArrayList<>();

        gaussians.add(new Gaussian(9, 6.7, 4.3));
        gaussians.add(new Gaussian(6, 22.7, 4.3));
        gaussians.add(new Gaussian(4, 41.7, 4.3));

        return gaussians;
    }

    @Override
    public Hitbox getHitbox() {
        return this.sprite.getHitbox();
    }

    @Override
    public void performAction() {
        // Custom attack behavior
        damagePlayer(target);
    }

    @Override
    protected void damagePlayer(PlayableCharacter target) {
        for (Rectangle rect : target.collisionBox) {
            for (Rectangle otherRect : this.getHitbox()) {
                if (rect.overlaps(otherRect) && !hasDamagedPlayer) {
                    target.changeHealth(-1);
                    hasDamagedPlayer = true;
                }
            }
        }

    }
}
