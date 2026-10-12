package Main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

public class ExperimentalZombie {

    public int x, y;

    // =========================================================
    // DRAW SIZE
    // =========================================================

    public final int width = 64;
    public final int height = 64;

    // =========================================================
    // COLLISION HITBOX
    // =========================================================

    private final Rectangle hitbox = new Rectangle();

    // =========================================================
    // SPRITE
    // =========================================================

    private BufferedImage sheet;

    // 0 = Down
    // 1 = Left
    // 2 = Right
    // 3 = Up
    private int direction = 0;

    // =========================================================
    // ANIMATION
    // =========================================================

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

    // =========================================================
    // STATS
    // =========================================================

    private int speed = 2;

    private int health = 10;

    private static final int MAX_HEALTH = 10;

    private static final int CHASE_RADIUS = 350;

    private static final int ATTACK_RADIUS = 48;

    // =========================================================
    // ATTACK
    // =========================================================

    private long lastAttack = 0;

    private int attackTimer = 0;

    // =========================================================
    // HEALTH BAR
    // =========================================================

    private long healthBarUntil = 0;

    private static final long HEALTH_BAR_DURATION = 1500;

    // =========================================================
    // PATHFINDING
    // =========================================================

    private Point pathTarget = null;

    private long lastPathUpdate = 0;

    // =========================================================
    // REGENERATION
    // =========================================================

    private long lastRegeneration = 0;

    private static final long REGEN_DELAY = 2000;

    private static final long REGEN_COOLDOWN = 2500;

    private boolean regenerationPending = false;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public ExperimentalZombie(int x, int y) {

        this.x = x;
        this.y = y;

        try {

            sheet = ImageIO.read(
                    getClass().getResource(
                            "/assets/experimental_zombie_sheet.png"
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // =========================================================
    // CHAPTER 2 UPDATE
    // =========================================================

    public void update(Player player, Map map) {

        if (dead)
            return;

        // =====================================================
        // DEATH
        // =====================================================

        if (dying) {

            state = State.DEAD;

            animate();

            return;
        }

        // =====================================================
        // ATTACK LOCK
        // =====================================================

        if (attackTimer > 0) {

            attackTimer--;

            state = State.ATTACK;

            animate();

            updateRegeneration();

            return;
        }

        // =====================================================
        // REGENERATION
        // =====================================================

        updateRegeneration();

        // =====================================================
        // PLAYER DISTANCE
        // =====================================================

        int playerDX = player.x - x;
        int playerDY = player.y - y;

        double playerDistance =
                Math.sqrt(
                        playerDX * playerDX
                        + playerDY * playerDY
                );

        // =====================================================
        // NOISE INVESTIGATION
        // =====================================================

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
                        dx * dx
                        + dy * dy
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
            // DOMINANT DIRECTION
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
            // SECOND AXIS
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
            // PATHFINDING
            // -------------------------------------------------

            if (!moved) {

                if (pathTarget == null
                        || now - lastPathUpdate > 250
                        || distanceToPathTarget() < 10) {

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

                    if (Math.abs(pathDX)
                            > Math.abs(pathDY)) {

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

        // =====================================================
        // STATE
        // =====================================================

        if (dist <= ATTACK_RADIUS) {

            // Attack is triggered ONLY by canAttack().
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
                dx * dx
                + dy * dy
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

    public boolean canAttack(Player player) {

        if (dead || dying)
            return false;

        Rectangle attackBox;

        switch (direction) {

            case 0:

                attackBox =
                        new Rectangle(
                                x + 16,
                                y + 30,
                                32,
                                28
                        );

                break;

            case 1:

                attackBox =
                        new Rectangle(
                                x - 6,
                                y + 18,
                                30,
                                30
                        );

                break;

            case 2:

                attackBox =
                        new Rectangle(
                                x + 40,
                                y + 18,
                                30,
                                30
                        );

                break;

            case 3:

                attackBox =
                        new Rectangle(
                                x + 16,
                                y - 6,
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

            // Actual attack sound only.
            // No zombie growl.

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

        // Start regeneration cycle.
        regenerationPending = true;

        if (health <= 0) {

            dying = true;

            state = State.DEAD;

            frame = 0;
            tick = 0;

            regenerationPending = false;
        }
    }

    // =========================================================
    // REGENERATION
    // =========================================================

    private void updateRegeneration() {

        if (!regenerationPending)
            return;

        if (health <= 0 || health >= MAX_HEALTH) {

            regenerationPending = false;

            return;
        }

        long now =
                System.currentTimeMillis();

        if (lastRegeneration == 0) {

            lastRegeneration = now;
        }

        // First regeneration after being damaged.
        if (now - lastRegeneration >= REGEN_DELAY) {

            health++;

            lastRegeneration = now;

            regenerationPending = false;
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
    // HEALTH
    // =========================================================

    public int getHealth() {

        return health;
    }

    public int getMaxHealth() {

        return MAX_HEALTH;
    }

    // =========================================================
    // DRAW HEALTH BAR
    // =========================================================

    private void drawHealthBar(
            Graphics2D g2
    ) {

        if (dead || dying)
            return;

        if (System.currentTimeMillis()
                > healthBarUntil)
            return;

        final int barWidth = 46;
        final int barHeight = 5;

        int barX =
                x + (width - barWidth) / 2;

        int barY =
                y - 8;

        // Background

        g2.setColor(Color.BLACK);

        g2.fillRect(
                barX,
                barY,
                barWidth,
                barHeight
        );

        // Health

        int currentWidth =
                (int) (
                        barWidth
                        * (health
                        / (double) MAX_HEALTH)
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

    // =========================================================
    // DRAW
    // =========================================================

    public void draw(Graphics2D g2) {

        if (dead || sheet == null)
            return;

        int row = 0;
        int col = 0;

        switch (state) {

            // -------------------------------------------------
            // IDLE
            // -------------------------------------------------

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

            // -------------------------------------------------
            // WALK
            // -------------------------------------------------

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

            // -------------------------------------------------
            // ATTACK
            // -------------------------------------------------

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

            // -------------------------------------------------
            // DEAD
            // -------------------------------------------------

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