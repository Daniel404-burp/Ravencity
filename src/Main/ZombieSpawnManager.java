package Main;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ZombieSpawnManager {

    // =========================================================
    // WORLD
    // =========================================================

    private static final int WORLD_WIDTH = 2000;
    private static final int WORLD_HEIGHT = 2000;

    // Keep zombies away from Ethan when spawning.
    private static final int MIN_PLAYER_DISTANCE = 300;

    // Number of spawn attempts per zone.
    private static final int MAX_ATTEMPTS_PER_SPAWN = 30;

    private final Random random = new Random();

    // =========================================================
    // SPAWNED ZOMBIES
    // =========================================================

    private final List<Enemy> normalZombies =
            new ArrayList<>();

    private final List<FastZombie> fastZombies =
            new ArrayList<>();

    private final List<PoliceZombie> policeZombies =
            new ArrayList<>();

    private final List<HeavyZombie> heavyZombies =
            new ArrayList<>();

    private final List<ExperimentalZombie> experimentalZombies =
            new ArrayList<>();

    // =========================================================
    // INITIAL SPAWN
    // =========================================================

    public void spawnInitialZombies(
            Map map,
            Player player
    ) {

        clear();

        if (map == null || player == null) {
            return;
        }

        // =====================================================
        // NORTH-WEST RESIDENTIAL
        // =====================================================

        spawnNormal(map, player, 3,
                40, 40, 760, 760);

        spawnFast(map, player, 1,
                40, 40, 760, 760);

        // =====================================================
        // NORTH-EAST RESIDENTIAL
        // =====================================================

        spawnNormal(map, player, 2,
                1200, 40, 1950, 760);

        spawnFast(map, player, 1,
                1200, 40, 1950, 760);

        // =====================================================
        // WEST / CENTRAL SHOPS
        // =====================================================

        spawnNormal(map, player, 2,
                40, 650, 760, 1250);

        spawnFast(map, player, 1,
                40, 650, 760, 1250);

        // =====================================================
        // CENTRAL SHOP / STREET AREA
        // =====================================================

        spawnNormal(map, player, 2,
                650, 650, 1350, 1250);

        spawnFast(map, player, 1,
                650, 650, 1350, 1250);

        // =====================================================
        // EAST / DOWNTOWN SIDE
        // Police infected are concentrated here.
        // =====================================================

        spawnPolice(map, player, 2,
                1200, 650, 1950, 1250);

        // =====================================================
        // SOUTH / CLINIC AREA
        // Heavy + Experimental infected are rare here.
        // =====================================================

        spawnNormal(map, player, 2,
                550, 1200, 1450, 1800);

        spawnHeavy(map, player, 1,
                1200, 1200, 1550, 1800);

        // Rare Project RAVEN-related infected.
        spawnExperimental(map, player, 1,
                1000, 1350, 1450, 1800);

        // =====================================================
        // SOUTH RESIDENTIAL
        // =====================================================

        spawnNormal(map, player, 2,
                40, 1200, 760, 1850);

        spawnFast(map, player, 1,
                40, 1200, 760, 1850);

        // =====================================================
        // SOUTH-EAST / DOWNTOWN
        // =====================================================

        spawnNormal(map, player, 1,
                1200, 1200, 1950, 1850);
    }

    // =========================================================
    // NORMAL ZOMBIES
    // =========================================================

    private void spawnNormal(
            Map map,
            Player player,
            int amount,
            int minX,
            int minY,
            int maxX,
            int maxY
    ) {

        for (int i = 0; i < amount; i++) {

            int[] position =
                    findValidSpawn(
                            map,
                            player,
                            minX,
                            minY,
                            maxX,
                            maxY,
                            40,
                            40
                    );

            if (position == null) {
                continue;
            }

            Enemy zombie =
                    new Enemy(position[0]);

            zombie.y = position[1];

            normalZombies.add(zombie);
        }
    }

    // =========================================================
    // FAST ZOMBIES
    // =========================================================

    private void spawnFast(
            Map map,
            Player player,
            int amount,
            int minX,
            int minY,
            int maxX,
            int maxY
    ) {

        for (int i = 0; i < amount; i++) {

            int[] position =
                    findValidSpawn(
                            map,
                            player,
                            minX,
                            minY,
                            maxX,
                            maxY,
                            40,
                            40
                    );

            if (position == null) {
                continue;
            }

            fastZombies.add(
                    new FastZombie(
                            position[0],
                            position[1]
                    )
            );
        }
    }

    // =========================================================
    // POLICE ZOMBIES
    // =========================================================

    private void spawnPolice(
            Map map,
            Player player,
            int amount,
            int minX,
            int minY,
            int maxX,
            int maxY
    ) {

        for (int i = 0; i < amount; i++) {

            int[] position =
                    findValidSpawn(
                            map,
                            player,
                            minX,
                            minY,
                            maxX,
                            maxY,
                            40,
                            40
                    );

            if (position == null) {
                continue;
            }

            policeZombies.add(
                    new PoliceZombie(
                            position[0],
                            position[1]
                    )
            );
        }
    }

    // =========================================================
    // HEAVY ZOMBIES
    // =========================================================

    private void spawnHeavy(
            Map map,
            Player player,
            int amount,
            int minX,
            int minY,
            int maxX,
            int maxY
    ) {

        for (int i = 0; i < amount; i++) {

            int[] position =
                    findValidSpawn(
                            map,
                            player,
                            minX,
                            minY,
                            maxX,
                            maxY,
                            48,
                            44
                    );

            if (position == null) {
                continue;
            }

            heavyZombies.add(
                    new HeavyZombie(
                            position[0],
                            position[1]
                    )
            );
        }
    }

    // =========================================================
    // EXPERIMENTAL INFECTED
    // =========================================================

    private void spawnExperimental(
            Map map,
            Player player,
            int amount,
            int minX,
            int minY,
            int maxX,
            int maxY
    ) {

        for (int i = 0; i < amount; i++) {

            int[] position =
                    findValidSpawn(
                            map,
                            player,
                            minX,
                            minY,
                            maxX,
                            maxY,
                            40,
                            40
                    );

            if (position == null) {
                continue;
            }

            experimentalZombies.add(
                    new ExperimentalZombie(
                            position[0],
                            position[1]
                    )
            );
        }
    }

    // =========================================================
    // FIND VALID SPAWN
    // =========================================================

    private int[] findValidSpawn(
            Map map,
            Player player,
            int minX,
            int minY,
            int maxX,
            int maxY,
            int hitboxWidth,
            int hitboxHeight
    ) {

        for (
                int attempt = 0;
                attempt < MAX_ATTEMPTS_PER_SPAWN;
                attempt++
        ) {

            int x =
                    minX +
                    random.nextInt(
                            Math.max(
                                    1,
                                    maxX - minX + 1
                            )
                    );

            int y =
                    minY +
                    random.nextInt(
                            Math.max(
                                    1,
                                    maxY - minY + 1
                            )
                    );

            // Keep spawn position inside the world.
            x = clamp(
                    x,
                    32,
                    WORLD_WIDTH - hitboxWidth - 32
            );

            y = clamp(
                    y,
                    32,
                    WORLD_HEIGHT - hitboxHeight - 32
            );

            Rectangle spawnBounds =
                    new Rectangle(
                            x + 12,
                            y + 20,
                            hitboxWidth,
                            hitboxHeight
                    );

            // Don't spawn inside walls / closed doors.
            if (map.collidesWithBuilding(spawnBounds)) {
                continue;
            }

            // Don't spawn directly beside Ethan.
            if (tooCloseToPlayer(
                    x,
                    y,
                    player
            )) {
                continue;
            }

            return new int[] {
                    x,
                    y
            };
        }

        return null;
    }

    // =========================================================
    // PLAYER DISTANCE CHECK
    // =========================================================

    private boolean tooCloseToPlayer(
            int zombieX,
            int zombieY,
            Player player
    ) {

        int playerCenterX =
                player.x + 32;

        int playerCenterY =
                player.y + 32;

        int zombieCenterX =
                zombieX + 32;

        int zombieCenterY =
                zombieY + 32;

        int dx =
                playerCenterX -
                zombieCenterX;

        int dy =
                playerCenterY -
                zombieCenterY;

        double distance =
                Math.sqrt(
                        dx * dx +
                        dy * dy
                );

        return distance <
                MIN_PLAYER_DISTANCE;
    }

    // =========================================================
    // CLAMP
    // =========================================================

    private int clamp(
            int value,
            int min,
            int max
    ) {

        return Math.max(
                min,
                Math.min(
                        max,
                        value
                )
        );
    }

    // =========================================================
    // CLEAR
    // =========================================================

    public void clear() {

        normalZombies.clear();
        fastZombies.clear();
        policeZombies.clear();
        heavyZombies.clear();
        experimentalZombies.clear();
    }

    // =========================================================
    // GETTERS
    // =========================================================

    public List<Enemy> getNormalZombies() {

        return normalZombies;
    }

    public List<FastZombie> getFastZombies() {

        return fastZombies;
    }

    public List<PoliceZombie> getPoliceZombies() {

        return policeZombies;
    }

    public List<HeavyZombie> getHeavyZombies() {

        return heavyZombies;
    }

    public List<ExperimentalZombie> getExperimentalZombies() {

        return experimentalZombies;
    }
}