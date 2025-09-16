package com.mjolkster.artifice.core.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mjolkster.artifice.core.actions.AttackAction;
import com.mjolkster.artifice.core.entities.enemy.BaseEnemy;
import com.mjolkster.artifice.core.entities.enemy.SlugEnemy;
import com.mjolkster.artifice.io.FileHandler;
import com.mjolkster.artifice.core.world.generation.MapGenerator;
import com.mjolkster.artifice.core.items.Item;
import com.mjolkster.artifice.core.items.TimedEffect;
import com.mjolkster.artifice.io.input.ControllerInputHandler;
import com.mjolkster.artifice.io.input.HybridInputHandler;
import com.mjolkster.artifice.io.input.InputHandler;
import com.mjolkster.artifice.io.input.InputState;
import com.mjolkster.artifice.registry.registries.AttacksRegistry;
import com.mjolkster.artifice.graphics.screen.GameScreen;
import com.mjolkster.artifice.util.combat.DamageIndicatorManager;
import com.mjolkster.artifice.util.geometry.Hitbox;
import com.mjolkster.artifice.core.items.Inventory;
import com.mjolkster.artifice.util.graphics.Sprite;
import com.mjolkster.artifice.util.geometry.Line;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PlayableCharacter extends Entity {

    // State
    public final Archetype archetype;
    // Constants
    private final float attackDuration = 0.45f;
    private final float dashDuration = 0.20f;
    private final int slotNumber;
    public Hitbox collisionBox;
    public Inventory invTemp;
    public Inventory invPerm;
    public int strength;
    // Bools
    public boolean attacking = false;
    public boolean canMove = true;
    public AttackAction currentAttack;
    public float distanceTraveledThisTurn = 0f;
    public int roundsPassed;
    private boolean dashing = false;
    private float attackTimer = 0f;
    private float dashTimer = 0f;
    private float dashVelX = 0f;
    private List<TimedEffect> activeEffects;
    private Context context = Context.DUNGEON;
    private float damageTaken;

    private final InputHandler inputHandler;
    private final InputState inputState = new InputState();

    // Constructors

    /**
     * Makes a whole new player character
     * @param a The desired archetype
     * @param spawnpoint The Vector2 spawnpoint, generated during map gen
     * @param gameScreen The GameScreen you want the player spawned in
     */
    public PlayableCharacter(Archetype a, Vector2 spawnpoint, GameScreen gameScreen, InputHandler inputHandler) {
        super(
            a.healthMax,
            a.armorClass,
            a.moveDistance,
            a.actionPoints,
            new Sprite("SpriteSheet.png", 8, 5, 0.1f),
            gameScreen
        );

        this.archetype = a;
        this.x = spawnpoint.x / 32f;
        this.y = spawnpoint.y / 32f;

        this.invTemp = new Inventory(3);
        this.invPerm = new Inventory(5);

        this.strength = 2;

        this.activeEffects = new ArrayList<>();
        this.slotNumber = 0;

        this.inputHandler = inputHandler;

        createHitboxes();

        FileHandler.CreateNewSave(this, 0, 0);
    }

    public PlayableCharacter(
        float health,
        List<Item> tempInv,
        List<Item> permInv,
        Archetype a,
        int roundsPassedINPT,
        int slotNumber,
        Vector2 spawnpoint,
        GameScreen gameScreen,
        InputHandler inputHandler
    ) {
        super(
            a.healthMax,
            a.armorClass,
            a.moveDistance,
            a.actionPoints,
            new Sprite("SpriteSheet.png", 8, 5, 0.1f),
            gameScreen
        );

        this.archetype = a;
        this.health = health;
        this.roundsPassed = roundsPassedINPT;
        Gdx.app.log("Player", "Rounds passed: " + this.roundsPassed);

        this.x = spawnpoint.x / 32f;
        this.y = spawnpoint.y / 32f;

        this.invTemp = new Inventory(3);
        this.invTemp.setContents(tempInv);

        this.invPerm = new Inventory(5);
        this.invPerm.setContents(permInv);

        this.strength = 2;
        this.slotNumber = slotNumber;

        this.inputHandler = inputHandler;

        createHitboxes();
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void changeHealth(int amount) {
        this.health = Math.min(this.maxHealth, this.health + amount);
        if (amount < 0) {
            sprite.flashRed();
            InputHandler inputHandler = gameScreen.getEntityManager().getInputHandler();
            if (inputHandler instanceof ControllerInputHandler) {
                ((ControllerInputHandler) inputHandler).vibrate();
            } else if (inputHandler instanceof HybridInputHandler) {
                ((HybridInputHandler) inputHandler).getControllerHandler().vibrate();
            }
            Vector3 screenPos = getScreenPosition(gameScreen.getCamera());
            DamageIndicatorManager.showDamage(screenPos.x, screenPos.y, amount);

        }
    }

    // Inventory helpers
    public boolean addItemToPermanentInv(Item item) {
        item.onAcquire(this);
        return invPerm.addItem(item);
    }

    public boolean addItemToTemporaryInv(Item item) {
        item.onAcquire(this);
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

        if (effectTime == 0 && bonus != null) {
            switch (bonus) {
                case MAX_HEALTH: {
                    this.maxHealth += bonusAmount;
                }
                break;
                case MOVEMENT: {
                    this.moveDistance += bonusAmount;
                }
                break;
                case STRENGTH: {
                    this.strength += bonusAmount;
                }
                break;
                case MAX_ACTION_POINTS: {
                    this.maxActionPoints += bonusAmount;
                }
                break;
                case HEALTH: {
                    changeHealth(bonusAmount);
                }
                break;
                default: {
                    break;
                }
            }
        } else if (bonus != null) {
            this.activeEffects.add(new TimedEffect(itemBonus, bonusAmount, effectTime));
        }
    }

    public void removeBonus(Item.Bonus itemBonus, int bonusAmount) {
        if (itemBonus != null) {
            switch (itemBonus) {
                case MAX_HEALTH: {
                    this.maxHealth -= bonusAmount;
                }
                break;
                case MOVEMENT: {
                    this.moveDistance -= bonusAmount;
                }
                break;
                case STRENGTH: {
                    this.strength -= bonusAmount;
                }
                break;
                case MAX_ACTION_POINTS: {
                    this.maxActionPoints -= bonusAmount;
                }
                break;
            }
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

    public boolean hasCompletedMove() {
        if (distanceTraveledThisTurn >= moveDistance) {
            distanceTraveledThisTurn = 0f;
            actionPoints = archetype.actionPoints;

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

        this.collisionBox = this.sprite.getHitbox();
        if (collisionBox != null) {
            collisionBox.setOrigin(x, y);
        }

        if (this.health <= 0) {
            this.roundsPassed = 0;
            for (int i = 0; i < invTemp.getContents().size(); i++) {
                this.invTemp.removeItemFromSlot(i);
            }
            this.health = maxHealth;
            FileHandler.saveTemp(this.slotNumber, this);
            gameScreen.requestClose();
            return;
        }

        if (attacking && context == Context.DUNGEON) {
            updateAttackState(delta, collisionBoxes);
        } else {
            handleZoom(delta, camera);
            if (canMove) {
                handleInput(delta, collisionBoxes, camera);
            }
            sprite.update(delta);
        }

        updateEffects();

        if (collisionBox != null) {
            collisionBox.setOrigin(x, y);
        }
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

    public void handleInput(float delta, Set<Line> collisionBoxes, OrthographicCamera camera) {
        inputHandler.update(inputState, delta);

        // Movement
        if (inputState.moveX != 0 || inputState.moveY != 0) {
            if (inputState.moveY > 0) sprite.setDirection(Sprite.Direction.UP);
            else if (inputState.moveY < 0) sprite.setDirection(Sprite.Direction.DOWN);
            else if (inputState.moveX < 0) sprite.setDirection(Sprite.Direction.LEFT);
            else if (inputState.moveX > 0) sprite.setDirection(Sprite.Direction.RIGHT);

            handleMovement(inputState.moveX, inputState.moveY, x, y, collisionBoxes);
        } else if (inputState.idle) {
            sprite.setDirection(Sprite.Direction.IDLE);
        }

        // Actions
        if (context == Context.DUNGEON && this.actionPoints > 0) {
            if (inputState.spaceAction) {
                startAttack();
                return;
            }
            if (inputState.lAltAction) {
                startDash();
                return;
            }
        }

        // Zoom
        handleZoom(delta, camera);
    }

    private void startAttack() {
        attacking = true;
        attackTimer = 0f;

        dashing = false;
        dashTimer = 0f;
        dashVelX = 0f;

        if (inputHandler.isController()) {
            if (inputState.moveX >= 0) {
                currentAttack = AttacksRegistry.slash_right.get();
            } else if (inputState.moveX < 0) {
                currentAttack = AttacksRegistry.slash_left.get();
            }
        } else {
            if (Gdx.input.getX() / 32f < x) {
                currentAttack = AttacksRegistry.slash_left.get();
            } else {
                currentAttack = AttacksRegistry.slash_right.get();
            }
        }

        currentAttack.execute(this, null, this.gameScreen);
    }

    private void startDash() {
        attacking = true;
        attackTimer = 0f;

        dashing = true;
        dashTimer = 0f;

        if (inputHandler.isController()){
            if (inputState.moveX >= 0) {
                dashVelX = 8f;
                currentAttack = AttacksRegistry.dash_right.get();
            } else if (inputState.moveX < 0) {
                dashVelX = -8f;
                currentAttack = AttacksRegistry.dash_left.get();
            }
        } else {
            if (Gdx.input.getX() / 32f < x) {
                dashVelX = -8f;
                currentAttack = AttacksRegistry.dash_left.get();
            } else {
                dashVelX = 8f;
                currentAttack = AttacksRegistry.dash_right.get();
            }
        }

        currentAttack.execute(this, null, this.gameScreen);
    }

    private void handleMovement(float moveX, float moveY, float originalX, float originalY, Set<Line> collisionBoxes) {
        float dx = moveX;
        float dy = moveY;

        float newX = originalX + dx;
        float newY = originalY + dy;
        double sqrt = Math.sqrt(dx * dx + dy * dy);

        collisionBox.setOrigin(newX, newY);
        if (!collides(collisionBoxes)) {
            x = newX;
            y = newY;
            distanceTraveledThisTurn += (float) sqrt;
            return;
        }

        collisionBox.setOrigin(originalX + dx, originalY);
        if (!collides(collisionBoxes)) {
            x = originalX + dx;
            y = originalY;
            distanceTraveledThisTurn += (float) sqrt;
            return;
        }

        collisionBox.setOrigin(originalX, originalY + dy);
        if (!collides(collisionBoxes)) {
            x = originalX;
            y = originalY + dy;
            distanceTraveledThisTurn += (float) sqrt;
            return;
        }

        x = originalX;
        y = originalY;
        collisionBox.setOrigin(x, y);

        // Horizontal
        if (moveX != 0) {
            // enemy collision
            if (gameScreen != null && gameScreen.getNPCs() != null) {
                for (BaseEnemy npc : gameScreen.getNPCs()) {
                    if (npc instanceof SlugEnemy) {
                        if (collisionBox.overlaps(npc.getHitbox())) {
                            damageTaken += Math.abs(moveX);
                            if (damageTaken >= 1) {
                                changeHealth(-1);
                                damageTaken = 0;
                            }
                            x += 0.5f * moveX;
                            distanceTraveledThisTurn += 2 * Math.abs(moveX);
                            return;
                        }
                    }
                }
            }
        }

        // Vertical
        if (moveY != 0) {
            if (gameScreen != null && gameScreen.getNPCs() != null) {
                for (BaseEnemy npc : gameScreen.getNPCs()) {
                    if (npc instanceof SlugEnemy) {
                        if (collisionBox.overlaps(npc.getHitbox())) {
                            damageTaken += Math.abs(moveY);
                            if (damageTaken >= 1) {
                                changeHealth(-1);
                                damageTaken = 0;
                            }
                            y += 0.5f * moveY;
                            distanceTraveledThisTurn += 2 * Math.abs(moveY);
                            return;
                        }
                    }
                }
            }
        }
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
        if (context == Context.DUNGEON) {
            if (attacking) {
                currentAttack.draw(batch, x - 0.5f, y);
            } else {
                sprite.draw(batch, x, y);
            }
        } else sprite.draw(batch, x, y);
    }

    public void createHitboxes() {

        this.sprite.setHitbox(Sprite.Direction.UP, new Hitbox(new Rectangle(11f/32f, 2f/32f, 10f/32f, 17f/32f)));
        this.sprite.setHitbox(Sprite.Direction.DOWN, new Hitbox(new Rectangle(11f/32f, 2f/32f, 10f/32f, 15f/32f)));

        this.sprite.setHitbox(Sprite.Direction.LEFT, new Hitbox(List.of(
            new Rectangle( 4f/32f,  4f/32f, 24f/32f,  5f/32f),
            new Rectangle(9f/32f,  2f/32f,  5f/32f,  2f/32f),
            new Rectangle(21f/32f,  2f/32f,  7f/32f,  2f/32f),
            new Rectangle(16f/32f, 9f/32f, 10f/32f,  2f/32f)
        )));

        this.sprite.setHitbox(Sprite.Direction.RIGHT, new Hitbox(List.of(
            new Rectangle( 4f/32f,  4f/32f, 24f/32f,  5f/32f),
            new Rectangle(18f/32f,  2f/32f,  5f/32f,  2f/32f),
            new Rectangle(4f/32f,  2f/32f,  7f/32f,  2f/32f),
            new Rectangle(6f/32f, 9f/32f, 10f/32f,  2f/32f)
        )));

        this.sprite.setHitbox(Sprite.Direction.IDLE, new Hitbox(List.of(
            new Rectangle( 13f/32f,  0f/32f, 10f/32f,  12f/32f),
            new Rectangle(17f/32f,  12f/32f,  6f/32f,  6f/32f),
            new Rectangle(18f/32f,  18f/32f,  8f/32f,  4f/32f)
        )));
    }

    private boolean collides(Set<Line> collisionBoxes) {
        if (collisionBoxes != null) {
            for (Line line : collisionBoxes) {
                if (collisionBox.overlaps(line)) return true;
            }
        }
        return false;
    }

    public enum Context {
        DUNGEON,
        HUB
    }
}
