package Main;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Enemy {

    public int x, y;

    // Draw size
    public final int width = 64;
    public final int height = 64;

    // Collision hitbox
    private final Rectangle hitbox = new Rectangle();

    private BufferedImage sheet;

    // Directions
    // 0 = Down, 1 = Left, 2 = Right, 3 = Up
    private int direction = 0;

    // Animation states
    private enum State {
        IDLE,
        WALK,
        ATTACK,
        DEAD
    }

    private State state = State.IDLE;

    private int frame = 0;
    private int tick = 0;

    private boolean dying = false;
    private boolean dead = false;

    // Stats
    private int speed = 2;
    private int health = 3;
    private static final int MAX_HEALTH = 3;

    // Health bar is shown briefly after taking damage.
    private long healthBarUntil = 0;
    private static final long HEALTH_BAR_DURATION = 1500;

    // AI
    private long lastAttack = 0;
    private final int CHASE_RADIUS = 250;
    private final int ATTACK_RADIUS = 42;
    private int attackTimer = 0;

    // Chapter 2 pathfinding
    private Point pathTarget = null;
    private long lastPathUpdate = 0;

    public Enemy(int x) {

        this.x = x;
        this.y = 280;

        try {

            sheet = ImageIO.read(
                    getClass().getResource(
                            "/assets/zombie_sheet.png"
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // =========================================================
    // CHAPTER 1 UPDATE
    // =========================================================
    //
    // DO NOT CHANGE THIS BEHAVIOR.
    // Chapter 1 still uses this method.
    //
    // =========================================================

    public void update(Player player) {

        if (dead)
            return;

        // ================= DEATH =================

        if (dying) {

            state = State.DEAD;

            animate();

            return;
        }

        // ================= ATTACK LOCK =================

        if (attackTimer > 0) {

            attackTimer--;

            state = State.ATTACK;

            animate();

            return;
        }

        int dx = player.x - x;
        int dy = player.y - y;

        double dist =
                Math.sqrt(
                        dx * dx +
                        dy * dy
                );

        boolean moving = false;

        // ================= CHASE =================

        if (dist < CHASE_RADIUS &&
            dist > ATTACK_RADIUS) {

            moving = true;

            int absDx = Math.abs(dx);
            int absDy = Math.abs(dy);

            final int DIRECTION_THRESHOLD = 8;

            if (absDx >
                absDy + DIRECTION_THRESHOLD) {

                if (dx > 0) {

                    x += speed;
                    direction = 2;

                } else {

                    x -= speed;
                    direction = 1;
                }

            } else if (
                    absDy >
                    absDx + DIRECTION_THRESHOLD) {

                if (dy > 0) {

                    y += speed;
                    direction = 0;

                } else {

                    y -= speed;
                    direction = 3;
                }

            } else {

                switch (direction) {

                    case 0:
                        y += speed;
                        break;

                    case 1:
                        x -= speed;
                        break;

                    case 2:
                        x += speed;
                        break;

                    case 3:
                        y -= speed;
                        break;
                }
            }
        }

        // ================= NORMAL STATE =================
        //
        // ATTACK is triggered ONLY by canAttack().
        // Do not select ATTACK just because Ethan is inside the
        // attack radius; doing that makes the bite animation loop
        // and look like a twitch.

        if (moving) {

            state = State.WALK;

        } else {

            state = State.IDLE;
        }

        animate();
    }

    // =========================================================
    // CHAPTER 2 UPDATE WITH BUILDING NAVIGATION
    // =========================================================

    public void update(Player player, Map map) {

        if (dead)
            return;

        // ================= DEATH =================

        if (dying) {

            state = State.DEAD;

            animate();

            return;
        }

        // ================= ATTACK LOCK =================

        if (attackTimer > 0) {

            attackTimer--;

            state = State.ATTACK;

            animate();

            return;
        }

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
                        x + width / 2,
                        y + height / 2
                );

        int targetX = investigatingNoise
                ? NoiseSystem.getNoiseX()
                : player.x;

        int targetY = investigatingNoise
                ? NoiseSystem.getNoiseY()
                : player.y;

        int dx = targetX - x;
        int dy = targetY - y;

        double dist =
                Math.sqrt(
                        dx * dx +
                        dy * dy
                );

        boolean moving = false;

        long now = System.currentTimeMillis();

        // =====================================================
        // CHASE
        // =====================================================

        if ((investigatingNoise && dist > 16)
                || (!investigatingNoise
                    && playerDistance < CHASE_RADIUS
                    && playerDistance > ATTACK_RADIUS)) {

            int absDx = Math.abs(dx);
            int absDy = Math.abs(dy);

            boolean moved = false;

            // -------------------------------------------------
            // Try dominant direction first
            // -------------------------------------------------

            if (absDx > absDy) {

                if (dx > 0) {

                    moved = moveX(
                            speed,
                            map
                    );

                    if (moved)
                        direction = 2;

                } else {

                    moved = moveX(
                            -speed,
                            map
                    );

                    if (moved)
                        direction = 1;
                }

            } else {

                if (dy > 0) {

                    moved = moveY(
                            speed,
                            map
                    );

                    if (moved)
                        direction = 0;

                } else {

                    moved = moveY(
                            -speed,
                            map
                    );

                    if (moved)
                        direction = 3;
                }
            }

            // -------------------------------------------------
            // Try other axis
            // -------------------------------------------------

            if (!moved) {

                if (absDx > absDy) {

                    if (dy > 0) {

                        moved =
                                moveY(
                                        speed,
                                        map
                                );

                        if (moved)
                            direction = 0;

                    } else {

                        moved =
                                moveY(
                                        -speed,
                                        map
                                );

                        if (moved)
                            direction = 3;
                    }

                } else {

                    if (dx > 0) {

                        moved =
                                moveX(
                                        speed,
                                        map
                                );

                        if (moved)
                            direction = 2;

                    } else {

                        moved =
                                moveX(
                                        -speed,
                                        map
                                );

                        if (moved)
                            direction = 1;
                    }
                }
            }

            // -------------------------------------------------
            // WALL BLOCKED
            // FIND DOORWAY
            // -------------------------------------------------

            if (!moved) {

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

                        if (pathDX > 0) {

                            if (moveX(speed, map))
                                direction = 2;

                        } else {

                            if (moveX(-speed, map))
                                direction = 1;
                        }

                    } else {

                        if (pathDY > 0) {

                            if (moveY(speed, map))
                                direction = 0;

                        } else {

                            if (moveY(-speed, map))
                                direction = 3;
                        }
                    }
                }

                moving = true;

            } else {

                moving = true;
            }
        }

        // ================= STATE =================

        if (dist <= ATTACK_RADIUS) {

            // Attack animation is started by canAttack(), not by distance alone.
            state = State.IDLE;
            frame = 0;
            tick = 0;

        } else if (moving) {

            state = State.WALK;

        } else {

            state = State.IDLE;
        }

        animate();
    }

    // =========================================================
    // MOVE X
    // =========================================================

    private boolean moveX(
            int amount,
            Map map
    ) {

        Rectangle testBounds =
                new Rectangle(
                        x + amount + 12,
                        y + 20,
                        40,
                        40
                );

        if (map.collidesWithBuilding(
                testBounds)) {

            return false;
        }

        x += amount;

        return true;
    }

    // =========================================================
    // MOVE Y
    // =========================================================

    private boolean moveY(
            int amount,
            Map map
    ) {

        Rectangle testBounds =
                new Rectangle(
                        x + 12,
                        y + amount + 20,
                        40,
                        40
                );

        if (map.collidesWithBuilding(
                testBounds)) {

            return false;
        }

        y += amount;

        return true;
    }

    // =========================================================
    // PATH DISTANCE
    // =========================================================

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
    // ANIMATION
    // =========================================================

    private void animate() {

        tick++;

        switch (state) {

            case IDLE -> {

                if (tick >= 18) {

                    frame =
                            (frame + 1) % 2;

                    tick = 0;
                }
            }

            case WALK -> {

                if (tick >= 8) {

                    frame =
                            (frame + 1) % 4;

                    tick = 0;
                }
            }

            case ATTACK -> {

                if (tick >= 10) {

                    frame++;

                    if (frame >= 2)
                        frame = 0;

                    tick = 0;
                }
            }

            case DEAD -> {

                if (tick >= 8) {

                    if (frame < 5) {

                        frame++;

                    } else {

                        dead = true;
                    }

                    tick = 0;
                }
            }
        }
    }

    // =========================================================
    // ATTACK
    // =========================================================

    public boolean canAttack(
            Player player
    ) {

        if (dead || dying)
            return false;

        Rectangle attackBox;

        switch (direction) {

            case 0:

                attackBox =
                        new Rectangle(
                                x + 16,
                                y + 28,
                                32,
                                28
                        );

                break;

            case 1:

                attackBox =
                        new Rectangle(
                                x - 4,
                                y + 18,
                                28,
                                30
                        );

                break;

            case 2:

                attackBox =
                        new Rectangle(
                                x + 40,
                                y + 18,
                                28,
                                30
                        );

                break;

            case 3:

                attackBox =
                        new Rectangle(
                                x + 16,
                                y - 4,
                                32,
                                28
                        );

                break;

            default:
                return false;
        }

        if (!attackBox.intersects(
                player.getBounds())) {

            return false;
        }

        long now =
                System.currentTimeMillis();

        if (now - lastAttack >= 1000) {

            lastAttack = now;

            SoundManager.play(
                    "zombie_bite.wav"
            );

            state = State.ATTACK;

            frame = 0;
            tick = 0;

            attackTimer = 20;

            return true;
        }

        return false;
    }

    // =========================================================
    // DAMAGE
    // =========================================================

    public void takeDamage() {

        if (dead || dying)
            return;

        health--;

        healthBarUntil =
                System.currentTimeMillis()
                + HEALTH_BAR_DURATION;

        if (health <= 0) {

            dying = true;

            state = State.DEAD;

            frame = 0;
            tick = 0;
        }
    }

    // =========================================================
    // DEAD
    // =========================================================

    public boolean isDead() {

        return dead;
    }

    // =========================================================
    // BOUNDS
    // =========================================================

    public Rectangle getBounds() {

        hitbox.setBounds(
                x + 12,
                y + 20,
                40,
                40
        );

        return hitbox;
    }

    // =========================================================
    // DRAW
    // =========================================================

    private void drawHealthBar(Graphics2D g2) {

        if (dead || dying)
            return;

        if (System.currentTimeMillis() > healthBarUntil)
            return;

        final int barWidth = 42;
        final int barHeight = 5;

        int barX = x + (width - barWidth) / 2;
        int barY = y - 8;

        // Background
        g2.setColor(Color.BLACK);
        g2.fillRect(
                barX,
                barY,
                barWidth,
                barHeight
        );

        // Current health
        int currentWidth =
                (int) (
                        barWidth
                        * (health / (double) MAX_HEALTH)
                );

        if (currentWidth > 0) {

            g2.setColor(Color.GREEN);
            g2.fillRect(
                    barX,
                    barY,
                    currentWidth,
                    barHeight
            );
        }

        // Border
        g2.setColor(Color.WHITE);
        g2.drawRect(
                barX,
                barY,
                barWidth - 1,
                barHeight - 1
        );
    }

    public void draw(Graphics2D g2) {

        if (dead ||
            sheet == null) {

            return;
        }

        int row = 0;
        int col = 0;

        switch (state) {

            case IDLE -> {

                if (direction == 0) {
                    row = 0;
                    col = frame;
                }

                if (direction == 1) {
                    row = 0;
                    col = 2 + frame;
                }

                if (direction == 2) {
                    row = 0;
                    col = 4 + frame;
                }

                if (direction == 3) {
                    row = 4;
                    col = frame;
                }
            }

            case WALK -> {

                if (direction == 0) {
                    row = 1;
                    col = frame;
                }

                if (direction == 1) {
                    row = 2;
                    col = frame;
                }

                if (direction == 2) {
                    row = 3;
                    col = frame;
                }

                if (direction == 3) {
                    row = 4;
                    col = frame;
                }
            }

            case ATTACK -> {

                if (direction == 0) {
                    row = 1;
                    col = 4 + frame;
                }

                if (direction == 1) {
                    row = 2;
                    col = 4 + frame;
                }

                if (direction == 2) {
                    row = 3;
                    col = 4 + frame;
                }

                if (direction == 3) {
                    row = 4;
                    col = 4 + frame;
                }
            }

            case DEAD -> {

                row = 5;
                col = frame;
            }
        }

        g2.drawImage(
                sheet,

                x,
                y,
                x + width,
                y + height,

                col * 64,
                row * 64,

                (col + 1) * 64,
                (row + 1) * 64,

                null
        );
        drawHealthBar(g2);
    }
}
