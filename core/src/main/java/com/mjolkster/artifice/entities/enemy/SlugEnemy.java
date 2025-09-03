package com.mjolkster.artifice.entities.enemy;

import com.badlogic.gdx.math.Vector2;
import com.mjolkster.artifice.entities.NonPlayableCharacter;
import com.mjolkster.artifice.entities.PlayableCharacter;
import com.mjolkster.artifice.screen.GameScreen;
import com.mjolkster.artifice.util.Sprite;
import com.mjolkster.artifice.util.wrappers.Gaussian;

import java.util.ArrayList;

public class SlugEnemy extends NonPlayableCharacter {

    public SlugEnemy(Vector2 spawnPoint, PlayableCharacter target, GameScreen gameScreen) {
        super(spawnPoint, target, new Sprite("SlimeTest.png", 5, 5, 0.2f), (int) (10 + (Math.random() * 5)), 5f, gameScreen);
    }

    @Override
    public void performAction() {
        // Custom attack behavior
        damagePlayer(target);
    }

    public static ArrayList<Gaussian> getGaussians() {
        ArrayList<Gaussian> gaussians = new ArrayList<>();

        gaussians.add(new Gaussian(9, 6.7, 4.3));
        gaussians.add(new Gaussian(6, 22.7, 4.3));
        gaussians.add(new Gaussian(4, 41.7, 4.3));

        return gaussians;
    }
}
