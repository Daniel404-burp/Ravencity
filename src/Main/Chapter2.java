package Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

public class Chapter2 extends JPanel implements Runnable, KeyListener, MouseListener {

    private static final int SCREEN_WIDTH = 800;
    private static final int SCREEN_HEIGHT = 600;
    private static final int FPS = 30;
    private static final double UPDATE_INTERVAL = 1.0 / FPS;
    private static final long PLAYER_DAMAGE_COOLDOWN_MS = 500L;

    private final boolean[] keys = new boolean[256];
    private final List<Chapter2Bullet> bullets = new java.util.ArrayList<>();
    private final List<LootContainer> lootContainers = new java.util.ArrayList<>();

    private Thread gameThread;
    private Player player;
    private Map map;
    private Investigation investigation;
    private ZombieSpawnManager zombieSpawnManager;

    private final int WORLD_WIDTH = 2000;
    private final int WORLD_HEIGHT = 2000;
    private int cameraX = 0;
    private int cameraY = 0;

    private String message = "";
    private long messageTimer = 0L;
    private boolean inventoryOpen = false;
    private boolean insideBuilding = false;
    private long lastPlayerDamageTime = 0L;

    private boolean shouldDrawZombie(Rectangle zombieBounds) {
        if (map == null || zombieBounds == null || player == null) return false;

        int zombieCenterX = zombieBounds.x + zombieBounds.width / 2;
        int zombieCenterY = zombieBounds.y + zombieBounds.height / 2;
        int playerCenterX = player.x + player.width / 2;
        int playerCenterY = player.y + player.height / 2;

        int zombieBuilding = map.getBuildingIndexAt(zombieCenterX, zombieCenterY);
        int playerBuilding = map.getBuildingIndexAt(playerCenterX, playerCenterY);

        if (zombieBuilding == -1) return true;
        return zombieBuilding == playerBuilding;
    }

    public Chapter2() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        requestFocusInWindow();

        player = new Player(968, 1050);
        player.setSpeed(6);
        player.hasGun = true;

        map = new Map();
        investigation = map.getInvestigation();

        lootContainers.clear();
        lootContainers.add(new LootContainer(850, 950, 48, 48, LootSystem.createAmmo(12)));
        lootContainers.add(new LootContainer(1050, 950, 48, 48, LootSystem.createHealing(1)));

        zombieSpawnManager = new ZombieSpawnManager();
        zombieSpawnManager.spawnInitialZombies(map, player);

        updateCamera();
        startGame();
    }

    private void startGame() {
        if (gameThread != null) return;
        gameThread = new Thread(this, "RavenCity-Chapter2");
        gameThread.setDaemon(true);
        gameThread.start();
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double accumulator = 0.0;
        final long frameTime = 1_000_000_000L / FPS;
        long nextFrameTime = System.nanoTime();

        while (true) {
            long currentTime = System.nanoTime();
            double deltaTime = (currentTime - lastTime) / 1_000_000_000.0;
            lastTime = currentTime;
            if (deltaTime > 0.25) deltaTime = 0.25;
            accumulator += deltaTime;

            while (accumulator >= UPDATE_INTERVAL) {
                updateGame();
                accumulator -= UPDATE_INTERVAL;
            }

            repaint();
            nextFrameTime += frameTime;
            long sleepTime = nextFrameTime - System.nanoTime();

            if (sleepTime > 0) {
                try {
                    long millis = sleepTime / 1_000_000;
                    int nanos = (int) (sleepTime % 1_000_000);
                    Thread.sleep(millis, nanos);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } else {
                nextFrameTime = System.nanoTime();
            }
        }
    }

    private void updateGame() {
        if (player != null && !player.isDead()) {
            updatePlayerMovement();
        }

        updateInsideBuildingState();
        updateZombieAttacks();
        updateBullets();
        updateCamera();
        updateMessageTimer();
    }

    private void updateInsideBuildingState() {
        if (map == null || player == null) return;

        boolean newInsideState = map.isInsideBuilding(player.x, player.y);
        if (newInsideState != insideBuilding) {
            insideBuilding = newInsideState;
            showMessage(insideBuilding ? "Entered building" : "Exited building");
        }
    }

    private void updateZombieAttacks() {
        if (zombieSpawnManager == null || player == null) return;

        handleZombieAttacks(zombieSpawnManager.getNormalZombies(), 10, "The infected attacked you!");
        handleZombieAttacks(zombieSpawnManager.getFastZombies(), 10, "A fast infected attacked you!");
        handleZombieAttacks(zombieSpawnManager.getPoliceZombies(), 10, "A police infected attacked you!");
        handleZombieAttacks(zombieSpawnManager.getHeavyZombies(), 15, "The heavy infected hit you!");
        handleZombieAttacks(zombieSpawnManager.getExperimentalZombies(), 15, "The experimental infected attacked you!");
    }

    private <T extends Enemy> void handleZombieAttacks(List<T> zombies, int damage, String messageText) {
        for (T zombie : zombies) {
            if (zombie == null || zombie.isDead()) continue;
            zombie.update(player, map);

            if (zombie.canAttack(player)) {
                damagePlayer(damage, messageText);
            }
        }
    }

    private void damagePlayer(int damage, String messageText) {
        if (player == null || player.isDead()) return;

        long now = System.currentTimeMillis();
        if (now - lastPlayerDamageTime < PLAYER_DAMAGE_COOLDOWN_MS) {
            return;
        }

        lastPlayerDamageTime = now;
        player.takeDamage(damage);
        showMessage(messageText);
    }

    private void updateBullets() {
        for (int i = 0; i < bullets.size(); i++) {
            Chapter2Bullet bullet = bullets.get(i);
            bullet.update(map);

            if (!bullet.alive) {
                bullets.remove(i);
                i--;
                continue;
            }

            if (zombieSpawnManager != null) {
                if (bulletHitAnyZombie(bullet, zombieSpawnManager.getNormalZombies())) continue;
                if (bulletHitAnyZombie(bullet, zombieSpawnManager.getFastZombies())) continue;
                if (bulletHitAnyZombie(bullet, zombieSpawnManager.getPoliceZombies())) continue;
                if (bulletHitAnyZombie(bullet, zombieSpawnManager.getHeavyZombies())) continue;
                if (bulletHitAnyZombie(bullet, zombieSpawnManager.getExperimentalZombies())) continue;
            }

            if (!bullet.alive) {
                bullets.remove(i);
                i--;
            }
        }
    }

    private <T extends Enemy> boolean bulletHitAnyZombie(Chapter2Bullet bullet, List<T> zombies) {
        for (T zombie : zombies) {
            if (zombie == null || zombie.isDead()) continue;
            if (bullet.getBounds().intersects(zombie.getBounds())) {
                zombie.takeDamage();
                bullet.alive = false;
                return true;
            }
        }
        return false;
    }

    private void updatePlayerMovement() {
        int oldX = player.x;
        int oldY = player.y;
        player.update(keys);

        if (player.x < 0) player.x = 0;
        if (player.y < 0) player.y = 0;
        if (player.x > WORLD_WIDTH - player.width) player.x = WORLD_WIDTH - player.width;
        if (player.y > WORLD_HEIGHT - player.height) player.y = WORLD_HEIGHT - player.height;

        if (map != null && map.collidesWithBuilding(player.getBounds())) {
            int newX = player.x;
            int newY = player.y;
            player.x = newX;
            player.y = oldY;
            if (map.collidesWithBuilding(player.getBounds())) {
                player.x = oldX;
                player.y = newY;
            }
            if (map.collidesWithBuilding(player.getBounds())) {
                player.x = oldX;
                player.y = oldY;
            }
        }
    }

    private void updateCamera() {
        if (player == null) return;
        int playerCenterX = player.x + player.width / 2;
        int playerCenterY = player.y + player.height / 2;
        cameraX = playerCenterX - SCREEN_WIDTH / 2;
        cameraY = playerCenterY - SCREEN_HEIGHT / 2;
        cameraX = Math.max(0, Math.min(cameraX, WORLD_WIDTH - SCREEN_WIDTH));
        cameraY = Math.max(0, Math.min(cameraY, WORLD_HEIGHT - SCREEN_HEIGHT));
    }

    private void showMessage(String text) {
        if (text == null || text.isBlank()) return;
        message = text;
        messageTimer = 2000L;
    }

    private void updateMessageTimer() {
        if (messageTimer > 0) {
            messageTimer = Math.max(0L, messageTimer - 33L);
            if (messageTimer <= 0) {
                message = "";
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(-cameraX, -cameraY);

        if (map != null) map.draw(g2, player == null ? 0 : player.x, player == null ? 0 : player.y);

        if (insideBuilding) {
            for (LootContainer container : lootContainers) {
                if (container != null) container.draw(g2);
            }
        }

        if (zombieSpawnManager != null) {
            for (Enemy zombie : zombieSpawnManager.getNormalZombies()) {
                if (shouldDrawZombie(zombie.getBounds())) zombie.draw(g2);
            }
            for (FastZombie zombie : zombieSpawnManager.getFastZombies()) {
                if (shouldDrawZombie(zombie.getBounds())) zombie.draw(g2);
            }
            for (PoliceZombie zombie : zombieSpawnManager.getPoliceZombies()) {
                if (shouldDrawZombie(zombie.getBounds())) zombie.draw(g2);
            }
            for (HeavyZombie zombie : zombieSpawnManager.getHeavyZombies()) {
                if (shouldDrawZombie(zombie.getBounds())) zombie.draw(g2);
            }
            for (ExperimentalZombie zombie : zombieSpawnManager.getExperimentalZombies()) {
                if (shouldDrawZombie(zombie.getBounds())) {
                    zombie.draw(g2);
                }
            }
        }

        for (Chapter2Bullet bullet : bullets) bullet.draw(g2);
        if (player != null) player.draw(g2);

        g2.translate(cameraX, cameraY);
        if (player != null) drawHUD(g2);
        if (inventoryOpen && player != null) drawInventory(g2);
        if (message != null && !message.isEmpty()) drawMessage(g2);
        g2.dispose();
    }

    private void drawMessage(Graphics2D g2) {
        int width = 340;
        int height = 42;
        int x = (SCREEN_WIDTH - width) / 2;
        int y = 105;

        String safeMessage = message;
        if (safeMessage.length() > 28) {
            safeMessage = safeMessage.substring(0, 27) + "…";
        }

        g2.setColor(new Color(0, 0, 0, 190));
        g2.fillRoundRect(x, y, width, height, 10, 10);
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(x, y, width, height, 10, 10);
        g2.drawString(safeMessage, x + 16, y + 26);
    }

    private void drawHUD(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(15, 15, 230, 35);
        g2.setColor(Color.RED);
        g2.fillRect(20, 20, Math.max(0, player.health * 2), 20);
        g2.setColor(Color.WHITE);
        g2.drawRect(20, 20, 200, 20);
        g2.drawString("HP: " + player.health + "/100", 25, 35);

        g2.setColor(Color.BLACK);
        g2.fillRect(15, 60, 230, 35);
        g2.setColor(Color.WHITE);
        g2.drawString("Ammo: " + player.ammo + " / " + player.reserveAmmo, 25, 82);

        g2.drawString(insideBuilding ? "INSIDE" : "OUTSIDE", 680, 30);
        g2.drawString("WASD - Move", 15, SCREEN_HEIGHT - 50);
        g2.drawString("E - Investigate / Interact", 15, SCREEN_HEIGHT - 35);
        g2.drawString("H - Heal", 15, SCREEN_HEIGHT - 20);
        g2.drawString("I - Inventory", 120, SCREEN_HEIGHT - 35);
        g2.drawString("LMB - Shoot    R - Reload", 120, SCREEN_HEIGHT - 20);
    }

    private void drawInventory(Graphics2D g2) {
        int width = 400;
        int height = 300;
        int x = (SCREEN_WIDTH - width) / 2;
        int y = (SCREEN_HEIGHT - height) / 2;

        g2.setColor(new Color(0, 0, 0, 220));
        g2.fillRect(x, y, width, height);
        g2.setColor(Color.WHITE);
        g2.drawRect(x, y, width, height);

        g2.setFont(new Font("Arial", Font.BOLD, 22));
        g2.drawString("INVENTORY", x + 20, y + 35);

        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        List<Item> items = player.getInventory().getItems();

        if (items.isEmpty()) {
            g2.drawString("Inventory is empty.", x + 20, y + 70);
        } else {
            int itemY = y + 70;
            for (Item item : items) {
                g2.drawString(item.getName() + " x" + item.getAmount(), x + 20, itemY);
                itemY += 25;
            }
        }

        g2.drawString("Press I to close", x + 20, y + height - 25);
    }

    private boolean tryLoot() {
        if (!insideBuilding || player == null) return false;

        for (LootContainer container : lootContainers) {
            if (container == null) continue;
            if (!container.canInteract(player)) continue;

            Item item = container.open();
            if (item == null) continue;

            if (item.getType() == ItemType.AMMO) {
                player.reserveAmmo += item.getAmount();
            } else {
                player.getInventory().addItem(item);
            }

            showMessage("Picked up " + item.getName() + " x" + item.getAmount());
            return true;
        }
        return false;
    }

    private void useHealingItem() {
        if (player == null) return;

        List<Item> items = player.getInventory().getItems();

        for (Item item : items) {
            if (item.getType() == ItemType.HEALING) {
                if (player.health >= 100) {
                    showMessage("Health is already full!");
                    return;
                }

                player.heal(30);
                player.getInventory().removeItem(item);
                showMessage("Used Medkit. +30 HP");
                return;
            }
        }

        showMessage("No healing items.");
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key >= 0 && key < keys.length) keys[key] = true;

        if (key == KeyEvent.VK_E) {
            if (map != null && player != null) {
                InvestigationPoint point = map.investigate(player.getBounds());
                if (point != null) {
                    showMessage(point.getTitle() + ": " + point.getMessage());
                    return;
                }
            }

            if (map != null && player != null && map.interactWithWindow(player.getBounds())) {
                showMessage("Window toggled.");
                return;
            }

            if (tryLoot()) return;

            if (map != null && player != null && map.interactWithDoor(player.getBounds())) {
                showMessage("Door toggled.");
            }
        }

        if (key == KeyEvent.VK_H) useHealingItem();
        if (key == KeyEvent.VK_R && player != null) player.reload();
        if (key == KeyEvent.VK_I && player != null) inventoryOpen = !inventoryOpen;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key >= 0 && key < keys.length) keys[key] = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (player == null || player.isDead() || inventoryOpen) return;
        if (!player.shoot()) return;

        int dx = 0;
        int dy = 0;

        switch (player.getDirection()) {
            case 0 -> dy = 1;
            case 1 -> dx = -1;
            case 2 -> dx = 1;
            case 3 -> dy = -1;
        }

        int bulletX = player.x + 32;
        int bulletY = player.y + 32;

        switch (player.getDirection()) {
            case 0 -> {
                bulletX = player.x + 28;
                bulletY = player.y + 54;
            }
            case 1 -> {
                bulletX = player.x + 8;
                bulletY = player.y + 25;
            }
            case 2 -> {
                bulletX = player.x + 56;
                bulletY = player.y + 25;
            }
            case 3 -> {
                bulletX = player.x + 32;
                bulletY = player.y + 9;
            }
        }

        bullets.add(new Chapter2Bullet(bulletX, bulletY, dx, dy));
        NoiseSystem.createNoise(player.x + 32, player.y + 32);
    }

    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Raven City - Chapter 2");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            Chapter2 chapter2 = new Chapter2();
            frame.setContentPane(chapter2);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            chapter2.requestFocusInWindow();
        });
    }
}
