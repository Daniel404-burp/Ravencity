package Main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class InvestigationPoint {

    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final String title;
    private final String message;

    private boolean investigated;

    public InvestigationPoint(
            int x,
            int y,
            int width,
            int height,
            String title,
            String message
    ) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.title = title;
        this.message = message;
        this.investigated = false;
    }

    public boolean canInteract(Rectangle actorBounds) {
        if (actorBounds == null || investigated) {
            return false;
        }

        Rectangle interactionBounds = new Rectangle(
                x - 35,
                y - 35,
                width + 70,
                height + 70
        );

        return actorBounds.intersects(interactionBounds);
    }

    public boolean investigate() {
        if (investigated) {
            return false;
        }

        investigated = true;
        return true;
    }

    public void draw(Graphics2D g2) {
        if (investigated) {
            return;
        }

        g2.setColor(new Color(210, 190, 120));
        g2.fillRect(x, y, width, height);

        g2.setColor(Color.BLACK);
        g2.drawRect(x, y, width, height);

        g2.drawString("!", x + width / 2 - 3, y + height / 2 + 5);
    }

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

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public boolean isInvestigated() {
        return investigated;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}
