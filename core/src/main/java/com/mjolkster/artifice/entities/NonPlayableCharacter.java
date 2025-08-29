package com.mjolkster.artifice.entities;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mjolkster.artifice.screen.GameScreen;
import com.mjolkster.artifice.util.Sprite;
import com.mjolkster.artifice.util.tools.AStarPathfinder;
import com.mjolkster.artifice.util.wrappers.Line;

import java.util.*;

public class NonPlayableCharacter extends Entity {

    private final PlayableCharacter target;
    private final Rectangle hitbox;
    private final AStarPathfinder pathfinder = new AStarPathfinder();
    public boolean hasCompletedTurn = true;
    public boolean hasDamagedPlayer = false;
    public float blockTimer;
    private Queue<Vector2> currentPath = new LinkedList<>();
    private float distanceTravelled = 0f;

    // Death / Life
    private NPCState state = NPCState.ALIVE;
    private float deathTimer = 0f;
    public enum NPCState {
        ALIVE,
        DYING,
        DEAD
    }

    public boolean hasSpawnedChest = false;

    public NonPlayableCharacter(Vector2 spawnPoint, PlayableCharacter player) {
        super(10, 0, 10f, 0, new Sprite("SlimeTest.png", 8, 5, 0.2f));
        this.x = spawnPoint.x / 32f;
        this.y = spawnPoint.y / 32f;
        this.target = player;

        this.hitbox = new Rectangle(x, y + 1/32f, 1f, 20/32f);

        sprite.setDirection(Sprite.Direction.IDLE);

    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    @Override
    public void update(float delta, OrthographicCamera camera, Set<Line> collisionBoxes) {

        if (state == NPCState.ALIVE && health <= 0) {
            state = NPCState.DYING;
            deathTimer = 0f;
        }

        if (state == NPCState.ALIVE) {
            followPath(delta);
            sprite.update(delta);
            sprite.getHitbox().setPosition(x, y);
            if (!hasCompletedTurn){
                damagePlayer(GameScreen.player);
            }

        } else if (state == NPCState.DYING) {
            deathTimer += delta;
            sprite.update(delta);

            if (deathTimer >= 1f) {
                state = NPCState.DEAD;
            }

        } else if (state == NPCState.DEAD) {
            if (!hasSpawnedChest) {
                if (Math.random() > 0.0f) {
                    ChestEntity chestEntity = new ChestEntity(10, 20, 0, 0);

                    Rectangle playerBounds = target.collisionBox.getBounds();
                    Rectangle chestBox = new Rectangle(x, y, 0.5f, 0.5f);

                    if (!chestBox.overlaps(playerBounds)) {
                        chestEntity.spawnChest(x, y);
                    } else chestEntity.spawnChest(x + 1, y + 1);

                    GameScreen.chests.add(chestEntity);

                    Rectangle finalChestBox = chestEntity.getHitbox();

                    float x1 = finalChestBox.x;
                    float x2 = finalChestBox.x + finalChestBox.width;
                    float y1 = finalChestBox.y;
                    float y2 = finalChestBox.y + finalChestBox.height;

                    List<Line> lines = new ArrayList<>();
                    lines.add(new Line(x1, y1, x2, y1));
                    lines.add(new Line(x1, y1, x1, y2));
                    lines.add(new Line(x1, y2, x2, y2));
                    lines.add(new Line(x2, y1, x2, y2));

                    GameScreen.collisionBoxes.addAll(lines);

                }
                hasSpawnedChest = true;
            }
        }
    }

    public void damagePlayer(PlayableCharacter target) {
        if (this.hitbox.overlaps(target.collisionBox.getBounds()) && !hasDamagedPlayer) {
            System.out.println("Damaging player");
            target.changeHealth(-1);
            hasDamagedPlayer = true;
        }
    }

    public void recalculatePath() {
        Vector2 start = new Vector2((int) x, (int) y);
        Vector2 goal = new Vector2((int) Math.ceil(target.getX()), (int) Math.ceil(target.getY()));
        currentPath = pathfinder.createAStarPathfinder(start, goal);
        distanceTravelled = 0f;
        hasCompletedTurn = false;
    }

    private void followPath(float delta) {
        if (currentPath == null || currentPath.isEmpty() || distanceTravelled >= moveDistance) {
            hasCompletedTurn = true;
            return;
        }

        float moveSpeed = 2f;

        Vector2 nextPoint = currentPath.peek();
        Vector2 targetPos = new Vector2(nextPoint.x, nextPoint.y);

        boolean blocked = isBlocked(targetPos, this, target);

        if (blocked) {

            blockTimer += delta;
            if (blockTimer > 0.5f) { // half a second stuck
                currentPath.poll(); // skip this tile
                blockTimer = 0f;
            }

            Vector2 separation = new Vector2(0, 0);
            for (NonPlayableCharacter other : GameScreen.NPCs) {
                if (other == this) continue;

                float dx = x - other.getX();
                float dy = y - other.getY();
                float dist2 = dx * dx + dy * dy;

                if (dist2 < .5f * .5f && dist2 > 0.01f) {
                    Vector2 push = new Vector2(dx, dy).scl(0.2f / dist2);
                    separation.add(push);
                }
            }

            if (!separation.isZero()) {
                separation.nor();
                x += separation.x * moveSpeed * delta * 0.5f;
                y += separation.y * moveSpeed * delta * 0.5f;
            }

        } else {

            Vector2 direction = targetPos.cpy().sub(x, y);

            if (direction.len() < 0.05f) {
                x = targetPos.x;
                y = targetPos.y;
                currentPath.poll();
            } else {
                direction.nor();
                float dx = direction.x * moveSpeed * delta;
                float dy = direction.y * moveSpeed * delta;

                x += dx;
                y += dy;
                distanceTravelled += (float) Math.sqrt(dx * dx + dy * dy);
            }
        }

        hitbox.setPosition(x, y);
    }

    private static boolean isBlocked(Vector2 targetPos, NonPlayableCharacter nonPlayableCharacter, PlayableCharacter target) {
        boolean blocked = false;
        for (NonPlayableCharacter other : GameScreen.NPCs) {
            if (other == nonPlayableCharacter) continue;

            int ox = (int) other.getX();
            int oy = (int) other.getY();
            if (ox == (int) targetPos.x && oy == (int) targetPos.y) {
                blocked = true;
                break;
            }
        }

        int ox = (int) target.getX();
        int oy = (int) target.getY();

        if (ox == (int) targetPos.x && oy == (int) targetPos.y) {
            blocked = true;
        }
        return blocked;
    }

    public void drawPath(ShapeRenderer shapeRenderer) {
        if (currentPath == null || currentPath.isEmpty()) return;

        float lastX = x + 0.5f;
        float lastY = y + 0.5f;

        for (Vector2 point : currentPath) {
            float nextX = point.x;
            float nextY = point.y;
            shapeRenderer.line(lastX, lastY, nextX, nextY);
            lastX = nextX;
            lastY = nextY;
        }

    }

    public boolean hasCompletedMove() {
        return hasCompletedTurn;
    }

    public NPCState getState() {
        return this.state;
    }

}
