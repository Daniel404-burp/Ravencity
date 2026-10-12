package Main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class PoliceZombie {

    // =========================================================
    // SPRITE / ANIMATION
    // =========================================================

    private BufferedImage spriteSheet;

    // 0 = Down, 1 = Left, 2 = Right, 3 = Up
    private int direction = 0;

    private enum State {
        IDLE,
        WALK,
        ATTACK,
        DEAD
    }

    private State state = State.IDLE;

    private int frame = 0;
    private int tick = 0;

    private int attackFrame = 0;
    private int attackTick = 0;
    private boolean attackActive = false;
    private int deathFrame = 0;
    private int deathTick = 0;

    private static final int ATTACK_TICKS_PER_FRAME = 4;
    private static final int DEATH_TICKS_PER_FRAME = 5;


    public int x;
    public int y;

    private final int WIDTH = 64;
    private final int HEIGHT = 64;

    private final int speed = 2;
    private int health = 5;

    private final int CHASE_RADIUS = 280;
    private final int ATTACK_RADIUS = 45;

    private boolean dead = false;

    private long lastAttackTime = 0;
    private final long ATTACK_COOLDOWN = 1000;

    private Point pathTarget = null;
    private long lastPathUpdate = 0;

    public PoliceZombie(int x, int y) {

        loadSprite();

        this.x = x;
        this.y = y;
    }

    // =========================================================
    // OLD UPDATE
    // =========================================================

    public void update(Player player) {

        if (dead)
            return;

        if (attackActive) {
            updateAttackAnimation();
            return;
        }

        int oldX = x;
        int oldY = y;

        int dx = player.x - x;
        int dy = player.y - y;

        double distance =
                Math.sqrt(dx * dx + dy * dy);

        if (distance <= CHASE_RADIUS &&
            distance > ATTACK_RADIUS) {

            if (Math.abs(dx) > Math.abs(dy)) {

                if (dx > 0)
                    x += speed;
                else
                    x -= speed;

            } else {

                if (dy > 0)
                    y += speed;
                else
                    y -= speed;
            }
        }

        updateMovementAnimation(x - oldX, y - oldY, dx, dy);
    }

    // =========================================================
    // CHAPTER 2 UPDATE
    // =========================================================

    public void update(
            Player player,
            Map map
    ) {

        if (dead)
            return;

        if (attackActive) {
            updateAttackAnimation();
            return;
        }

        int oldX = x;
        int oldY = y;

        int playerDX = player.x - x;
        int playerDY = player.y - y;

        double playerDistance =
                Math.sqrt(
                        playerDX * playerDX +
                        playerDY * playerDY
                );

        boolean investigatingNoise =
                playerDistance > CHASE_RADIUS
                && NoiseSystem.shouldInvestigate(
                        x + WIDTH / 2,
                        y + HEIGHT / 2
                );

        int targetX = investigatingNoise
                ? NoiseSystem.getNoiseX()
                : player.x;

        int targetY = investigatingNoise
                ? NoiseSystem.getNoiseY()
                : player.y;

        int dx = targetX - x;
        int dy = targetY - y;

        double distance =
                Math.sqrt(
                        dx * dx +
                        dy * dy
                );

        if ((!investigatingNoise
                && playerDistance <= CHASE_RADIUS
                && playerDistance > ATTACK_RADIUS)
                || (investigatingNoise && distance > 16)) {

            boolean moved = false;

            // Direct movement
            if (Math.abs(dx) > Math.abs(dy)) {

                if (dx > 0)
                    moved = moveX(speed, map);
                else
                    moved = moveX(-speed, map);

            } else {

                if (dy > 0)
                    moved = moveY(speed, map);
                else
                    moved = moveY(-speed, map);
            }

            // Other axis
            if (!moved) {

                if (Math.abs(dx) > Math.abs(dy)) {

                    if (dy > 0)
                        moved = moveY(speed, map);
                    else
                        moved = moveY(-speed, map);

                } else {

                    if (dx > 0)
                        moved = moveX(speed, map);
                    else
                        moved = moveX(-speed, map);
                }
            }

            // Door/path finding
            if (!moved) {

                long now =
                        System.currentTimeMillis();

                if (pathTarget == null ||
                    now - lastPathUpdate > 250 ||
                    distanceToPathTarget() < 10) {

                    pathTarget =
                            ZombiePathfinder.findNextStep(
                                    map,
                                    x,
                                    y,
                                    targetX,
                                    targetY,
                                    12,
                                    20,
                                    40,
                                    40
                            );

                    lastPathUpdate = now;
                }

                if (pathTarget != null) {

                    int pathDX =
                            pathTarget.x - x;

                    int pathDY =
                            pathTarget.y - y;

                    if (Math.abs(pathDX) >
                        Math.abs(pathDY)) {

                        if (pathDX > 0)
                            moveX(speed, map);
                        else
                            moveX(-speed, map);

                    } else {

                        if (pathDY > 0)
                            moveY(speed, map);
                        else
                            moveY(-speed, map);
                    }
                }
            }
        }

        updateMovementAnimation(x - oldX, y - oldY, dx, dy);
    }

    private boolean moveX(
            int amount,
            Map map
    ) {

        Rectangle test =
                new Rectangle(
                        x + amount + 12,
                        y + 20,
                        40,
                        40
                );

        if (map.collidesWithBuilding(test))
            return false;

        x += amount;

        return true;
    }

    private boolean moveY(
            int amount,
            Map map
    ) {

        Rectangle test =
                new Rectangle(
                        x + 12,
                        y + amount + 20,
                        40,
                        40
                );

        if (map.collidesWithBuilding(test))
            return false;

        y += amount;

        return true;
    }

    private double distanceToPathTarget() {

        if (pathTarget == null)
            return Double.MAX_VALUE;

        int dx =
                pathTarget.x - x;

        int dy =
                pathTarget.y - y;

        return Math.sqrt(
                dx * dx +
                dy * dy
        );
    }

    // =========================================================
    // ATTACK
    // =========================================================

    public boolean canAttack(Player player) {

        if (dead || state == State.DEAD)
            return false;

        if (attackActive)
            return false;

        if (!getBounds().intersects(
                player.getBounds())) {
            return false;
        }

        long currentTime =
                System.currentTimeMillis();

        if (currentTime - lastAttackTime <
            ATTACK_COOLDOWN) {
            return false;
        }

        lastAttackTime = currentTime;
        attackActive = true;
        attackFrame = 0;
        attackTick = 0;
        state = State.ATTACK;
        frame = 0;
        tick = 0;

        int dx = player.x - x;
        int dy = player.y - y;

        if (Math.abs(dx) > Math.abs(dy))
            direction = dx > 0 ? 2 : 1;
        else if (dy != 0)
            direction = dy > 0 ? 0 : 3;

        SoundManager.play("zombie_bite.wav");
        return true;
    }

    // =========================================================
    // DAMAGE
    // =========================================================

    public void takeDamage() {

        if (dead)
            return;

        health--;

        if (health <= 0) {

            health = 0;
            state = State.DEAD;
            deathFrame = 0;
            deathTick = 0;
        }
    }

    // =========================================================
    // SPRITE LOADING
    // =========================================================

    private void loadSprite() {

        try {

            spriteSheet = ImageIO.read(
                    getClass().getResource(
                            "/assets/police_zombie_sheet.png"
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // =========================================================
    // ANIMATION
    // Based on Enemy.java:
    // animation advances from update(), not draw().
    // =========================================================

    private void updateMovementAnimation(
            int moveDX,
            int moveDY,
            int targetDX,
            int targetDY
    ) {

        boolean moving = moveDX != 0 || moveDY != 0;

        if (!moving) {
            state = State.IDLE;
            frame = 0;
            tick = 0;
            return;
        }

        final int DIRECTION_THRESHOLD = 8;
        int absDX = Math.abs(targetDX);
        int absDY = Math.abs(targetDY);

        if (absDX > absDY + DIRECTION_THRESHOLD) {
            if (moveDX != 0) direction = moveDX > 0 ? 2 : 1;
        } else if (absDY > absDX + DIRECTION_THRESHOLD) {
            if (moveDY != 0) direction = moveDY > 0 ? 0 : 3;
        }

        state = State.WALK;
        tick++;
        if (tick >= 8) {
            frame = (frame + 1) % 6;
            tick = 0;
        }
    }

    private void updateAttackAnimation() {
        state = State.ATTACK;
        attackTick++;
        if (attackTick < ATTACK_TICKS_PER_FRAME) return;
        attackTick = 0;
        attackFrame++;
        if (attackFrame >= 6) {
            attackFrame = 0;
            attackActive = false;
            state = State.IDLE;
            frame = 0;
            tick = 0;
        }
    }

    private void updateDeathAnimation() {
        state = State.DEAD;
        deathTick++;
        if (deathTick >= DEATH_TICKS_PER_FRAME) {
            deathTick = 0;
            deathFrame++;
            if (deathFrame >= 6) {
                deathFrame = 5;
                dead = true;
            }
        }
    }

    // =========================================================
    // DRAW
    // =========================================================

    public void draw(Graphics2D g2) {

        if (dead || spriteSheet == null)
            return;

        int row;
        int col;

        switch (state) {
            case ATTACK:
                row = 4;
                col = attackFrame;
                break;
            case DEAD:
                row = 5;
                col = deathFrame;
                break;
            case IDLE:
            case WALK:
            default:
                row = 1;
                col = frame;
                break;
        }

        g2.drawImage(
                spriteSheet,
                x, y, x + WIDTH, y + HEIGHT,
                col * 64, row * 64,
                (col + 1) * 64, (row + 1) * 64,
                null
        );
    }

    // =========================================================
    // COLLISION
    // =========================================================

    public Rectangle getBounds() {

        return new Rectangle(
                x + 12,
                y + 20,
                40,
                40
        );
    }

    // =========================================================
    // GETTERS
    // =========================================================

    public boolean isDead() {
        return dead;
    }

    public int getHealth() {
        return health;
    }
}