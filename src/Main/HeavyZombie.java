package Main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class HeavyZombie {

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

    // Attack animation uses sprite-sheet row 5 (index 4).
    // Death animation uses sprite-sheet row 6 (index 5).
    private static final int ATTACK_ROW = 4;
    private static final int DEATH_ROW = 5;

    private static final int ATTACK_FRAMES = 6;
    private static final int DEATH_FRAMES = 6;

    private static final int ATTACK_TICKS_PER_FRAME = 4;
    private static final int DEATH_TICKS_PER_FRAME = 5;

    private boolean attackHitGiven = false;
    private boolean attackActive = false;
    private int attackFrame = 0;
    private int attackTick = 0;

    public int x;
    public int y;

    private static final int WIDTH = 64;
    private static final int HEIGHT = 64;

    private final int speed = 1;
    private int health = 8;
    private static final int MAX_HEALTH = 8;

    private final int CHASE_RADIUS = 220;
    private final int ATTACK_RADIUS = 50;

    private boolean dead = false;
    private boolean dying = false;

    private long lastAttackTime = 0;
    private final long ATTACK_COOLDOWN = 1200;

    private Point pathTarget = null;
    private long lastPathUpdate = 0;

    private long lastDamageTime = 0;

    public HeavyZombie(int x, int y) {
        loadSprite();
        this.x = x;
        this.y = y;
    }

    // =========================================================
    // ORIGINAL UPDATE
    // =========================================================
    public void update(Player player) {

        if (dead)
            return;

        if (dying) {
            updateDeathAnimation();
            return;
        }

        if (attackActive) {
            updateAttackAnimation();
            return;
        }

        int oldX = x;
        int oldY = y;

        int dx = player.x - x;
        int dy = player.y - y;

        double distance = Math.sqrt(dx * dx + dy * dy);

        boolean moving = false;

        if (distance <= CHASE_RADIUS && distance > ATTACK_RADIUS) {

            moving = true;

            if (Math.abs(dx) > Math.abs(dy)) {
                x += Integer.signum(dx) * speed;
                direction = dx > 0 ? 2 : 1;
            } else {
                y += Integer.signum(dy) * speed;
                direction = dy > 0 ? 0 : 3;
            }
        }

        updateMovementAnimation(
                x - oldX,
                y - oldY,
                dx,
                dy,
                moving
        );
    }

    // =========================================================
    // CHAPTER 2 UPDATE
    // =========================================================
    public void update(Player player, Map map) {

        if (dead)
            return;

        if (dying) {
            updateDeathAnimation();
            return;
        }

        // Freeze movement during the actual bite animation.
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
                        playerDX * playerDX
                        + playerDY * playerDY
                );

        boolean investigatingNoise =
                playerDistance > CHASE_RADIUS
                && NoiseSystem.shouldInvestigate(
                        x + 32,
                        y + 32
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
                        dx * dx
                        + dy * dy
                );

        if (!investigatingNoise
                && playerDistance > CHASE_RADIUS) {
            state = State.IDLE;
            frame = 0;
            tick = 0;
            return;
        }

        if ((!investigatingNoise && playerDistance <= ATTACK_RADIUS)
                || (investigatingNoise && distance <= 16)) {
            state = State.IDLE;
            frame = 0;
            tick = 0;
            return;
        }

        boolean moved = false;

        // Direct movement first.
        if (Math.abs(dx) > Math.abs(dy)) {

            moved = dx > 0
                    ? moveX(speed, map)
                    : moveX(-speed, map);

        } else {

            moved = dy > 0
                    ? moveY(speed, map)
                    : moveY(-speed, map);
        }

        // Try the other axis if blocked.
        if (!moved) {

            if (Math.abs(dx) > Math.abs(dy)) {

                moved = dy > 0
                        ? moveY(speed, map)
                        : moveY(-speed, map);

            } else {

                moved = dx > 0
                        ? moveX(speed, map)
                        : moveX(-speed, map);
            }
        }

        // Existing Chapter 2 doorway/pathfinding behavior.
        if (!moved) {

            long now = System.currentTimeMillis();

            if (pathTarget == null ||
                now - lastPathUpdate > 250 ||
                distanceToPathTarget() < 10) {

                pathTarget = ZombiePathfinder.findNextStep(
                        map,
                        x,
                        y,
                        targetX,
                        targetY,
                        12,
                        20,
                        48,
                        44
                );

                lastPathUpdate = now;
            }

            if (pathTarget != null) {

                int pathDX = pathTarget.x - x;
                int pathDY = pathTarget.y - y;

                if (Math.abs(pathDX) > Math.abs(pathDY)) {

                    moved = pathDX > 0
                            ? moveX(speed, map)
                            : moveX(-speed, map);

                } else {

                    moved = pathDY > 0
                            ? moveY(speed, map)
                            : moveY(-speed, map);
                }
            }
        }

        updateMovementAnimation(
                x - oldX,
                y - oldY,
                dx,
                dy,
                moved
        );
    }

    // =========================================================
    // MOVEMENT
    // =========================================================
    private boolean moveX(int amount, Map map) {

        Rectangle test = new Rectangle(
                x + amount + 12,
                y + 20,
                48,
                44
        );

        if (map.collidesWithBuilding(test))
            return false;

        x += amount;
        return true;
    }

    private boolean moveY(int amount, Map map) {

        Rectangle test = new Rectangle(
                x + 12,
                y + amount + 20,
                48,
                44
        );

        if (map.collidesWithBuilding(test))
            return false;

        y += amount;
        return true;
    }

    private double distanceToPathTarget() {

        if (pathTarget == null)
            return Double.MAX_VALUE;

        int dx = pathTarget.x - x;
        int dy = pathTarget.y - y;

        return Math.sqrt(dx * dx + dy * dy);
    }

    // =========================================================
    // ATTACK
    // =========================================================
    public boolean canAttack(Player player) {

        if (dead || dying)
            return false;

        // An active attack owns the zombie until the bite animation ends.
        // Distance is NOT checked here, so running away cannot freeze it.
        if (attackActive) {

            if (attackFrame == 2 && !attackHitGiven) {
                attackHitGiven = true;
                return true;
            }

            return false;
        }

        if (!getBounds().intersects(player.getBounds()))
            return false;

        long now = System.currentTimeMillis();

        if (now - lastAttackTime < ATTACK_COOLDOWN)
            return false;

        lastAttackTime = now;

        attackActive = true;
        attackFrame = 0;
        attackTick = 0;
        attackHitGiven = false;

        state = State.ATTACK;
        frame = 0;
        tick = 0;

        // Lock the facing direction for the whole bite.
        int dx = player.x - x;
        int dy = player.y - y;

        if (Math.abs(dx) > Math.abs(dy)) {
            direction = dx > 0 ? 2 : 1;
        } else if (dy != 0) {
            direction = dy > 0 ? 0 : 3;
        }

        SoundManager.play("zombie_bite.wav");

        return false;
    }

    private void updateAttackAnimation() {

        if (dead)
            return;

        // Attack animation is completely independent of Ethan's
        // current position. This prevents the frozen-last-frame bug.
        state = State.ATTACK;

        attackTick++;

        if (attackTick < ATTACK_TICKS_PER_FRAME)
            return;

        attackTick = 0;
        attackFrame++;

        // Draw frame from the dedicated attack row.
        frame = attackFrame;

        if (attackFrame >= ATTACK_FRAMES) {

            attackFrame = 0;
            attackTick = 0;
            attackHitGiven = false;
            attackActive = false;

            // Return to normal AI immediately.
            state = State.IDLE;
            frame = 0;
            tick = 0;
        }
    }

    // =========================================================
    // DAMAGE
    // =========================================================
    public void takeDamage() {

        if (dead || dying)
            return;

        health--;
        lastDamageTime = System.currentTimeMillis();

        if (health <= 0) {

            health = 0;
            dying = true;
            state = State.DEAD;
            frame = 0;
            tick = 0;
        }
    }

    // =========================================================
    // ANIMATION
    // =========================================================
    private void updateMovementAnimation(
            int moveDX,
            int moveDY,
            int targetDX,
            int targetDY,
            boolean moving
    ) {

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

            if (moveDX != 0)
                direction = moveDX > 0 ? 2 : 1;

        } else if (absDY > absDX + DIRECTION_THRESHOLD) {

            if (moveDY != 0)
                direction = moveDY > 0 ? 0 : 3;
        }

        state = State.WALK;

        tick++;

        if (tick >= 8) {
            frame = (frame + 1) % 6;
            tick = 0;
        }
    }

    private void updateDeathAnimation() {

        state = State.DEAD;

        tick++;

        if (tick >= DEATH_TICKS_PER_FRAME) {
            tick = 0;
            frame++;

            if (frame >= DEATH_FRAMES) {
                frame = DEATH_FRAMES - 1;
                dead = true;
            }
        }
    }

    // =========================================================
    // SPRITE LOADING
    // =========================================================
    private void loadSprite() {

        try {
            spriteSheet = ImageIO.read(
                    getClass().getResource(
                            "/assets/heavy_zombie_sheet.png"
                    )
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================================================
    // DRAW
    // =========================================================
    public void draw(Graphics2D g2) {

        if (dead || spriteSheet == null)
            return;

        int row;
        int col = frame;

        switch (state) {

            case IDLE:
            case WALK:
                // ROW 2 = idle / movement animation
                row = 1;
                break;

            case ATTACK:
                // ROW 5 = attack animation
                row = 4;
                break;

            case DEAD:
                row = DEATH_ROW;
                break;

            default:
                row = direction;
                break;
        }

        g2.drawImage(
                spriteSheet,
                x,
                y,
                x + WIDTH,
                y + HEIGHT,
                col * 64,
                row * 64,
                (col + 1) * 64,
                (row + 1) * 64,
                null
        );

        drawHealthBar(g2);
    }

    private void drawHealthBar(Graphics2D g2) {

        if (dead || dying)
            return;

        if (lastDamageTime == 0)
            return;

        if (System.currentTimeMillis() - lastDamageTime > 1500)
            return;

        final int BAR_WIDTH = 48;
        final int BAR_HEIGHT = 5;
        final int BAR_X = x + 8;
        final int BAR_Y = y - 8;

        int currentWidth =
                (int) ((health / (double) MAX_HEALTH) * BAR_WIDTH);

        g2.setColor(Color.BLACK);
        g2.fillRect(
                BAR_X - 1,
                BAR_Y - 1,
                BAR_WIDTH + 2,
                BAR_HEIGHT + 2
        );

        g2.setColor(new Color(120, 20, 20));
        g2.fillRect(
                BAR_X,
                BAR_Y,
                BAR_WIDTH,
                BAR_HEIGHT
        );

        if (currentWidth > 0) {
            g2.setColor(new Color(200, 45, 45));
            g2.fillRect(
                    BAR_X,
                    BAR_Y,
                    currentWidth,
                    BAR_HEIGHT
            );
        }
    }

    // =========================================================
    // COLLISION / GETTERS
    // =========================================================
    public Rectangle getBounds() {
        return new Rectangle(
                x + 12,
                y + 20,
                48,
                44
        );
    }

    public boolean isDead() {
        return dead;
    }

    public int getHealth() {
        return health;
    }
}
