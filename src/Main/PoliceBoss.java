package Main;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class PoliceBoss {

    public int x, y;
    public int width = 40;
    public int height = 40;

    private BufferedImage sheet;

    // Direction
    // 0 = Down
    // 1 = Left
    // 2 = Right
    // 3 = Up
    private int direction = 0;

    private int frame = 0;
    private int animCounter = 0;

    // ================= COMBAT =================
    private boolean attacking = false;
    private int attackTimer = 0;
    private long lastAttack = 0;

    private final int ATTACK_COOLDOWN = 1000;
    private final int ATTACK_DURATION = 20;

    // ================= DEATH =================
    private boolean dying = false;
    private boolean dead = false;
    private int deathTick = 0;
    private int deathFrame = 0;

    // ================= STATS =================
    private int speed = 2;
    private int health = 8;

    private final int CHASE_RADIUS = 320;
    private final int ATTACK_RADIUS = 45;

    // ================= CONSTRUCTOR =================
    public PoliceBoss(int x, int y) {

        this.x = x;
        this.y = y;

        try {
            sheet = ImageIO.read(
                getClass().getResourceAsStream(
                    "/assets/police_boss_sheet.png"
                )
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= UPDATE =================
    public void update(Player player) {

        if (dead) {
            return;
        }

        // ================= DEATH =================
        if (dying) {

            deathTick++;

            if (deathTick % 5 == 0) {
                if (deathFrame < 5) {
                    deathFrame++;
                }
            }

            if (deathTick >= 30) {
                dead = true;
            }

            return;
        }

        // ================= ATTACK ANIMATION =================
        if (attacking) {

            attackTimer--;

            // Two-frame heavy attack
            if (attackTimer > ATTACK_DURATION / 2) {
                frame = 0;
            } else {
                frame = 1;
            }

            if (attackTimer <= 0) {
                attacking = false;
                frame = 0;
            }

            return;
        }

        int dx = player.x - x;
        int dy = player.y - y;

        double dist = Math.sqrt(dx * dx + dy * dy);

        boolean moving = false;

        // ================= CHASE =================
        if (dist < CHASE_RADIUS && dist > ATTACK_RADIUS) {

            moving = true;

            int absDx = Math.abs(dx);
            int absDy = Math.abs(dy);

            // Prevent direction flickering when almost diagonal
            final int DIRECTION_THRESHOLD = 8;

            if (absDx > absDy + DIRECTION_THRESHOLD) {

                if (dx > 0) {
                    x += speed;
                    direction = 2; // RIGHT
                } else {
                    x -= speed;
                    direction = 1; // LEFT
                }

            } else if (absDy > absDx + DIRECTION_THRESHOLD) {

                if (dy > 0) {
                    y += speed;
                    direction = 0; // DOWN
                } else {
                    y -= speed;
                    direction = 3; // UP
                }

            } else {

                // Keep previous direction when nearly diagonal
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

        // ================= WALK ANIMATION =================
        if (moving) {

            animCounter++;

            if (animCounter >= 8) {

                frame = (frame + 1) % 4;
                animCounter = 0;
            }

        } else {

            frame = 0;
            animCounter = 0;
        }
    }

    // ================= ATTACK =================
    public boolean canAttack(Player player) {

        if (dead || dying || attacking) {
            return false;
        }

        Rectangle attackBox;

        switch (direction) {

            case 0: // DOWN
                attackBox = new Rectangle(
                    x + 10,
                    y + 38,
                    44,
                    28
                );
                break;

            case 1: // LEFT
                attackBox = new Rectangle(
                    x - 28,
                    y + 10,
                    28,
                    44
                );
                break;

            case 2: // RIGHT
                attackBox = new Rectangle(
                    x + width,
                    y + 10,
                    28,
                    44
                );
                break;

            case 3: // UP
                attackBox = new Rectangle(
                    x + 10,
                    y - 28,
                    44,
                    28
                );
                break;

            default:
                return false;
        }

        if (!attackBox.intersects(player.getBounds())) {
            return false;
        }

        long now = System.currentTimeMillis();

        if (now - lastAttack >= ATTACK_COOLDOWN) {

            lastAttack = now;

            attacking = true;
            attackTimer = ATTACK_DURATION;

            frame = 0;
            animCounter = 0;

            return true;
        }

        return false;
    }

    // ================= DAMAGE =================
    public void takeDamage() {

        if (dead || dying) {
            return;
        }

        health--;

        if (health <= 0) {

            health = 0;

            dying = true;

            deathTick = 0;
            deathFrame = 0;

            frame = 0;
        }
    }

    // ================= STATUS =================
    public boolean isDead() {
        return dead;
    }

    public boolean isDying() {
        return dying;
    }

    public boolean isAttacking() {
        return attacking;
    }

    public int getHealth() {
        return health;
    }

    // ================= COLLISION =================
    public Rectangle getBounds() {

        return new Rectangle(
            x,
            y,
            width,
            height
        );
    }

    // ================= DRAW =================
    public void draw(Graphics2D g2) {

        if (dead || sheet == null) {
            return;
        }

        int row;
        int col;

        // ================= DEATH =================
        if (dying) {

            row = 5;
            col = deathFrame;

        }

        // ================= ATTACK =================
        else if (attacking) {

            switch (direction) {

                case 0: // DOWN
                    row = 1;
                    break;

                case 1: // LEFT
                    row = 2;
                    break;

                case 2: // RIGHT
                    row = 3;
                    break;

                case 3: // UP
                    row = 4;
                    break;

                default:
                    row = 1;
                    break;
            }

            // Heavy attack = columns 4 and 5
            col = 4 + frame;

        }

        // ================= NORMAL =================
        else {

            switch (direction) {

                case 0: // DOWN
                    row = 1;
                    break;

                case 1: // LEFT
                    row = 2;
                    break;

                case 2: // RIGHT
                    row = 3;
                    break;

                case 3: // UP
                    row = 4;
                    break;

                default:
                    row = 1;
                    break;
            }

            // Walking / idle = columns 0-3
            col = frame;
        }

        g2.drawImage(
            sheet,
            x,
            y,
            x + 64,
            y + 64,
            col * 64,
            row * 64,
            (col + 1) * 64,
            (row + 1) * 64,
            null
        );
    }
}