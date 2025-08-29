package com.mjolkster.artifice.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mjolkster.artifice.actions.AttackAction;
import com.mjolkster.artifice.files.FileHandler;
import com.mjolkster.artifice.generators.MapGenerator;
import com.mjolkster.artifice.items.TimedEffect;
import com.mjolkster.artifice.registration.registries.AttacksRegistry;
import com.mjolkster.artifice.screen.GameScreen;
import com.mjolkster.artifice.util.Hitbox;
import com.mjolkster.artifice.util.Inventory;
import com.mjolkster.artifice.items.Item;
import com.mjolkster.artifice.util.Sprite;
import com.mjolkster.artifice.util.wrappers.Line;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PlayableCharacter extends Entity {

    // Constants
    private final float attackDuration = 0.45f;
    private final float dashDuration   = 0.20f;

    // State
    public final Archetype archetype;
    public Hitbox collisionBox;
    public Inventory invTemp;
    public Inventory invPerm;
    public int strength;

    // Bools
    public boolean attacking = false;
    public boolean canMove   = true;
    private boolean dashing  = false;

    public AttackAction currentAttack;
    public float distanceTraveledThisTurn = 0f;
    public int roundsPassed;

    private float attackTimer = 0f;
    private float dashTimer   = 0f;
    private float dashVelX    = 0f;

    private List<TimedEffect> activeEffects;
    // Constructors
    public PlayableCharacter(Archetype a, Vector2 spawnpoint) {
        super(
            a.healthMax,
            a.armorClass,
            a.moveDistance,
            a.actionPoints,
            new Sprite("SpriteSheet.png", 8, 5, 0.1f)
        );

        this.archetype = a;
        this.x = spawnpoint.x / 32f;
        this.y = spawnpoint.y / 32f;

        this.invTemp = new Inventory(3);
        this.invPerm = new Inventory(5);

        this.strength = 2;

        this.collisionBox = new Hitbox(new Rectangle(x + 0.5f, y, 0.8f, 0.6f));

        this.activeEffects = new ArrayList<>();

        System.out.println(health);

        FileHandler.CreateNewSave(this, GameScreen.seed, 0);
    }

    public PlayableCharacter(
        float health,
        List<Item> tempInv,
        List<Item> permInv,
        Archetype a,
        int roundsPassedINPT,
        int slotNumber,
        Vector2 spawnpoint
    ) {
        super(
            a.healthMax,
            a.armorClass,
            a.moveDistance,
            a.actionPoints,
            new Sprite("SpriteSheet.png", 8, 5, 0.1f)
        );

        this.archetype = a;
        this.health = health;
        this.roundsPassed = roundsPassedINPT;

        this.x = spawnpoint.x / 32f;
        this.y = spawnpoint.y / 32f;

        this.invTemp = new Inventory(3);
        this.invTemp.setContents(tempInv);

        this.invPerm = new Inventory(5);
        this.invPerm.setContents(permInv);

        this.strength = 2;

        this.collisionBox = new Hitbox(new Rectangle(x + 0.5f, y, 0.8f, 0.6f));

        FileHandler.CreateNewSave(this, GameScreen.seed, slotNumber);
    }

    // Inventory helpers
    public boolean addItemToPermanentInv(Item item) {
        return invPerm.addItem(item);
    }

    public boolean addItemToTemporaryInv(Item item) {
        System.out.println("Item " + item.getItemName() + " added to Temporary Inventory");
        return invTemp.addItem(item);
    }

    public boolean removeItemFromPermanentInv(Item item) {
        return invPerm.getContents().remove(item);
    }

    public boolean removeItemFromTemporaryInv(Item item) {
        return invTemp.getContents().remove(item);
    }

    // Bonus effects
    public void applyBonus(Item.Bonus itemBonus, int bonusAmount, int effectTime) {
        Item.Bonus bonus = itemBonus;

        if (effectTime == 0) {
            switch (bonus) {
                case MAX_HEALTH : {
                    this.maxHealth += bonusAmount;
                }
                break;
                case MOVEMENT : {
                    this.moveDistance += bonusAmount;
                }
                break;
                case STRENGTH : {
                    this.strength += bonusAmount;
                }
                break;
                case MAX_ACTION_POINTS : {
                    this.maxActionPoints += bonusAmount;
                }
                break;
                case HEALTH: {
                    changeHealth(bonusAmount);
                }
                break;
            }
        } else {
            this.activeEffects.add(new TimedEffect(itemBonus, bonusAmount, effectTime));
        }
    }

    public void removeBonus(Item.Bonus itemBonus, int bonusAmount) {
        switch(itemBonus) {
            case MAX_HEALTH : {
                this.maxHealth -= bonusAmount;
            } break;
            case MOVEMENT : {
                this.moveDistance -= bonusAmount;
            } break;
            case STRENGTH : {
                this.strength -= bonusAmount;
            } break;
            case MAX_ACTION_POINTS : {
                this.maxActionPoints -= bonusAmount;
            } break;
        }
    }

    public void updateEffects() {
        if (activeEffects == null) return;
        Iterator<TimedEffect> it = activeEffects.iterator();
        while (it.hasNext()) {
            TimedEffect e = it.next();
            e.duration--;
            if (e.duration <= 0) {
                removeBonus(e.bonus, e.amount);
                it.remove();
            }
        }
    }

    // Rounds
    public void incrementRounds() {
        roundsPassed++;
    }

    public boolean hasCompletedMove() {
        if (distanceTraveledThisTurn >= moveDistance) {
            distanceTraveledThisTurn = 0f;
            actionPoints = archetype.actionPoints;

            System.out.println("Player at: " + x + ", " + y);
            return true;
        }
        return false;
    }

    public void resetForNewTurn() {
        canMove = true;
        distanceTraveledThisTurn = 0f;
        actionPoints = archetype.actionPoints;
    }

    // Update Loop
    @Override
    public void update(float delta, OrthographicCamera camera, Set<Line> collisionBoxes) {
        if (attacking) {
            updateAttackState(delta, collisionBoxes);
        } else {
            handleZoom(delta, camera);

            if (canMove) {
                handleInput(delta, collisionBoxes, camera);
            }
            sprite.update(delta);
        }

        updateEffects();

        collisionBox.setPosition(x + 0.1f, y);
    }

    private void updateAttackState(float delta, Set<Line> collisionBoxes) {
        attackTimer += delta;

        if (currentAttack != null) {
            currentAttack.update(delta);
        }

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
            currentAttack = null;
        }
    }

    // Camera
    public void handleCamera(Viewport viewport, MapGenerator mapGenerator, OrthographicCamera camera) {
        float halfW = viewport.getWorldWidth() / 2f;
        float halfH = viewport.getWorldHeight() / 2f;

        float mapW = mapGenerator.width;
        float mapH = mapGenerator.height;

        float cameraX = x;
        float cameraY = y;

        // Horizontal clamp
        if (mapW < viewport.getWorldWidth()) {
            cameraX = mapW / 2f;
        } else {
            cameraX = Math.max(halfW, Math.min(cameraX, mapW - halfW + 2));
        }

        // Vertical clamp
        if (mapH < viewport.getWorldHeight()) {
            cameraY = mapH / 2f;
        } else {
            cameraY = Math.max(halfH, Math.min(cameraY, mapH - halfH + 2));
        }

        camera.position.set(cameraX, cameraY, 0);

        // Clamp player position
        x = Math.max(0, Math.min(x, mapW + collisionBox.getBounds().width / 2));
        y = Math.max(0, Math.min(y, mapH + collisionBox.getBounds().height / 2));
    }

    // Input
    public void handleInput(float delta, Set<Line> collisionBoxes, OrthographicCamera camera) {
        float moveSpeed = 2f;
        float moveX = 0f, moveY = 0f;

        float originalX = x;
        float originalY = y;

        // Movement
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

        // Attacks
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            startAttack();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ALT_LEFT)) {
            startDash();
            return;
        }

        // Collision with NPCs and map
        handleMovement(moveX, moveY, originalX, originalY, collisionBoxes);
    }

    private void startAttack() {
        attacking = true;
        attackTimer = 0f;

        dashing   = false;
        dashTimer = 0f;
        dashVelX  = 0f;

        if (Gdx.input.getX() / 32f < x) {
            currentAttack = AttacksRegistry.slash_left.get();
        } else {
            currentAttack = AttacksRegistry.slash_right.get();
        }

        currentAttack.execute(this, null);
    }

    private void startDash() {
        attacking   = true;
        attackTimer = 0f;

        dashing   = true;
        dashTimer = 0f;

        if (Gdx.input.getX() / 32f < x) {
            dashVelX = -8f;
            currentAttack = AttacksRegistry.dash_left.get();
        } else {
            dashVelX = 8f;
            currentAttack = AttacksRegistry.dash_right.get();
        }

        currentAttack.execute(this, null);
    }

    private void handleMovement(float moveX, float moveY, float originalX, float originalY, Set<Line> collisionBoxes) {
        // Horizontal
        if (moveX != 0) {
            for (NonPlayableCharacter npc : GameScreen.NPCs) {
                if (collisionBox.overlaps(npc.getHitbox())) {
                    changeHealth(-Math.abs(moveX));
                    x += 0.5f * moveX;
                    distanceTraveledThisTurn += 2 * Math.abs(moveX);
                    return;
                }
            }

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

        // Vertical
        if (moveY != 0) {
            for (NonPlayableCharacter npc : GameScreen.NPCs) {
                if (collisionBox.overlaps(npc.getHitbox())) {
                    changeHealth(-Math.abs(moveY));
                    y += 0.5f * moveY;
                    distanceTraveledThisTurn += 2 * Math.abs(moveY);
                    return;
                }
            }

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
        distanceTraveledThisTurn += (float) Math.sqrt(dx * dx + dy * dy);
    }

    // Zoom
    public void handleZoom(float delta, OrthographicCamera camera) {
        float zoomSpeed = 1f;

        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            camera.zoom -= zoomSpeed * delta; // zoom in
        }
        if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            camera.zoom += zoomSpeed * delta; // zoom out
        }

        camera.zoom = Math.max(0.5f, Math.min(camera.zoom, 30f));
    }

    // Draw
    @Override
    public void draw(SpriteBatch batch) {
        if (attacking) {
            currentAttack.draw(batch, x - 0.5f, y);
        } else {
            sprite.draw(batch, x, y);
        }
    }
}
