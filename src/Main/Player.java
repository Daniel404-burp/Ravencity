package Main;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

public class Player {

    // ================= POSITION =================

    public int x, y;

    public final int width = 64;
    public final int height = 64;


    // ================= STATS =================

    public int health = 100;

    public int speed = 4;

    public void setSpeed(int speed) {
        this.speed = speed;
    }


    // ================= AMMO =================

    public final int MAG_SIZE = 8;

    public int ammo = 8;

    public int reserveAmmo = 24;

    public boolean hasGun = true;


    // ================= INVENTORY =================

    private Inventory inventory;


    // ================= SPRITE =================

    private BufferedImage sheet;


    // 0 = Down
    // 1 = Left
    // 2 = Right
    // 3 = Up

    private int direction = 0;


    // ================= STATE =================

    private enum State {

        IDLE,
        WALK,
        SHOOT,
        DEAD
    }


    private State state = State.IDLE;


    // ================= ANIMATION =================

    private int frame = 0;

    private int tick = 0;


    // ================= FLAGS =================

    private boolean moving = false;

    private boolean shooting = false;

    private boolean dead = false;


    // ================= FOOTSTEP =================

    private int footstepTimer = 0;


    // ================= CONSTRUCTOR =================

    public Player(int x, int y) {

        this.x = x;

        this.y = y;

        // Create Ethan's inventory
        inventory = new Inventory();


        try {

            sheet = ImageIO.read(
                    getClass().getResource(
                            "/assets/ethan_gun_sheet.png"
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // ================= UPDATE =================

    public void update(boolean[] keys) {

        if (dead) {

            animate();

            return;
        }


        moving = false;


        // =========================
        // MOVEMENT
        // =========================

        if (keys[KeyEvent.VK_W]) {

            y -= speed;

            direction = 3;

            moving = true;
        }


        if (keys[KeyEvent.VK_S]) {

            y += speed;

            direction = 0;

            moving = true;
        }


        if (keys[KeyEvent.VK_A]) {

            x -= speed;

            direction = 1;

            moving = true;
        }


        if (keys[KeyEvent.VK_D]) {

            x += speed;

            direction = 2;

            moving = true;
        }


        // =========================
        // ANIMATION STATE
        // =========================

        State newState =
                shooting
                        ? State.SHOOT
                        : (moving
                                ? State.WALK
                                : State.IDLE);


        if (newState != state) {

            state = newState;

            frame = 0;

            tick = 0;
        }


        animate();


        // =========================
        // FOOTSTEP SOUND
        // =========================

        if (moving && !shooting) {

            footstepTimer++;


            if (footstepTimer >= 20) {

                SoundManager.play(
                        "walk.wav"
                );

                footstepTimer = 0;
            }

        } else {

            footstepTimer = 0;
        }
    }


    // ================= ANIMATION =================

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


            case SHOOT -> {

                if (tick >= 6) {

                    frame++;


                    if (frame >= 2) {

                        frame = 0;

                        shooting = false;
                    }


                    tick = 0;
                }
            }


            case DEAD -> {

                if (tick >= 10) {

                    if (frame < 5) {

                        frame++;
                    }


                    tick = 0;
                }
            }
        }
    }


    // ================= SHOOT =================

    public boolean shoot() {

        if (dead)
            return false;


        if (ammo <= 0)
            return false;


        ammo--;


        shooting = true;

        state = State.SHOOT;

        frame = 0;

        tick = 0;


        SoundManager.play(
                "pistol.wav"
        );


        return true;
    }


    // ================= RELOAD =================

    public void reload() {

        if (ammo == MAG_SIZE)
            return;


        if (reserveAmmo <= 0)
            return;


        int need =
                MAG_SIZE - ammo;


        int load =
                Math.min(
                        need,
                        reserveAmmo
                );


        ammo += load;

        reserveAmmo -= load;


        SoundManager.play(
                "reload.wav"
        );
    }


    // ================= DRAW =================

    public void draw(Graphics2D g2) {

        if (sheet == null)
            return;


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

                    col = 0;
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


            case SHOOT -> {

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
    }


    // ================= DAMAGE =================

    public void takeDamage(int dmg) {

        if (dead)
            return;


        health -= dmg;


        if (health <= 0) {

            health = 0;

            dead = true;

            state = State.DEAD;

            frame = 0;

            tick = 0;
        }
    }


    // ================= HEAL =================

    public void heal(int amount) {

        health += amount;


        if (health > 100)

            health = 100;
    }


    // ================= RESPAWN =================

    public void respawn(
            int spawnX,
            int spawnY
    ) {

        x = spawnX;

        y = spawnY;


        health = 100;


        ammo = MAG_SIZE;

        reserveAmmo = 24;


        dead = false;

        shooting = false;

        moving = false;


        direction = 0;


        state = State.IDLE;

        frame = 0;

        tick = 0;


        footstepTimer = 0;
    }


    // ================= GETTERS =================

    public Rectangle getBounds() {

        return new Rectangle(

                x + 16,

                y + 20,

                32,

                40
        );
    }


    public int getDirection() {

        return direction;
    }


    public boolean isDead() {

        return dead;
    }


    // ================= INVENTORY =================

    public Inventory getInventory() {

        return inventory;
    }
}