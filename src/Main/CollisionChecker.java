package Main;

import java.awt.Rectangle;
import java.util.ArrayList;

public class CollisionChecker {

    private final ArrayList<Rectangle> walls = new ArrayList<>();

    public void clear() {
        walls.clear();
    }

    public void addWall(int x, int y, int w, int h) {
        walls.add(new Rectangle(x, y, w, h));
    }

    public boolean isColliding(Rectangle player) {

        for (Rectangle wall : walls) {
            if (player.intersects(wall))
                return true;
        }

        return false;
    }
}