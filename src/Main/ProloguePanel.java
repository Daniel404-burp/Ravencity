package Main;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class ProloguePanel extends JPanel implements KeyListener {

    private BufferedImage background;

    public ProloguePanel() {

        setFocusable(true);
        addKeyListener(this);

        try {
            background = ImageIO.read(
                getClass().getResource("/assets/prologue.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Timer timer = new Timer(16, e -> repaint());
        timer.start();

        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        // Draw prologue background
        if (background != null) {
            g2.drawImage(background, 0, 0, 800, 600, null);
        }

        // Press E prompt
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRoundRect(560, 520, 190, 38, 10, 10);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.BOLD, 18));
        g2.drawString("PRESS E", 610, 545);
    }

    @Override
    public void keyPressed(KeyEvent e) {

        if (e.getKeyCode() == KeyEvent.VK_E) {

            Main.window.setContentPane(new SafehousePanel());
            Main.window.revalidate();
            Main.window.repaint();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}
}