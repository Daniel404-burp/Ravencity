package Main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Window {

    private final int x;
    private final int y;
    private final int width;
    private final int height;

    private boolean open;

    public Window(
            int x,
            int y,
            int width,
            int height
    ) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.open = false;
    }

    // =========================================================
    // STATE
    // =========================================================

    public boolean isOpen() {
        return open;
    }

    public void open() {
        open = true;
    }

    public void close() {
        open = false;
    }

    public boolean toggle() {
        open = !open;
        return open;
    }

    // =========================================================
    // INTERACTION
    // =========================================================

    public boolean canInteract(Rectangle actorBounds) {

        if (actorBounds == null) {
            return false;
        }

        Rectangle interactionBounds =
                new Rectangle(
                        x - 45,
                        y - 45,
                        width + 90,
                        height + 90
                );

        return actorBounds.intersects(interactionBounds);
    }

    // =========================================================
    // COLLISION
    // =========================================================

    public Rectangle getCollisionBounds() {
        return new Rectangle(
                x,
                y,
                width,
                height
        );
    }

    // =========================================================
    // DRAW
    // =========================================================

    public void draw(Graphics2D g2) {

        if (open) {

            // Open window
            g2.setColor(
                    new Color(70, 70, 70)
            );

            g2.fillRect(
                    x,
                    y,
                    width,
                    height
            );

            g2.setColor(
                    new Color(120, 160, 120)
            );

            g2.drawRect(
                    x,
                    y,
                    width,
                    height
            );

        } else {

            // Closed window
            g2.setColor(
                    new Color(65, 90, 110)
            );

            g2.fillRect(
                    x,
                    y,
                    width,
                    height
            );

            g2.setColor(Color.BLACK);

            g2.drawRect(
                    x,
                    y,
                    width,
                    height
            );

            // Window cross
            g2.drawLine(
                    x + width / 2,
                    y,
                    x + width / 2,
                    y + height
            );

            g2.drawLine(
                    x,
                    y + height / 2,
                    x + width,
                    y + height / 2
            );
        }
    }

    // =========================================================
    // GETTERS
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
}