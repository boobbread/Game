package com.mjolkster.artifice.entities;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mjolkster.artifice.util.wrappers.Line;
import com.mjolkster.artifice.util.Sprite;

import java.util.Set;

public abstract class Entity {

    public float health;
    public int maxHealth;
    public int armorClass;
    public float moveDistance;
    public int actionPoints;
    public int maxActionPoints;
    public Sprite sprite;

    public float x;
    public float y;

    public Entity(int maxHealth, int armorClass, float movement, int maxActionPoints, Sprite sprite) {
        this.health = maxHealth;
        this.maxHealth = maxHealth;
        this.armorClass = armorClass;
        this.maxActionPoints = maxActionPoints;
        this.sprite = sprite;
        this.moveDistance = movement;

    }

    public void changeHealth(int amount) {
        this.health += amount;
        if (amount < 0) {
            sprite.flashRed();
        }
    }

    public void changeHealth(float amount) {
        this.health += amount;
        if (amount < 0) {
            sprite.flashRed();
        }
    }

    public void changeAC(int amount) {
        this.armorClass += amount;
    }

    public void spendActionPoint(int amount) {
        if (actionPoints > 0) {
            this.actionPoints -= amount;
        }
    }

    public void resetActionPoints() {
        this.actionPoints = maxActionPoints;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch, x, y);
    }

    public void update(float delta, OrthographicCamera camera, Set<Line> collisionBoxes) {
        sprite.update(delta);
    }
}
