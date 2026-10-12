package Main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Chapter 2 bullet.
 *
 * Bullets use the same 8-pixel travel speed as before, but movement is
 * checked one pixel at a time so a bullet cannot skip through a wall.
 * Closed doors are treated as walls through Map.collidesWithBuilding().
 */
public class Chapter2Bullet {

    public int x, y;
    private final int dx, dy;
    public boolean alive = true;

    private static final int SPEED = 8;
    private static final int WORLD_WIDTH = 2000;
    private static final int WORLD_HEIGHT = 2000;

    public Chapter2Bullet(int x, int y, int dx, int dy) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
    }

    /**
     * Move the bullet while checking every pixel of its path.
     * This prevents fast bullets from tunneling through thin walls.
     */
    public void update(Map map) {

        for (int step = 0; step < SPEED; step++) {

            int nextX = x + dx;
            int nextY = y + dy;

            // World bounds.
            if (nextX < 0
                    || nextX > WORLD_WIDTH
                    || nextY < 0
                    || nextY > WORLD_HEIGHT) {

                alive = false;
                return;
            }

            // Building + closed-door collision.
            if (map != null
                    && map.collidesWithBuilding(
                            new Rectangle(nextX, nextY, 8, 8)
                    )) {

                alive = false;
                return;
            }

            x = nextX;
            y = nextY;
        }
    }

    public void draw(Graphics2D g2) {
        g2.setColor(Color.YELLOW);
        g2.fillOval(x, y, 8, 8);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 8, 8);
    }
}
