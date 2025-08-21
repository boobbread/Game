package com.mjolkster.artifice.entities;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mjolkster.artifice.actions.AttackAction;
import com.mjolkster.artifice.generators.MapGenerator;
import com.mjolkster.artifice.registration.registries.AttacksRegistry;
import com.mjolkster.artifice.util.*;
import com.mjolkster.artifice.util.wrappers.Line;

import java.util.Set;

public class PlayableCharacter extends Entity {

    public Hitbox collisionBox;
    private final float scaleX = 1f, scaleY = 1f; // world units

    private PointLight light;

    public Inventory invTemp;
    public Inventory invPerm;
    public AttackAction slash;

    public final Archetype archetype;
    public Sprite attackSprite;

    // state
    public boolean attacking = false;
    private boolean dashing  = false;

    private float attackTimer = 0f;
    private float attackDuration = 0.45f; // length of your attack anim

    private float distanceTraveledThisTurn = 0f;

    // dash
    private float dashTimer = 0f;
    private float dashDuration = 0.20f;
    private float dashVelX = 0f; // units/sec

    public PlayableCharacter(Archetype a, Vector2 spawnpoint, RayHandler rayHandler) {
        super(a.healthMax, a.armorClass, a.moveDistance, a.actionPoints, new Sprite("SpriteSheet.png", 8, 5, 0.1f));
        this.archetype = a;

        this.x = spawnpoint.x * 1 / 32f;
        this.y = spawnpoint.y * 1 / 32f;

        this.invTemp = new Inventory(3);
        this.invPerm = new Inventory(5);

        this.collisionBox = new Hitbox(new Rectangle(x + 0.5f, y, 0.8f, 0.8f));

        // Current light engine, to be changed
        this.light = new PointLight(rayHandler, 200, Color.GOLDENROD, 6, x, y);
        attackSprite = new Sprite("FighterAttacks.png", 9,5, 0.05f);
    }

    public boolean addItemToPermanentInv(Item item) {
        return invPerm.addItem(item);
    }

    public boolean addItemToTemporaryInv(Item item) {
        return invTemp.addItem(item);
    }

    @Override
    public void update(float delta, OrthographicCamera camera, Set<Line> collisionBoxes) {
        if (attacking) {
            attackTimer += delta;
            attackSprite.update(delta);
            attackSprite.setPosition(x, y);

            if (dashing) {
                dashTimer += delta;
                float stepX = dashVelX * delta;

                float oldX = x;
                x += stepX;
                collisionBox.translate(stepX, 0);

                for (Line line : collisionBoxes) {
                    if (collisionBox.overlaps(line)) {
                        x = oldX;
                        collisionBox.translate(-stepX, 0);

                        dashing = false;
                        dashVelX = 0f;
                        break;
                    }
                }

                if (dashTimer >= dashDuration) {
                    dashing = false;
                    dashVelX = 0f;
                }
            }

            if (attackTimer >= attackDuration) {
                attacking = false;

            }
        } else {
            // only when not attacking
            handleZoom(delta, camera);
            handleInput(delta, collisionBoxes, camera);
            sprite.update(delta);

        }

        collisionBox.setPosition(x + 0.1f, y);

        // light follow
        light.setPosition(x + 0.5f, y + 0.5f);
    }

    public void handleCamera(Viewport viewport, MapGenerator mapGenerator, OrthographicCamera camera) {
        float halfViewportWidth = viewport.getWorldWidth() / 2f;
        float halfViewportHeight = viewport.getWorldHeight() / 2f;
        float mapWidth = mapGenerator.width;
        float mapHeight = mapGenerator.height;
        float cameraX = x;
        float cameraY = y;
        if (mapWidth < viewport.getWorldWidth()) {
            cameraX = mapWidth / 2f;
        } else {
            cameraX = Math.max(halfViewportWidth, Math.min(cameraX, mapWidth - halfViewportWidth + 2));
        }
        if (mapHeight < viewport.getWorldHeight()) {
            cameraY = mapHeight / 2f;
        } else {
            cameraY = Math.max(halfViewportHeight, Math.min(cameraY, mapHeight - halfViewportHeight + 2));
        }
        camera.position.set(cameraX, cameraY, 0);
        x = Math.max(0, Math.min(x, mapWidth + collisionBox.getBounds().width / 2));
        y = Math.max(0, Math.min(y, mapHeight + collisionBox.getBounds().height / 2));
    }

    public void handleInput(float delta, Set<Line> collisionBoxes, OrthographicCamera camera) {
        float moveSpeed = 2f;
        float moveX = 0, moveY = 0;
        float originalX = x, originalY = y;

        // ---- movement keys (only when NOT attacking) ----
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            moveY = moveSpeed * delta;
            sprite.setDirection(Sprite.Direction.LEFT);
        } else if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            moveY = -moveSpeed * delta;
            sprite.setDirection(Sprite.Direction.UP);
        } else if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            moveX = -moveSpeed * delta;
            sprite.setDirection(Sprite.Direction.DOWN);
        } else if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            moveX = moveSpeed * delta;
            sprite.setDirection(Sprite.Direction.RIGHT);
        } else {
            sprite.setDirection(Sprite.Direction.IDLE);
        }

        // ---- attacks ----
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            attacking = true;
            attackTimer = 0f;
            dashing = false; dashTimer = 0f; dashVelX = 0f;

            if (Gdx.input.getX() / 64f < x) {
                attackSprite.setDirection(Sprite.Direction.DOWN);
            } else {
                attackSprite.setDirection(Sprite.Direction.UP);
            }

            slash = AttacksRegistry.slash.get();

            // Position the attack hitbox relative to player direction
            float hbWidth = 1.5f;
            float hbHeight = 1.0f;

            Rectangle bounds = slash.getHitbox().getBounds();
            if (sprite.getCurrentDirection() == Sprite.Direction.UP) {
                bounds.set(x, y + 1f, hbWidth, hbHeight);
                System.out.println("up");
            } else if (sprite.getCurrentDirection() == Sprite.Direction.DOWN) {
                bounds.set(x, y - 1f, hbWidth, hbHeight);
                System.out.println("down");
            } else if (sprite.getCurrentDirection() == Sprite.Direction.LEFT) {
                bounds.set(x - 1f, y, hbHeight, hbWidth);
                System.out.println("left");

            } else if (sprite.getCurrentDirection() == Sprite.Direction.RIGHT) {
                bounds.set(x + 1f, y, hbHeight, hbWidth);
                System.out.println("right");

            }

            // now execute the attack with updated hitbox
            slash.execute(this, null);

            attackSprite.reset();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ALT_LEFT)) {
            attacking = true;
            attackTimer = 0f;

            dashing = true; dashTimer = 0f;
            if (Gdx.input.getX() / 64f < x) {
                attackSprite.setDirection(Sprite.Direction.RIGHT);
                dashVelX = -8f;
            } else {
                attackSprite.setDirection(Sprite.Direction.LEFT);
                dashVelX = 8f;
            }

            attackSprite.reset();
            return;
        }

        if (moveX != 0) {
            x += moveX;
            collisionBox.translate(moveX, 0);
            for (Line line : collisionBoxes) {
                if (collisionBox.overlaps(line)) {
                    x = originalX;
                    collisionBox.translate(-moveX, 0);
                    break;
                }
            }
        }

        if (moveY != 0) {
            y += moveY;
            collisionBox.translate(0, moveY);
            for (Line line : collisionBoxes) {
                if (collisionBox.overlaps(line)) {
                    y = originalY;
                    collisionBox.translate(0, -moveY);
                    break;
                }
            }
        }

        float dx = x - originalX;
        float dy = y - originalY;
        distanceTraveledThisTurn += (float)Math.sqrt(dx * dx + dy * dy);
    }

    public void handleZoom(float delta, OrthographicCamera camera) {
        float zoomSpeed = 1f; // zoom units per second

        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            camera.zoom -= zoomSpeed * delta; // zoom in
        }
        if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            camera.zoom += zoomSpeed * delta; // zoom out
        }

        // Optional: clamp the zoom level
        camera.zoom = Math.max(0.5f, Math.min(camera.zoom, 30f));
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (attacking) {
            attackSprite.draw(batch, x - 0.5f, y);
        } else {
            sprite.draw(batch, x, y);
        }
    }

    public boolean hasCompletedMove() {
        if (distanceTraveledThisTurn >= moveDistance) {
            distanceTraveledThisTurn = 0f; // reset for next turn
            System.out.println("Player at: " + x + ", " + y);
            return true;
        }
        return false;
    }


}
