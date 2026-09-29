package Main;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Barricade {

    public int x, y;
    private BufferedImage image;

    public Barricade(int x, int y) {
        this.x = x;
        this.y = y;

        try {
            image = ImageIO.read(getClass().getResource("/assets/barricade.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g2) {

        if (image != null) {

            // Size
            int drawWidth = 450;
            int drawHeight = 250;

            // Position
            int drawX = x - 225;
            int drawY = y - 120;

            // Save transform
            AffineTransform old = g2.getTransform();

            // Rotate 90° around center
            g2.rotate(
                Math.toRadians(90),
                drawX + drawWidth / 2.0,
                drawY + drawHeight / 2.0
            );

            // Draw rotated image
            g2.drawImage(
                image,
                drawX,
                drawY,
                drawWidth,
                drawHeight,
                null
            );

            // Restore transform
            g2.setTransform(old);

        } else {

            // Fallback rectangle
            g2.setColor(new Color(100, 100, 100));
            g2.fillRect(x - 190, y, 380, 35);

            g2.setColor(Color.WHITE);
            g2.drawRect(x - 190, y, 380, 35);
        }
    }
}