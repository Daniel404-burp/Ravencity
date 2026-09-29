package Main;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class MainMenuPanel extends JPanel implements KeyListener {

    private BufferedImage bg;
    private BufferedImage menuBg;
    
    private final String[] options = {
            "START GAME",
            "CREDITS",
            "EXIT"
    };

    private int selected = 0;

    public MainMenuPanel() {

        setFocusable(true);
        addKeyListener(this);
        

        try {
            menuBg = ImageIO.read(
                getClass().getResource("/assets/menu_bg.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        if (menuBg != null) {
            g2.drawImage(menuBg, 0, 0, 800, 600, null);
        }

        
    
        // Menu buttons
        g2.setFont(new Font("Monospaced", Font.BOLD, 24));

        int startY = 355;

        for (int i = 0; i < options.length; i++) {

            int x = 245;
            int y = startY + (i * 55);

            if (i == selected) {

                g2.setColor(Color.WHITE);
                g2.fillRoundRect(x, y, 280, 42, 8, 8);

                g2.setColor(Color.BLACK);
                g2.drawString(options[i], x + 48, y + 28);

            } else {

                g2.setColor(new Color(0, 0, 0, 160));
                g2.fillRoundRect(x, y, 280, 42, 8, 8);

                g2.setColor(Color.WHITE);
                g2.drawRoundRect(x, y, 280, 42, 8, 8);

                g2.drawString(options[i], x + 48, y + 28);
            }
        }

        g2.setFont(new Font("Arial", Font.PLAIN, 13));
        g2.setColor(new Color(220,220,220));
        g2.drawString("↑ ↓ Navigate   ENTER Select", 255, 545);
    }

    @Override
    public void keyPressed(KeyEvent e) {

        switch (e.getKeyCode()) {

            case KeyEvent.VK_UP -> {
                selected--;
                if (selected < 0) selected = options.length - 1;
                repaint();
            }

            case KeyEvent.VK_DOWN -> {
                selected++;
                if (selected >= options.length) selected = 0;
                repaint();
            }

            case KeyEvent.VK_ENTER -> selectOption();
        }
    }

    private void selectOption() {

        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);

        switch (selected) {

            case 0 -> {
                frame.setContentPane(new ProloguePanel());
                frame.revalidate();
            }

            case 1 -> JOptionPane.showMessageDialog(
                    this,
                    "Raven City V2\n\nDeveloped by Team Raven",
                    "Credits",
                    JOptionPane.INFORMATION_MESSAGE
            );

            case 2 -> System.exit(0);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}
}