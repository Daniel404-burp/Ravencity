package Main;

import javax.swing.*;
import java.awt.*;

public class ScalePanel extends JPanel {

    private final JPanel game;

    public ScalePanel(JPanel game) {
        this.game = game;
        setLayout(null);
        add(game);
        game.setBounds(0, 0, 800, 600);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    @Override
    public void doLayout() {

        int sw = getWidth();
        int sh = getHeight();

        double scale = Math.min(sw / 800.0, sh / 600.0);

        int w = (int)(800 * scale);
        int h = (int)(600 * scale);

        int x = (sw - w) / 2;
        int y = (sh - h) / 2;

        game.setBounds(x, y, w, h);
    }
}