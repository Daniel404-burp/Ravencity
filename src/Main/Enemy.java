
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
    private enum State{
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

    // AI
    private long lastAttack = 0;
    private final int CHASE_RADIUS = 250;
    private final int ATTACK_RADIUS = 42;
    private int attackTimer = 0;

    // Zombie ambient growl timing
    private long nextGrowlTime = 0;
    
    public Enemy(int x){

        this.x = x;
        this.y = 280;

        try{
            sheet = ImageIO.read(
                getClass().getResource("/assets/zombie_sheet.png"));
        }catch(Exception e){
            e.printStackTrace();
        }

        // First ambient growl after a short delay
        nextGrowlTime = System.currentTimeMillis() + 3000 + (long)(Math.random() * 3000);
    }

    public void update(Player player){

        if(dead) return;

        // ================= DEATH =================

        if(dying){
            state = State.DEAD;
            animate();
            return;
        }

        // ================= AMBIENT GROWL =================
        long now = System.currentTimeMillis();
        if(now >= nextGrowlTime && attackTimer <= 0){
            SoundManager.play("zombie_growl.wav");
            nextGrowlTime = now + 3000 + (long)(Math.random() * 4000);
        }

        // ================= ATTACK LOCK =================
        // Prevent WALK/IDLE from interrupting the bite animation.

        if(attackTimer > 0){

            attackTimer--;

            state = State.ATTACK;

            animate();

            return;
        }

        // ================= DISTANCE =================

        int dx = player.x - x;
        int dy = player.y - y;

        double dist = Math.sqrt(dx * dx + dy * dy);

        boolean moving = false;

        // ================= CHASE =================

        if(dist < CHASE_RADIUS && dist > ATTACK_RADIUS){

            moving = true;

            int absDx = Math.abs(dx);
            int absDy = Math.abs(dy);

            final int DIRECTION_THRESHOLD = 8;

            if(absDx > absDy + DIRECTION_THRESHOLD){

                // HORIZONTAL

                if(dx > 0){
                    x += speed;
                    direction = 2; // RIGHT
                }else{
                    x -= speed;
                    direction = 1; // LEFT
                }

            }else if(absDy > absDx + DIRECTION_THRESHOLD){

                // VERTICAL

                if(dy > 0){
                    y += speed;
                    direction = 0; // DOWN
                }else{
                    y -= speed;
                    direction = 3; // UP
                }

            }else{

                // VERY DIAGONAL
                // Keep the previous direction.
                // This prevents rapid direction switching.
                
                switch(direction){

                    case 0: // DOWN
                        y += speed;
                        break;

                    case 1: // LEFT
                        x -= speed;
                        break;

                    case 2: // RIGHT
                        x += speed;
                        break;

                    case 3: // UP
                        y -= speed;
                        break;
                }
            }
            }
        

        // ================= NORMAL STATE =================

        if(dist <= ATTACK_RADIUS){

            state = State.ATTACK;

        }else if(moving){

            state = State.WALK;

        }else{

            state = State.IDLE;
        }

        animate();
    }

    private void animate(){

        tick++;

        switch(state){

            case IDLE ->{

                if(tick >= 18){
                    frame = (frame + 1) % 2;
                    tick = 0;
                }
            }

            case WALK ->{

                if(tick >= 8){
                    frame = (frame + 1) % 4;
                    tick = 0;
                }
            }

            case ATTACK ->{

                if(tick >= 10){
                    frame++;

                    if(frame >= 2){
                        frame = 0;
                    }

                    tick = 0;
                }
            }

            case DEAD ->{

                if(tick >= 8){

                    if(frame < 5){
                        frame++;
                    }else{
                        dead = true;
                    }

                    tick = 0;
                }
            }
        }
    }

    public boolean canAttack(Player player){

        if(dead || dying) return false;

        Rectangle attackBox;

        switch(direction){

            case 0: // DOWN
                attackBox = new Rectangle(
                    x + 16,
                    y + 28,
                    32,
                    28
                );
                break;

            case 1: // LEFT
                attackBox = new Rectangle(
                    x - 4,
                    y + 18,
                    28,
                    30
                );
                break;

            case 2: // RIGHT
                attackBox = new Rectangle(
                    x + 40,
                    y + 18,
                    28,
                    30
                );
                break;

            case 3: // UP
                attackBox = new Rectangle(
                    x + 16,
                    y - 4,
                    32,
                    28
                );
                break;

            default:
                return false;
        }

        // Player must actually be inside the zombie's bite area
        if(!attackBox.intersects(player.getBounds()))
            return false;

        long now = System.currentTimeMillis();

        if(now - lastAttack >= 1000){

            lastAttack = now;

            SoundManager.play("zombie_bite.wav");

            state = State.ATTACK;
            frame = 0;
            tick = 0;
            attackTimer = 20;

            return true;
        }

        return false;
    }

    public void takeDamage(){

        if(dead || dying) return;

        health--;

        if(health <= 0){

            dying = true;
            state = State.DEAD;
            frame = 0;
            tick = 0;
        }
    }

    public boolean isDead(){
        return dead;
    }

    public Rectangle getBounds(){

        hitbox.setBounds(
            x + 12,
            y + 20,
            40,
            40
        );

        return hitbox;
    }

    public void draw(Graphics2D g2){

        if(dead || sheet == null) return;

        int row = 0;
        int col = 0;

        switch(state){

            case IDLE ->{

                if(direction == 0){ row = 0; col = frame; }
                if(direction == 1){ row = 0; col = 2 + frame; }
                if(direction == 2){ row = 0; col = 4 + frame; }
                if(direction == 3){ row = 4; col = frame; }
            }

            case WALK ->{

                if(direction == 0){ row = 1; col = frame; }
                if(direction == 1){ row = 2; col = frame; }
                if(direction == 2){ row = 3; col = frame; }
                if(direction == 3){ row = 4; col = frame; }
            }

            case ATTACK ->{

                if(direction == 0){ row = 1; col = 4 + frame; }
                if(direction == 1){ row = 2; col = 4 + frame; }
                if(direction == 2){ row = 3; col = 4 + frame; }
                if(direction == 3){ row = 4; col = 4 + frame; }
            }

            case DEAD ->{

                row = 5;
                col = frame;
            }
        }

        g2.drawImage(
            sheet,
            x, y,
            x + width,
            y + height,
            col * 64,
            row * 64,
            (col + 1) * 64,
            (row + 1) * 64,
            null
        );
    }
}