package Main;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Keycard {

    private int x, y;
    private boolean collected = false;
    private BufferedImage image;

    public Keycard(int x, int y) {
        this.x = x;
        this.y = y;

        try {
            BufferedImage sheet = ImageIO.read(
                getClass().getResource("/assets/ui_icons.png"));

            // Yellow keycard (3rd icon)
            image = sheet.getSubimage(861, 23, 221, 744);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 22, 22);
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        collected = true;
    }

    public void draw(Graphics2D g2) {
        if (collected) return;
        g2.drawImage(image, x, y, 22, 22, null);
    }
}