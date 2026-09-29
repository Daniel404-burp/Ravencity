package Main;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class AmmoDrop {

    private int x, y;
    private int amount;
    private boolean collected = false;
    private BufferedImage icon;

    public AmmoDrop(int x, int y, int amount) {
        this.x = x;
        this.y = y;
        this.amount = amount;

        try {
        	BufferedImage sheet = ImageIO.read(
        		    getClass().getResource("/assets/ui_icons.png"));

        	icon = sheet.getSubimage(121, 23, 152, 744);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 24, 24);
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect(Player player) {
        if (!collected) {
            player.reserveAmmo += amount;
            collected = true;
        }
    }

    public void draw(Graphics2D g2) {
        if (collected) return;

        g2.drawImage(icon, x, y, 24, 24, null);
    }
}