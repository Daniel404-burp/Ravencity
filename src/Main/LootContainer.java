package Main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class LootContainer {

    private int x;
    private int y;
    private int width;
    private int height;

    private Item item;
    private boolean opened;

    public LootContainer(
            int x,
            int y,
            int width,
            int height,
            Item item
    ) {

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.item = item;
        this.opened = false;
    }

    // =========================================================
    // DRAW
    // =========================================================

    public void draw(Graphics2D g2) {

        // -----------------------------------------------------
        // OPENED / EMPTY
        // -----------------------------------------------------

        if (opened) {

            g2.setColor(
                    new Color(80, 80, 80)
            );

            g2.fillRect(
                    x,
                    y,
                    width,
                    height
            );

            g2.setColor(
                    Color.DARK_GRAY
            );

            g2.drawRect(
                    x,
                    y,
                    width,
                    height
            );

            return;
        }

        // -----------------------------------------------------
        // CLOSED
        // -----------------------------------------------------

        g2.setColor(
                new Color(120, 75, 35)
        );

        g2.fillRect(
                x,
                y,
                width,
                height
        );

        g2.setColor(
                Color.BLACK
        );

        g2.drawRect(
                x,
                y,
                width,
                height
        );

        // Center line

        g2.drawLine(
                x,
                y + height / 2,
                x + width,
                y + height / 2
        );
    }

    // =========================================================
    // INTERACTION
    // =========================================================

    public boolean canInteract(Player player) {

        if (player == null) {
            return false;
        }

        if (opened || item == null) {
            return false;
        }

        Rectangle playerBounds = player.getBounds();

        Rectangle interactionBounds = new Rectangle(
                x - 20,
                y - 20,
                width + 40,
                height + 40
        );

        return playerBounds.intersects(interactionBounds);
    }
    public Item open() {

        if (opened) {
            return null;
        }

        if (item == null) {
            return null;
        }

        opened = true;

        Item collectedItem =
                item;

        item = null;

        return collectedItem;
    }

    // =========================================================
    // LOOT
    // =========================================================

    public boolean hasLoot() {

        return item != null;
    }

    public Item getItem() {

        return item;
    }

    public void setItem(
            Item item
    ) {

        this.item = item;

        this.opened = false;
    }

    // =========================================================
    // STATE
    // =========================================================

    public boolean isOpened() {

        return opened;
    }

    // =========================================================
    // POSITION
    // =========================================================

    public int getX() {

        return x;
    }

    public int getY() {

        return y;
    }

    public int getWidth() {

        return width;
    }

    public int getHeight() {

        return height;
    }

    // =========================================================
    // COLLISION / BOUNDS
    // =========================================================

    public Rectangle getBounds() {

        return new Rectangle(
                x,
                y,
                width,
                height
        );
    }
}