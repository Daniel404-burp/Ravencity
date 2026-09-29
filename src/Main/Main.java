package Main;

import javax.swing.*;

public class Main {

    public static JFrame window;

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            window = new JFrame("Raven City");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            window.setResizable(false);
            window.setSize(800, 600);
            window.setLocationRelativeTo(null);

            window.setContentPane(new MainMenuPanel());

            window.setVisible(true);
        });
    }
}