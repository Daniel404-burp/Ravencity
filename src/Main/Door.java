package Main;

import java.awt.Rectangle;

public class Door {

    private final int x;
    private final int y;
    private final int width;
    private final int height;

    private boolean open = false;

    public Door(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean isOpen() {
        return open;
    }

    public void open() {
        open = true;
    }

    public void close() {
        open = false;
    }

    public boolean toggle(Rectangle actorBounds) {

        if (!open) {
            // Opening a door never depends on loot.
            open = true;
            return true;
        }

        // Do not close the door while Ethan is standing inside it.
        // This prevents the door from trapping him in the doorway.
        if (actorBounds != null &&
            actorBounds.intersects(getCollisionBounds())) {
            return false;
        }

        open = false;
        return true;
    }

    public boolean canInteract(Rectangle actorBounds) {

        if (actorBounds == null) {
            return false;
        }

        Rectangle interaction = new Rectangle(
                x - 45,
                y - 45,
                width + 90,
                height + 90
        );

        return actorBounds.intersects(interaction);
    }

    public Rectangle getCollisionBounds() {
        return new Rectangle(x, y, width, height);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
